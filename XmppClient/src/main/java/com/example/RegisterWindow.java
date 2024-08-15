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

public class RegisterWindow extends Application {

    private XmppClient xmppClient;

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

        // Configurar el layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(10);

        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(registerButton, 1, 2);
        gridPane.add(backButton, 0, 2);

        // Configurar evento del botón de registro
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            try {
                // Intentar registrar una nueva cuenta en el servidor XMPP
                xmppClient.connect("", "");  // Conectar sin credenciales para acceder al AccountManager
                xmppClient.registerAccount(username, password);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Account registered successfully.");
                alert.showAndWait();
                primaryStage.close();  // Cerrar la ventana de registro después de registrar
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

    public static void main(String[] args) {
        launch(args);
    }
}
