package com.springboot.MyTodoList.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springboot.MyTodoList.agent.AgentOrchestrator;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.model.enums.TaskPriority;
import com.springboot.MyTodoList.model.enums.TaskStatus;
import com.springboot.MyTodoList.service.DeepSeekService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.SprintTaskService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@ExtendWith(MockitoExtension.class)
class BotActionsTest {

    private static final long CHAT_ID = 99L;
    private static final long TELEGRAM_ID = 8238992749L;
    private static final long USER_ID = 7L;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private TaskService taskService;

    @Mock
    private SprintService sprintService;

    @Mock
    private SprintTaskService sprintTaskService;

    @Mock
    private UserService userService;

    @Mock
    private DeepSeekService deepSeekService;

    @Mock
    private AgentOrchestrator agentOrchestrator;

    @Captor
    private ArgumentCaptor<SendMessage> sendMessageCaptor;

    private BotActions botActions;

    @BeforeEach
    void setUp() {
        botActions = new BotActions(
                telegramClient,
                taskService,
                sprintService,
                sprintTaskService,
                userService,
                deepSeekService,
                agentOrchestrator);
        botActions.setChatId(CHAT_ID);
        botActions.setTelegramUserId(TELEGRAM_ID);
    }

    @Test
    void fnStartSendsWelcomeMessage() throws Exception {
        botActions.setRequestText(BotCommands.START_COMMAND.getCommand());

        botActions.fnStart();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.WELCOME.getMessage());
    }

    @Test
    void fnRegisterRecognizesRafaelFromTelegramId() throws Exception {
        User rafael = userWithTelegramId(USER_ID, TELEGRAM_ID, "Rafael");
        when(userService.findAll()).thenReturn(List.of(rafael));
        botActions.setRequestText(BotCommands.REGISTER_COMMAND.getCommand());

        botActions.fnRegister();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.USER_OK.getMessage() + " Rafael!");
    }

    @Test
    void fnAddTaskUsesRafaelAsCurrentAndAssignedUser() throws Exception {
        User rafael = userWithTelegramId(USER_ID, TELEGRAM_ID, "Rafael");
        when(userService.findAll()).thenReturn(List.of(rafael));
        when(taskService.add(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        botActions.setRequestText(
                "/addtask \"Implementar prueba Telegram\" | \"Validar integración\" | 2 | HIGH | Rafael");

        botActions.fnAddTask();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).add(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getTitle()).isEqualTo("Implementar prueba Telegram");
        assertThat(savedTask.getDescription()).isEqualTo("Validar integración");
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(savedTask.getAssignedTo()).isEqualTo(USER_ID);
        assertThat(savedTask.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(savedTask.getVector()).isEqualTo("TELEGRAM");

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_CREATED.getMessage());
    }

    @Test
    void fnAssignTaskMovesTaskToSprint() throws Exception {
        Task task = new Task();
        task.setId(11L);
        task.setTitle("Revisar API");
        task.setStatus(TaskStatus.PENDING);
        when(taskService.getById(11L)).thenReturn(ResponseEntity.ok(task));
        when(sprintService.findAll()).thenReturn(List.of(sprintWithId(3L, "Sprint 3")));
        botActions.setRequestText("/assigntask 11 | Sprint 3");

        botActions.fnAssignTask();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).update(eq(11L), taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        ArgumentCaptor<SprintTask> sprintTaskCaptor = ArgumentCaptor.forClass(SprintTask.class);
        verify(sprintTaskService).add(sprintTaskCaptor.capture());
        assertThat(sprintTaskCaptor.getValue().getTaskId()).isEqualTo(11L);
        assertThat(sprintTaskCaptor.getValue().getSprintId()).isEqualTo(3L);

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_ASSIGNED.getMessage());
    }

    @Test
    void fnCompleteTaskMarksTaskDone() throws Exception {
        Task task = new Task();
        task.setId(21L);
        task.setTitle("Cerrar tarea");
        task.setStatus(TaskStatus.IN_PROGRESS);
        when(taskService.getById(21L)).thenReturn(ResponseEntity.ok(task));
        botActions.setRequestText("/completetask 21 | 3");

        botActions.fnCompleteTask();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).update(eq(21L), taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(taskCaptor.getValue().getHoursDone()).isEqualTo(3);

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_COMPLETED.getMessage() + " (3h)");
    }

    @Test
    void fnListTasksShowsOnlyTasksAssignedToRafael() throws Exception {
        User rafael = userWithTelegramId(USER_ID, TELEGRAM_ID, "Rafael");
        Task taskOne = taskWithIdAndAssignment(101L, "Tarea uno", USER_ID, TaskStatus.PENDING);
        Task taskTwo = taskWithIdAndAssignment(102L, "Tarea dos", 99L, TaskStatus.DONE);
        when(userService.findAll()).thenReturn(List.of(rafael));
        when(taskService.findAll()).thenReturn(List.of(taskOne, taskTwo));
        botActions.setRequestText(BotCommands.LIST_TASKS.getCommand());

        botActions.fnListTasks();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .contains(BotMessages.TASK_LIST_HEADER.getMessage())
                .contains("ID: 101 | Tarea uno | PENDING")
                .doesNotContain("Tarea dos");
    }

    @Test
    void fnLlmSendsTheGeneratedResponse() throws Exception {
        when(deepSeekService.generateText(any())).thenReturn("Respuesta generada");
        botActions.setRequestText("/llm resume la semana");

        botActions.fnLLM();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.LLM_RESPONSE.getMessage() + "Respuesta generada");
    }

    @Test
    void fnElseSendsAgentResponseWhenItIsNotACommand() throws Exception {
        when(agentOrchestrator.handleMessage("hola bot")).thenReturn("Hola Rafael");
        botActions.setRequestText("hola bot");

        botActions.fnElse();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText()).isEqualTo("Hola Rafael");
    }

    private User userWithTelegramId(Long id, Long telegramId, String name) {
        User user = new User();
        user.setId(id);
        user.setTelegramId(String.valueOf(telegramId));
        user.setName(name);
        return user;
    }

    private Sprint sprintWithId(Long id, String name) {
        Sprint sprint = new Sprint();
        sprint.setId(id);
        sprint.setName(name);
        sprint.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        sprint.setEndDate(LocalDateTime.of(2026, 1, 15, 0, 0));
        return sprint;
    }

    private Task taskWithIdAndAssignment(Long id, String title, Long assignedTo, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setAssignedTo(assignedTo);
        task.setStatus(status);
        return task;
    }
}