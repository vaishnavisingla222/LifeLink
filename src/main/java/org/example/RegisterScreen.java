
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

public class RegisterScreen {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("LifeLink - Register");

        Label title = new Label("Create Your LifeLink Account");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (minimum 8 characters)");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("SEEKER", "DONOR");
        roleBox.setValue("SEEKER");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Label bloodLabel = new Label("Blood Group");

        ComboBox<String> bloodGroupBox = new ComboBox<>();
        bloodGroupBox.getItems().addAll(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        bloodGroupBox.setPromptText("Select Blood Group");
        bloodGroupBox.setMaxWidth(Double.MAX_VALUE);

        TextField cityField = new TextField();
        cityField.setPromptText("City");

        TextField areaField = new TextField();
        areaField.setPromptText("Area / Locality");

        TextField pincodeField = new TextField();
        pincodeField.setPromptText("Pincode");

        CheckBox availableBox = new CheckBox("Available to donate blood");
        availableBox.setSelected(true);

        Label bloodDetailsLabel = new Label("Donor Details");
        bloodDetailsLabel.setStyle("-fx-font-weight: bold;");

        VBox donorFields = new VBox(
                10,
                bloodDetailsLabel,
                bloodLabel,
                bloodGroupBox,
                cityField,
                areaField,
                pincodeField,
                availableBox
        );

        donorFields.setVisible(false);
        donorFields.setManaged(false);

        roleBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            boolean isDonor = "DONOR".equals(newValue);
            donorFields.setVisible(isDonor);
            donorFields.setManaged(isDonor);
        });

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        registerButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String role = roleBox.getValue();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please fill in all required fields.");
                return;
            }

            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                messageLabel.setText("Please enter a valid email address.");
                return;
            }

            if (password.length() < 8) {
                messageLabel.setText("Password must be at least 8 characters.");
                return;
            }

            String bloodGroup = bloodGroupBox.getValue();
            String city = cityField.getText().trim();
            String area = areaField.getText().trim();
            String pincode = pincodeField.getText().trim();

            if ("DONOR".equals(role)) {
                if (bloodGroup == null || city.isEmpty()) {
                    messageLabel.setText(
                            "Donors must select a blood group and enter a city."
                    );
                    return;
                }

                if (!pincode.isEmpty() && !pincode.matches("\\d{6}")) {
                    messageLabel.setText(
                            "Pincode must contain exactly 6 digits."
                    );
                    return;
                }
            }

            String userSql = """
                    INSERT INTO users (full_name, email, password_hash, role)
                    VALUES (?, ?, ?, ?)
                    """;

            String donorSql = """
                    INSERT INTO donors
                    (user_id, blood_group, city, area, pincode, available)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try {
                String hashedPassword = PasswordUtil.hashPassword(password);

                try (Connection connection = DBConnection.getConnection()) {
                    connection.setAutoCommit(false);

                    try {
                        int userId;

                        try (PreparedStatement statement =
                                     connection.prepareStatement(
                                             userSql,
                                             java.sql.Statement.RETURN_GENERATED_KEYS
                                     )) {

                            statement.setString(1, name);
                            statement.setString(2, email);
                            statement.setString(3, hashedPassword);
                            statement.setString(4, role);
                            statement.executeUpdate();

                            try (ResultSet keys = statement.getGeneratedKeys()) {
                                if (!keys.next()) {
                                    throw new SQLException(
                                            "Could not retrieve the new user ID."
                                    );
                                }
                                userId = keys.getInt(1);
                            }
                        }

                        if ("DONOR".equals(role)) {
                            try (PreparedStatement statement =
                                         connection.prepareStatement(donorSql)) {

                                statement.setInt(1, userId);
                                statement.setString(2, bloodGroup);
                                statement.setString(3, city);
                                statement.setString(4,
                                        area.isEmpty() ? null : area);
                                statement.setString(5,
                                        pincode.isEmpty() ? null : pincode);
                                statement.setBoolean(6,
                                        availableBox.isSelected());

                                statement.executeUpdate();
                            }
                        }

                        connection.commit();

                    } catch (Exception e) {
                        connection.rollback();
                        throw e;
                    } finally {
                        connection.setAutoCommit(true);
                    }
                }

                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(
                        "Registration successful! You can now log in."
                );

                nameField.clear();
                emailField.clear();
                passwordField.clear();
                bloodGroupBox.setValue(null);
                cityField.clear();
                areaField.clear();
                pincodeField.clear();
                availableBox.setSelected(true);
                roleBox.setValue("SEEKER");

            } catch (java.security.GeneralSecurityException e) {
                messageLabel.setText("Could not secure the password.");
                e.printStackTrace();

            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    messageLabel.setText("This email is already registered.");
                } else {
                    messageLabel.setText(
                            "Database error: " + e.getMessage()
                    );
                    e.printStackTrace();
                }
            } catch (Exception e) {
                messageLabel.setText("Registration failed. Please try again.");
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(
                12,
                title,
                nameField,
                emailField,
                passwordField,
                new Label("Account Type"),
                roleBox,
                donorFields,
                registerButton,
                messageLabel
        );

        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(400);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 440, 600);
        stage.setScene(scene);
        stage.show();
    }
}
