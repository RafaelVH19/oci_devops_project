package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.SprintTask;
import com.springboot.MyTodoList.model.SprintTaskId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@EnableTransactionManagement
public interface SprintTaskRepository extends JpaRepository<SprintTask, SprintTaskId> {

	Optional<SprintTask> findFirstByIdTaskIdAndRemovedAtIsNull(Long taskId);

	List<SprintTask> findByIdSprintId(Long sprintId);

	List<SprintTask> findByIdTaskId(Long taskId);
}