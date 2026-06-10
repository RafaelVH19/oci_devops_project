package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Sprint;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Repositorio responsable de la administración de entidades.
 *
 * Permite realizar operaciones de persistencia sobre los sprints
 * del sistema utilizando las capacidades de Spring Data JPA.
 */
@Repository
@Transactional
@EnableTransactionManagement
public interface SprintRepository extends JpaRepository<Sprint, Long> {
}