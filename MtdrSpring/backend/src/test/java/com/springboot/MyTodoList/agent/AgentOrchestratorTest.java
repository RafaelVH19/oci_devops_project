package com.springboot.MyTodoList.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Conjunto de pruebas unitarias para la clase {@link AgentOrchestrator}.
 *
 * Estas pruebas verifican el comportamiento del orquestador encargado de
 * coordinar la interpretación de mensajes mediante inteligencia artificial
 * y la ejecución de acciones sobre el espacio de trabajo del proyecto.
 */
@ExtendWith(MockitoExtension.class)
class AgentOrchestratorTest {

    @Mock
    private LlmIntentParser llmIntentParser;

    @Mock
    private ProjectWorkspaceService workspaceService;

        private AgentOrchestrator newOrchestrator() {
                return new AgentOrchestrator(llmIntentParser, workspaceService);
        }

    /** Test que verifica que el constructor rechace dependencias nulas. */
    @Test
    void constructorRejectsNullDependencies() {
        assertThatThrownBy(() -> new AgentOrchestrator(null, workspaceService))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new AgentOrchestrator(llmIntentParser, null))
                .isInstanceOf(NullPointerException.class);
    }
    /** Test que verifica que cuando el mensaje recibido es nulo, */
    @Test
    void handleMessageWithNullTextReturnsDefaultResponse() {
                String response = newOrchestrator().handleMessage(null);

        assertThat(response)
                .isEqualTo("No pude interpretar la solicitud. Escribe ayuda para ver ejemplos.");
        verifyNoInteractions(llmIntentParser, workspaceService);
    }

    /** Test que verifica que cuando el analizador de intenciones devuelve un resultado nulo, el orquestador responda con el mensaje de ayuda predeterminado. */
    @Test
    void handleMessageWithNullParsedIntentReturnsDefaultResponse() {
        when(llmIntentParser.parse("hola"))
                .thenReturn(null);

        String response = newOrchestrator().handleMessage("hola");

        assertThat(response)
                .isEqualTo("No pude interpretar la solicitud. Escribe ayuda para ver ejemplos.");
    }

    /** Test que verifica que la operación de listado de tareas funcione correctamente cuando el servicio devuelve una colección nula */
    @Test
    void handleMessageWithNullTaskListUsesEmptyFallback() {
        ParsedIntent parsedIntent = new ParsedIntent();
        parsedIntent.setIntent(IntentType.LIST_TASKS);
        when(llmIntentParser.parse("lista"))
                .thenReturn(parsedIntent);
        when(workspaceService.findAllTasks()).thenReturn(null);

        String response = newOrchestrator().handleMessage("lista");

        assertThat(response)
                .contains("Estas son las tareas registradas:")
                .contains("No encontré tareas para ese criterio.");
    }

    /** Verifica que el resumen de carga del equipo se genere correctamente incluso cuando el servicio devuelve datos nulos */
    @Test
    void teamLoadSummaryWithNullTotalsUsesEmptyFallback() {
        ParsedIntent parsedIntent = new ParsedIntent();
        parsedIntent.setIntent(IntentType.TEAM_LOAD_SUMMARY);
        when(llmIntentParser.parse("carga"))
                .thenReturn(parsedIntent);
        when(workspaceService.storyPointsByAssignee()).thenReturn(null);

        String response = newOrchestrator().handleMessage("carga");

        assertThat(response).isEqualTo("Carga actual del equipo");
    }
}