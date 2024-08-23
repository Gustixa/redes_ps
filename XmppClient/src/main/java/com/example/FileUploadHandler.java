package com.example;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.Base64.InputStream;
import org.jxmpp.jid.Jid;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class FileUploadHandler {

    private final XMPPConnection xmppConnection;

    public FileUploadHandler(XMPPConnection connection) {
        this.xmppConnection = connection;
    }

    public void handleUpload(File file, Jid toJid) throws Exception {
        // Supongamos que tienes un servidor HTTP donde puedes subir archivos y obtener un enlace de descarga.
        URL uploadUrl = new URL("https://alumchat.lol:7443/httpfileupload"); // Reemplaza con la URL de tu servidor de subida

        // Subir el archivo usando el URL proporcionado
        String downloadLink = uploadFile(file, uploadUrl);

        // Enviar el URL de descarga al destinatario a través de un mensaje XMPP
        sendFileLink(toJid, downloadLink);
    }

    private String uploadFile(File file, URL uploadUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", Files.probeContentType(file.toPath()));
        connection.setRequestProperty("Content-Length", String.valueOf(file.length()));

        try (FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = connection.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            throw new IOException("Failed to upload file: " + responseCode);
        }

        // Leer la respuesta del servidor, que debería ser el enlace de descarga
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();  // La respuesta se espera que sea un String (el enlace de descarga)
        }
    }

    private void sendFileLink(Jid toJid, String downloadLink) throws NotConnectedException, InterruptedException {
        Message message = new Message();
        message.setTo(toJid);
        message.setBody(downloadLink);
        xmppConnection.sendStanza(message);
    }
}




