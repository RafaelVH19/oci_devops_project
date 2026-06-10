package com.springboot.MyTodoList.util;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.agent.AgentOrchestrator;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.Team;
import com.springboot.MyTodoList.model.TeamMember;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.model.enums.TaskPriority;
import com.springboot.MyTodoList.model.enums.TaskStatus;
import com.springboot.MyTodoList.service.DeepSeekService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.SprintTaskService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.TeamMemberService;
import com.springboot.MyTodoList.service.TeamService;
import com.springboot.MyTodoList.service.UserService;

/**
 * Conjunto de pruebas unitarias para la clase.
 *
 * Estas pruebas verifican el comportamiento de los comandos del bot de Telegram,
 * incluyendo la gestión de tareas, asignación a sprints, cierre de tareas,
 * consultas de KPIs, interacción con el agente conversacional y validación de permisos.
 */
@ExtendWith(MockitoExtension.class)
class BotActionsTest {

    private static final long CHAT_ID = 99L;
    private static final long TELEGRAM_ID_DEVELOPER = 8434899056L;
    private static final long USER_ID_DEVELOPER = 6L;
    private static final long TELEGRAM_ID_MANAGER = 8238992749L;
    private static final long USER_ID_MANAGER = 7L;

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
    private TeamService teamService;

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private DeepSeekService deepSeekService;

    @Mock
    private AgentOrchestrator agentOrchestrator;

    @Captor
    private ArgumentCaptor<SendMessage> sendMessageCaptor;

    private BotActions botActions;

    /**
     * Inicializa la instancia de y configura los valores
     * base utilizados en todas las pruebas.
     *
     * Se establece un identificador de chat y un usuario de Telegram
     * predeterminado para simular las interacciones del bot.
     */
    @BeforeEach
    void setUp() {
        botActions = new BotActions(
                telegramClient,
                taskService,
                sprintService,
                sprintTaskService,
                userService,
                teamService,
                teamMemberService,
                deepSeekService,
                agentOrchestrator);
        botActions.setChatId(CHAT_ID);
        botActions.setTelegramUserId(TELEGRAM_ID_DEVELOPER);
    }

    /** Test que verifica que el comando de inicio (/start) envíe correctamente el mensaje de bienvenida configurado para el bot. */
    @Test
    void fnWelcome() throws Exception {
        botActions.setRequestText(BotCommands.START_COMMAND.getCommand());

        botActions.fnStart();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.WELCOME.getMessage());
    }

    /** Test que comprueba que un usuario registrado en el sistema pueda asociar correctamente su cuenta de Telegram y reciba el mensaje de confirmación correspondiente. */
    @Test
    void fnRegister() throws Exception {
        User developer = userWithTelegramId(USER_ID_DEVELOPER, TELEGRAM_ID_DEVELOPER, "DEVELOPER");
        when(userService.findAll()).thenReturn(List.of(developer));
        botActions.setRequestText(BotCommands.REGISTER_COMMAND.getCommand());

        botActions.fnRegister();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.USER_OK.getMessage() + " DEVELOPER!");
    }

    /** Test que verifica la creación correcta de una tarea a partir del comando recibido por Telegram. */
    @Test
    void fnAddTask() throws Exception {
        User developer = userWithTelegramId(USER_ID_DEVELOPER, TELEGRAM_ID_DEVELOPER, "DEVELOPER");
        when(userService.findAll()).thenReturn(List.of(developer));
        when(taskService.add(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        botActions.setRequestText(
                "/addtask \"Implementar prueba Telegram\" | \"Validar integración\" | 2 | HIGH | true | DEVELOPER");

        botActions.fnAddTask();

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).add(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getTitle()).isEqualTo("Implementar prueba Telegram");
        assertThat(savedTask.getDescription()).isEqualTo("Validar integración");
        assertThat(savedTask.getExpectedHours()).isEqualTo(2);
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(savedTask.getHoursDone()).isEqualTo(0);
        assertThat(savedTask.getIsBug()).isEqualTo(true);
        assertThat(savedTask.getAssignedTo()).isEqualTo(USER_ID_DEVELOPER);
        assertThat(savedTask.getCreatedBy()).isEqualTo(USER_ID_DEVELOPER);
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.PENDING);

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_CREATED.getMessage());
    }

    /** Test que comprueba que una tarea existente pueda eliminarse correctamente mediante el comando correspondiente. */
    @Test
    void fnDeleteTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.PENDING);
        when(taskService.getById(1L)).thenReturn(ResponseEntity.ok(task));
        botActions.setRequestText("/deletetask 1");

        botActions.fnDeleteTask();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_DELETED.getMessage());
        verify(taskService).delete(1L);
    }

    /** Test que verifica que el sistema informe adecuadamente cuando se intenta eliminar una tarea inexistente. */
    @Test
    void fnDeleteTaskFail() throws Exception {
        when(taskService.getById(1L)).thenReturn(ResponseEntity.ok(null));
        botActions.setRequestText("/deletetask 1");

        botActions.fnDeleteTask();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_NOT_FOUND.getMessage());
    }

    /** Test que comprueba que un comando de creación de tarea con formato inválido genere un mensaje de error para el usuario. */
    @Test
    void fnAddTaskInvalidFormatShowsError() throws Exception {
        botActions.setRequestText("/addtask \"Tarea incompleta\"");

        botActions.fnAddTask();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_ERROR.getMessage());
    }

    /** Test que verifica que un comando de asignación de tareas mal formado sea detectado y produzca una respuesta de error. */
    @Test
    void fnAssignTaskInvalidFormatShowsError() throws Exception {
        botActions.setRequestText("/assigntask 11");

        botActions.fnAssignTask();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_ASSIGN_ERROR.getMessage());
    }

    /** Test que comprueba que un comando de finalización de tareas con parámetros incompletos o inválidos genere el mensaje de error esperado. */
    @Test
    void fnCompleteTaskInvalidFormatShowsError() throws Exception {
        botActions.setRequestText("/completetask 21");

        botActions.fnCompleteTask();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_COMPLETE_ERROR.getMessage());
    }

    /** Test que verifica que el sistema maneje correctamente intentos de eliminación de tareas sin proporcionar un identificador válido. */
    @Test
    void fnDeleteTaskEmptyInputShowsError() throws Exception {
        botActions.setRequestText("/deletetask ");

        botActions.fnDeleteTask();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TASK_DELETE_ERROR.getMessage());
    }

    /** Test que comprueba la asignación de una tarea a un sprint existente. */
    @Test
    void fnAssignToSprint() throws Exception {
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

    /** Test que verifica que una tarea en progreso pueda marcarse como completada. */
    @Test
    void fnCompleteTask() throws Exception {
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

    /** Test que comprueba que un desarrollador únicamente visualice las tareas que le han sido asignadas. */
    @Test
    void fnListTasksDeveloper() throws Exception {
        User developer = userWithTelegramId(USER_ID_DEVELOPER, TELEGRAM_ID_DEVELOPER, "DEVELOPER");
        Task taskOne = taskWithIdAndAssignment(101L, "Tarea uno", USER_ID_DEVELOPER, TaskStatus.PENDING);
        Task taskTwo = taskWithIdAndAssignment(102L, "Tarea dos", 99L, TaskStatus.DONE);
        when(userService.findAll()).thenReturn(List.of(developer));
        when(taskService.findAll()).thenReturn(List.of(taskOne, taskTwo));
        botActions.setRequestText(BotCommands.LIST_TASKS.getCommand());

        botActions.fnListTasks();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .contains(BotMessages.TASK_LIST_HEADER.getMessage())
                .contains("ID: 101 | Tarea uno | PENDING")
                .doesNotContain("Tarea dos");
    }

    /** Test que verifica la integración con el servicio de inteligencia artificial. */
    @Test
    void fnLlmResponse() throws Exception {
        when(deepSeekService.generateText(any())).thenReturn("Respuesta generada");
        botActions.setRequestText("/llm resume la semana");

        botActions.fnLLM();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.LLM_RESPONSE.getMessage() + "Respuesta generada");
    }

    /** Comprueba que los mensajes no asociados a comandos explícitos sean procesados por el agente conversacional. */
    @Test
    void fnElseResponse() throws Exception {
        User developer = userWithTelegramId(USER_ID_DEVELOPER, TELEGRAM_ID_DEVELOPER, "DEVELOPER");
        developer.setRole("DEVELOPER");
        when(userService.findAll()).thenReturn(List.of(developer));
        when(agentOrchestrator.handleMessage(any(String.class), any(String.class))).thenReturn("Hola DEVELOPER");
        botActions.setRequestText("hola bot");

        botActions.fnElse();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText()).isEqualTo("Hola DEVELOPER");
    }

    /** Test que verifica el manejo de errores cuando el agente conversacional genera una excepción durante el procesamiento del mensaje. */
    @Test
    void fnElseHandlesAgentFailure() throws Exception {
        when(agentOrchestrator.handleMessage(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("boom"));
        botActions.setRequestText("hola bot");

        botActions.fnElse();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.UNKNOWN_COMMAND.getMessage());
    }

    /** Construye una instancia de usuario para pruebas con los datos mínimos necesarios para simular autenticación y autorización. */
    private User userWithTelegramId(Long id, Long telegramId, String name) {
        User user = new User();
        user.setId(id);
        user.setTelegramId(String.valueOf(telegramId));
        user.setName(name);
        return user;
    }

    /** Genera un sprint de prueba con fechas predefinidas. */
    private Sprint sprintWithId(Long id, String name) {
        Sprint sprint = new Sprint();
        sprint.setId(id);
        sprint.setName(name);
        sprint.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        sprint.setEndDate(LocalDateTime.of(2026, 1, 15, 0, 0));
        return sprint;
    }

    /** Crea una tarea de prueba asociada a un usuario específico. */
    private Task taskWithIdAndAssignment(Long id, String title, Long assignedTo, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setAssignedTo(assignedTo);
        task.setStatus(status);
        return task;
    }

    /** Test que verifica que únicamente los usuarios con rol MANAGER puedan consultar los indicadores de rendimiento del equipo. */
    @Test
    void fnTeamKpis() throws Exception {
        User nonManager = userWithTelegramId(USER_ID_DEVELOPER, TELEGRAM_ID_DEVELOPER, "DEVELOPER");
        nonManager.setRole("DEVELOPER");
        when(userService.findAll()).thenReturn(List.of(nonManager));
        botActions.setRequestText("/teamkpis 2");

        botActions.fnTeamKpis();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.MANAGER_ONLY.getMessage());
    }

    /** Test que comprueba el cálculo y la visualización de los KPIs de un desarrollador perteneciente al equipo del gerente. */
    @Test
    void fnShowDeveloperKpis() throws Exception {

        User manager = userWithTelegramId(USER_ID_MANAGER, TELEGRAM_ID_MANAGER, "MANAGER");
        manager.setRole("MANAGER");
        botActions.setTelegramUserId(TELEGRAM_ID_MANAGER);

        User developer = userWithTelegramId(2L, 111111L, "DEVELOPER");
        Team team = teamWithIdAndManager(1L, "Team A", 7L);
        
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(1L);
        teamMember.setMemberUserId(2L);
        
        Task task1 = taskWithIdAndAssignment(1L, "Completed Task", 2L, TaskStatus.DONE);
        task1.setHoursDone(3);
        task1.setPriority(TaskPriority.HIGH);
        Task task2 = taskWithIdAndAssignment(2L, "In Progress Task", 2L, TaskStatus.IN_PROGRESS);
        Task task3 = taskWithIdAndAssignment(3L, "Pending Task", 2L, TaskStatus.PENDING);
        
        when(userService.findAll()).thenReturn(List.of(manager, developer));
        when(teamService.findAll()).thenReturn(List.of(team));
        when(teamMemberService.findAll()).thenReturn(List.of(teamMember));
        when(taskService.findAll()).thenReturn(List.of(task1, task2, task3));
        
        botActions.setRequestText("/teamkpis 2");

        botActions.fnTeamKpis();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        String response = sendMessageCaptor.getValue().getText();
        assertThat(response)
                .contains(BotMessages.TEAM_KPIS_HEADER.getMessage())
                .contains("Desarrollador: DEVELOPER")
                .contains("Total de tareas completadas: 1")
                .contains("Total de horas trabajadas: 3h")
                .contains("Tareas en progreso: 1")
                .contains("Tareas pendientes: 1");
    }

    /** Test que verifica que un gerente no pueda consultar métricas de desarrolladores que no pertenecen a su equipo. */
    @Test
    void fnTeamKpisDenied() throws Exception {
        User manager = userWithTelegramId(USER_ID_MANAGER, TELEGRAM_ID_MANAGER, "MANAGER");
        manager.setRole("MANAGER");
        botActions.setTelegramUserId(TELEGRAM_ID_MANAGER);
        
        User developer = userWithTelegramId(2L, 111111L, "Developer");

        Team team = teamWithIdAndManager(1L, "Team A", 7L);

        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(1L);
        teamMember.setMemberUserId(99L);
        
        when(userService.findAll()).thenReturn(List.of(manager, developer));
        when(teamService.findAll()).thenReturn(List.of(team));
        when(teamMemberService.findAll()).thenReturn(List.of(teamMember));
        
        botActions.setRequestText("/teamkpis 2");

        botActions.fnTeamKpis();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.DEVELOPER_NOT_IN_TEAM.getMessage());
    }

    /** Test que comprueba que un gerente pueda visualizar todas las tareas asociadas a los miembros de su equipo. */
    @Test
    void fnTeamTasks() throws Exception {

        User manager = userWithTelegramId(USER_ID_MANAGER, TELEGRAM_ID_MANAGER, "MANAGER");
        manager.setRole("MANAGER");
        botActions.setTelegramUserId(TELEGRAM_ID_MANAGER);
        User dev2 = userWithTelegramId(2L, 222222L, "DEVELOPER");
        User dev6 = userWithTelegramId(6L, 333333L, "DEVELOPER");
        User dev22 = userWithTelegramId(22L, 444444L, "DEVELOPER");
        User dev23 = userWithTelegramId(23L, 555555L, "DEVELOPER");
        
        Team team = teamWithIdAndManager(1L, "Team A", 7L);
        TeamMember tm1 = new TeamMember();
        tm1.setTeamId(1L);
        tm1.setMemberUserId(2L);
        TeamMember tm2 = new TeamMember();
        tm2.setTeamId(1L);
        tm2.setMemberUserId(6L);
        TeamMember tm3 = new TeamMember();
        tm3.setTeamId(1L);
        tm3.setMemberUserId(22L);
        TeamMember tm4 = new TeamMember();
        tm4.setTeamId(1L);
        tm4.setMemberUserId(23L);

        Task task1 = taskWithIdAndAssignment(1L, "Task for Dev 2", 2L, TaskStatus.IN_PROGRESS);
        task1.setPriority(TaskPriority.HIGH);
        Task task2 = taskWithIdAndAssignment(2L, "Task for Dev 6", 6L, TaskStatus.PENDING);
        task2.setPriority(TaskPriority.MEDIUM);
        Task task3 = taskWithIdAndAssignment(3L, "Task for Dev 22", 22L, TaskStatus.DONE);
        task3.setPriority(TaskPriority.LOW);
        
        when(userService.findAll()).thenReturn(List.of(manager, dev2, dev6, dev22, dev23));
        when(teamService.findAll()).thenReturn(List.of(team));
        when(teamMemberService.findAll()).thenReturn(List.of(tm1, tm2, tm3, tm4));
        when(taskService.findAll()).thenReturn(List.of(task1, task2, task3));
        
        botActions.setRequestText("/teamtasks");

        botActions.fnTeamTasks();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        String response = sendMessageCaptor.getValue().getText();
        assertThat(response)
                .contains(BotMessages.TEAM_TASKS_HEADER.getMessage())
                .contains("Task for Dev 2")
                .contains("Task for Dev 6")
                .contains("Task for Dev 22")
                .contains("Total de tareas: 3");
    }

    /** Test que verifica que los usuarios sin privilegios de gerente no puedan acceder a la consulta global de tareas del equipo. */
    @Test
    void fnTeamTasksDenied() throws Exception {
        User nonManager = userWithTelegramId(USER_ID_DEVELOPER, TELEGRAM_ID_DEVELOPER, "DEVELOPER");
        nonManager.setRole("DEVELOPER");
        when(userService.findAll()).thenReturn(List.of(nonManager));
        botActions.setRequestText("/teamtasks");

        botActions.fnTeamTasks();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.MANAGER_ONLY.getMessage());
    }

    /** Test que comprueba el comportamiento cuando un equipo no posee tareas registradas. */
    @Test
    void fnTeamTasksEmpty() throws Exception {
        User manager = userWithTelegramId(USER_ID_MANAGER, TELEGRAM_ID_MANAGER, "MANAGER");
        manager.setRole("MANAGER");
        botActions.setTelegramUserId(TELEGRAM_ID_MANAGER);
        
        Team team = teamWithIdAndManager(1L, "Team A", 7L);
        
        when(userService.findAll()).thenReturn(List.of(manager));
        when(teamService.findAll()).thenReturn(List.of(team));
        when(teamMemberService.findAll()).thenReturn(List.of());
        when(taskService.findAll()).thenReturn(List.of());
        
        botActions.setRequestText("/teamtasks");

        botActions.fnTeamTasks();

        verify(telegramClient).execute(sendMessageCaptor.capture());
        assertThat(sendMessageCaptor.getValue().getText())
                .isEqualTo(BotMessages.TEAM_TASKS_EMPTY.getMessage());
    }

    /** Construye una entidad de equipo para escenarios de prueba. */
    private Team teamWithIdAndManager(Long id, String name, Long managerId) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setManagerId(managerId);
        team.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return team;
    }
}
