package com.example;

import com.formdev.flatlaf.FlatLightLaf;
import javafx.application.Application;

/**
 * Clase principal de la aplicación que configura el tema FlatLightLaf y lanza la interfaz gráfica.
 * <p>
 * Esta clase configura el tema de la interfaz gráfica utilizando la biblioteca FlatLaf y luego lanza
 * la aplicación JavaFX a partir de la clase {@link LoginWindow}.
 * </p>
 * 
 * <p>La clase {@link Application} proporciona el entorno necesario para inicializar y ejecutar
 * la aplicación JavaFX.</p>
 * 
 * @see com.formdev.flatlaf.FlatLightLaf
 * @see javafx.application.Application
 */
public class App {
    
    /**
     * Método principal de la aplicación que configura el tema de la interfaz gráfica y lanza la
     * aplicación JavaFX.
     *
     * @param args Los argumentos de línea de comandos pasados a la aplicación.
     */
    public static void main(String[] args) {
        // Configura el tema FlatLightLaf para la interfaz gráfica.
        FlatLightLaf.setup();
        
        // Lanza la aplicación JavaFX a partir de la clase LoginWindow.
        Application.launch(LoginWindow.class, args);
    }
}
