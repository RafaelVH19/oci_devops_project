package com.springboot.MyTodoList.util;

import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.springboot.MyTodoList.config.BotProps;

/**
 * Configures and creates the Telegram client used by the bot
 * to communicate with the Telegram API.
 */
@Configuration
public class BotClient {

    /**
     * Creates a Telegram client using the bot token
     * configured in the application properties.
     */
    @Bean
    public TelegramClient telegramClient(BotProps botProps) {
        return new OkHttpTelegramClient(botProps.getToken());
    }
}