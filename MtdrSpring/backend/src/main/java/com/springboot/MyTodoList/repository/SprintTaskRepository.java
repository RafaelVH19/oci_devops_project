package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.SprintTaskId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface SprintTaskRepository extends JpaRepository<SprintTask, SprintTaskId> {
}