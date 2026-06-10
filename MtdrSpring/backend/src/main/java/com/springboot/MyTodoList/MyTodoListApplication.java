package com.springboot.MyTodoList;

import com.springboot.MyTodoList.config.AiProps;
import com.springboot.MyTodoList.config.BotProps;
import com.springboot.MyTodoList.config.DeepSeekConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Clase principal de arranque de la aplicación MyTodoList.
 *
 * Esta clase actúa como punto de entrada del sistema y es responsable de
 * inicializar el contexto de Spring Boot, cargar las configuraciones
 * necesarias y habilitar las propiedades externas utilizadas por la
 * aplicación.
 */
@SpringBootApplication
@EnableConfigurationProperties({BotProps.class, AiProps.class})
@Import(DeepSeekConfig.class)
public class MyTodoListApplication {

	/** Método principal que inicia la ejecución de la aplicación Spring Boot. */
	public static void main(String[] args) {
		SpringApplication.run(MyTodoListApplication.class, args);
	}

}
