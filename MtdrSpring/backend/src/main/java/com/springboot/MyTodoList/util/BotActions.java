package com.springboot.MyTodoList.util;

import com.springboot.MyTodoList.agent.AgentOrchestrator;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.SprintTaskId;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.model.enums.TaskPriority;
import com.springboot.MyTodoList.model.enums.TaskStatus;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.SprintTaskService;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.service.DeepSeekService;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class BotActions{

    private static final Logger logger = LoggerFactory.getLogger(BotActions.class);

    String requestText;
    long chatId;
    Long telegramUserId;
    TelegramClient telegramClient;
    boolean exit;

    TaskService taskService;
    SprintService sprintService;
    SprintTaskService sprintTaskService;
    UserService userService;
    DeepSeekService deepSeekService;
    AgentOrchestrator agentOrchestrator;

    public BotActions(TelegramClient tc, TaskService ts, SprintService ss, SprintTaskService sts, UserService us, DeepSeekService ds, AgentOrchestrator ao) {
        telegramClient = tc;
        taskService = ts;
        sprintService = ss;
        sprintTaskService = sts;
        userService = us;
        deepSeekService = ds;
        agentOrchestrator = ao;
        exit  = false;
    }

    public void setRequestText(String cmd){
        requestText=cmd;
    }

    public void setChatId(long chId){
        chatId=chId;
    }

    public void setTelegramUserId(Long tgUserId){
        telegramUserId = tgUserId;
    }

    public void setTelegramClient(TelegramClient tc){
        telegramClient=tc;
    }

    public void setDeepSeekService(DeepSeekService dssvc){
        deepSeekService = dssvc;
    }

    public DeepSeekService getDeepSeekService(){
        return deepSeekService;
    }

    private User findUserByTelegramId(String telegramId) {
        List<User> users = userService.findAll();

        for (User u : users) {
            if (u.getTelegramId() != null &&
                u.getTelegramId().equals(telegramId)) {
                return u;
            }
        }

        return null;
    }

    

    public void fnStart() {
        if (!(requestText.equals(BotCommands.START_COMMAND.getCommand()) || requestText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) || exit) 
            return;

        BotHelper.sendMessageToTelegram(chatId, BotMessages.WELCOME.getMessage(), telegramClient);
        exit = true;
    }

    public void fnRegister() {
        if (!requestText.startsWith(BotCommands.REGISTER_COMMAND.getCommand()) || exit) return;

        List<User> users = userService.findAll();

        for (User u : users) {
            if (u.getTelegramId() != null &&
                u.getTelegramId().equals(String.valueOf(telegramUserId))) {

                String nombre =(u.getName() != null) ? u.getName() : "usuario";

                BotHelper.sendMessageToTelegram(chatId,
                        BotMessages.USER_OK.getMessage() + " " + nombre + "!",
                        telegramClient);

                exit = true;
                return;
            }
        }

        BotHelper.sendMessageToTelegram(chatId,
                BotMessages.USER_NOT_FOUND.getMessage(),
                telegramClient);

        exit = true;
    }

    public void fnAddTask() {
        if (!requestText.startsWith(BotCommands.ADD_TASK.getCommand()) || exit) return;

        try {
            String[] parts = requestText.replace("/addtask", "").trim().split("\\|");

            if (parts.length < 5) {
                throw new IllegalArgumentException("Formato incompleto para /addtask");
            }

            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }

            String title = extractQuotedValue(parts[0]);
            String description = extractQuotedValue(parts[1]);

            int horas = Integer.parseInt(parts[2]);

            if (horas > 4) {
                BotHelper.sendMessageToTelegram(chatId,
                        BotMessages.TASK_MAX_HOURS.getMessage(),
                        telegramClient);
                exit = true;
                return;
            }

            User user = findUserByTelegramId(String.valueOf(telegramUserId));

            if (user == null) {
                BotHelper.sendMessageToTelegram(chatId,
                        BotMessages.USER_NOT_FOUND.getMessage(),
                        telegramClient);
                exit = true;
                return;
            }

            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(TaskPriority.valueOf(parts[3]));
            task.setAssignedTo(Long.parseLong(parts[4]));
            task.setCreatedBy(user.getId());
            task.setStatus(TaskStatus.PENDING);
            task.setVector("TELEGRAM");

            taskService.add(task);

            logger.info("Task creada: " + task.getTitle());

            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.TASK_CREATED.getMessage(),
                    telegramClient);

        } catch (Exception e) {
            logger.error("Error creando task", e);

            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.TASK_ERROR.getMessage(),
                    telegramClient);
        }

        exit = true;
    }

    private String extractQuotedValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Valor nulo");
        }

        String trimmed = value.trim();
        if (trimmed.length() < 2 || !trimmed.startsWith("\"") || !trimmed.endsWith("\"")) {
            throw new IllegalArgumentException("El titulo y la descripcion deben estar entre comillas dobles");
        }

        String unquoted = trimmed.substring(1, trimmed.length() - 1).trim();
        if (unquoted.isEmpty()) {
            throw new IllegalArgumentException("Valor vacio");
        }

        return unquoted;
    }

    public void fnAssignTask() {
        if (!requestText.startsWith(BotCommands.ASSIGN_TASK.getCommand()) || exit) return;

        try {
            String[] parts = requestText.replace("/assigntask", "").trim().split("\\|");

            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }

            Long taskId = Long.parseLong(parts[0]);
            Long sprintId = Long.parseLong(parts[1]);

            Task task = taskService.getById(taskId).getBody();

            if (task == null) {
                BotHelper.sendMessageToTelegram(chatId,
                        BotMessages.TASK_NOT_FOUND.getMessage(),
                        telegramClient);
                exit = true;
                return;
            }

            task.setStatus(TaskStatus.IN_PROGRESS);
            taskService.update(taskId, task);

            SprintTask st = new SprintTask();
            st.setTaskId(taskId);
            st.setSprintId(sprintId);
            st.setAddedAt(LocalDateTime.now());

            sprintTaskService.add(st);

            logger.info("Task asignada");

            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.TASK_ASSIGNED.getMessage(),
                    telegramClient);

        } catch (Exception e) {
            logger.error("Error assign", e);

            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.TASK_ASSIGN_ERROR.getMessage(),
                    telegramClient);
        }

        exit = true;
    }


    public void fnCompleteTask() {
        if (!requestText.startsWith(BotCommands.COMPLETE_TASK.getCommand()) || exit) return;

        try {
            String[] parts = requestText.replace("/completetask", "").trim().split("\\|");

            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }

            Long taskId = Long.parseLong(parts[0]);
            int horas = Integer.parseInt(parts[1]);

            Task task = taskService.getById(taskId).getBody();

            if (task == null) {
                BotHelper.sendMessageToTelegram(chatId,
                        BotMessages.TASK_NOT_FOUND.getMessage(),
                        telegramClient);
                exit = true;
                return;
            }

            task.setStatus(TaskStatus.DONE);
            task.setHoursDone(horas);
            taskService.update(taskId, task);

            logger.info("Task completada");

            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.TASK_COMPLETED.getMessage() + " (" + horas + "h)",
                    telegramClient);

        } catch (Exception e) {
            logger.error("Error complete", e);

            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.TASK_COMPLETE_ERROR.getMessage(),
                    telegramClient);
        }

        exit = true;
    }

    public void fnListTasks() {
        if (!requestText.startsWith(BotCommands.LIST_TASKS.getCommand()) || exit) return;

        User user = findUserByTelegramId(String.valueOf(telegramUserId));
        if (user == null) {
            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.USER_NOT_FOUND.getMessage(),
                    telegramClient);
            exit = true;
            return;
        }

        List<Task> tasks = taskService.findAll();
        List<Task> assignedTasks = tasks.stream()
                .filter(t -> user.getId() != null && user.getId().equals(t.getAssignedTo()))
                .collect(Collectors.toList());

        String msg = BotMessages.TASK_LIST_HEADER.getMessage();

        for (Task t : assignedTasks) {
            msg += "ID: " + t.getId()
                    + " | " + t.getTitle()
                    + " | " + t.getStatus()
                    + "\n";
        }

        if (assignedTasks.isEmpty()) {
            msg += "\nNo tienes tareas asignadas.";
        }

        BotHelper.sendMessageToTelegram(chatId, msg, telegramClient);

        exit = true;
    }

    public void fnLLM() {
        if (!requestText.startsWith(BotCommands.LLM_REQ.getCommand()) || exit) return;

        try {
            String response = deepSeekService.generateText(requestText);

            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.LLM_RESPONSE.getMessage() + response,
                    telegramClient);

        } catch (Exception e) {
            logger.error("Error con LLM", e);

            BotHelper.sendMessageToTelegram(chatId,
                    "Error al generar respuesta con IA",
                    telegramClient);
        }

        exit = true;
    }

    public void fnElse() {
        if (exit) return;

        try {
            String response = agentOrchestrator.handleMessage(requestText);
            if (response != null && !response.isBlank()) {
                BotHelper.sendMessageToTelegram(chatId, response, telegramClient);
            }
        } catch (Exception e) {
            logger.error("Error con el agente de IA", e);
            BotHelper.sendMessageToTelegram(chatId,
                    BotMessages.UNKNOWN_COMMAND.getMessage(),
                    telegramClient);
        }
    }

}