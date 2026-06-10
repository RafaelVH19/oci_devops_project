package com.springboot.MyTodoList.controller;

// 1. Imports de Java estándar
import java.util.Map;

// 2. Imports de Spring Web y Core
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

/**
 * Controlador encargado de exponer servicios de generación de texto
 * utilizando el modelo Google GenAI integrado mediante Spring AI.
 */
@RestController
public class GenAIChatController {

    private final GoogleGenAiChatModel chatModel;

    /**
     * Inicializa el controlador con el modelo de IA configurado.
     */
    @Autowired
    public GenAIChatController(GoogleGenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /** Genera una respuesta utilizando el modelo de IA. */
    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    /**
     * Genera una respuesta en modo streaming utilizando el modelo de IA.
     *
     * Permite recibir fragmentos de la respuesta conforme son generados.
     */
    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}