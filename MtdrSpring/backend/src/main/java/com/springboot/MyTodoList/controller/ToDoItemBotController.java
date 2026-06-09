package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.agent.AgentOrchestrator;
import com.springboot.MyTodoList.config.BotProps;
import com.springboot.MyTodoList.service.DeepSeekService;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.SprintTaskService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.TeamService;
import com.springboot.MyTodoList.service.TeamMemberService;
import com.springboot.MyTodoList.service.UserService;
import com.springboot.MyTodoList.util.BotActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Controlador principal del bot de Telegram.
 *
 * Recibe mensajes enviados por los usuarios y delega
 * su procesamiento a la clase BotActions.
 */
@Component
public class ToDoItemBotController  implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	private DeepSeekService deepSeekService;
	private AgentOrchestrator agentOrchestrator;
	private final TelegramClient telegramClient;
	
	private final BotProps botProps;

	private TaskService taskService;
    private SprintService sprintService;
    private SprintTaskService sprintTaskService;
    private UserService userService;
    private TeamService teamService;
    private TeamMemberService teamMemberService;

	@Value("${telegram.bot.token}")
	private String telegramBotToken;


	//** Obtiene el token del bot de Telegram */
	@Override
    public String getBotToken() {
		if(telegramBotToken != null && !telegramBotToken.trim().isEmpty()){
        	return telegramBotToken;
		}else{
			return botProps.getToken();
		}
    }

	//** Inicializa el controlador del bot de Telegram y sus dependencias */
	public ToDoItemBotController(BotProps bp, TaskService ts, SprintService ss, SprintTaskService sts, UserService us, TeamService tms, TeamMemberService tmms, DeepSeekService ds, AgentOrchestrator ao) {
		this.botProps = bp;
		telegramClient = new OkHttpTelegramClient(getBotToken());
		taskService = ts;
		sprintService = ss;
		sprintTaskService = sts;
		userService = us;
		teamService = tms;
		teamMemberService = tmms;
		deepSeekService = ds;
		agentOrchestrator = ao;
	}

	//** Devuelve el consumidor encargado de procesar las actualizaciones recibidas desde Telegram */
	@Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

	//** Procesa un mensaje recibido desde Telegram y ejecuta las acciones correspondientes */
	@Override
	public void consume(Update update) {

		if (!update.hasMessage() || !update.getMessage().hasText()) return;

		

		String messageTextFromTelegram = update.getMessage().getText();
		long chatId = update.getMessage().getChatId();
		Long telegramUserId = update.getMessage().getFrom().getId();

		logger.info("Mensaje recibido: " + messageTextFromTelegram);

		BotActions actions = new BotActions(telegramClient, taskService, sprintService, sprintTaskService, userService, teamService, teamMemberService, deepSeekService, agentOrchestrator);
		actions.setRequestText(messageTextFromTelegram);
		actions.setChatId(chatId);
		actions.setTelegramUserId(telegramUserId);

		actions.fnStart();
        actions.fnRegister();
        actions.fnAddTask();
		actions.fnDeleteTask();
        actions.fnAssignTask();
        actions.fnCompleteTask();
        actions.fnListTasks();
        actions.fnTeamKpis();
        actions.fnTeamTasks();
        actions.fnLLM();   
        actions.fnElse();

	}

	//** Método ejecutado automáticamente después del registro del bot */
	@AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }

}


