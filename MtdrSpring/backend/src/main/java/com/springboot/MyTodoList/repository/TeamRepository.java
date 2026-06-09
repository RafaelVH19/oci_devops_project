package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Team;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Repositorio responsable de la persistencia de entidades.
 *
 * Proporciona acceso a la información de los equipos de trabajo
 * registrados en la plataforma mediante operaciones CRUD estándar.
 *
 * Las funcionalidades son implementadas automáticamente por
 * Spring Data JPA.
 */
@Repository
@Transactional
@EnableTransactionManagement
public interface TeamRepository extends JpaRepository<Team, Long> {
}