package com.springboot.MyTodoList.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RuleBasedIntentParserTest {

    private final RuleBasedIntentParser parser = new RuleBasedIntentParser();

    @Test
    void parseKpiByDeveloperId() {
        ParsedIntent intent = parser.parse("Muestrame los KPIs del usuario con ID 6");

        assertThat(intent.getIntent()).isEqualTo(IntentType.GET_DEVELOPER_KPI);
        assertThat(intent.getTaskId()).isEqualTo("6");
    }

    @Test
    void parseDeleteTaskById() {
        ParsedIntent intent = parser.parse("elimina la tarea 5");

        assertThat(intent.getIntent()).isEqualTo(IntentType.DELETE_TASK);
        assertThat(intent.getTaskId()).isEqualTo("5");
    }

    @Test
    void parseDeleteTaskByTitle() {
        ParsedIntent intent = parser.parse("borra la tarea revisar api");

        assertThat(intent.getIntent()).isEqualTo(IntentType.DELETE_TASK);
        assertThat(intent.getTitle()).isNotBlank();
    }
}
