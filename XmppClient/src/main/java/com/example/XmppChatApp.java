package com.example;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import java.util.Collection;

import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * La clase XmppChatApp es una aplicación basada en JavaFX que proporciona una interfaz gráfica de usuario 
 * para interactuar con un servidor XMPP (Protocolo Extensible de Mensajería y Presencia). 
 * Los usuarios pueden enviar y recibir mensajes, gestionar su lista de contactos, actualizar su estado de presencia, 
 * cerrar sesión y eliminar su cuenta.
 */
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

    /**
     * Establece las credenciales para el cliente XMPP.
     *
     * @param username El nombre de usuario del usuario.
     * @param password La contraseña del usuario.
    */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Inicia la aplicación JavaFX e inicializa los componentes de la interfaz de usuario.
     *
     * @param primaryStage El escenario principal de esta aplicación.
     */
    @Override
    public void start(Stage primaryStage) {
        // Crear elementos de la interfaz
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

        primaryStage.setTitle("XmppClient");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Conectar con XMPP usando las credenciales
        connectToXmpp();
    }
    
    /**
     * Conecta al servidor XMPP e inicializa la lista de contactos y el oyente de mensajes.
     */
    private void connectToXmpp() {
        try {
            xmppClient.connect(username, password);
            xmppClient.addIncomingMessageListener((from, message, chat) -> {
                chatArea.appendText(from + ": " + message.getBody() + "\n");
            });

            // Obtener el Roster y registrar el RosterListener
            Roster roster = Roster.getInstanceFor(xmppClient.getConnection());
            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {
                    updateContactList();
                }

                @Override
                public void entriesUpdated(Collection<Jid> addresses) {
                    updateContactList();
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {
                    updateContactList();
                }

                @Override
                public void presenceChanged(Presence presence) {
                    updateContactList();
                }
            });

            updateContactList(); // Cargar los contactos iniciales después de conectarse

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Connection Failed", "Failed to connect to the XMPP server.");
        }
    }

    
    /**
     * Actualiza la lista de contactos en la interfaz de usuario obteniendo los contactos actuales del servidor XMPP.
     */
private void updateContactList() {
    try {
        contactList.getItems().clear();
        Roster roster = Roster.getInstanceFor(xmppClient.getConnection());

        for (RosterEntry entry : roster.getEntries()) {
            BareJid contactJid = entry.getJid().asBareJid();  // Convertir a BareJid
            Presence presence = roster.getPresence(contactJid); // Obtener la presencia con BareJid
            String presenceStatus = presence.isAvailable() ? presence.getMode().toString() : "Offline";
            String contactDisplay = contactJid + " (" + presenceStatus + ")";
            contactList.getItems().add(contactDisplay);
        }
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve contact list.");
    }
}

    

    /**
     * Envía un mensaje al contacto seleccionado.
     */
    private void sendMessage() {
        String selectedUser = contactList.getSelectionModel().getSelectedItem();
        String message = messageField.getText();

        if (selectedUser != null && !message.isEmpty()) {
            try {
                xmppClient.sendMessage(selectedUser, message);
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

    /**
     * Agrega un nuevo contacto a la lista del usuario.
     */
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

    /**
     * Cierra la sesión del usuario y cierra la ventana de la aplicación.
     *
     * @param primaryStage El escenario principal de la aplicación.
     */
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

    /**
     * Elimina la cuenta del usuario en el servidor XMPP después de una confirmación.
     *
     * @param primaryStage El escenario principal de la aplicación.
     */
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
    /**
     * Actualiza el estado de presencia del usuario en el servidor XMPP.
     */
    private void updatePresence() {
        String presenceMessage = presenceField.getText();
        if (presenceMessage != null && !presenceMessage.isEmpty()) {
            try {
                xmppClient.setPresence(presenceMessage, Presence.Mode.available); // Puedes cambiar el modo según lo que desees
                showAlert(Alert.AlertType.INFORMATION, "Success", "Presence updated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update presence.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a presence message.");
        }
    }

    /**
     * Muestra un cuadro de diálogo de alerta con el título y mensaje especificados.
     *
     * @param type    El tipo de la alerta (por ejemplo, ERROR, WARNING, INFORMATION).
     * @param title   El título del cuadro de diálogo de alerta.
     * @param message El mensaje a mostrar en el cuadro de diálogo de alerta.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Desconecta del servidor XMPP cuando se detiene la aplicación.
     *
     * @throws Exception Si ocurre un error durante el proceso de desconexión.
     */
    @Override
    public void stop() throws Exception {
        if (xmppClient.isConnected()) {
            xmppClient.disconnect();
        }
        super.stop();
    }
}
