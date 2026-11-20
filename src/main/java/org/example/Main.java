package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Application title
        Label title = new Label("LifeLink ❤️");
        title.setStyle(
                "-fx-font-size: 38px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

// Subtitle
        Label subtitle = new Label("Connecting Hearts, Saving Lives");
        subtitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #444444;"
        );

        Label message = new Label(
                "Every drop counts. Every connection matters. " +
                        "Together, we can make a difference. 🩸"
        );
        message.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-text-fill: #666666;"
        );
        // Buttons
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        loginButton.setPrefWidth(220);
        registerButton.setPrefWidth(220);

        loginButton.setStyle(
                "-fx-background-color: #c62828;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-cursor: hand;"
        );

        registerButton.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-cursor: hand;"
        );

        // Connect buttons to their screens
        loginButton.setOnAction(
                e -> new LoginScreen().show()
        );

        registerButton.setOnAction(
                e -> new RegisterScreen().show()
        );



        // Footer
        Label footer = new Label(
                "Every donation can make a difference."
        );
        footer.setStyle(
                "-fx-text-fill: #666666;" +
                        "-fx-font-style: italic;"
        );

        // Main layout
        VBox layout = new VBox(
                18,
                title,
                subtitle,
                message,
                loginButton,
                registerButton,
                footer
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #fffafa;");

        // Application scene
        Scene scene = new Scene(layout, 650, 550);

        stage.setTitle("LifeLink - Local Blood Donor Finder");
        stage.setScene(scene);
        stage.setMinWidth(550);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}