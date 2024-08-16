package com.example;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.Jid;

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

    private Map<String, StringBuilder> conversations = new HashMap<>();

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void start(Stage primaryStage) {
        contactList = new ListView<>();
        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();
        sendButton = new Button("Send");

        newUserField = new TextField();
        newUserField.setPromptText("Enter new user JID");
        addUserButton = new Button("Add User");

        logoutButton = new Button("Logout");
        deleteAccountButton = new Button("Delete Account");

        presenceLabel = new Label("Presence:");
        presenceField = new TextField();
        updatePresenceButton = new Button("Update Presence");

        sendButton.setOnAction(e -> sendMessage());
        addUserButton.setOnAction(e -> addUser());
        logoutButton.setOnAction(e -> logout(primaryStage));
        deleteAccountButton.setOnAction(e -> deleteAccount(primaryStage));
        updatePresenceButton.setOnAction(e -> updatePresence());

        VBox leftPane = new VBox(10, contactList, newUserField, addUserButton, presenceLabel, presenceField, updatePresenceButton, logoutButton, deleteAccountButton);
        leftPane.setPadding(new Insets(10));

        HBox bottomPane = new HBox(10, messageField, sendButton);
        bottomPane.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setLeft(leftPane);
        root.setCenter(chatArea);
        root.setBottom(bottomPane);

        Scene scene = new Scene(root, 800, 600);

                // Aquí es donde agregas el listener a la lista de contactos
        contactList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateChatArea(newValue);
            }
        });
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
            xmppClient.addIncomingMessageListener((from, message, chat) -> {
                Platform.runLater(() -> {
                    String sender = from.asEntityBareJidString();
                    StringBuilder conversation = conversations.getOrDefault(sender, new StringBuilder());
                    conversation.append(sender).append(": ").append(message.getBody()).append("\n");
                    conversations.put(sender, conversation);

                    if (sender.equals(contactList.getSelectionModel().getSelectedItem())) {
                        updateChatArea(sender);
                    }
                });
            });

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
        // Obtener historial de mensajes del contacto seleccionado
        List<String> conversation = messageHistory.get(userJid);
        if (conversation != null) {
            chatArea.clear(); // Limpia el área de chat
            for (String message : conversation) {
                chatArea.appendText(message + "\n"); // Muestra cada mensaje en el área de chat
            }
        } else {
            chatArea.setText("No se encontró ninguna conversación para: " + userJid);
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

    @Override
    public void stop() throws Exception {
        if (xmppClient.isConnected()) {
            xmppClient.disconnect();
        }
        super.stop();
    }
}
