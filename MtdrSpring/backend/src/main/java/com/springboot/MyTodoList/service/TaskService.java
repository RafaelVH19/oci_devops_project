package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.enums.TaskStatus;
import com.springboot.MyTodoList.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.embedding.EmbeddingModel;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TaskService(TaskRepository taskRepository, EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        this.taskRepository = taskRepository;
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public ResponseEntity<Task> getById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Task add(Task task) {
        Task saved = taskRepository.saveAndFlush(task);
        embedAndStore(saved.getId(), saved.getTitle(), saved.getDescription());
        return saved;
    }

    @Transactional
    public Task update(Long id, Task updated) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            Task current = task.get();
            TaskStatus previousStatus = current.getStatus();
            boolean contentChanged = !equals(current.getTitle(), updated.getTitle())
                    || !equals(current.getDescription(), updated.getDescription());

            current.setTitle(updated.getTitle());
            current.setDescription(updated.getDescription());
            current.setStatus(updated.getStatus());
            current.setPriority(updated.getPriority());
            current.setAssignedTo(updated.getAssignedTo());
            current.setCreatedBy(updated.getCreatedBy());
            current.setHoursDone(updated.getHoursDone());
            current.setExpectedHours(updated.getExpectedHours());
            current.setIsBug(updated.getIsBug());
            current.setDependsOnId(updated.getDependsOnId());
            if (updated.getCreatedAt() != null) {
                current.setCreatedAt(updated.getCreatedAt());
            }
            if (TaskStatus.DONE.equals(updated.getStatus())) {
                if (current.getCompletedDate() == null || !TaskStatus.DONE.equals(previousStatus)) {
                    current.setCompletedDate(LocalDateTime.now());
                }
            } else {
                current.setCompletedDate(null);
            }
            current.setUpdatedAt(LocalDateTime.now());
            Task saved = taskRepository.saveAndFlush(current);

            if (contentChanged) {
                embedAndStore(saved.getId(), saved.getTitle(), saved.getDescription());
            }

            return saved;
        }
        return null;
    }

    public boolean delete(Long id) {
        try {
            taskRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void embedAndStore(Long taskId, String title, String description) {
        String text = buildEmbeddingText(title, description);
        float[] vector = embeddingModel.embed(text);
        String vectorStr = Arrays.toString(vector);
        jdbcTemplate.update(
            "UPDATE TASKS SET INSIGHT = TO_VECTOR(TO_CLOB(?)) WHERE ID = ?",
            ps -> {
                ps.setClob(1, new StringReader(vectorStr));
                ps.setLong(2, taskId);
            }
        );
    }

    private String buildEmbeddingText(String title, String description) {
        if (description == null || description.isBlank()) return title == null ? "" : title;
        if (title == null || title.isBlank()) return description;
        return title + ". " + description;
    }

    private boolean equals(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }
}