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
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smack.packet.StanzaBuilder;
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

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;

import java.util.Base64;

/**
 * XmppClient es una clase que proporciona funcionalidades para conectarse a un servidor XMPP, 
 * enviar y recibir mensajes, manejar contactos, manejar la presencia y unirse a chats grupales.
 * 
 * Uso básico:
 * <pre>
 * {@code
 * XmppClient client = new XmppClient();
 * client.connect("usuario", "password");
 * client.sendMessage("destinatario@ejemplo.com", "Hola!");
 * client.disconnect();
 * }
 * </pre>
 */
public class XmppClient {

    private XMPPTCPConnection connection;
    private final Map<String, List<String>> messageHistory = new HashMap<>(); // Mapa para guardar el historial de mensajes
    

    /**
     * Retorna la conexión XMPP activa.
     * 
     * @return la conexión XMPP actual
     */
    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    /**
     * Retorna el roster (lista de contactos) para la conexión actual.
     * 
     * @return el roster de la conexión actual
     */
    public Roster getRoster() {
        return Roster.getInstanceFor(connection);
    }

    /**
     * Conecta al servidor XMPP usando las credenciales proporcionadas.
     * 
     * @param username el nombre de usuario
     * @param password la contraseña
     * @throws XmppStringprepException si hay un error en la preparación del JID
     * @throws XMPPException si ocurre un error en la conexión XMPP
     * @throws SmackException si ocurre un error en la biblioteca Smack
     * @throws IOException si ocurre un error de entrada/salida
     * @throws InterruptedException si la operación es interrumpida
     */
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
 
    /**
     * Registra una nueva cuenta en el servidor XMPP.
     * 
     * @param username el nombre de usuario para la nueva cuenta
     * @param password la contraseña para la nueva cuenta
     * @throws XmppStringprepException si hay un error en la preparación del JID
     * @throws SmackException si ocurre un error en la biblioteca Smack
     * @throws IOException si ocurre un error de entrada/salida
     * @throws InterruptedException si la operación es interrumpida
     * @throws XMPPException si ocurre un error en la conexión XMPP
     */
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

     /**
     * Elimina la cuenta de usuario actualmente conectada.
     * 
     * @throws SmackException.NotLoggedInException si no está conectado
     * @throws SmackException.NoResponseException si no hay respuesta del servidor
     * @throws XMPPException.XMPPErrorException si ocurre un error XMPP
     * @throws SmackException.NotConnectedException si la conexión no está activa
     * @throws InterruptedException si la operación es interrumpida
     */
    public void deleteAccount() throws SmackException.NotLoggedInException, SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.deleteAccount();
        System.out.println("Account deleted.");
    }

    /**
     * Envía un mensaje a un destinatario específico.
     * 
     * @param toJid el JID del destinatario
     * @param message el mensaje a enviar
     * @throws XmppStringprepException si hay un error en la preparación del JID
     * @throws SmackException si ocurre un error en la biblioteca Smack
     * @throws InterruptedException si la operación es interrumpida
     * @throws NotConnectedException si la conexión no está activa
     * @throws IOException si ocurre un error de entrada/salida
     */
    public void sendMessage(String toJid, String message) throws XmppStringprepException, SmackException, InterruptedException, NotConnectedException, IOException {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(toJid);
        chatManager.chatWith(jid).send(message);
        System.out.println("Message sent to " + toJid + ": " + message);
    }
    
    /**
     * Añade un listener para mensajes entrantes.
     * 
     * @param listener el listener que manejará los mensajes entrantes
     */
    public void addIncomingMessageListener(IncomingChatMessageListener listener) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(listener);
    }

    /**
     * Obtiene una lista de contactos del roster actual.
     * 
     * @return una lista de JIDs de los contactos
     * @throws Exception si ocurre un error al obtener la lista de contactos
     */
    public List<String> getContactList() throws Exception {
        List<String> contacts = new ArrayList<>();
        Roster roster = Roster.getInstanceFor(connection);
        for (RosterEntry entry : roster.getEntries()) {
            contacts.add(entry.getJid().toString());
        }
        return contacts;
    }

    /**
     * Establece la presencia del usuario conectado.
     * 
     * @param statusMessage el mensaje de estado para la presencia
     * @param selectedPresence el tipo de presencia (Available, Away, Busy, Chat, Offline)
     * @throws SmackException.NotConnectedException si la conexión no está activa
     * @throws InterruptedException si la operación es interrumpida
     */
    public void setPresence(String statusMessage, String selectedPresence) throws SmackException.NotConnectedException, InterruptedException {
        Presence presence = null;

        switch (selectedPresence) {
            case "Available":
                presence = PresenceBuilder.buildPresence().ofType(Presence.Type.available)
                            .setStatus(statusMessage)
                            .build();
                break;
            case "Away":
                presence = PresenceBuilder.buildPresence().ofType(Presence.Type.available)
                            .setMode(Presence.Mode.away)
                            .setStatus(statusMessage)
                            .build();
                break;
            case "Busy":
                presence = PresenceBuilder.buildPresence().ofType(Presence.Type.available)
                            .setMode(Presence.Mode.dnd)
                            .setStatus(statusMessage)
                            .build();
                break;
            case "Chat":
                presence = PresenceBuilder.buildPresence().ofType(Presence.Type.available)
                            .setMode(Presence.Mode.chat)
                            .setStatus(statusMessage)
                            .build();
                break;
            case "Offline":
                presence = PresenceBuilder.buildPresence().ofType(Presence.Type.unavailable)
                            .setStatus(statusMessage)
                            .build();
                break;
            default:
                throw new IllegalArgumentException("Unknown presence status: " + selectedPresence);
        }
    
        connection.sendStanza(presence);
    }

    /**
     * Desconecta la conexión XMPP activa.
     * 
     * @throws SmackException.NotConnectedException si la conexión no está activa
     */
    public void disconnect() throws SmackException.NotConnectedException {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    /**
     * Verifica si la conexión XMPP está activa.
     * 
     * @return true si la conexión está activa, false en caso contrario
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     * Añade un contacto a la lista de contactos.
     * 
     * @param jid el JID del nuevo contacto
     * @param name el nombre del nuevo contacto
     * @throws XmppStringprepException si hay un error en la preparación del JID
     * @throws SmackException si ocurre un error en la biblioteca Smack
     * @throws InterruptedException si la operación es interrumpida
     * @throws XMPPException si ocurre un error en la conexión XMPP
     */
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

    /**
     * Muestra la lista de contactos en la consola.
     * 
     * @throws SmackException.NotLoggedInException si no está conectado
     * @throws SmackException.NotConnectedException si la conexión no está activa
     * @throws InterruptedException si la operación es interrumpida
     */
    public void showContacts() throws SmackException.NotLoggedInException, SmackException.NotConnectedException, InterruptedException {
        Roster roster = Roster.getInstanceFor(connection);
        for (RosterEntry entry : roster.getEntries()) {
            Presence presence = roster.getPresence(entry.getJid());
            System.out.println(entry.getName() + " (" + entry.getJid() + "): " + presence.getType());
        }
    }

    /**
     * Muestra los detalles de un contacto específico en la consola.
     * 
     * @param jid el JID del contacto
     * @throws XmppStringprepException si hay un error en la preparación del JID
     * @throws SmackException.NotLoggedInException si no está conectado
     * @throws SmackException.NotConnectedException si la conexión no está activa
     * @throws InterruptedException si la operación es interrumpida
     */
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

    /**
     * Se une a un chat grupal con el nombre y apodo proporcionados.
     * 
     * @param roomName el nombre del chat grupal
     * @param nickname el apodo a usar en el chat grupal
     * @throws XmppStringprepException si hay un error en la preparación del JID
     * @throws SmackException si ocurre un error en la biblioteca Smack
     * @throws IOException si ocurre un error de entrada/salida
     * @throws InterruptedException si la operación es interrumpida
     * @throws XMPPException si ocurre un error en la conexión XMPP
     */
    public void joinGroupChat(String roomName, String nickname) throws XmppStringprepException, SmackException, IOException, InterruptedException, XMPPException {
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
        EntityBareJid mucJid = JidCreate.entityBareFrom(roomName + "@conference.alumchat.lol");
        MultiUserChat muc = manager.getMultiUserChat(mucJid);
        muc.join(Resourcepart.from(nickname));
        System.out.println("Joined group chat: " + roomName);
    }

    /**
     * Establece la presencia del usuario conectado.
     * 
     * @param type el tipo de presencia
     * @param status el mensaje de estado
     * @throws SmackException.NotConnectedException si la conexión no está activa
     * @throws InterruptedException si la operación es interrumpida
     */
    public void setPresence(Presence.Type type, String status) throws SmackException.NotConnectedException, InterruptedException {
        Presence presence = PresenceBuilder.buildPresence().ofType(type).setStatus(status).build();
        connection.sendStanza(presence);
        System.out.println("Presence set to: " + type + " (" + status + ")");
    }

    /**
     * Configura la recepción de confirmaciones de entrega de mensajes.
     */
    public void setupDeliveryReceipts() {
        DeliveryReceiptManager receiptManager = DeliveryReceiptManager.getInstanceFor(connection);
        receiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        receiptManager.addReceiptReceivedListener((fromJid, toJid, receiptId, stanza) -> {
            System.out.println("Message delivered to " + fromJid);
        });
    }

    /**
     * Envía un archivo codificado en Base64 a un destinatario.
     * 
     * @param jidDestino el JID del destinatario
     * @param archivo el archivo a enviar
     * @throws Exception si ocurre un error al leer el archivo o al enviar el mensaje
     */
    public void enviarArchivoBase64(String jidDestino, File archivo) throws Exception {
        // Leer el archivo y codificarlo en Base64
        byte[] bytesArchivo = Files.readAllBytes(archivo.toPath());
        String archivoCodificado = Base64.getEncoder().encodeToString(bytesArchivo);
    
        // Determinar el tipo MIME del archivo (puedes usar una librería más sofisticada para MIME si lo necesitas)
        String mimeType = Files.probeContentType(archivo.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream"; // Si no se puede determinar, usar un tipo por defecto
        }
    
        // Crear el enlace data URI
        String dataUri = "data:" + mimeType + ";base64," + archivoCodificado;
    
        // Crear el mensaje XMPP y adjuntar el enlace
        MessageBuilder mensajeBuilder = StanzaBuilder.buildMessage()
                .to(JidCreate.entityBareFrom(jidDestino))
                .ofType(Message.Type.chat) // Especifica que es un mensaje de chat
                .setBody("Archivo: " + dataUri)
                .addSubject("file-transfer", archivo.getName()); // Para identificar que es una transferencia de archivo
    
        // Construir y enviar el mensaje
        getConnection().sendStanza(mensajeBuilder.build());
    }
        
  }