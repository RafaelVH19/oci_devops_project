package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.agent.AgentOrchestrator;
import com.springboot.MyTodoList.controller.dto.GenAiChatMessage;
import com.springboot.MyTodoList.controller.dto.GenAiChatRequest;
import com.springboot.MyTodoList.controller.dto.GenAiChatResponse;
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
        ALWAYS reply in English, even if the user writes in Spanish or another language. Be warm and concise.

        Formatting:
        - Use markdown in your replies.
        - When an answer contains several items (tasks, steps, people, options), present them as a bullet list.
        - Use **bold** for names of tasks, sprints, teams, and projects.

        Important:
        - Talk like a human colleague, not a command manual.
        - Never tell the user to type slash commands (/addtask, /register, etc.).
        - If details are missing for something they want created, ask for ALL missing details in ONE message
          (as a short bullet list), never one question at a time.
        - Do not claim you already created teams, projects, or sprints unless the user message says it was done.
        - Created items appear in Dashboard → Team / Projects.
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

    public GenAiChatResponse reply(GenAiChatRequest request) {
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (message.isBlank()) {
            return new GenAiChatResponse("Send a message to start chatting with Lumi.");
        }

        Optional<String> ruleReply = lumiActionService.tryHandle(message);
        if (ruleReply.isPresent()) {
            return new GenAiChatResponse(ruleReply.get(), isWorkspaceSuccess(ruleReply.get()));
        }

        if (lumiIntentService.isAvailable()) {
            Optional<LumiActionPlan> plan = lumiIntentService.extractPlan(
                message, request.getHistory(), request.getUserName(), request.getUserRole());
            if (plan.isPresent()) {
                LumiActionPlan resolved = plan.get();
                if (resolved.isNeedsClarification()
                    && resolved.getClarificationQuestion() != null
                    && !resolved.getClarificationQuestion().isBlank()) {
                    return new GenAiChatResponse(resolved.getClarificationQuestion().trim());
                }
                Optional<String> executed = lumiActionService.executePlan(resolved, request.getUserName());
                if (executed.isPresent()) {
                    return new GenAiChatResponse(executed.get(), isMutationAction(resolved.getAction()));
                }
                if (resolved.getAction() == Action.TASK_QUERY) {
                    String agentReply = agentOrchestrator.handleMessage(message, request.getUserRole(), request.getUserName());
                    if (isUsefulAgentReply(agentReply)) {
                        return new GenAiChatResponse(agentReply, indicatesWorkspaceMutation(agentReply));
                    }
                }
            }
        }

        if (looksLikeTaskQuery(message)) {
            String agentReply = agentOrchestrator.handleMessage(message, request.getUserRole(), request.getUserName());
            if (isUsefulAgentReply(agentReply)) {
                return new GenAiChatResponse(agentReply, indicatesWorkspaceMutation(agentReply));
            }
        }

        List<GenAiChatMessage> history = request.getHistory();
        try {
            List<Message> msgs = new ArrayList<>();
            msgs.add(new SystemMessage(buildSystemPrompt(request)));
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
                return new GenAiChatResponse(geminiReply.trim());
            }
        } catch (Exception ex) {
            logger.warn("Gemini chat request failed", ex);
        }

        return new GenAiChatResponse(
            "Tell me what you'd like — create a team, project, or sprint, or ask about workload. I'll do it in the workspace.");
    }

    private boolean isMutationAction(Action action) {
        return action == Action.CREATE_TEAM
            || action == Action.CREATE_PROJECT
            || action == Action.CREATE_SPRINT
            || action == Action.CREATE_TASK
            || action == Action.COMPLETE_TASK;
    }

    private boolean isWorkspaceSuccess(String reply) {
        if (reply == null) {
            return false;
        }
        String trimmed = reply.trim();
        return trimmed.startsWith("Done —") || trimmed.startsWith("Done -");
    }

    private boolean indicatesWorkspaceMutation(String reply) {
        if (reply == null || reply.isBlank()) {
            return false;
        }
        String lower = reply.toLowerCase(Locale.ROOT);
        return lower.contains("tarea creada")
            || lower.contains("created the task")
            || lower.contains("i created the task")
            || lower.contains("marked as completed")
            || lower.contains("marked as done")
            || lower.contains("eliminad")
            || lower.contains("deleted")
            || lower.contains("updated")
            || lower.contains("actualiz");
    }

    private String buildSystemPrompt(GenAiChatRequest request) {
        String userName = request.getUserName();
        String userRole = request.getUserRole();
        if ((userName == null || userName.isBlank()) && (userRole == null || userRole.isBlank())) {
            return LUMI_SYSTEM_PROMPT;
        }
        StringBuilder prompt = new StringBuilder(LUMI_SYSTEM_PROMPT);
        prompt.append("\nYou are talking to a signed-in user:");
        if (userName != null && !userName.isBlank()) {
            prompt.append(" name: ").append(userName.trim()).append(".");
        }
        if (userRole != null && !userRole.isBlank()) {
            prompt.append(" role: ").append(userRole.trim()).append(".");
        }
        prompt.append("\nGreet and refer to them by their name. Never call them a guest.");
        return prompt.toString();
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
