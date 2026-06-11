package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.config.AiProps;
import com.springboot.MyTodoList.controller.dto.GenAiChatMessage;
import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.LumiActionPlan.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LumiIntentService {

    private static final Logger logger = LoggerFactory.getLogger(LumiIntentService.class);

    private static final String PLANNER_PROMPT = """
        You are Lumi's action planner for a project management app.
        Read the user message (and short history) and decide if they want you to DO something in the database.

        Respond with JSON only (no markdown). Schema:
        {
          "action": "NONE|CHAT|CREATE_TEAM|CREATE_PROJECT|CREATE_SPRINT|CREATE_TASK|COMPLETE_TASK|WORKLOAD|TASK_QUERY",
          "teamName": "",
          "memberNames": [],
          "managerName": "",
          "projectName": "",
          "sourceTeamName": "",
          "sprintName": "",
          "startDate": "YYYY-MM-DD or YYYY-MM-DD HH:MM",
          "endDate": "YYYY-MM-DD or YYYY-MM-DD HH:MM",
          "taskTitle": "",
          "taskDescription": "",
          "assigneeName": "",
          "expectedHours": 0,
          "taskPriority": "LOW|MEDIUM|HIGH",
          "taskDueDate": "YYYY-MM-DD",
          "hoursDone": 0,
          "needsClarification": false,
          "clarificationQuestion": ""
        }

        Rules:
        - Natural language is fine ("Can you set up a team called X with Y and Z?").
        - Map intent to CREATE_* when the user wants something created, even without the words "create" or "crear".
        - Use CREATE_TASK when the user wants a task/tarea created ("create a task X for sprint 4, 2h").
          Extract taskTitle, expectedHours (number, "2h" -> 2), assigneeName, and sprintName if mentioned.
        - Task priority: if the user states a priority, use it. Otherwise INFER taskPriority from how close
          today's date is to the task's target date (its due date, or the end date of the sprint it goes into,
          using the sprint dates from the workspace context):
          due within 2 days -> HIGH, within 7 days -> MEDIUM, later -> LOW. No date at all -> MEDIUM.
        - taskDueDate: set it if the user mentions a deadline; otherwise leave it empty.
        - Use COMPLETE_TASK when the user says they finished, completed, or closed a task
          ("complete the task X", "I finished X", "terminé X", "mark X as done").
          Set taskTitle to the task they mean — match it against the open tasks in the workspace context.
          Extract hoursDone if they mention time spent ("took me 3h" -> 3).
          If you cannot tell WHICH task they mean, set needsClarification=true and ask (in English)
          which task it is, listing the open tasks from the context as bullets.
        - Use WORKLOAD for questions about remaining work, capacity, or hours this week.
        - Use TASK_QUERY for listing tasks, sprint status, assignee workload (not WORKLOAD).
        - Use CHAT for general questions with no workspace change.
        - Use NONE only if truly unrelated.
        - Match memberNames/managerName/assigneeName to people from the workspace context when possible.
        - When the user says "me", "yo", "I", or "myself", set assigneeName to the current user's name from the context.
        - EXTRACT EVERYTHING IN ONE PASS: pull every field you can from the message AND the conversation history.
          Never re-ask for information the user already gave earlier in the conversation.
        - Only set needsClarification=true when a REQUIRED field is truly missing:
          CREATE_TASK requires only taskTitle (assignee defaults to the current user; sprint and hours are optional).
          CREATE_TEAM requires teamName. CREATE_PROJECT requires projectName. CREATE_SPRINT requires sprintName, startDate, endDate.
        - If clarification is needed, ask ONE single question in clarificationQuestion that covers ALL missing fields
          at once (use a short markdown bullet list), instead of asking one by one.
        - clarificationQuestion MUST ALWAYS be written in English, regardless of the user's language.
          Never mention slash commands, /addtask, or BotFather-style syntax.
        - Dates: infer from phrases like "next two weeks", "today until tomorrow", "May 20 to June 3 2026" when possible.
        """;

    private final AiProps aiProps;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final TeamService teamService;
    private final SprintService sprintService;
    private final TaskService taskService;

    public LumiIntentService(AiProps aiProps,
                             ObjectMapper objectMapper,
                             UserService userService,
                             TeamService teamService,
                             SprintService sprintService,
                             TaskService taskService) {
        this.aiProps = aiProps;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.teamService = teamService;
        this.sprintService = sprintService;
        this.taskService = taskService;
    }

    public boolean isAvailable() {
        return aiProps.isEnabled()
            && aiProps.getApiKey() != null
            && !aiProps.getApiKey().isBlank();
    }

    public Optional<LumiActionPlan> extractPlan(String message, List<GenAiChatMessage> history) {
        return extractPlan(message, history, null, null);
    }

    public Optional<LumiActionPlan> extractPlan(String message, List<GenAiChatMessage> history,
                                                String userName, String userRole) {
        if (!isAvailable() || message == null || message.isBlank()) {
            return Optional.empty();
        }

        try {
            RestClient client = RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiProps.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

            String endpoint = aiProps.getBaseUrl().replaceAll("/$", "") + "/chat/completions";
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(messageOf("system",
                PLANNER_PROMPT + "\n\n" + workspaceContext() + "\n" + sessionContext(userName, userRole)));

            if (history != null) {
                int start = Math.max(0, history.size() - 6);
                for (int i = start; i < history.size(); i++) {
                    GenAiChatMessage item = history.get(i);
                    if (item == null || item.getContent() == null || item.getContent().isBlank()) {
                        continue;
                    }
                    messages.add(messageOf(normalizeRole(item.getRole()), item.getContent()));
                }
            }

            messages.add(messageOf("user", message));

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", aiProps.getModel());
            payload.put("messages", messages);
            payload.put("temperature", 0);
            payload.put("response_format", Map.of("type", "json_object"));

            String responseBody = client.post()
                .uri(endpoint)
                .body(payload)
                .retrieve()
                .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                return Optional.empty();
            }

            return Optional.of(parsePlan(content.asText()));
        } catch (Exception ex) {
            logger.warn("Lumi intent extraction failed", ex);
            return Optional.empty();
        }
    }

    private LumiActionPlan parsePlan(String json) throws Exception {
        String payload = extractJsonPayload(json);
        JsonNode node = objectMapper.readTree(payload);
        LumiActionPlan plan = new LumiActionPlan();

        String actionRaw = node.path("action").asText("NONE");
        try {
            plan.setAction(Action.valueOf(actionRaw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            plan.setAction(Action.NONE);
        }

        plan.setTeamName(textOrNull(node, "teamName"));
        plan.setManagerName(textOrNull(node, "managerName"));
        plan.setProjectName(textOrNull(node, "projectName"));
        plan.setSourceTeamName(textOrNull(node, "sourceTeamName"));
        plan.setSprintName(textOrNull(node, "sprintName"));
        plan.setStartDate(textOrNull(node, "startDate"));
        plan.setEndDate(textOrNull(node, "endDate"));
        plan.setTaskTitle(textOrNull(node, "taskTitle"));
        plan.setTaskDescription(textOrNull(node, "taskDescription"));
        plan.setAssigneeName(textOrNull(node, "assigneeName"));
        JsonNode hours = node.path("expectedHours");
        if (hours.isNumber() && hours.asInt() > 0) {
            plan.setExpectedHours(hours.asInt());
        }
        plan.setTaskPriority(textOrNull(node, "taskPriority"));
        plan.setTaskDueDate(textOrNull(node, "taskDueDate"));
        JsonNode doneHours = node.path("hoursDone");
        if (doneHours.isNumber() && doneHours.asInt() > 0) {
            plan.setHoursDone(doneHours.asInt());
        }
        plan.setNeedsClarification(node.path("needsClarification").asBoolean(false));
        plan.setClarificationQuestion(textOrNull(node, "clarificationQuestion"));

        JsonNode members = node.path("memberNames");
        if (members.isArray()) {
            List<String> names = new ArrayList<>();
            members.forEach(item -> {
                if (item.isTextual() && !item.asText().isBlank()) {
                    names.add(item.asText().trim());
                }
            });
            plan.setMemberNames(names);
        }

        return plan;
    }

    private String workspaceContext() {
        List<User> users = userService.findAll();
        List<Team> teams = teamService.findAll();

        String people = users.isEmpty()
            ? "(no developers in database yet)"
            : users.stream()
                .map(u -> "- " + (u.getName() != null ? u.getName() : "user " + u.getId())
                    + (u.getRole() != null ? " [" + u.getRole() + "]" : ""))
                .collect(Collectors.joining("\n"));

        String teamLines = teams.isEmpty()
            ? "(no teams yet)"
            : teams.stream()
                .map(t -> "- " + t.getName())
                .collect(Collectors.joining("\n"));

        String sprintLines;
        try {
            List<com.springboot.MyTodoList.model.Sprint> sprints = sprintService.findAll();
            sprintLines = sprints.isEmpty()
                ? "(no sprints yet)"
                : sprints.stream()
                    .map(s -> "- " + s.getName()
                        + (s.getStartDate() != null ? " (start " + s.getStartDate().toLocalDate() : " (start ?")
                        + (s.getEndDate() != null ? ", end " + s.getEndDate().toLocalDate() + ")" : ", end ?)"))
                    .collect(Collectors.joining("\n"));
        } catch (Exception ex) {
            sprintLines = "(sprints unavailable)";
        }

        String taskLines;
        try {
            Map<Long, String> namesById = users.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, u -> u.getName() != null ? u.getName() : "user " + u.getId(),
                    (a, b) -> a));
            List<com.springboot.MyTodoList.model.Task> openTasks = taskService.findAll().stream()
                .filter(t -> t.getStatus() == null
                    || !"DONE".equalsIgnoreCase(t.getStatus().name()))
                .limit(20)
                .collect(Collectors.toList());
            taskLines = openTasks.isEmpty()
                ? "(no open tasks)"
                : openTasks.stream()
                    .map(t -> "- " + t.getTitle()
                        + (t.getAssignedTo() != null && namesById.containsKey(t.getAssignedTo())
                            ? " [assigned to " + namesById.get(t.getAssignedTo()) + "]"
                            : ""))
                    .collect(Collectors.joining("\n"));
        } catch (Exception ex) {
            taskLines = "(tasks unavailable)";
        }

        return "Workspace context:\nDevelopers:\n" + people + "\nTeams/projects:\n" + teamLines
            + "\nSprints:\n" + sprintLines + "\nOpen tasks:\n" + taskLines;
    }

    private String sessionContext(String userName, String userRole) {
        StringBuilder context = new StringBuilder("Today's date: " + java.time.LocalDate.now() + ".");
        if (userName != null && !userName.isBlank()) {
            context.append(" Current signed-in user: ").append(userName.trim());
            if (userRole != null && !userRole.isBlank()) {
                context.append(" (role ").append(userRole.trim()).append(")");
            }
            context.append(". When they say \"me\", \"yo\", or \"I\", they mean this user.");
        }
        return context.toString();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "user";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if ("assistant".equals(normalized) || "system".equals(normalized) || "user".equals(normalized)) {
            return normalized;
        }
        return "user";
    }

    private Map<String, Object> messageOf(String role, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String extractJsonPayload(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            if (firstNewLine >= 0) {
                trimmed = trimmed.substring(firstNewLine + 1).trim();
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
