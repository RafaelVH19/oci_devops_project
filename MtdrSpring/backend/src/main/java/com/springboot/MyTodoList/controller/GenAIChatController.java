package com.springboot.MyTodoList.controller;

// 1. Imports de Java estándar
import java.util.Map;

// 2. Imports de Spring Web y Core
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 3. Imports base de Spring AI (Clases comunes de Chat)
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

// 4. Import específico del nuevo modelo de Google GenAI
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
// 5. Import de Project Reactor (Para el manejo de flujos asíncronos con Flux)
import reactor.core.publisher.Flux;

// 6. Imports para el endpoint de chat de Lumi
import com.springboot.MyTodoList.controller.dto.GenAiChatRequest;
import com.springboot.MyTodoList.controller.dto.GenAiChatResponse;
import com.springboot.MyTodoList.service.GenAiChatService;

@RestController
public class GenAIChatController {

    private final GoogleGenAiChatModel chatModel;
    private final GenAiChatService genAiChatService;

    @Autowired
    public GenAIChatController(GoogleGenAiChatModel chatModel, GenAiChatService genAiChatService) {
        this.chatModel = chatModel;
        this.genAiChatService = genAiChatService;
    }

    /** Lumi chat endpoint used by the frontend. */
    @PostMapping("/api/genai/chat")
    public GenAiChatResponse chat(@RequestBody GenAiChatRequest request) {
        return new GenAiChatResponse(genAiChatService.reply(request));
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}