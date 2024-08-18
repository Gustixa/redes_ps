package com.example;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;


import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;


public class XmppClient {

    private XMPPTCPConnection connection;
    private final Map<String, List<String>> messageHistory = new HashMap<>(); // Mapa para guardar el historial de mensajes

 
    public void connect(String username, String password) throws XmppStringprepException, XMPPException, SmackException, IOException, InterruptedException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain("alumchat.lol")
                .setHost("alumchat.lol")
                .setUsernameAndPassword(username, password)
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled) // Desactivar TLS/SSL
                .build();

        connection = new XMPPTCPConnection(config);
        connection.connect().login();

        System.out.println("Connected as: " + connection.getUser());
                // Añadir el listener para los mensajes entrantes
        // Añadir el listener para los mensajes entrantes
        addIncomingMessageListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid fromJid, Message message, Chat chat) {
                // Guardar el mensaje en el historial
                messageHistory.computeIfAbsent(fromJid.toString(), k -> new ArrayList<>()).add(message.getBody());
                System.out.println("Received message from " + fromJid + ": " + message.getBody());
            }
        });
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

 
    public void registerAccount(String username, String password) throws XmppStringprepException, SmackException, IOException, InterruptedException, XMPPException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        if (accountManager.supportsAccountCreation()) {
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(username), password);
            System.out.println("Cuenta registrada: " + username);
        } else {
            System.out.println("Creación de cuenta no soportada.");
        }
    }

 
    public void deleteAccount() throws SmackException.NotLoggedInException, SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.deleteAccount();
        System.out.println("Account deleted.");
    }


    public void sendMessage(String toJid, String message) throws XmppStringprepException, SmackException, InterruptedException, NotConnectedException, IOException {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(toJid);
        chatManager.chatWith(jid).send(message);
        System.out.println("Message sent to " + toJid + ": " + message);
    }


    public void addIncomingMessageListener(IncomingChatMessageListener listener) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(listener);
    }

  
    public List<String> getContactList() throws Exception {
        List<String> contacts = new ArrayList<>();
        Roster roster = Roster.getInstanceFor(connection);
        for (RosterEntry entry : roster.getEntries()) {
            contacts.add(entry.getJid().toString());
        }
        return contacts;
    }

 
    public void setPresence(String statusMessage, Mode mode) throws SmackException.NotConnectedException, InterruptedException {
        Presence presence = PresenceBuilder.buildPresence()
                .ofType(Type.available)    // Ajustar el tipo de presencia
                .setMode(mode)             // Establece el modo de presencia (available, away, etc.)
                .setStatus(statusMessage)  // Establece el mensaje de estado
                .build();
        
        connection.sendStanza(presence);
    }


    public void disconnect() throws SmackException.NotConnectedException {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

   
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

 
    public void addContact(String jid, String name) throws XmppStringprepException, SmackException.NotLoggedInException, SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException {
        Roster roster = Roster.getInstanceFor(connection);
        BareJid bareJid = JidCreate.bareFrom(jid);

        if (!roster.contains(bareJid)) {
            // Enviar solicitud de suscripción
            Presence subscribe = PresenceBuilder.buildPresence()
                    .ofType(Presence.Type.subscribe)
                    .to(bareJid)
                    .build();

            connection.sendStanza(subscribe);

            System.out.println("Subscription request sent to: " + jid);
        } else {
            System.out.println("Contact already exists: " + jid);
        }
    }


    public void showContacts() throws SmackException.NotLoggedInException, SmackException.NotConnectedException, InterruptedException {
        Roster roster = Roster.getInstanceFor(connection);
        for (RosterEntry entry : roster.getEntries()) {
            Presence presence = roster.getPresence(entry.getJid());
            System.out.println(entry.getName() + " (" + entry.getJid() + "): " + presence.getType());
        }
    }

    
    public void showContactDetails(String jid) throws XmppStringprepException, SmackException.NotLoggedInException, SmackException.NotConnectedException, InterruptedException {
        Roster roster = Roster.getInstanceFor(connection);
        BareJid bareJid = JidCreate.bareFrom(jid);
        RosterEntry entry = roster.getEntry(bareJid);
        if (entry != null) {
            System.out.println("Name: " + entry.getName());
            System.out.println("JID: " + entry.getJid());
            System.out.println("Groups: " + entry.getGroups());
            System.out.println("Subscription: " + entry.getType());
        } else {
            System.out.println("Contact not found: " + jid);
        }
    }

    public void joinGroupChat(String roomName, String nickname) throws XmppStringprepException, SmackException, IOException, InterruptedException, XMPPException {
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
        EntityBareJid mucJid = JidCreate.entityBareFrom(roomName + "@conference.alumchat.lol");
        MultiUserChat muc = manager.getMultiUserChat(mucJid);
        muc.join(Resourcepart.from(nickname));
        System.out.println("Joined group chat: " + roomName);
    }

    public void setPresence(Presence.Type type, String status) throws SmackException.NotConnectedException, InterruptedException {
        Presence presence = PresenceBuilder.buildPresence().ofType(type).setStatus(status).build();
        connection.sendStanza(presence);
        System.out.println("Presence set to: " + type + " (" + status + ")");
    }

    public void setupDeliveryReceipts() {
        DeliveryReceiptManager receiptManager = DeliveryReceiptManager.getInstanceFor(connection);
        receiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        receiptManager.addReceiptReceivedListener((fromJid, toJid, receiptId, stanza) -> {
            System.out.println("Message delivered to " + fromJid);
        });
    }

    public void sendFile(String toJid, File file) throws XmppStringprepException, SmackException, InterruptedException, IOException {
        FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(JidCreate.entityFullFrom(toJid + "/Smack"));
        transfer.sendFile(file, "Sending file");
        System.out.println("File sent to " + toJid);
    }


    private void handleSubscriptionRequest(BareJid from) {
        // Aquí podrías mostrar un cuadro de diálogo en la interfaz de usuario para que el usuario acepte o rechace la solicitud
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Subscription Request");
        alert.setHeaderText("New subscription request from: " + from);
        alert.setContentText("Do you want to accept this subscription request?");

        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        alert.showAndWait().ifPresent(type -> {
            if (type == buttonYes) {
                try {
                    acceptSubscription(from);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (type == buttonNo) {
                try {
                    rejectSubscription(from);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void acceptSubscription(BareJid from) throws SmackException.NotConnectedException, InterruptedException {
        Presence subscribed = PresenceBuilder.buildPresence()
                .ofType(Presence.Type.subscribed)
                .build();
        connection.sendStanza(subscribed);
        System.out.println("Subscription accepted from: " + from);
    }
    

    private void rejectSubscription(BareJid from) throws SmackException.NotConnectedException, InterruptedException {
        Presence unsubscribed = PresenceBuilder.buildPresence()
                .ofType(Presence.Type.unsubscribed)
                .build();
        connection.sendStanza(unsubscribed);
        System.out.println("Subscription rejected from: " + from);
    }    
}