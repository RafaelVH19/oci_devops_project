package com.springboot.MyTodoList.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
/**
 * Entidad que registra todas las interacciones ejecutadas por el bot de Telegram.
 *
 * Esta tabla permite almacenar información relacionada con los comandos
 * enviados por los usuarios, incluyendo el mensaje original, la respuesta
 * generada por el sistema y el estado de ejecución.
 *
 * Su propósito principal es proporcionar trazabilidad, auditoría y soporte
 * para el análisis de uso del bot.
 */
@Entity
@Table(name = "BOT_COMMAND_LOGGING")
public class BotCommandLogging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TELEGRAM_ID", nullable = false)
    private Long telegramId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "COMMAND", nullable = false, length = 100)
    private String command;

    @Column(name = "RAW_MESSAGE", length = 1000)
    private String rawMessage;

    @Column(name = "RESPONSE_MESSAGE", length = 1000)
    private String responseMessage;

    @Column(name = "EXECUTION_STATUS", nullable = false, length = 20)
    private String executionStatus;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    /** Constructor vacío requerido por JPA */
    public BotCommandLogging() {
    }

    /** Obtiene el ID del registro */
    public Long getId() {
        return id;
    }

    /** Establece el ID del registro */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el ID de Telegram */
    public Long getTelegramId() {
        return telegramId;
    }

    /** Establece el ID de Telegram */
    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    /** Obtiene el ID del usuario */
    public Long getUserId() {
        return userId;
    }

    /** Establece el ID del usuario */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** Obtiene el comando */
    public String getCommand() {
        return command;
    }

    /** Establece el comando */
    public void setCommand(String command) {
        this.command = command;
    }

    /** Obtiene el mensaje original */
    public String getRawMessage() {
        return rawMessage;
    }

    /** Establece el mensaje original */
    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    /** Obtiene el mensaje de respuesta */
    public String getResponseMessage() {
        return responseMessage;
    }

    /** Establece el mensaje de respuesta */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /** Obtiene el estado de ejecución */
    public String getExecutionStatus() {
        return executionStatus;
    }

    /** Establece el estado de ejecución */
    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    /** Obtiene la fecha de creación del registro */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Establece la fecha de creación del registro */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}