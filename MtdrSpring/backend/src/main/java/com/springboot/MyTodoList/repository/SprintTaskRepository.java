package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.SprintTaskId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio encargado de gestionar las relaciones entre tareas y sprints.
 *
 * Administra la entidad, la cual implementa una
 * relación muchos a muchos entre las entidades Sprint y Task mediante
 * una clave primaria compuesta.
 *
 * Además de las operaciones CRUD heredadas de JpaRepository,
 * proporciona métodos especializados para consultar asociaciones activas
 * y relaciones filtradas por sprint o tarea.
 */
@Repository
@Transactional
@EnableTransactionManagement
public interface SprintTaskRepository extends JpaRepository<SprintTask, SprintTaskId> {

	/** Obtiene la relación de tarea y sprint no eliminada */
	Optional<SprintTask> findFirstByIdTaskIdAndRemovedAtIsNull(Long taskId);

	/** Obtiene todas las relaciones de tarea y sprint por ID de sprint */
	List<SprintTask> findByIdSprintId(Long sprintId);

	/** Obtiene todas las relaciones de tarea y sprint por ID de tarea */
	List<SprintTask> findByIdTaskId(Long taskId);
}