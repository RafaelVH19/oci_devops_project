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

    @Test
    void parseCreateTaskWithStoryPoints() {
        ParsedIntent intent = parser.parse("crea una tarea para refactorizar código y asigna a Juan con 8 puntos");

        assertThat(intent.getIntent()).isEqualTo(IntentType.CREATE_TASK);
        assertThat(intent.getTitle()).contains("refactorizar código");
        assertThat(intent.getAssignee()).isEqualTo("Juan");
        assertThat(intent.getStoryPoints()).isEqualTo(8);
    }

    @Test
    void parseCreateTaskWithExpectedHours() {
        ParsedIntent intent = parser.parse("crea una tarea para implementar feature con 3 horas esperadas");

        assertThat(intent.getIntent()).isEqualTo(IntentType.CREATE_TASK);
        assertThat(intent.getTitle()).contains("implementar feature");
        assertThat(intent.getExpectedHours()).isEqualTo(3);
    }

    @Test
    void parseCreateBugTask() {
        ParsedIntent intent = parser.parse("crea un bug para corregir pantalla de login");

        assertThat(intent.getIntent()).isEqualTo(IntentType.CREATE_TASK);
        assertThat(intent.getTitle()).contains("corregir pantalla de login");
        assertThat(intent.getIsBug()).isEqualTo(true);
    }

    @Test
    void parseReportBug() {
        ParsedIntent intent = parser.parse("reporta un bug en tarea 3 con severidad HIGH");

        assertThat(intent.getIntent()).isEqualTo(IntentType.REPORT_BUG);
        assertThat(intent.getTaskId()).isEqualTo("3");
        assertThat(intent.getBugSeverity()).isEqualTo("HIGH");
    }

    @Test
    void parseReportBugWithoutSeverity() {
        ParsedIntent intent = parser.parse("reporta un bug en tarea 7");

        assertThat(intent.getIntent()).isEqualTo(IntentType.REPORT_BUG);
        assertThat(intent.getTaskId()).isEqualTo("7");
    }
}
