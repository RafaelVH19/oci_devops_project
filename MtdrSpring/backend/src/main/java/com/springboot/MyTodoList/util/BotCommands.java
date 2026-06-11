package com.springboot.MyTodoList.util;

/**
 * Defines all commands supported by the Telegram bot.
 */
public enum BotCommands {

	START_COMMAND("/start"), 
	REGISTER_COMMAND("/register"), 
	LOGOUT_COMMAND("/logout"), 
	ADD_TASK("/addtask"),
	DELETE_TASK("/deletetask"),
	ASSIGN_TASK("/assigntask"),
	COMPLETE_TASK("/completetask"),
	LIST_TASKS("/mytasks"),
	TEAM_KPIS("/teamkpis"),
	TEAM_TASKS("/teamtasks"),
	LLM_REQ("/llm");

	private final String command;


	/** Creates a command enum value. */
	BotCommands(String enumCommand) {
		this.command = enumCommand;
	}

	/** Returns the string representation of the command. */
	public String getCommand() {
		return command;
	}
}
