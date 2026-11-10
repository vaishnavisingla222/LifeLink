
package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginScreen {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("LifeLink - Login");

        Label title = new Label("Welcome Back to LifeLink");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        loginButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter your email and password.");
                return;
            }

            String sql = """
                    SELECT user_id, full_name, password_hash, role
                    FROM users
                    WHERE email = ?
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, email);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        String storedHash = result.getString("password_hash");

                        if (PasswordUtil.verifyPassword(password, storedHash)) {

                            int userId = result.getInt("user_id");
                            String name = result.getString("full_name");
                            String role = result.getString("role");

                            UserSession.login(
                                    new UserSession(userId, name, email, role)
                            );

                            messageLabel.setStyle("-fx-text-fill: green;");
                            messageLabel.setText(
                                    "Login successful! Welcome, " + name
                            );

                            stage.close();

                            new DashboardScreen().show();

                            // Dashboard navigation will be added next.
                        } else {
                            messageLabel.setStyle("-fx-text-fill: red;");
                            messageLabel.setText("Incorrect email or password.");
                        }
                    } else {
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("Incorrect email or password.");
                    }
                }

            } catch (SQLException | java.security.GeneralSecurityException e) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Login failed. Please try again.");
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(
                12,
                title,
                emailField,
                passwordField,
                loginButton,
                messageLabel
        );

        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(400);

        Scene scene = new Scene(layout, 420, 300);
        stage.setScene(scene);
        stage.show();
    }
}
