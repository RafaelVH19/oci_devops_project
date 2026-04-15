package com.springboot.MyTodoList.util;

public enum BotCommands {

	START_COMMAND("/start"), 
	REGISTER_COMMAND("/register"), 
	ADD_TASK("/addtask"),
	ASSIGN_TASK("/assigntask"),
	COMPLETE_TASK("/completetask"),
	LIST_TASKS("/mytasks"),
	LLM_REQ("/llm");

	private String command;

	BotCommands(String enumCommand) {
		this.command = enumCommand;
	}

	public String getCommand() {
		return command;
	}
}
