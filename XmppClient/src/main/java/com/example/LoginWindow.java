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
 * Clase LoginWindow que extiende Application y proporciona una interfaz gráfica para que los usuarios inicien sesión en la aplicación XMPP.
 */
public class LoginWindow extends Application {

    private XmppClient xmppClient;

    /**
     * Método principal que lanza la aplicación JavaFX.
     *
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Método de inicio que configura la ventana de inicio de sesión y los elementos de la interfaz gráfica de usuario (GUI).
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

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // Crear un HBox para centrar los botones
        HBox buttonBox = new HBox(10); // Espaciado de 10 píxeles entre los botones
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerButton, loginButton);

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

    /**
     * Valida las credenciales de inicio de sesión intentando conectar con el servidor XMPP.
     *
     * @param username El nombre de usuario ingresado.
     * @param password La contraseña ingresada.
     * @return true si las credenciales son válidas y la conexión se establece correctamente; false en caso contrario.
     */
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

    /**
     * Lanza la aplicación de chat XMPP y cierra la ventana de inicio de sesión.
     *
     * @param primaryStage La ventana principal de la aplicación.
     * @param username El nombre de usuario ingresado.
     * @param password La contraseña ingresada.
     */
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
}
