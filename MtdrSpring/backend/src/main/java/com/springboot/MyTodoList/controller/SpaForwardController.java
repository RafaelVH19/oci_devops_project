package com.springboot.MyTodoList.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador encargado de redirigir las rutas del frontend
 * hacia el archivo index.html de la aplicación SPA.
 *
 * Permite que el sistema de navegación del frontend maneje
 * correctamente las rutas internas.
 */
@Controller
public class SpaForwardController {
     @GetMapping({
             "/",
             "/landing",
             "/login",
             "/app",
             "/dashboard",
             "/dashboard/**",
             "/lumi",
             "/manager"
     })

    /** Redirige las rutas configuradas al punto de entrada principal de la aplicacion frontend */
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}