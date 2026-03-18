package dto;

/**
 * Data Transfer Object for the AI response.
 * Cites requirements for answer and sql fields [cite: 1264-1266].
 */
public class AIResponseDTO {
    public String answer;
    public String sql;

    public AIResponseDTO() {}

    public AIResponseDTO(String answer, String sql) {
        this.answer = answer;
        this.sql = sql;
    }
}