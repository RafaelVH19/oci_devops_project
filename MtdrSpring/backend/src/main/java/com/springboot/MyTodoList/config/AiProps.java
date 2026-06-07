package com.springboot.MyTodoList.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stores AI agent configuration properties loaded from application settings.
 * Properties are mapped from the prefix "agent.ai".
 */
@ConfigurationProperties(prefix = "agent.ai")
public class AiProps {

    private boolean enabled;
    private String baseUrl;
    private String apiKey;
    private String model;

    /** Returns whether AI integration is enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enables or disables AI integration. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Returns the AI service base URL. */
    public String getBaseUrl() {
        return baseUrl;
    }

    /** Sets the AI service base URL. */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** Returns the API key used to access the AI service. */
    public String getApiKey() {
        return apiKey;
    }

    /** Sets the API key used to access the AI service. */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /** Returns the AI model name configured for requests. */
    public String getModel() {
        return model;
    }

    /** Sets the AI model name configured for requests. */
    public void setModel(String model) {
        this.model = model;
    }
}
