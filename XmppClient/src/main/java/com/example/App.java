package com.example;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class App {
    public static void main(String[] args) {
        System.out.println("Iniciando la aplicación GUI para XMPP Client");
        
        // Establecer el Look and Feel de FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Crear e iniciar la ventana de login
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.createAndShowLoginWindow();
    }
}
