package com.springboot.MyTodoList.service;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de interactuar con el modelo de lenguaje DeepSeek
 * para generar respuestas basadas en prompts proporcionados por los usuarios.
 *
 * Utiliza un cliente HTTP para enviar solicitudes a la API de DeepSeek
 * y procesar las respuestas recibidas.
 */
@Service
public class DeepSeekService{
    private final CloseableHttpClient httpClient;
    private final HttpPost httpPost;

    /** Constructor que inyecta el cliente HTTP y la solicitud HTTP POST */
    public DeepSeekService(CloseableHttpClient httpClient, HttpPost httpPost) {
        this.httpClient = httpClient;
        this.httpPost = httpPost;
    }

    /** Genera texto utilizando el modelo de lenguaje DeepSeek a partir de un prompt dado */
    public String generateText(String prompt) throws IOException, org.apache.hc.core5.http.ParseException {
        String requestBody = String.format("{\"model\": \"deepseek-chat\",\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}", prompt);

        try {
            httpPost.setEntity(new StringEntity(requestBody));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw e;
        }
    }
}
