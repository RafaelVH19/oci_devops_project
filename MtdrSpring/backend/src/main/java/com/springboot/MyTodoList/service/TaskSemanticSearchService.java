package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.agent.TaskItem;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Performs semantic (vector-similarity) search over the TASKS table using the
 * INSIGHT column stored in Oracle 23ai.  The query text is embedded with the
 * same model that populated INSIGHT so the distance metric is meaningful.
 */
@Service
public class TaskSemanticSearchService {

    private static final Logger logger = LoggerFactory.getLogger(TaskSemanticSearchService.class);

    private static final int DEFAULT_LIMIT = 5;

    private static final String SQL = """
        SELECT t.ID, t.TITLE, t.STATUS, t.EXPECTED_HOURS, t.HOURS_DONE, t.IS_BUG,
               u.NAME AS ASSIGNEE_NAME,
               (SELECT s.NAME FROM SPRINT_TASKS st
                JOIN SPRINTS s ON s.ID = st.SPRINT_ID
                WHERE st.TASK_ID = t.ID AND st.REMOVED_AT IS NULL
                ORDER BY st.ADDED_AT DESC FETCH FIRST 1 ROW ONLY) AS SPRINT_NAME
        FROM TASKS t
        LEFT JOIN USERS u ON u.ID = t.ASSIGNED_TO
        WHERE t.INSIGHT IS NOT NULL
        ORDER BY VECTOR_DISTANCE(t.INSIGHT, TO_VECTOR(TO_CLOB(?)), COSINE)
        FETCH FIRST ? ROWS ONLY
        """;

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public TaskSemanticSearchService(EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Find the top {@code limit} tasks whose embeddings are closest to the query. */
    public List<TaskItem> findSimilarTasks(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();
        int safeLimit = limit <= 0 ? DEFAULT_LIMIT : limit;
        try {
            float[] vector = embeddingModel.embed(query);
            String vectorStr = Arrays.toString(vector);
            return jdbcTemplate.query(SQL,
                ps -> {
                    ps.setClob(1, new StringReader(vectorStr));
                    ps.setInt(2, safeLimit);
                },
                (rs, rowNum) -> new TaskItem(
                    rs.getLong("ID"),
                    rs.getString("TITLE"),
                    rs.getString("ASSIGNEE_NAME"),
                    rs.getString("STATUS"),
                    rs.getInt("EXPECTED_HOURS"),
                    rs.getInt("HOURS_DONE"),
                    rs.getBoolean("IS_BUG"),
                    rs.getString("SPRINT_NAME")
                )
            );
        } catch (Exception ex) {
            logger.warn("Semantic task search failed for query '{}': {}", query, ex.getMessage());
            return List.of();
        }
    }

    /** Convenience overload using the default limit. */
    public List<TaskItem> findSimilarTasks(String query) {
        return findSimilarTasks(query, DEFAULT_LIMIT);
    }
}
