package service;

import dto.AIResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for converting natural language to SQL and executing it.
 * Includes logic to clean AI output and handle REST communication with Gemini.
 */
@Service
public class AIService {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;

    // Injected from application.properties or CLI to prevent 403 Leak errors
    @Value("${google.api.key}")
    private String apiKey;

    // CLEAN URL - Ensure no extra brackets or characters are present here
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    public AIService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = new RestTemplate();
    }

    // Entry point for processing AI requests
    public AIResponseDTO processAIQuery(String userQuery) {
        String systemPrompt = """
            You are a SQL expert for an Enigma Machine PostgreSQL database.
            Your task is to convert English questions into valid PostgreSQL queries.
    
            SCHEMA:
            1. 'machines': Columns [id (UUID), name (text), rotors_count (int), abc (text)]
              - 'abc' is the alphabet used by the machine.
            2. 'processing': Columns [id (UUID), machine_id (FK), session_id (text), code (text), input (text), output (text), time (bigint)]
               - 'code' stores the machine configuration (e.g., <1,2><A,B><I>).
               - 'time' stores the duration of processing in nanoseconds.
            3. 'machine_rotors': Columns [id (UUID), machine_id (FK), rotor_id (int), notch (int), mapping (text)]
            4. 'machine_reflectors': Columns [id (UUID), machine_id (FK), reflector_id (text), mapping (text)]

            RULES:
            - Return ONLY the raw SQL string.
            - Do not use markdown (no ```sql).
            - Use the 'machines' table to answer about machine specs.
            - Use the 'processing' table for statistics (counts, averages, input/output).
            - Always JOIN on machine_id when needed.
            """;

        String generatedSql = "";
        try {
            // Call Gemini to get the SQL
            String rawAiOutput = callGemini(systemPrompt + "\nUser Question: " + userQuery);

            // Clean potential markdown from the SQL string
            generatedSql = cleanSqlOutput(rawAiOutput);

            // Run the query
            List<Map<String, Object>> dbResults = jdbcTemplate.queryForList(generatedSql);

            // Get human-friendly interpretation from AI
            String interpretationPrompt = String.format(
                    "Database results for '%s' are: %s. Summarize this answer concisely.",
                    userQuery, dbResults
            );

            String finalAnswer = callGemini(interpretationPrompt);
            return new AIResponseDTO(finalAnswer, generatedSql);

        } catch (Exception e) {
            // Error handling for API or SQL failures
            return new AIResponseDTO("Processing Error: " + e.getMessage(), generatedSql);
        }
    }

    // Removes markdown backticks (```sql) from the AI's response
    private String cleanSqlOutput(String rawSql) {
        return rawSql.replace("```sql", "")
                .replace("```", "")
                .replace(";", "")
                .trim();
    }

    // Performs the actual REST call to Google Gemini
    private String callGemini(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        Map<String, Object> response = restTemplate.postForObject(GEMINI_API_URL + apiKey, requestBody, Map.class);

        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);

            return firstPart.get("text").toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage());
        }
    }
}