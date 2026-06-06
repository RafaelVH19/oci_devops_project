package com.springboot.MyTodoList.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// This is only used for local testing.

/**
 * Stores database connection settings used for local testing.
 * Properties are loaded from the "spring.datasource" prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DbSettings {
    private String url;
    private String username;
    private String password;
    private String driver_class_name;

    /** Returns the database connection URL. */
    public String getUrl() {
        return url;
    }

    /** Sets the database connection URL. */
    public void setUrl(String url) {
        this.url = url;
    }

    /** Returns the database username. */
    public String getUsername() {
        return username;
    }

    /** Sets the database username. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns the database password. */
    public String getPassword() {
        return password;
    }

    /** Sets the database password. */
    public void setPassword(String password) {
        this.password = password;
    }

    /** Returns the database driver class name. */
    public String getDriver_class_name() {
        return driver_class_name;
    }

    /** Sets the database driver class name. */
    public void setDriver_class_name(String driver_class_name) {
        this.driver_class_name = driver_class_name;
    }
}
