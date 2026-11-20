
package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DonorDashboard {

    public void show() {

        UserSession user = UserSession.getCurrentUser();

        if (user == null || !"DONOR".equalsIgnoreCase(user.getRole())) {
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("LifeLink - Donor Dashboard");

        Label title = new Label("Be Someone's Lifeline ❤️");
        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

        Label welcome = new Label(
                "Welcome, " + user.getFullName() + " ✨"
        );
        welcome.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #333333;"
        );

        Label info = new Label(
                "Your kindness has the power to save lives.🩸 "
        );
        info.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-text-fill: #666666;"
        );

        Label availabilityLabel = new Label();
        availabilityLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );

        Button availabilityButton = new Button();
        availabilityButton.setPrefWidth(220);

        Button requestsButton =
                new Button("View Blood Requests");

        Button notificationsButton =
                new Button("My Notifications");

        Button logoutButton = new Button("Logout");

        requestsButton.setPrefWidth(220);
        notificationsButton.setPrefWidth(220);
        logoutButton.setPrefWidth(220);

        final boolean[] isAvailable = {false};

        // Load donor availability
        Runnable loadAvailability = () -> {

            String sql = """
                    SELECT available
                    FROM donors
                    WHERE user_id = ?
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, user.getUserId());

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {

                        isAvailable[0] =
                                result.getBoolean("available");

                        updateAvailabilityUI(
                                isAvailable[0],
                                availabilityLabel,
                                availabilityButton
                        );

                    } else {

                        availabilityLabel.setText(
                                "Donor profile not found."
                        );

                        availabilityButton.setDisable(true);
                    }
                }

            } catch (SQLException e) {

                availabilityLabel.setText(
                        "Could not load availability."
                );

                availabilityButton.setDisable(true);
                e.printStackTrace();
            }
        };

        // Toggle availability
        availabilityButton.setOnAction(event -> {

            boolean newStatus = !isAvailable[0];

            String sql = """
                    UPDATE donors
                    SET available = ?
                    WHERE user_id = ?
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setBoolean(1, newStatus);
                statement.setInt(2, user.getUserId());

                int updated = statement.executeUpdate();

                if (updated == 1) {

                    isAvailable[0] = newStatus;

                    updateAvailabilityUI(
                            newStatus,
                            availabilityLabel,
                            availabilityButton
                    );

                } else {

                    availabilityLabel.setText(
                            "Could not update availability."
                    );
                }

            } catch (SQLException e) {

                availabilityLabel.setText(
                        "Database update failed."
                );

                e.printStackTrace();
            }
        });

        // Open blood requests page
        requestsButton.setOnAction(event ->
                new DonorRequestsScreen().show()
        );

        // Open separate notifications page
        notificationsButton.setOnAction(event ->
                new DonorNotificationsScreen().show()
        );

        // Logout
        logoutButton.setOnAction(event -> {

            UserSession.logout();
            stage.close();
        });

        VBox layout = new VBox(
                20,
                title,
                welcome,
                info,
                availabilityLabel,
                availabilityButton,
                requestsButton,
                notificationsButton,
                logoutButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #fffafa;");

        stage.setScene(new Scene(layout, 500, 500));

        loadAvailability.run();

        stage.show();
    }

    private void updateAvailabilityUI(
            boolean available,
            Label label,
            Button button) {

        if (available) {

            label.setText("Availability: AVAILABLE");

            label.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: green;"
            );

            button.setText("Mark as Unavailable");

        } else {

            label.setText("Availability: UNAVAILABLE");

            label.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: red;"
            );

            button.setText("Mark as Available");
        }
    }
}