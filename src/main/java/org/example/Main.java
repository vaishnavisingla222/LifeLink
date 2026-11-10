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
        Label title = new Label("LifeLink");
        title.setStyle(
                "-fx-font-size: 36px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

        // Subtitle
        Label subtitle = new Label("Local Blood Donor Finder");
        subtitle.setStyle("-fx-font-size: 18px;");

        Label message = new Label(
                "Connecting blood donors with people in need."
        );
        message.setStyle("-fx-font-size: 14px;");

        // Buttons
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button searchButton = new Button("Find Blood Donors");
        Button requestButton = new Button("Request Blood");
        Button historyButton = new Button("My Blood Requests");

        loginButton.setPrefWidth(220);
        registerButton.setPrefWidth(220);
        searchButton.setPrefWidth(220);
        requestButton.setPrefWidth(220);
        historyButton.setPrefWidth(220);

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

        searchButton.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-cursor: hand;"
        );

        requestButton.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-cursor: hand;"
        );
        historyButton.setStyle(
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

        searchButton.setOnAction(
                e -> new DonorSearchScreen().show()
        );

        requestButton.setOnAction(
                e -> new BloodRequestScreen().show()
        );
        historyButton.setOnAction(
                e -> new MyRequestsScreen().show()
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
                searchButton,
                requestButton,
                historyButton,
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