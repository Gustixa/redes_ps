package com.example;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
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
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

/**
 * XmppClient proporciona métodos para interactuar con un servidor XMPP.
 * Incluye funcionalidades como conectar, enviar mensajes, gestionar contactos,
 * y manejar la presencia y transferencias de archivos.
 */
public class XmppClient {

    private XMPPTCPConnection connection;

    /**
     * Conecta al servidor XMPP utilizando el nombre de usuario y contraseña proporcionados.
     *
     * @param username El nombre de usuario para conectarse.
     * @param password La contraseña para el nombre de usuario.
     * @throws XmppStringprepException Si hay un problema con el JID de XMPP.
     * @throws XMPPException Si ocurre un error genérico de XMPP.
     * @throws SmackException Si ocurre un error con las operaciones de Smack.
     * @throws IOException Si ocurre un error de E/S durante la conexión.
     * @throws InterruptedException Si la conexión es interrumpida.
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

        System.out.println("Conectado como: " + connection.getUser());
    }

    /**
     * Registra una nueva cuenta en el servidor XMPP con el nombre de usuario y contraseña proporcionados.
     *
     * @param username El nombre de usuario para registrar.
     * @param password La contraseña para la nueva cuenta.
     * @throws XmppStringprepException Si hay un problema con el JID de XMPP.
     * @throws SmackException Si ocurre un error con las operaciones de Smack.
     * @throws IOException Si ocurre un error de E/S durante el registro.
     * @throws InterruptedException Si el registro es interrumpido.
     * @throws XMPPException Si ocurre un error genérico de XMPP.
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
     * Devuelve la conexión XMPP actual.
     *
     * @return La conexión XMPPTCPConnection actual.
     */
    public XMPPTCPConnection getConnection() {
        return connection;
    }

    /**
     * Elimina la cuenta actual conectada en el servidor XMPP.
     *
     * @throws SmackException.NotLoggedInException Si no se ha iniciado sesión.
     * @throws SmackException.NoResponseException Si no hay respuesta del servidor.
     * @throws XMPPException.XMPPErrorException Si ocurre un error de XMPP.
     * @throws SmackException.NotConnectedException Si no hay conexión.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    public void deleteAccount() throws SmackException.NotLoggedInException, SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.deleteAccount();
        System.out.println("Cuenta eliminada.");
    }

    /**
     * Envía un mensaje al JID proporcionado.
     *
     * @param toJid El JID del destinatario.
     * @param message El mensaje a enviar.
     * @throws XmppStringprepException Si hay un problema con el JID de XMPP.
     * @throws SmackException Si ocurre un error con las operaciones de Smack.
     * @throws InterruptedException Si el envío es interrumpido.
     * @throws NotConnectedException Si no hay conexión al servidor.
     * @throws IOException Si ocurre un error de E/S durante el envío.
     */
    public void sendMessage(String toJid, String message) throws XmppStringprepException, SmackException, InterruptedException, NotConnectedException, IOException {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(toJid);
        chatManager.chatWith(jid).send(message);
        System.out.println("Mensaje enviado a " + toJid + ": " + message);
    }

    /**
     * Agrega un listener para los mensajes entrantes.
     *
     * @param listener El listener para manejar mensajes entrantes.
     */
    public void addIncomingMessageListener(IncomingChatMessageListener listener) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(listener);
    }

    /**
     * Obtiene la lista de contactos del roster.
     *
     * @return Una lista de contactos como cadenas de texto.
     * @throws Exception Si ocurre un error al obtener los contactos.
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
     * Establece la presencia del usuario en el servidor XMPP.
     *
     * @param statusMessage El mensaje de estado de la presencia.
     * @param mode El modo de presencia (disponible, ausente, etc.).
     * @throws SmackException.NotConnectedException Si no hay conexión al servidor.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    public void setPresence(String statusMessage, Mode mode) throws SmackException.NotConnectedException, InterruptedException {
        Presence presence = PresenceBuilder.buildPresence()
                .ofType(Type.available)
                .setMode(mode)
                .setStatus(statusMessage)
                .build();

        connection.sendStanza(presence);
    }

    /**
     * Desconecta del servidor XMPP.
     *
     * @throws SmackException.NotConnectedException Si no hay conexión al servidor.
     */
    public void disconnect() throws SmackException.NotConnectedException {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    /**
     * Verifica si hay una conexión activa al servidor XMPP.
     *
     * @return true si está conectado, false en caso contrario.
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     * Agrega un contacto al roster y envía una solicitud de suscripción si el contacto no existe ya en la lista.
     *
     * @param jid El JID del contacto a agregar.
     * @param name El nombre del contacto (actualmente no utilizado en este método).
     * @throws XmppStringprepException Si hay un problema con el formato del JID.
     * @throws SmackException.NotLoggedInException Si no se ha iniciado sesión.
     * @throws SmackException.NoResponseException Si no hay respuesta del servidor.
     * @throws XMPPException.XMPPErrorException Si ocurre un error de XMPP.
     * @throws SmackException.NotConnectedException Si no hay conexión al servidor.
     * @throws InterruptedException Si la operación es interrumpida.
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
     * Muestra la lista de contactos en el roster junto con su estado de presencia.
     *
     * @throws SmackException.NotLoggedInException Si no se ha iniciado sesión.
     * @throws SmackException.NotConnectedException Si no hay conexión al servidor.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    public void showContacts() throws SmackException.NotLoggedInException, SmackException.NotConnectedException, InterruptedException {
        Roster roster = Roster.getInstanceFor(connection);
        for (RosterEntry entry : roster.getEntries()) {
            Presence presence = roster.getPresence(entry.getJid());
            System.out.println(entry.getName() + " (" + entry.getJid() + "): " + presence.getType());
        }
    }

    /**
     * Muestra los detalles de un contacto específico en el roster.
     *
     * @param jid El JID del contacto del que se desea ver los detalles.
     * @throws XmppStringprepException Si hay un problema con el formato del JID.
     * @throws SmackException.NotLoggedInException Si no se ha iniciado sesión.
     * @throws SmackException.NotConnectedException Si no hay conexión al servidor.
     * @throws InterruptedException Si la operación es interrumpida.
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
     * Se une a un chat grupal (MultiUserChat) con el nombre de sala y apodo proporcionados.
     *
     * @param roomName El nombre de la sala de chat.
     * @param nickname El apodo que se utilizará en la sala.
     * @throws XmppStringprepException Si hay un problema con el formato del JID.
     * @throws SmackException Si ocurre un error con las operaciones de Smack.
     * @throws IOException Si ocurre un error de E/S durante la operación.
     * @throws InterruptedException Si la operación es interrumpida.
     * @throws XMPPException Si ocurre un error genérico de XMPP.
     */
    public void joinGroupChat(String roomName, String nickname) throws XmppStringprepException, SmackException, IOException, InterruptedException, XMPPException {
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
        EntityBareJid mucJid = JidCreate.entityBareFrom(roomName + "@conference.alumchat.lol");
        MultiUserChat muc = manager.getMultiUserChat(mucJid);
        muc.join(Resourcepart.from(nickname));
        System.out.println("Joined group chat: " + roomName);
    }

    /**
     * Establece la presencia del usuario en el servidor XMPP con el tipo y estado proporcionados.
     *
     * @param type El tipo de presencia (available, unavailable, etc.).
     * @param status El mensaje de estado de la presencia.
     * @throws SmackException.NotConnectedException Si no hay conexión al servidor.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    public void setPresence(Presence.Type type, String status) throws SmackException.NotConnectedException, InterruptedException {
        Presence presence = PresenceBuilder.buildPresence().ofType(type).setStatus(status).build();
        connection.sendStanza(presence);
        System.out.println("Presence set to: " + type + " (" + status + ")");
    }

    /**
     * Configura la recepción automática de acuses de recibo de entrega de mensajes.
     */
    public void setupDeliveryReceipts() {
        DeliveryReceiptManager receiptManager = DeliveryReceiptManager.getInstanceFor(connection);
        receiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        receiptManager.addReceiptReceivedListener((fromJid, toJid, receiptId, stanza) -> {
            System.out.println("Message delivered to " + fromJid);
        });
    }

    /**
     * Envía un archivo al JID especificado.
     *
     * @param toJid El JID del destinatario.
     * @param file El archivo a enviar.
     * @throws XmppStringprepException Si hay un problema con el formato del JID.
     * @throws SmackException Si ocurre un error con las operaciones de Smack.
     * @throws InterruptedException Si el envío es interrumpido.
     * @throws IOException Si ocurre un error de E/S durante el envío.
     */
    public void sendFile(String toJid, File file) throws XmppStringprepException, SmackException, InterruptedException, IOException {
        FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(JidCreate.entityFullFrom(toJid + "/Smack"));
        transfer.sendFile(file, "Sending file");
        System.out.println("File sent to " + toJid);
    }

    /**
     * Obtiene el estado de presencia de un contacto dado su JID.
     *
     * @param jid El JID del contacto.
     * @return El estado de presencia del contacto como una cadena de texto.
     * @throws XmppStringprepException Si hay un problema con el formato del JID.
     */
    public String getContactPresence(String jid) throws XmppStringprepException {
        Roster roster = Roster.getInstanceFor(connection);
        BareJid bareJid = JidCreate.bareFrom(jid);
        Presence presence = roster.getPresence(bareJid);

        if (presence != null) {
            // Retorna el tipo de presencia (available, unavailable, etc.) y el mensaje de estado.
            return presence.getType() + " (" + presence.getStatus() + ")";
        } else {
            return "Contact not found or no presence information available.";
        }
    }
}
