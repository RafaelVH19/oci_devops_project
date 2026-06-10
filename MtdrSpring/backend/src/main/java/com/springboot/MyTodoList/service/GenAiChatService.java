package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.agent.AgentOrchestrator;
import com.springboot.MyTodoList.controller.dto.GenAiChatMessage;
import com.springboot.MyTodoList.controller.dto.GenAiChatRequest;
import com.springboot.MyTodoList.service.LumiActionPlan.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class GenAiChatService {

    private static final Logger logger = LoggerFactory.getLogger(GenAiChatService.class);

    private static final String AGENT_FALLBACK = "No pude interpretar la solicitud. Escribe ayuda para ver ejemplos.";

    private static final String LUMI_SYSTEM_PROMPT = """
        You are Lumi, a friendly project assistant in the Lumen app.
        Reply in the same language the user uses (English or Spanish). Be warm and concise.

        Important:
        - Talk like a human colleague, not a command manual.
        - Never tell the user to type slash commands (/addtask, /register, etc.).
        - If they want something created, ask naturally for missing details (names, dates, people).
        - Do not claim you already created teams, projects, or sprints unless the user message says it was done.
        - The app does not require login; created items appear in Dashboard → Team / Projects.
        """;

    private final LumiActionService lumiActionService;
    private final LumiIntentService lumiIntentService;
    private final AgentOrchestrator agentOrchestrator;
    private final GoogleGenAiChatModel chatModel;

    public GenAiChatService(LumiActionService lumiActionService,
                            LumiIntentService lumiIntentService,
                            AgentOrchestrator agentOrchestrator,
                            GoogleGenAiChatModel chatModel) {
        this.lumiActionService = lumiActionService;
        this.lumiIntentService = lumiIntentService;
        this.agentOrchestrator = agentOrchestrator;
        this.chatModel = chatModel;
    }

    public String reply(GenAiChatRequest request) {
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (message.isBlank()) {
            return "Send a message to start chatting with Lumi.";
        }

        Optional<String> ruleReply = lumiActionService.tryHandle(message);
        if (ruleReply.isPresent()) {
            return ruleReply.get();
        }

        if (lumiIntentService.isAvailable()) {
            Optional<LumiActionPlan> plan = lumiIntentService.extractPlan(message, request.getHistory());
            if (plan.isPresent()) {
                LumiActionPlan resolved = plan.get();
                if (resolved.isNeedsClarification()
                    && resolved.getClarificationQuestion() != null
                    && !resolved.getClarificationQuestion().isBlank()) {
                    return resolved.getClarificationQuestion().trim();
                }
                Optional<String> executed = lumiActionService.executePlan(resolved);
                if (executed.isPresent()) {
                    return executed.get();
                }
                if (resolved.getAction() == Action.TASK_QUERY) {
                    String agentReply = agentOrchestrator.handleMessage(message, request.getUserRole());
                    if (isUsefulAgentReply(agentReply)) {
                        return agentReply;
                    }
                }
            }
        }

        if (looksLikeTaskQuery(message)) {
            String agentReply = agentOrchestrator.handleMessage(message, request.getUserRole());
            if (isUsefulAgentReply(agentReply)) {
                return agentReply;
            }
        }

        List<GenAiChatMessage> history = request.getHistory();
        try {
            List<Message> msgs = new ArrayList<>();
            msgs.add(new SystemMessage(LUMI_SYSTEM_PROMPT));
            if (history != null) {
                for (GenAiChatMessage item : history) {
                    if (item == null || item.getContent() == null || item.getContent().isBlank()) continue;
                    if ("assistant".equalsIgnoreCase(item.getRole())) {
                        msgs.add(new AssistantMessage(item.getContent()));
                    } else {
                        msgs.add(new UserMessage(item.getContent()));
                    }
                }
            }
            msgs.add(new UserMessage(message));
            String geminiReply = chatModel.call(new Prompt(msgs))
                .getResult().getOutput().getText();
            if (geminiReply != null && !geminiReply.isBlank()) {
                return geminiReply.trim();
            }
        } catch (Exception ex) {
            logger.warn("Gemini chat request failed", ex);
        }

        return "Tell me what you'd like — create a team, project, or sprint, or ask about workload. I'll do it in the workspace.";
    }

    private boolean looksLikeTaskQuery(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains("task")
            || lower.contains("tarea")
            || lower.contains("sprint")
            || lower.contains("kpi")
            || lower.contains("assignee")
            || lower.contains("asignad")
            || lower.contains("pending")
            || lower.contains("pendiente")
            // semantic search triggers
            || lower.contains("most important")
            || lower.contains("más importante")
            || lower.contains("mas importante")
            || lower.contains("easiest")
            || lower.contains("hardest")
            || lower.contains("most urgent")
            || lower.contains("más urgente")
            || lower.contains("mas urgente")
            || lower.contains("related to")
            || lower.contains("relacionad")
            || lower.contains("backend")
            || lower.contains("frontend")
            || lower.contains("database");
    }

    private boolean isUsefulAgentReply(String agentReply) {
        return agentReply != null
            && !agentReply.isBlank()
            && !AGENT_FALLBACK.equals(agentReply.trim());
    }
}
