package com.example;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.EntityBareJid;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class XmppChatApp extends Application {

    private XmppClient xmppClient = new XmppClient();
    private String username;
    private String password;

    private ListView<String> contactList;
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private TextField newUserField;
    private Button addUserButton;
    private Button logoutButton;
    private Button deleteAccountButton;
    private Label presenceLabel;
    private TextField presenceField;
    private Button updatePresenceButton;
    private Button showDetailsButton;

    private Map<String, StringBuilder> conversations = new HashMap<>();
    private ListView<String> notificationList;


    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void start(Stage primaryStage) {
        contactList = new ListView<>();
        chatArea = new TextArea();
        chatArea.setEditable(false);

        notificationList = new ListView<>();
        notificationList.setPrefHeight(200); // Aumenta la altura preferida para el cuadro de notificaciones
        notificationList.setPrefWidth(250);


        notificationList.setOnMouseClicked(event -> {
            String selectedNotification = notificationList.getSelectionModel().getSelectedItem();
            if (selectedNotification != null) {
                String senderJid = selectedNotification.split(" ")[3]; // Extrae el JID del mensaje
                contactList.getSelectionModel().select(senderJid); // Selecciona el contacto
                updateChatArea(senderJid); // Actualiza el área de chat con la conversación del contacto
            }
        });

        VBox rightPane = new VBox(10, notificationList);
        rightPane.setPadding(new Insets(10));

        messageField = new TextField();
        messageField.setPrefWidth(400);
        sendButton = new Button("Send");

        newUserField = new TextField();
        newUserField.setPromptText("Enter new user JID");
        addUserButton = new Button("Add User");

        logoutButton = new Button("Logout");
        deleteAccountButton = new Button("Delete Account");

        presenceLabel = new Label("Presence:");
        presenceField = new TextField();
        updatePresenceButton = new Button("Update Presence");

        // Buton para mostrar detalles del contacto
        showDetailsButton = new Button("Show Details");
        showDetailsButton.setOnAction(e -> showContactDetails());

        sendButton.setOnAction(e -> sendMessage());
        addUserButton.setOnAction(e -> addUser());
        logoutButton.setOnAction(e -> logout(primaryStage));
        deleteAccountButton.setOnAction(e -> deleteAccount(primaryStage));
        updatePresenceButton.setOnAction(e -> updatePresence());

        VBox leftPane = new VBox(10, contactList, newUserField, addUserButton, showDetailsButton,presenceLabel, presenceField, updatePresenceButton, logoutButton, deleteAccountButton);
        leftPane.setPadding(new Insets(10));

        HBox bottomPane = new HBox(10, messageField, sendButton);
        bottomPane.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setLeft(leftPane);
        root.setCenter(chatArea);
        root.setBottom(bottomPane);
        root.setRight(rightPane);

        Scene scene = new Scene(root, 2000, 1600);

        primaryStage.setTitle("XmppClient");
        primaryStage.setScene(scene);
        primaryStage.show();

        contactList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateChatArea(newValue);
            }
        });

        connectToXmpp();
    }

    private void connectToXmpp() {
        try {
            xmppClient.connect(username, password);

            // Agregar listener para manejar las Stanzas de mensajes entrantes
            xmppClient.getConnection().addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processStanza(Stanza stanza) {
                    if (stanza instanceof Message) {
                        Message message = (Message) stanza;
                        EntityBareJid from = (EntityBareJid) message.getFrom().asEntityBareJidIfPossible();

                        if (from != null && message.getBody() != null) {
                            Platform.runLater(() -> {
                                String sender = from.asEntityBareJidString();
                                String notification = "Nuevo mensaje de " + sender;

                                // Agregar la notificación al notificationList
                                notificationList.getItems().add(notification);

                                // Guardar la conversación en el historial
                                StringBuilder conversation = conversations.getOrDefault(sender, new StringBuilder());
                                conversation.append(sender).append(": ").append(message.getBody()).append("\n");
                                conversations.put(sender, conversation);

                                // Mostrar el mensaje en el área de chat si el contacto está seleccionado
                                String selectedContact = contactList.getSelectionModel().getSelectedItem();
                                if (selectedContact != null && selectedContact.equals(sender)) {
                                    chatArea.appendText(sender + ": " + message.getBody() + "\n");
                                }
                            });
                        }
                    }
                }
            }, stanza -> stanza instanceof Message);

            // Agregar listener para solicitudes de suscripción entrantes
            xmppClient.getConnection().addAsyncStanzaListener(stanza -> {
                Presence presence = (Presence) stanza;
                if (presence.getType() == Presence.Type.subscribe) {
                    Platform.runLater(() -> {
                        // Mostrar una alerta para aceptar o rechazar la solicitud
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
                            "El usuario " + presence.getFrom().asBareJid() + " quiere agregarte como contacto. ¿Aceptar?", 
                            ButtonType.YES, ButtonType.NO);
                        alert.showAndWait();

                        try {
                            if (alert.getResult() == ButtonType.YES) {
                                // Aceptar la solicitud
                                Presence subscribedPresence = PresenceBuilder.buildPresence()
                                        .ofType(Presence.Type.subscribed)
                                        .to(presence.getFrom())
                                        .build();
                                xmppClient.getConnection().sendStanza(subscribedPresence);

                                // También puedes agregar el contacto automáticamente si lo deseas
                                xmppClient.addContact(presence.getFrom().asBareJid().toString(), presence.getFrom().asBareJid().toString());
                                updateContactList();
                            } else {
                                // Rechazar la solicitud
                                Presence unsubscribedPresence = PresenceBuilder.buildPresence()
                                        .ofType(Presence.Type.unsubscribed)
                                        .to(presence.getFrom())
                                        .build();
                                xmppClient.getConnection().sendStanza(unsubscribedPresence);
                            }
                        } catch (SmackException.NotConnectedException | InterruptedException | XmppStringprepException |
                                SmackException.NotLoggedInException | SmackException.NoResponseException |
                                XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }, stanza -> stanza instanceof Presence && ((Presence) stanza).getType() == Presence.Type.subscribe);


            contactList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateChatArea(newValue);
                    
                    try {
                        EntityBareJid contactJid = JidCreate.entityBareFrom(newValue);
                        fetchConversationHistory(contactJid); // Llama a fetchConversationHistory cuando se selecciona un contacto
                    } catch (XmppStringprepException e) {
                        e.printStackTrace();
                    }
                }
            });

            xmppClient.getConnection().addAsyncStanzaListener(stanza -> {
                if (stanza instanceof IQ) {
                    IQ iq = (IQ) stanza;
                    if (iq.getType() == IQ.Type.error) {
                        System.out.println("Error received: " + iq.getError().toString());
                    }
                }
            }, stanza -> stanza instanceof IQ);

            
            Roster roster = Roster.getInstanceFor(xmppClient.getConnection());
            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {
                    Platform.runLater(() -> updateContactList());
                }

                @Override
                public void entriesUpdated(Collection<Jid> addresses) {
                    Platform.runLater(() -> updateContactList());
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {
                    Platform.runLater(() -> updateContactList());
                }

                @Override
                public void presenceChanged(Presence presence) {
                    Platform.runLater(() -> updateContactList());
                }
            });

            updateContactList();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Connection Failed", "Failed to connect to the XMPP server.");
        }
    }

    private void fetchConversationHistory(EntityBareJid contactJid) {
        try {
            MamQueryIQ mamQuery = new MamQueryIQ(contactJid);
            StanzaCollector stanzaCollector = xmppClient.getConnection().createStanzaCollectorAndSend(mamQuery);
    
            while (true) {
                Stanza stanza = stanzaCollector.nextResultOrThrow();
                if (stanza == null) {
                    break; // No hay más resultados
                }
    
                if (stanza instanceof Message) {
                    Message message = (Message) stanza;
                    if (message.getBody() != null) {
                        String sender = message.getFrom().asEntityBareJidIfPossible().toString();
                        String body = message.getBody();
    
                        Platform.runLater(() -> {
                            StringBuilder conversation = conversations.getOrDefault(sender, new StringBuilder());
                            conversation.append(sender).append(": ").append(body).append("\n");
                            conversations.put(sender, conversation);
    
                            if (contactList.getSelectionModel().getSelectedItem().equals(sender)) {
                                chatArea.appendText(sender + ": " + body + "\n");
                            }
                        });
                    }
                }
            }
    
            stanzaCollector.cancel();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to fetch conversation history.");
        }
    }  

    private void updateContactList() {
        try {
            contactList.getItems().clear();
            List<String> contacts = xmppClient.getContactList();
            if (contacts != null) {
                contactList.getItems().addAll(contacts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve contact list.");
        }
    }

    private void updateChatArea(String userJid) {
        chatArea.clear();
        StringBuilder conversation = conversations.get(userJid);
        if (conversation != null) {
            chatArea.appendText(conversation.toString());
        }
    }

    private void sendMessage() {
        String selectedUser = contactList.getSelectionModel().getSelectedItem();
        String message = messageField.getText();

        if (selectedUser != null && !message.isEmpty()) {
            try {
                xmppClient.sendMessage(selectedUser, message);
                StringBuilder conversation = conversations.getOrDefault(selectedUser, new StringBuilder());
                conversation.append("Me: ").append(message).append("\n");
                conversations.put(selectedUser, conversation);

                chatArea.appendText("Me: " + message + "\n");
                messageField.clear();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a user and enter a message.");
        }
    }

    private void addUser() {
        String newUserJid = newUserField.getText();
        if (newUserJid != null && !newUserJid.isEmpty()) {
            try {
                xmppClient.addContact(newUserJid, newUserJid);
                updateContactList();
                newUserField.clear();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add contact.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a valid JID.");
        }
    }

    private void logout(Stage primaryStage) {
        try {
            xmppClient.disconnect();
            primaryStage.close();
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout.");
        }
    }

    private void deleteAccount(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            try {
                xmppClient.deleteAccount();
                primaryStage.close();
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete account.");
            }
        }
    }

    private void updatePresence() {
        String presenceMessage = presenceField.getText();
        if (presenceMessage != null && !presenceMessage.isEmpty()) {
            try {
                xmppClient.setPresence(presenceMessage, Presence.Mode.available);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Presence updated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update presence.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a presence message.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showContactDetails() {
        String selectedUser = contactList.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                // Extraer el JID del contacto de la cadena seleccionada en la lista
                String contactJidStr = selectedUser.split(" ")[0];
                BareJid contactJid = JidCreate.bareFrom(contactJidStr); // Convertir el String a BareJid

                Roster roster = Roster.getInstanceFor(xmppClient.getConnection());
                RosterEntry entry = roster.getEntry(contactJid);

                if (entry != null) {
                    Presence presence = roster.getPresence(contactJid);
                    String presenceStatus = presence.isAvailable() ? presence.getMode().toString() : "Offline";
                    String presenceMessage = presence.getStatus();

                    String details = "JID: " + contactJid +
                                    "\nStatus: " + presenceStatus +
                                    "\nMessage: " + (presenceMessage != null ? presenceMessage : "No status message");

                    showAlert(Alert.AlertType.INFORMATION, "Contact Details", details);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Contact not found in the roster.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve contact details.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a contact first.");
        }
    }
}