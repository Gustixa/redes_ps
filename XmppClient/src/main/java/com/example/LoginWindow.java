package com.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow {
    private XmppClient xmppClient;

    public void createAndShowLoginWindow() {
        // Crear la ventana de login
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        // Crear un panel para organizar los componentes
        JPanel panel = new JPanel();
        frame.add(panel);

        // Configurar componentes
        placeComponents(panel, frame);

        // Hacer la ventana visible
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel, JFrame frame) {
        panel.setLayout(null);

        // Crear una etiqueta y campo de texto para el usuario
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        // Crear una etiqueta y campo de texto para la contraseña
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        // Crear un botón para conectar
        JButton connectButton = new JButton("Conectar");
        connectButton.setBounds(10, 80, 150, 25);
        panel.add(connectButton);

        // Crear una etiqueta para mostrar el estado de la conexión
        JLabel statusLabel = new JLabel("Estado: Desconectado");
        statusLabel.setBounds(10, 110, 300, 25);
        panel.add(statusLabel);

        // Agregar acción al botón de conectar
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                xmppClient = new XmppClient();
                try {
                    xmppClient.connect(username, password);
                    statusLabel.setText("Estado: Conectado como " + username);

                    // Cerrar la ventana de login y abrir la ventana de chat
                    frame.dispose();
                    ChatWindow chatWindow = new ChatWindow(xmppClient);
                    chatWindow.createAndShowChatWindow();

                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
}
