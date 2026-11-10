
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

public class BloodRequestScreen {

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("LifeLink - Blood Request");

        Label title = new Label("Request Blood");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Your registered SEEKER email");

        ComboBox<String> bloodGroupBox = new ComboBox<>();
        bloodGroupBox.getItems().addAll(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        bloodGroupBox.setPromptText("Required Blood Group");
        bloodGroupBox.setMaxWidth(Double.MAX_VALUE);

        TextField hospitalField = new TextField();
        hospitalField.setPromptText("Hospital Name");

        TextField cityField = new TextField();
        cityField.setPromptText("City");

        TextField unitsField = new TextField();
        unitsField.setPromptText("Number of Units Needed");

        ComboBox<String> urgencyBox = new ComboBox<>();
        urgencyBox.getItems().addAll("NORMAL", "URGENT", "EMERGENCY");
        urgencyBox.setValue("NORMAL");
        urgencyBox.setMaxWidth(Double.MAX_VALUE);

        Button submitButton = new Button("Submit Blood Request");
        submitButton.setMaxWidth(Double.MAX_VALUE);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        submitButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            String bloodGroup = bloodGroupBox.getValue();
            String hospital = hospitalField.getText().trim();
            String city = cityField.getText().trim();
            String unitsText = unitsField.getText().trim();
            String urgency = urgencyBox.getValue();

            if (email.isEmpty() || bloodGroup == null
                    || hospital.isEmpty() || city.isEmpty()
                    || unitsText.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }

            int units;

            try {
                units = Integer.parseInt(unitsText);

                if (units < 1) {
                    messageLabel.setText(
                            "Number of units must be at least 1."
                    );
                    return;
                }
            } catch (NumberFormatException e) {
                messageLabel.setText(
                        "Please enter a valid number of units."
                );
                return;
            }

            String userSql = """
                    SELECT user_id
                    FROM users
                    WHERE email = ? AND role = 'SEEKER'
                    """;

            String requestSql = """
                    INSERT INTO blood_requests
                    (seeker_id, blood_group, hospital_name, city,
                     units_needed, urgency)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (Connection connection = DBConnection.getConnection()) {

                int seekerId;

                try (PreparedStatement statement =
                             connection.prepareStatement(userSql)) {
                    statement.setString(1, email);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            messageLabel.setText(
                                    "No SEEKER account found for this email."
                            );
                            return;
                        }

                        seekerId = result.getInt("user_id");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(requestSql)) {

                    statement.setInt(1, seekerId);
                    statement.setString(2, bloodGroup);
                    statement.setString(3, hospital);
                    statement.setString(4, city);
                    statement.setInt(5, units);
                    statement.setString(6, urgency);

                    statement.executeUpdate();
                }

                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(
                        "Blood request submitted successfully!"
                );

                bloodGroupBox.setValue(null);
                hospitalField.clear();
                cityField.clear();
                unitsField.clear();
                urgencyBox.setValue("NORMAL");

            } catch (SQLException e) {
                messageLabel.setText(
                        "Could not submit request. Please try again."
                );
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(
                12,
                title,
                new Label("Registered Email"),
                emailField,
                new Label("Blood Group Required"),
                bloodGroupBox,
                new Label("Hospital"),
                hospitalField,
                new Label("City"),
                cityField,
                new Label("Units Needed"),
                unitsField,
                new Label("Urgency"),
                urgencyBox,
                submitButton,
                messageLabel
        );

        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(420);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        stage.setScene(new Scene(scrollPane, 460, 650));
        stage.show();
    }
}

