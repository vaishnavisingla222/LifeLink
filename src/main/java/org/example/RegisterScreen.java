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

        // Common fields for all users
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (minimum 8 characters)");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField cityField = new TextField();
        cityField.setPromptText("City");

        TextField areaField = new TextField();
        areaField.setPromptText("Area / Locality");

        TextField pincodeField = new TextField();
        pincodeField.setPromptText("Pincode");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("SEEKER", "DONOR");
        roleBox.setValue("SEEKER");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        // Donor-only fields
        Label bloodLabel = new Label("Blood Group");

        ComboBox<String> bloodGroupBox = new ComboBox<>();
        bloodGroupBox.getItems().addAll(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        bloodGroupBox.setPromptText("Select Blood Group");
        bloodGroupBox.setMaxWidth(Double.MAX_VALUE);

        CheckBox availableBox = new CheckBox("Available to donate blood");
        availableBox.setSelected(true);

        Label bloodDetailsLabel = new Label("Donor Details");
        bloodDetailsLabel.setStyle("-fx-font-weight: bold;");

        VBox donorFields = new VBox(
                10,
                bloodDetailsLabel,
                bloodLabel,
                bloodGroupBox,
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
            String phone = phoneField.getText().trim();
            String city = cityField.getText().trim();
            String area = areaField.getText().trim();
            String pincode = pincodeField.getText().trim();
            String role = roleBox.getValue();

            // Validate common fields
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()
                    || phone.isEmpty() || city.isEmpty()
                    || area.isEmpty() || pincode.isEmpty()) {

                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Please fill in all required fields.");
                return;
            }

            // Validate email
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Please enter a valid email address.");
                return;
            }

            // Validate password
            if (password.length() < 8) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(
                        "Password must be at least 8 characters."
                );
                return;
            }

            // Validate phone number
            if (!phone.matches("\\d{10}")) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(
                        "Phone number must contain exactly 10 digits."
                );
                return;
            }

            // Validate pincode
            if (!pincode.matches("\\d{6}")) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(
                        "Pincode must contain exactly 6 digits."
                );
                return;
            }

            String bloodGroup = bloodGroupBox.getValue();

            // Donor-specific validation
            if ("DONOR".equals(role)) {
                if (bloodGroup == null) {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText(
                            "Please select your blood group."
                    );
                    return;
                }
            }

            // Insert common user information
            String userSql = """
                    INSERT INTO users
                    (full_name, email, password_hash, role,
                     phone, city, area, pincode)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            // Insert donor-specific information
            String donorSql = """
                    INSERT INTO donors
                    (user_id, blood_group, available)
                    VALUES (?, ?, ?)
                    """;

            try {
                String hashedPassword = PasswordUtil.hashPassword(password);

                try (Connection connection = DBConnection.getConnection()) {

                    connection.setAutoCommit(false);

                    try {
                        int userId;

                        // Insert into users table
                        try (PreparedStatement statement =
                                     connection.prepareStatement(
                                             userSql,
                                             java.sql.Statement.RETURN_GENERATED_KEYS
                                     )) {

                            statement.setString(1, name);
                            statement.setString(2, email);
                            statement.setString(3, hashedPassword);
                            statement.setString(4, role);
                            statement.setString(5, phone);
                            statement.setString(6, city);
                            statement.setString(7, area);
                            statement.setString(8, pincode);

                            statement.executeUpdate();

                            try (ResultSet keys =
                                         statement.getGeneratedKeys()) {

                                if (!keys.next()) {
                                    throw new SQLException(
                                            "Could not retrieve the new user ID."
                                    );
                                }

                                userId = keys.getInt(1);
                            }
                        }

                        // Insert donor information only for DONOR
                        if ("DONOR".equals(role)) {

                            try (PreparedStatement statement =
                                         connection.prepareStatement(donorSql)) {

                                statement.setInt(1, userId);
                                statement.setString(2, bloodGroup);
                                statement.setBoolean(
                                        3,
                                        availableBox.isSelected()
                                );

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

                // Registration success
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(
                        "Registration successful! You can now log in."
                );

                // Clear fields
                nameField.clear();
                emailField.clear();
                passwordField.clear();
                phoneField.clear();
                cityField.clear();
                areaField.clear();
                pincodeField.clear();
                bloodGroupBox.setValue(null);
                availableBox.setSelected(true);
                roleBox.setValue("SEEKER");

            } catch (java.security.GeneralSecurityException e) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(
                        "Could not secure the password."
                );
                e.printStackTrace();

            } catch (SQLException e) {
                messageLabel.setStyle("-fx-text-fill: red;");

                if (e.getErrorCode() == 1062) {
                    messageLabel.setText(
                            "This email is already registered."
                    );
                } else {
                    messageLabel.setText(
                            "Database error: " + e.getMessage()
                    );
                    e.printStackTrace();
                }

            } catch (Exception e) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(
                        "Registration failed. Please try again."
                );
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(
                12,
                title,
                nameField,
                emailField,
                passwordField,
                phoneField,
                cityField,
                areaField,
                pincodeField,
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

        Scene scene = new Scene(scrollPane, 440, 650);

        stage.setScene(scene);
        stage.show();
    }
}