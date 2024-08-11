package com.example;

public class App {
    public static void main(String[] args) {
        System.out.println("Iniciando la aplicación GUI para XMPP Client");
        
        // Crear e iniciar la aplicación gráfica
        SimpleGuiApp guiApp = new SimpleGuiApp();
        guiApp.createAndShowGUI();
    }
}
