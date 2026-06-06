package com.springboot.MyTodoList.config;

//import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.PropertySource;

/**
 * Stores Telegram bot configuration properties.
 * Values are loaded from the prefix "telegram.bot".
 */
@ConfigurationProperties(prefix = "telegram.bot")
public class BotProps {
    private String name;
    private String token;

	/** Returns the Telegram bot token. */
    public String getToken(){
		return token;
	}

	/** Returns the Telegram bot name. */
    public String getName(){
		return name;
	}

	/** Sets the Telegram bot token. */
    public void setToken(String tkn){
		token = tkn;
	}

	/** Sets the Telegram bot name. */
    public void setName(String n){
		name = n;
	}
}