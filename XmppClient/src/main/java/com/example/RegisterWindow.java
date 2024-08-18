package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Clase RegisterWindow que extiende Application y proporciona una interfaz gráfica para que los usuarios registren una nueva cuenta en la aplicación XMPP.
 */
public class RegisterWindow extends Application {

    private XmppClient xmppClient;

    /**
     * Método principal que lanza la aplicación de registro JavaFX.
     *
     * @param primaryStage La ventana principal de la aplicación.
     */
    @Override
    public void start(Stage primaryStage) {
        xmppClient = new XmppClient();

        // Crear los elementos de la interfaz
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back");

        // Crear un HBox para centrar los botones
        HBox buttonBox = new HBox(10); // Espaciado de 10 píxeles entre los botones
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(backButton, registerButton);

        // Configurar el layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(10);

        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(buttonBox, 1, 2); // Añadir el HBox con los botones centrados

        // Configurar evento del botón de registro
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            System.out.println(username + " : " + password);

            try {
                // Intentar registrar una nueva cuenta en el servidor XMPP
                xmppClient.connect("arg211024-uvg", "211024");  // Conectar sin credenciales para acceder al AccountManager
                xmppClient.registerAccount(username, password);
                 // Desconectar correctamente
                // xmppClient.disconnect();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Account registered successfully.");
                alert.showAndWait();
                primaryStage.close();  // Cerrar la ventana de registro después de registrar
                redirigirPantallaPrincipal(username, password);
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Registration failed: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        // Configurar evento del botón de regreso
        backButton.setOnAction(e -> {
            primaryStage.close();
            LoginWindow loginWindow = new LoginWindow();
            try {
                loginWindow.start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Configurar la escena y la ventana
        Scene scene = new Scene(gridPane, 300, 200);
        primaryStage.setTitle("Register");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Metodo para redigir a la pantalla principal de conversaciones.
     * 
     * @param username, usuario actualmente registrado
     * @param password, password actualmente registrado
     */
    private void redirigirPantallaPrincipal(String username, String password){
        XmppChatApp chatApp = new XmppChatApp();
        chatApp.setCredentials(username, password);
        try{
            chatApp.start(new Stage());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
