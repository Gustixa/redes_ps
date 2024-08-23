// package com.example;

// import javafx.fxml.FXML;
// import javafx.scene.control.ListView;
// import javafx.stage.FileChooser;
// import org.jxmpp.jid.Jid;

// import java.io.File;

// public class ChatController {

//     private XMPPConnection xmppConnection;
//     private FileUploadHandler fileUploadHandler;

//     @FXML
//     private ListView<String> messageList;

//     private Jid selectedContactJid;

//     public void initialize() {
//         // Configurar la conexión XMPP y el manejador de subida de archivos
//         xmppConnection = createXMPPConnection();
//         fileUploadHandler = new FileUploadHandler(xmppConnection);
//     }

//     @FXML
//     private void handleFileChange() {
//         FileChooser fileChooser = new FileChooser();
//         File selectedFile = fileChooser.showOpenDialog(null);

//         if (selectedFile != null && selectedContactJid != null) {
//             try {
//                 fileUploadHandler.handleUpload(selectedFile, selectedContactJid);
//                 messageList.getItems().add("File uploaded: " + selectedFile.getName());
//             } catch (Exception e) {
//                 e.printStackTrace();
//                 messageList.getItems().add("Failed to upload file: " + selectedFile.getName());
//             }
//         }
//     }

//     private XMPPConnection createXMPPConnection() {
//         // Implementar la conexión XMPP aquí
//         return null;
//     }

//     public void setSelectedContactJid(Jid jid) {
//         this.selectedContactJid = jid;
//     }
// }
