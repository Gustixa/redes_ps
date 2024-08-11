package com.example;

import javax.swing.*;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;
// import org.jxmpp.jid.JidCreate;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimpleGuiApp {
    private XmppClient xmppClient;

    public void createAndShowGUI() {
        // Crear la ventana principal (JFrame)
        JFrame frame = new JFrame("XMPP Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // Crear un panel para organizar los componentes
        JPanel panel = new JPanel();
        frame.add(panel);

        // Configurar componentes
        placeComponents(panel);

        // Hacer la ventana visible
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel) {
        // Configurar el layout del panel
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

        // Crear una etiqueta y campo de texto para el destinatario
        JLabel toLabel = new JLabel("Para:");
        toLabel.setBounds(10, 110, 80, 25);
        panel.add(toLabel);

        JTextField toText = new JTextField(20);
        toText.setBounds(100, 110, 165, 25);
        panel.add(toText);

        // Crear un campo de texto para el mensaje
        JLabel messageLabel = new JLabel("Mensaje:");
        messageLabel.setBounds(10, 140, 80, 25);
        panel.add(messageLabel);

        JTextField messageText = new JTextField(20);
        messageText.setBounds(100, 140, 165, 25);
        panel.add(messageText);

        // Crear un botón para enviar el mensaje
        JButton sendButton = new JButton("Enviar");
        sendButton.setBounds(10, 170, 150, 25);
        panel.add(sendButton);

        // Crear una etiqueta para mostrar el estado de la conexión y mensajes recibidos
        JLabel statusLabel = new JLabel("Estado: Desconectado");
        statusLabel.setBounds(10, 200, 300, 25);
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

                    xmppClient.addIncomingMessageListener(new IncomingChatMessageListener() {
                        @Override
                        public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setText("Mensaje recibido de " + from + ": " + message.getBody());
                            });
                        }
                    });

                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // Agregar acción al botón de enviar mensaje
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (xmppClient != null && xmppClient.isConnected()) {
                    String toJid = toText.getText();
                    String message = messageText.getText();

                    try {
                        xmppClient.sendMessage(toJid, message);
                        statusLabel.setText("Mensaje enviado a " + toJid);
                    } catch (Exception ex) {
                        statusLabel.setText("Error al enviar: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    statusLabel.setText("No conectado");
                }
            }
        });
    }
}
