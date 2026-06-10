package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.BotCommandLogging;
import com.springboot.MyTodoList.repository.BotCommandLoggingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar los registros de comandos del bot.
 *
 * Proporciona métodos para realizar operaciones CRUD sobre la entidad
 * BotCommandLogging, que almacena información sobre los comandos ejecutados
 * por el bot, incluyendo detalles como el ID de Telegram, el comando,
 * el mensaje original y la respuesta generada.
 */
@Service
public class BotCommandLoggingService {

    @Autowired
    private BotCommandLoggingRepository botCommandLoggingRepository;

    /** Obtiene todos los registros de comandos del bot */
    public List<BotCommandLogging> findAll() {
        return botCommandLoggingRepository.findAll();
    }

    /** Obtiene un registro de comando del bot por su ID */
    public ResponseEntity<BotCommandLogging> getById(Long id) {
        Optional<BotCommandLogging> botLog = botCommandLoggingRepository.findById(id);
        return botLog.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Agrega un nuevo registro de comando del bot */
    public BotCommandLogging add(BotCommandLogging botCommandLogging) {
        return botCommandLoggingRepository.save(botCommandLogging);
    }

    /** Actualiza un registro de comando del bot */
    public BotCommandLogging update(Long id, BotCommandLogging updated) {
        Optional<BotCommandLogging> botLog = botCommandLoggingRepository.findById(id);
        if (botLog.isPresent()) {
            BotCommandLogging current = botLog.get();
            current.setTelegramId(updated.getTelegramId());
            current.setUserId(updated.getUserId());
            current.setCommand(updated.getCommand());
            current.setRawMessage(updated.getRawMessage());
            current.setResponseMessage(updated.getResponseMessage());
            current.setExecutionStatus(updated.getExecutionStatus());
            current.setCreatedAt(updated.getCreatedAt());
            return botCommandLoggingRepository.save(current);
        }
        return null;
    }

    /** Elimina un registro de comando del bot */
    public boolean delete(Long id) {
        try {
            botCommandLoggingRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}