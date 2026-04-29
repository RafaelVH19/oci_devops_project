package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.agent.ProjectWorkspaceService;
import com.springboot.MyTodoList.agent.SprintInfo;
import com.springboot.MyTodoList.agent.TaskItem;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.model.enums.TaskPriority;
import com.springboot.MyTodoList.model.enums.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DatabaseProjectWorkspaceService implements ProjectWorkspaceService {

    private final TaskService taskService;
    private final SprintService sprintService;
    private final SprintTaskService sprintTaskService;
    private final UserService userService;

    public DatabaseProjectWorkspaceService(TaskService taskService,
                                           SprintService sprintService,
                                           SprintTaskService sprintTaskService,
                                           UserService userService) {
        this.taskService = taskService;
        this.sprintService = sprintService;
        this.sprintTaskService = sprintTaskService;
        this.userService = userService;
    }

    @Override
    public List<TaskItem> findAllTasks() {
        return buildTaskItems(taskService.findAll());
    }

    @Override
    public List<TaskItem> findTasksByAssignee(String assignee) {
        if (assignee == null || assignee.isBlank()) {
            return findAllTasks();
        }
        String normalized = assignee.trim().toLowerCase(Locale.ROOT);
        List<Task> all = taskService.findAll();
        List<User> users = userService.findAll();

        Map<Long, String> userNameById = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u.getName() != null ? u.getName() : ""));

        List<Task> filtered = all.stream()
            .filter(task -> {
                String name = userNameById.getOrDefault(task.getAssignedTo(), "").toLowerCase(Locale.ROOT);
                return name.contains(normalized);
            })
            .collect(Collectors.toList());

        return buildTaskItems(filtered);
    }

    @Override
    public List<TaskItem> findTasksByStatus(String status) {
        String normalized = normalizeStatus(status);
        List<Task> all = taskService.findAll();
        List<Task> filtered = all.stream()
            .filter(task -> task.getStatus() != null && task.getStatus().name().equalsIgnoreCase(normalized))
            .collect(Collectors.toList());
        return buildTaskItems(filtered);
    }

    @Override
    public TaskItem createTask(String title, String assignee, int storyPoints, String sprintName) {
        List<User> users = userService.findAll();

        User assignedUser = null;
        if (assignee != null && !assignee.isBlank()) {
            String normalizedAssignee = assignee.trim().toLowerCase(Locale.ROOT);
            assignedUser = users.stream()
                .filter(u -> u.getName() != null && u.getName().toLowerCase(Locale.ROOT).contains(normalizedAssignee))
                .findFirst()
                .orElse(null);
        }
        if (assignedUser == null && !users.isEmpty()) {
            assignedUser = users.get(0);
        }

        Long assignedToId = assignedUser != null ? assignedUser.getId() : 1L;

        Task task = new Task();
        task.setTitle(title);
        task.setDescription("");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(storyPointsToPriority(storyPoints));
        task.setAssignedTo(assignedToId);
        task.setCreatedBy(assignedToId);
        task.setVector("TELEGRAM_AGENT");
        task.setDueDate(LocalDateTime.now().plusDays(5));

        Task saved = taskService.add(task);

        // Link to sprint if provided or default to current sprint
        Sprint sprint = resolveSprint(sprintName);
        if (sprint != null) {
            SprintTask sprintTask = new SprintTask();
            sprintTask.setTaskId(saved.getId());
            sprintTask.setSprintId(sprint.getId());
            sprintTask.setAddedAt(LocalDateTime.now());
            sprintTaskService.add(sprintTask);
        }

        String resolvedSprintName = sprint != null ? sprint.getName() : "Sin sprint";
        String assigneeName = assignedUser != null && assignedUser.getName() != null
            ? assignedUser.getName()
            : "Sin asignar";

        return new TaskItem(
            saved.getId(),
            saved.getTitle(),
            assigneeName,
            saved.getStatus().name(),
            storyPoints > 0 ? storyPoints : 3,
            resolvedSprintName,
            saved.getDueDate() != null ? saved.getDueDate().toLocalDate() : LocalDate.now().plusDays(5)
        );
    }

    @Override
    public SprintInfo getCurrentSprint() {
        List<Sprint> sprints = sprintService.findAll();
        if (sprints.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        // Prefer a sprint currently active (start <= now <= end)
        return sprints.stream()
            .filter(s -> s.getStartDate() != null && s.getEndDate() != null
                && !s.getStartDate().isAfter(now) && !s.getEndDate().isBefore(now))
            .max(Comparator.comparing(Sprint::getStartDate))
            .map(s -> new SprintInfo(s.getName(),
                s.getStartDate().toLocalDate(),
                s.getEndDate().toLocalDate()))
            .orElseGet(() -> {
                // Fall back to most recently created sprint
                Sprint latest = sprints.stream()
                    .max(Comparator.comparing(s -> s.getCreatedAt() != null ? s.getCreatedAt() : LocalDateTime.MIN))
                    .orElse(sprints.get(sprints.size() - 1));
                LocalDate start = latest.getStartDate() != null ? latest.getStartDate().toLocalDate() : LocalDate.now();
                LocalDate end = latest.getEndDate() != null ? latest.getEndDate().toLocalDate() : LocalDate.now().plusWeeks(2);
                return new SprintInfo(latest.getName(), start, end);
            });
    }

    @Override
    public Map<String, Integer> storyPointsByAssignee() {
        List<Task> tasks = taskService.findAll();
        List<User> users = userService.findAll();
        Map<Long, String> userNameById = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u.getName() != null ? u.getName() : "Usuario " + u.getId()));

        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Task task : tasks) {
            String name = userNameById.getOrDefault(task.getAssignedTo(), "Sin asignar");
            int pts = priorityToStoryPoints(task.getPriority());
            totals.merge(name, pts, Integer::sum);
        }
        return totals;
    }

    private List<TaskItem> buildTaskItems(List<Task> tasks) {
        List<User> users = userService.findAll();
        List<SprintTask> sprintTasks = sprintTaskService.findAll();
        List<Sprint> sprints = sprintService.findAll();

        Map<Long, String> userNameById = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u.getName() != null ? u.getName() : "Usuario " + u.getId()));

        Map<Long, String> sprintNameById = sprints.stream()
            .collect(Collectors.toMap(Sprint::getId, Sprint::getName));

        // For each task, find its most recent sprint assignment
        Map<Long, String> sprintNameByTaskId = new HashMap<>();
        for (SprintTask st : sprintTasks) {
            if (st.getRemovedAt() == null) {
                String sprintName = sprintNameById.get(st.getSprintId());
                if (sprintName != null) {
                    sprintNameByTaskId.put(st.getTaskId(), sprintName);
                }
            }
        }

        return tasks.stream()
            .sorted(Comparator.comparing(Task::getId))
            .map(task -> new TaskItem(
                task.getId(),
                task.getTitle(),
                userNameById.getOrDefault(task.getAssignedTo(), "Sin asignar"),
                task.getStatus() != null ? task.getStatus().name() : "PENDING",
                priorityToStoryPoints(task.getPriority()),
                sprintNameByTaskId.getOrDefault(task.getId(), "Sin sprint"),
                task.getDueDate() != null ? task.getDueDate().toLocalDate() : null
            ))
            .collect(Collectors.toList());
    }

    private Sprint resolveSprint(String sprintName) {
        List<Sprint> sprints = sprintService.findAll();
        if (sprints.isEmpty()) {
            return null;
        }
        if (sprintName != null && !sprintName.isBlank()) {
            String normalized = sprintName.trim().toLowerCase(Locale.ROOT);
            Sprint found = sprints.stream()
                .filter(s -> s.getName() != null && s.getName().toLowerCase(Locale.ROOT).contains(normalized))
                .findFirst()
                .orElse(null);
            if (found != null) {
                return found;
            }
        }
        // Fall back to current sprint
        LocalDateTime now = LocalDateTime.now();
        return sprints.stream()
            .filter(s -> s.getStartDate() != null && s.getEndDate() != null
                && !s.getStartDate().isAfter(now) && !s.getEndDate().isBefore(now))
            .max(Comparator.comparing(Sprint::getStartDate))
            .orElseGet(() -> sprints.stream()
                .max(Comparator.comparing(s -> s.getCreatedAt() != null ? s.getCreatedAt() : LocalDateTime.MIN))
                .orElse(null));
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            return "PENDING";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "pendiente":
            case "pending":
                return "PENDING";
            case "en progreso":
            case "in progress":
            case "in_progress":
                return "IN_PROGRESS";
            case "done":
            case "hecha":
            case "terminada":
                return "DONE";
            default:
                return normalized.toUpperCase(Locale.ROOT);
        }
    }

    private int priorityToStoryPoints(TaskPriority priority) {
        if (priority == null) {
            return 3;
        }
        switch (priority) {
            case LOW:
                return 1;
            case HIGH:
                return 5;
            default:
                return 3;
        }
    }

    private TaskPriority storyPointsToPriority(int storyPoints) {
        if (storyPoints <= 1) {
            return TaskPriority.LOW;
        } else if (storyPoints >= 5) {
            return TaskPriority.HIGH;
        } else {
            return TaskPriority.MEDIUM;
        }
    }
}
