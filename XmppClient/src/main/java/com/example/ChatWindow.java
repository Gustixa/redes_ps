package com.example;

import javax.swing.*;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.chat2.Chat;
import org.jxmpp.jid.EntityBareJid;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatWindow {
    private XmppClient xmppClient;

    public ChatWindow(XmppClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    public void createAndShowChatWindow() {
        // Crear la ventana de chat
        JFrame frame = new JFrame("Chat");
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
        panel.setLayout(null);

        // Crear una etiqueta y campo de texto para el destinatario
        JLabel toLabel = new JLabel("Para:");
        toLabel.setBounds(10, 20, 80, 25);
        panel.add(toLabel);

        JTextField toText = new JTextField(20);
        toText.setBounds(100, 20, 165, 25);
        panel.add(toText);

        // Crear un campo de texto para el mensaje
        JLabel messageLabel = new JLabel("Mensaje:");
        messageLabel.setBounds(10, 50, 80, 25);
        panel.add(messageLabel);

        JTextField messageText = new JTextField(20);
        messageText.setBounds(100, 50, 165, 25);
        panel.add(messageText);

        // Crear un botón para enviar el mensaje
        JButton sendButton = new JButton("Enviar");
        sendButton.setBounds(10, 80, 150, 25);
        panel.add(sendButton);

        // Crear una etiqueta para mostrar mensajes recibidos
        JLabel statusLabel = new JLabel("Estado: Conectado");
        statusLabel.setBounds(10, 110, 300, 25);
        panel.add(statusLabel);

        // Agregar acción al botón de enviar mensaje
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String toJid = toText.getText();
                String message = messageText.getText();

                try {
                    xmppClient.sendMessage(toJid, message);
                    statusLabel.setText("Mensaje enviado a " + toJid);
                } catch (Exception ex) {
                    statusLabel.setText("Error al enviar: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // Agregar listener para mensajes entrantes
        xmppClient.addIncomingMessageListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Mensaje recibido de " + from + ": " + message.getBody());
                });
            }
        });
    }
}
