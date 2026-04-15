package com.springboot.MyTodoList.util;

public enum BotMessages {
	
	WELCOME(
	"Hola! Soy MyTodoList Bot!\n Ingrese alguno de los comandos disponibles: \n /register - para validar el usuario \n /addtask - para agregar una tarea a la lista \n /assigntask - para asignar una tarea a un sprint \n /completetask - para marcar una tarea como completada \n /mytasks - para ver todas las tareas"),
	USER_OK("Usuario reconocido, bienvenido"),
	USER_NOT_FOUND("Usuario no encontrado."),

	TASK_CREATED("Tarea creada exitosamente!"),
	TASK_ERROR("Error al crear la tarea. Asegúrate de usar el formato correcto: /addtask <título> | <descripción> | <horas> | <prioridad> (LOW, MEDIUM, HIGH)> | <ID de Usuario> "),
	TASK_MAX_HOURS("La tarea no puede tener más de 4 horas. Porfavor subdivide esta tarea en tareas más pequeñas."),

	TASK_ASSIGNED("Tarea asignada al sprint exitosamente!"),
	TASK_ASSIGN_ERROR("Error al asignar la tarea. Asegúrate de usar el formato correcto: /assigntask <Numero de tarea> | <Numero de sprint>"),

	TASK_COMPLETED("Tarea marcada como completada!"),
	TASK_COMPLETE_ERROR("Error al completar la tarea. Asegúrate de usar el formato correcto: /completetask <Numero de tarea> | <horas> "),

	TASK_NOT_FOUND("Tarea no encontrada."),

	TASK_LIST_HEADER("Tus tareas:"),

	UNKNOWN_COMMAND("Comando desconocido. Por favor, ingresa un comando válido."),

	LLM_RESPONSE("Respuesta de la IA: ");

	private String message;

	BotMessages(String enumMessage) {
		this.message = enumMessage;
	}

	public String getMessage() {
		return message;
	}

}
