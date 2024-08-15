package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginWindow extends Application {

    private XmppClient xmppClient;

    @Override
    public void start(Stage primaryStage) {
        xmppClient = new XmppClient();

        // Crear los elementos de la interfaz
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // Configurar el layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(10);

        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(loginButton, 1, 2);
        gridPane.add(registerButton, 0, 2);

        // Configurar evento del botón de login
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (validateLogin(username, password)) {
                // Login exitoso: Iniciar XmppChatApp
                launchChatApp(primaryStage, username, password);
            } else {
                // Mostrar error de login y limpiar campos
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid username or password.");
                alert.showAndWait();
                usernameField.clear();
                passwordField.clear();
            }
        });

        // Configurar evento del botón de registro
        registerButton.setOnAction(e -> {
            primaryStage.close(); // Cerrar la ventana de login
            RegisterWindow registerWindow = new RegisterWindow();
            try {
                registerWindow.start(new Stage()); // Abrir la ventana de registro
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Configurar la escena y la ventana
        Scene scene = new Scene(gridPane, 300, 200);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean validateLogin(String username, String password) {
        try {
            // Intentar conectar con el servidor XMPP usando las credenciales proporcionadas
            xmppClient.connect(username, password);
            return true; // Credenciales válidas
        } catch (Exception e) {
            // Si ocurre una excepción, las credenciales no son válidas
            return false;
        }
    }

    private void launchChatApp(Stage primaryStage, String username, String password) {
        // Ocultar la ventana de login
        primaryStage.close();

        // Iniciar XmppChatApp con las credenciales proporcionadas
        XmppChatApp chatApp = new XmppChatApp();
        chatApp.setCredentials(username, password);
        try {
            chatApp.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
