package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Repositorio encargado de administrar la persistencia de entidades.
 *
 * Permite realizar operaciones CRUD sobre las tareas registradas
 * dentro del sistema de gestión de proyectos.
 *
 * Las funcionalidades son proporcionadas automáticamente por
 * Spring Data JPA mediante la extensión de JpaRepository.
 */
@Repository
@Transactional
@EnableTransactionManagement
public interface TaskRepository extends JpaRepository<Task, Long> {
}