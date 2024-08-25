package com.example;

import org.aspectj.weaver.patterns.ConcreteCflowPointcut.Slot;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.ChatManager;  // Mantener esta importación
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
// import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import java.util.Base64;


public class XmppClient {

    private XMPPTCPConnection connection;
    private final Map<String, List<String>> messageHistory = new HashMap<>(); // Mapa para guardar el historial de mensajes
    private FileUploadHandler fileUploadHandler; // Instancia de FileUploadHandler

    public XmppClient(){
        disableCertificateValidation();
    }
    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    public Roster getRoster() {
        return Roster.getInstanceFor(connection);
    }


    public void connect(String username, String password) throws XmppStringprepException, XMPPException, SmackException, IOException, InterruptedException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain("alumchat.lol")
                .setHost("alumchat.lol")
                .setUsernameAndPassword(username, password)
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled) // Desactivar TLS/SSL
                .build();

        connection = new XMPPTCPConnection(config);
        connection.connect().login();

        fileUploadHandler = new FileUploadHandler(connection); // Inicializar la instancia de FileUploadHandler

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

    public void sendFile(File file, String toJid) throws Exception {
        FileUploadIQ fileUploadIQ = new FileUploadIQ(file.getName(), file.length(), "application/octet-stream");
        fileUploadIQ.setTo(JidCreate.from("httpfileupload.alumchat.lol"));
    
        // Enviar el IQ
        connection.sendStanza(fileUploadIQ);
    
        // Implementar lógica para manejar la respuesta del servidor aquí
        System.out.println("Request sent for file upload: " + file.getName());
    
        // Esperar y manejar la respuesta del servidor
        connection.addAsyncStanzaListener(stanza -> {
            if (stanza instanceof IQ) {
                IQ response = (IQ) stanza;
                if (response.getType() == IQ.Type.result) {
                    try {
                        // Obtener la URL del archivo desde la respuesta y subir el archivo
                        String uploadUrl = extractUploadUrlFromResponse(response);
                        uploadFileToServer(file, uploadUrl);
    
                        // Enviar la URL del archivo al receptor
                        sendMessage(toJid, "File uploaded: " + uploadUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error during file upload: " + e.getMessage());
                    }
                } else {
                    System.out.println("File upload request failed.");
                }
            }
        }, stanza -> stanza.hasExtension("request", "urn:xmpp:http:upload:0"));
    
        // Llamar al método handleUpload del FileUploadHandler para subir el archivo
        fileUploadHandler.handleUpload(file, JidCreate.from(toJid));
        System.out.println("File sent to " + toJid + ": " + file.getName());
    }
    
    
    private void uploadFileToServer(File file, String uploadUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
    
        try (OutputStream outputStream = connection.getOutputStream()) {
            Files.copy(file.toPath(), outputStream);
        }
    
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            System.out.println("File uploaded successfully: " + uploadUrl);
        } else {
            System.out.println("File upload failed with response code: " + responseCode);
        }
    }
    

    private String extractUploadUrlFromResponse(IQ response) {
        String uploadUrl = null;
        // Convertir la respuesta en un objeto XML y buscar la URL
        try {
            String responseXML = response.toXML().toString();
            // Parsear la respuesta como un documento XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(responseXML)));
    
            // Buscar el nodo "put" que contiene la URL
            NodeList nodeList = document.getElementsByTagName("put");
            if (nodeList.getLength() > 0) {
                Element putElement = (Element) nodeList.item(0);
                uploadUrl = putElement.getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return uploadUrl;
    }

    public static void disableCertificateValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
    
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarArchivoBase64(String jidDestino, File archivo) throws Exception {
        // Leer el archivo y codificarlo en Base64
        byte[] bytesArchivo = Files.readAllBytes(archivo.toPath());
        String archivoCodificado = Base64.getEncoder().encodeToString(bytesArchivo);

        // Crear el mensaje XMPP y adjuntar el archivo codificado
        Message mensaje = new Message();
        mensaje.setTo(JidCreate.entityBareFrom(jidDestino));
        mensaje.setBody(archivoCodificado);
        mensaje.addSubject("file-transfer", archivo.getName()); // Para identificar que es una transferencia de archivo

        // Enviar el mensaje
        getConnection().sendStanza(mensaje);
    }
    
  }