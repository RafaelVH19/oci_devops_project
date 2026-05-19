package com.springboot.MyTodoList.agent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class AgentOrchestrator {

    private final LlmIntentParser llmIntentParser;
    private final ProjectWorkspaceService workspaceService;

    public AgentOrchestrator(LlmIntentParser llmIntentParser, ProjectWorkspaceService workspaceService) {
        this.llmIntentParser = llmIntentParser;
        this.workspaceService = workspaceService;
    }

    public String handleMessage(String messageText) {
        return handleMessage(messageText, null);
    }

    public String handleMessage(String messageText, String userRole) {
        ParsedIntent parsedIntent = llmIntentParser.parse(messageText);

        if (parsedIntent.getResponseText() != null && !parsedIntent.getResponseText().isBlank()) {
            return parsedIntent.getResponseText();
        }

        if (parsedIntent.isClarificationNeeded()) {
            return parsedIntent.getClarificationQuestion();
        }

        switch (parsedIntent.getIntent()) {
            case HELP:
                return helpText(userRole);
            case LIST_TASKS:
                return formatTasks("Estas son las tareas registradas:", workspaceService.findAllTasks());
            case LIST_TASKS_BY_ASSIGNEE:
                return formatTasks("Estas son las tareas de " + safe(parsedIntent.getAssignee()) + ":",
                    workspaceService.findTasksByAssignee(parsedIntent.getAssignee()));
            case LIST_TASKS_BY_STATUS:
                return formatTasks("Estas son las tareas con estado " + safe(parsedIntent.getStatus()) + ":",
                    workspaceService.findTasksByStatus(parsedIntent.getStatus()));
            case CREATE_TASK:
                return createTask(parsedIntent);
            case DELETE_TASK:
                return deleteTaskResponse(parsedIntent);
            case GET_DEVELOPER_KPI:
                return getDeveloperKpiResponse(parsedIntent);
            case CURRENT_SPRINT_SUMMARY:
                return sprintSummary();
            case TEAM_LOAD_SUMMARY:
                return teamLoadSummary();
            default:
                return "No pude interpretar la solicitud. Escribe ayuda para ver ejemplos.";
        }
    }

    private String createTask(ParsedIntent parsedIntent) {
        if (parsedIntent.getTitle() == null || parsedIntent.getTitle().isBlank()) {
            return "Necesito el titulo de la tarea para poder crearla.";
        }

        TaskItem task = workspaceService.createTask(
            parsedIntent.getTitle(),
            parsedIntent.getAssignee(),
            parsedIntent.getStoryPoints() == null ? 3 : parsedIntent.getStoryPoints(),
            parsedIntent.getSprintName()
        );

        return "Tarea creada correctamente.\n"
            + "Id: " + task.getId() + "\n"
            + "Titulo: " + task.getTitle() + "\n"
            + "Responsable: " + task.getAssignee() + "\n"
            + "Estado: " + task.getStatus() + "\n"
            + "Story points: " + task.getStoryPoints() + "\n"
            + "Sprint: " + task.getSprintName();
    }

    private String deleteTaskResponse(ParsedIntent parsedIntent) {
        if (parsedIntent.getTaskId() == null && parsedIntent.getTitle() == null) {
            return "Necesito el ID o nombre de la tarea para poder eliminarla.";
        }
        
        return "Para eliminar la tarea, usa el comando: /deletetask " 
            + (parsedIntent.getTaskId() != null ? parsedIntent.getTaskId() : parsedIntent.getTitle());
    }

    private String getDeveloperKpiResponse(ParsedIntent parsedIntent) {
        if (parsedIntent.getTaskId() == null && parsedIntent.getDeveloperName() == null) {
            return "Necesito el ID o nombre del desarrollador para ver sus KPIs.";
        }
        
        return "Para ver los KPIs del desarrollador, usa el comando: /teamkpis " 
            + (parsedIntent.getTaskId() != null ? parsedIntent.getTaskId() : parsedIntent.getDeveloperName());
    }

    private String sprintSummary() {
        SprintInfo sprint = workspaceService.getCurrentSprint();
        if (sprint == null) {
            return "No hay un sprint activo en este momento.";
        }

        List<TaskItem> sprintTasks = workspaceService.findAllTasks().stream()
            .filter(task -> sprint.getName().equals(task.getSprintName()))
            .collect(java.util.stream.Collectors.toList());

        long done = sprintTasks.stream().filter(task -> "DONE".equals(task.getStatus())).count();
        long inProgress = sprintTasks.stream().filter(task -> "IN_PROGRESS".equals(task.getStatus())).count();
        long pending = sprintTasks.stream().filter(task -> "PENDING".equals(task.getStatus())).count();
        int totalPoints = sprintTasks.stream().mapToInt(TaskItem::getStoryPoints).sum();

        return "Resumen del sprint actual\n"
            + "Sprint: " + sprint.getName() + "\n"
            + "Inicio: " + sprint.getStartDate() + "\n"
            + "Fin: " + sprint.getEndDate() + "\n"
            + "Tareas: " + sprintTasks.size() + "\n"
            + "DONE: " + done + "\n"
            + "IN_PROGRESS: " + inProgress + "\n"
            + "PENDING: " + pending + "\n"
            + "Story points totales: " + totalPoints;
    }

    private String teamLoadSummary() {
        Map<String, Integer> totals = workspaceService.storyPointsByAssignee();
        StringJoiner joiner = new StringJoiner("\n", "Carga actual del equipo\n", "");
        totals.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
            .forEach(entry -> joiner.add("- " + entry.getKey() + ": " + entry.getValue() + " pts"));
        return joiner.toString().trim();
    }

    private String formatTasks(String title, List<TaskItem> tasks) {
        if (tasks.isEmpty()) {
            return title + "\nNo encontré tareas para ese criterio.";
        }

        StringJoiner joiner = new StringJoiner("\n", title + "\n", "");
        for (TaskItem task : tasks) {
            joiner.add(String.format("- %d [%s] %s | %s | %d pts | %s",
                task.getId(),
                task.getStatus(),
                task.getTitle(),
                task.getAssignee(),
                task.getStoryPoints(),
                task.getSprintName()
            ));
        }
        return joiner.toString().trim();
    }

    private String helpText(String userRole) {
        String help = "Puedo ayudarte con consultas y acciones del proyecto, incluso en lenguaje natural.\n\n"
            + "Ejemplos:\n"
            + "- que tareas tiene ana\n"
            + "- que tareas siguen pendientes\n"
            + "- crea una tarea llamada revisar la api con descripcion validar contratos asignada a luis de prioridad alta con 4 horas\n"
            + "- como va el sprint actual\n"
            + "- quien tiene mas carga\n\n"
            + "Comandos disponibles:\n"
            + "/register - Registrarse como usuario\n"
            + "/addtask - Agregar nueva tarea\n"
            + "/deletetask - Eliminar una tarea\n"
            + "/assigntask - Asignar tarea a un sprint\n"
            + "/completetask - Marcar tarea como completada\n"
            + "/mytasks - Ver mis tareas\n"
            + "/llm - Hacer pregunta libre a la IA";
        
        if ("MANAGER".equalsIgnoreCase(userRole)) {
            help += "\n\nComandos de GERENTE:\n"
                + "/teamkpis <desarrollador> - Ver KPIs de un desarrollador\n"
                + "/teamtasks - Ver todas las tareas del equipo";
        }
        
        return help;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "sin filtro" : value;
    }
}
