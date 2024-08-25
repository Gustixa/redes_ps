package com.example;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.jid.EntityBareJid;
import java.io.File;
import javafx.application.Platform;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.ArrayList;

import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;

import org.jivesoftware.smack.packet.MessageBuilder;

public class XmppChatApp extends Application {

    private XmppClient xmppClient = new XmppClient();
    private String username;
    private String password;

    private ListView<Contact> contactList;
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
    private Button createGroupButton;
    private Button sendFileButton;

    private Map<String, StringBuilder> conversations = new HashMap<>();
    private ListView<String> notificationList;
    private List<String> groupList = new ArrayList<>(); // Lista para almacenar los nombres de los grupos creados
    private ListView<String> groupListView = new ListView<>(); // ListView para mostrar los grupos creados
    
    
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
        notificationList.setPrefHeight(500); // Aumenta la altura preferida para el cuadro de notificaciones
        notificationList.setPrefWidth(350);

        notificationList.setOnMouseClicked(event -> {
            String selectedNotification = notificationList.getSelectionModel().getSelectedItem();
            if (selectedNotification != null) {
                String senderJid = selectedNotification.split(" ")[3]; // Extrae el JID del mensaje
                Contact contact = findContactByJid(senderJid);
                if (contact != null) {
                    contactList.getSelectionModel().select(contact); // Selecciona el contacto
                    updateChatArea(contact.getJid()); // Actualiza el área de chat con la conversación del contacto
                }
            }
        });

            // Área para grupos
        groupListView = new ListView<>();
        groupListView.setPrefHeight(500); // Ajusta la altura preferida para el cuadro de grupos

        VBox rightPane = new VBox(10, notificationList, groupListView);
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

        createGroupButton = new Button("Create Group");
        createGroupButton.setOnAction(e -> showCreateGroupDialog());

        sendFileButton = new Button("Send File");
        sendFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                Contact selectedContact = contactList.getSelectionModel().getSelectedItem();
                if (selectedContact != null) {
                    try {
                        xmppClient.enviarArchivoBase64(selectedContact.getJid(), file);
                        chatArea.appendText("File sent to " + selectedContact.getJid() + ": " + file.getName() + "\n");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "File Transfer Error", "Failed to send file.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Contact Selected", "Please select a contact to send the file to.");
                }
            }
        });     

        VBox leftPane = new VBox(10, contactList, newUserField, addUserButton, createGroupButton,
        showDetailsButton,presenceLabel, presenceField, updatePresenceButton, logoutButton, deleteAccountButton,
        sendFileButton);
        leftPane.setPadding(new Insets(10));

        HBox bottomPane = new HBox(10, messageField, sendButton);
        bottomPane.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setLeft(leftPane);
        root.setCenter(chatArea);
        root.setBottom(bottomPane);
        root.setRight(rightPane);

        Scene scene = new Scene(root, 1000, 800);

        primaryStage.setTitle("XmppClient");
        primaryStage.setScene(scene);
        primaryStage.show();

        contactList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateChatArea(newValue.getJid());
            }
        });

        connectToXmpp();
    }

    private void connectToXmpp() {
        try {
            xmppClient.connect(username, password);

            xmppClient.getConnection().addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processStanza(Stanza stanza) {
                    if (stanza instanceof Message) {
                        Message message = (Message) stanza;
                        EntityBareJid from = (EntityBareJid) message.getFrom().asEntityBareJidIfPossible();
    
                        if (from != null && message.getBody() != null) {
                            Platform.runLater(() -> {
                                String sender = from.asEntityBareJidString();
                                String messageBody = message.getBody();
    
                                // Almacena el mensaje en el historial de conversaciones
                                StringBuilder conversation = conversations.computeIfAbsent(sender, k -> new StringBuilder());
                                conversation.append(sender).append(": ").append(messageBody).append("\n");
    
                                // Añade una notificación del nuevo mensaje
                                notificationList.getItems().add("Nuevo mensaje de " + sender);
    
                                // Si el contacto está actualmente seleccionado, actualiza el área de chat
                                Contact selectedContact = contactList.getSelectionModel().getSelectedItem();
                                if (selectedContact != null && selectedContact.getJid().equals(sender)) {
                                    updateChatArea(sender);
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
                    updateChatArea(newValue.getJid());
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

            // Listener para transferencias de archivos entrantes
            FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(xmppClient.getConnection());
            fileTransferManager.addFileTransferListener(new FileTransferListener() {
                @Override
                public void fileTransferRequest(FileTransferRequest request) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                                "Incoming file transfer request from " + request.getRequestor().asBareJid() + ". Accept?",
                                ButtonType.YES, ButtonType.NO);
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                try {
                                    File file = new File("downloads/" + request.getFileName());
                                    IncomingFileTransfer transfer = request.accept();
                                    
                                    // Monitor progress in a separate thread
                                    Thread transferThread = new Thread(() -> {
                                        try {
                                            transfer.receiveFile(file);
                                            while (!transfer.isDone()) {
                                                double progress = (double) transfer.getAmountWritten() / transfer.getFileSize();
                                                System.out.println("Transfer progress: " + (progress * 100) + "%");
                                                
                                                if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                                                    System.err.println("Error during file transfer: " + transfer.getError());
                                                    break;
                                                }
            
                                                if (transfer.getStatus().equals(FileTransfer.Status.cancelled) || transfer.getStatus().equals(FileTransfer.Status.refused)) {
                                                    System.out.println("File transfer cancelled or refused.");
                                                    break;
                                                }
            
                                                Thread.sleep(1000); // Wait for a second before checking again
                                            }
            
                                            if (transfer.getStatus().equals(FileTransfer.Status.complete)) {
                                                Platform.runLater(() -> chatArea.appendText("File received: " + file.getAbsolutePath() + "\n"));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "File Transfer Error", "Failed to receive file."));
                                        }
                                    });
                                    
                                    transferThread.start();
            
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "File Transfer Error", "Failed to receive file.");
                                }
                            }
                        });
                    });
                }
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

    private Contact findContactByJid(String jid) {
        for (Contact contact : contactList.getItems()) {
            if (contact.getJid().equals(jid)) {
                return contact;
            }
        }
        return null;
    }
    
    
    private void updateContactList() {
        try {
            contactList.getItems().clear();
            List<String> contacts = xmppClient.getContactList();
            if (contacts != null) {
                for (String contactJid : contacts) {
                    Roster roster = Roster.getInstanceFor(xmppClient.getConnection());
                    Presence presence = roster.getPresence(JidCreate.bareFrom(contactJid));
                    String status = presence.isAvailable() ? presence.getMode().toString() : "Offline";
                    
                    // Crear un objeto Contact
                    Contact contact = new Contact(contactJid, contactJid, status);
                    
                    contactList.getItems().add(contact);
                }
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
        String message = messageField.getText();
        Contact selectedContact = contactList.getSelectionModel().getSelectedItem();
        if (message.isEmpty() || selectedContact == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a contact and enter a message.");
            return;
        }
    
        String contactJid = selectedContact.getJid();
        String formattedMessage = "Me: " + message;
    
        try {
            xmppClient.sendMessage(contactJid, message);
            chatArea.appendText(formattedMessage + "\n");
    
            // Almacenar la conversación en el HashMap
            conversations.computeIfAbsent(contactJid, k -> new StringBuilder()).append(formattedMessage).append("\n");
    
            messageField.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message.");
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
        Contact selectedUser = contactList.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                BareJid contactJid = JidCreate.bareFrom(selectedUser.getJid());
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
    

    private void showCreateGroupDialog() {
        // Crear un diálogo para seleccionar los contactos para el grupo
        Dialog<List<Object>> dialog = new Dialog<>();
        dialog.setTitle("Create Group");
        dialog.setHeaderText("Enter group name and select contacts:");
    
        // Campo de texto para ingresar el nombre del grupo
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Group Name");
    
        // Lista de selección de contactos
        ListView<Contact> contactSelectionList = new ListView<>(contactList.getItems());
        contactSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    
        // Layout para organizar el campo de texto y la lista de contactos
        VBox dialogContent = new VBox(10, groupNameField, contactSelectionList);
        dialogContent.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(dialogContent);
    
        // Añadir botones de acción al diálogo
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
    
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType && !groupNameField.getText().trim().isEmpty()) {
                List<Object> result = new ArrayList<>();
                result.add(groupNameField.getText().trim()); // Añadir el nombre del grupo al inicio de la lista
                result.addAll(contactSelectionList.getSelectionModel().getSelectedItems());
                return result;
            }
            return null;
        });
    
        // Procesar el resultado cuando se presiona el botón "Create"
        dialog.showAndWait().ifPresent(result -> {
            if (result.size() > 1) {
                String groupName = (String) result.get(0);
                List<Contact> selectedContacts = new ArrayList<>();
                for (int i = 1; i < result.size(); i++) {
                    Object item = result.get(i);
                    if (item instanceof Contact) {
                        selectedContacts.add((Contact) item);
                    }
                }
                List<String> selectedContactJids = new ArrayList<>();
                for (Contact contact : selectedContacts) {
                    selectedContactJids.add(contact.getJid());
                }
                createGroup(groupName, selectedContactJids);
            }
        });
    }
    
    private void createGroup(String groupName, List<String> selectedContacts) {
        try {
            for (String contactJid : selectedContacts) {
                EntityBareJid memberJid = JidCreate.entityBareFrom(contactJid);
    
                // Crear y enviar un mensaje de notificación
                Message groupNotification = MessageBuilder.buildMessage()
                        .to(memberJid)
                        .setBody("You have been added to the group: " + groupName)
                        .build();
    
                xmppClient.getConnection().sendStanza(groupNotification);
            }
    
            // Añadir el nombre del grupo a la lista y actualizar la vista de grupos
            groupList.add(groupName);
            groupListView.getItems().setAll(groupList);
    
            showAlert(Alert.AlertType.INFORMATION, "Group Created", "Group '" + groupName + "' created successfully.");
        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create group.");
        }
    }
}