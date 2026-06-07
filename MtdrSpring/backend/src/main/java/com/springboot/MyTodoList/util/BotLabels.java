package com.springboot.MyTodoList.util;

/**
 * Defines labels used by the Telegram bot interface,
 * including button texts and menu options.
 */
public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	LIST_ALL_ITEMS("List All Items"), 
	ADD_NEW_ITEM("Add New Item"),
	DONE("DONE"),
	UNDO("UNDO"),
	DELETE("DELETE"),
	MY_TODO_LIST("MY TODO LIST"),
	DASH("-");

	private String label;

	/** Creates a label enum value. */
	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	/** Returns the string representation of the label. */
	public String getLabel() {
		return label;
	}

}
