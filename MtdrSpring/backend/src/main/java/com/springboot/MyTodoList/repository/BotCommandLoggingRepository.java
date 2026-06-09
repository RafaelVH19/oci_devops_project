package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.BotCommandLogging;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Repositorio encargado de gestionar las operaciones de persistencia
 * de la entidad.
 *
 * Proporciona acceso a los registros históricos de comandos ejecutados
 * por usuarios a través del bot de Telegram, permitiendo operaciones CRUD
 * estándar mediante Spring Data JPA.
 */
@Repository
@Transactional
@EnableTransactionManagement
public interface BotCommandLoggingRepository extends JpaRepository<BotCommandLogging, Long> {
}