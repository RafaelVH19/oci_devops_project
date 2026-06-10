package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.ToDoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio encargado de administrar la persistencia de entidades.
 *
 * Permite realizar operaciones CRUD sobre los elementos de la lista
 * de tareas almacenados en la tabla TODOITEM de la base de datos.
 *
 * Hereda todas las funcionalidades estándar de Spring Data JPA
 * mediante JpaRepository.
 */
@Repository
public interface ToDoItemRepository extends JpaRepository<ToDoItem, Integer> {
    
}