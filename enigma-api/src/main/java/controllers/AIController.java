package controllers;

import dto.AIRequestDTO;
import dto.AIResponseDTO;
import org.springframework.web.bind.annotation.*;
import service.AIService;

@RestController
@RequestMapping("/enigma/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public AIResponseDTO askEnigma(@RequestBody AIRequestDTO request) {
        // Delegate the processing to the AIService
        return aiService.processAIQuery(request.query);
    }
}