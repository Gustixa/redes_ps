package com.example;

import com.formdev.flatlaf.FlatLightLaf;
import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        Application.launch(LoginWindow.class, args);
    }
}
