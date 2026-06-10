package com.springboot.MyTodoList.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Conjunto de pruebas unitarias para la clase.
 *
 * Estas pruebas verifican que el analizador basado en reglas sea capaz de
 * interpretar correctamente distintos mensajes escritos en lenguaje natural
 * y convertirlos en objetos con la intención adecuada.
 */
class RuleBasedIntentParserTest {

    private final RuleBasedIntentParser parser = new RuleBasedIntentParser();

    /** Test que verifica que cuando se recibe un mensaje nulo, el sistema genere una intención desconocida y solicite una aclaración al usuario. */
    @Test
    void parseNullMessageFallsBackToClarification() {
        ParsedIntent intent = parser.parse(null);

        assertThat(intent.getIntent()).isEqualTo(IntentType.UNKNOWN);
        assertThat(intent.isClarificationNeeded()).isTrue();
    }

    /** Test que verifica que una solicitud de consulta de KPIs para un desarrollador específico sea interpretada correctamente. */
    @Test
    void parseKpiByDeveloperId() {
        ParsedIntent intent = parser.parse("Muestrame los KPIs del usuario con ID 6");

        assertThat(intent.getIntent()).isEqualTo(IntentType.GET_DEVELOPER_KPI);
        assertThat(intent.getTaskId()).isEqualTo("6");
    }

    /** Test que verifica que una instrucción para eliminar una tarea utilizando su identificador numérico sea reconocida correctamente. */
    @Test
    void parseDeleteTaskById() {
        ParsedIntent intent = parser.parse("elimina la tarea 5");

        assertThat(intent.getIntent()).isEqualTo(IntentType.DELETE_TASK);
        assertThat(intent.getTaskId()).isEqualTo("5");
    }

    /** Test que verifica que una instrucción para eliminar una tarea utilizando su título o descripción sea interpretada correctamente. */
    @Test
    void parseDeleteTaskByTitle() {
        ParsedIntent intent = parser.parse("borra la tarea revisar api");

        assertThat(intent.getIntent()).isEqualTo(IntentType.DELETE_TASK);
        assertThat(intent.getTitle()).isNotBlank();
    }

    /** Test que verifica que una solicitud de creación de tarea con responsable asignado y horas estimadas sea procesada correctamente. */
    @Test
    void parseCreateTaskWithAssigneeAndHours() {
        ParsedIntent intent = parser.parse("crea una tarea para refactorizar código y asigna a Juan con 8 horas esperadas");

        assertThat(intent.getIntent()).isEqualTo(IntentType.CREATE_TASK);
        assertThat(intent.getTitle()).contains("refactorizar código");
        assertThat(intent.getAssignee()).isEqualTo("Juan");
        assertThat(intent.getExpectedHours()).isEqualTo(8);
    }

    /** Test que verifica que una solicitud de creación de tarea con únicamente horas estimadas sea interpretada correctamente. */
    @Test
    void parseCreateTaskWithExpectedHours() {
        ParsedIntent intent = parser.parse("crea una tarea para implementar feature con 3 horas esperadas");

        assertThat(intent.getIntent()).isEqualTo(IntentType.CREATE_TASK);
        assertThat(intent.getTitle()).contains("implementar feature");
        assertThat(intent.getExpectedHours()).isEqualTo(3);
    }

    /** Test que verifica que una solicitud para registrar un bug sea interpretada como una tarea de tipo incidencia. */
    @Test
    void parseCreateBugTask() {
        ParsedIntent intent = parser.parse("crea un bug para corregir pantalla de login");

        assertThat(intent.getIntent()).isEqualTo(IntentType.CREATE_TASK);
        assertThat(intent.getTitle()).contains("corregir pantalla de login");
        assertThat(intent.getIsBug()).isEqualTo(true);
    }

}
