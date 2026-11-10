
package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DonorNotificationsScreen {

    private static class DonorNotification {

        String requestId;
        String bloodGroup;
        String hospital;
        String city;
        String responseStatus;
        String requestStatus;
        String date;
        String message;

        DonorNotification(
                String requestId,
                String bloodGroup,
                String hospital,
                String city,
                String responseStatus,
                String requestStatus,
                String date) {

            this.requestId = requestId;
            this.bloodGroup = bloodGroup;
            this.hospital = hospital;
            this.city = city;
            this.responseStatus = responseStatus;
            this.requestStatus = requestStatus;
            this.date = date;

            if ("CANCELLED".equalsIgnoreCase(requestStatus)) {

                this.message =
                        "This blood request has been cancelled by the seeker.";

            } else if ("SELECTED".equalsIgnoreCase(responseStatus)) {

                this.message =
                        "Congratulations! You have been selected by the seeker.";

            } else if ("NOT_SELECTED".equalsIgnoreCase(responseStatus)) {

                this.message =
                        "Another donor has been selected. Thank you for volunteering.";

            } else if ("INTERESTED".equalsIgnoreCase(responseStatus)) {

                this.message =
                        "Your response is recorded. Awaiting seeker's decision.";

            } else {

                this.message = "Request update available.";
            }
        }
    }

    public void show() {

        UserSession user = UserSession.getCurrentUser();

        if (user == null || !"DONOR".equalsIgnoreCase(user.getRole())) {
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("LifeLink - My Notifications");

        Label title = new Label("My Notifications");

        title.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

        Label subtitle = new Label(
                "Updates about your blood donation responses"
        );

        TableView<DonorNotification> table =
                new TableView<>();

        TableColumn<DonorNotification, String> idColumn =
                new TableColumn<>("Request ID");

        idColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().requestId));

        TableColumn<DonorNotification, String> bloodColumn =
                new TableColumn<>("Blood Group");

        bloodColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().bloodGroup));

        TableColumn<DonorNotification, String> hospitalColumn =
                new TableColumn<>("Hospital");

        hospitalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().hospital));

        TableColumn<DonorNotification, String> cityColumn =
                new TableColumn<>("City");

        cityColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().city));

        TableColumn<DonorNotification, String> responseColumn =
                new TableColumn<>("Your Response");

        responseColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().responseStatus));

        TableColumn<DonorNotification, String> statusColumn =
                new TableColumn<>("Request Status");

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().requestStatus));

        TableColumn<DonorNotification, String> dateColumn =
                new TableColumn<>("Response Date");

        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().date));

        TableColumn<DonorNotification, String> messageColumn =
                new TableColumn<>("Notification");

        messageColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().message));

        // Colour response status
        responseColumn.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(String status, boolean empty) {

                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(status);

                switch (status.toUpperCase()) {

                    case "SELECTED":
                        setStyle(
                                "-fx-text-fill: green;" +
                                        "-fx-font-weight: bold;"
                        );
                        break;

                    case "NOT_SELECTED":
                        setStyle(
                                "-fx-text-fill: #c62828;" +
                                        "-fx-font-weight: bold;"
                        );
                        break;

                    case "INTERESTED":
                        setStyle(
                                "-fx-text-fill: #1565c0;" +
                                        "-fx-font-weight: bold;"
                        );
                        break;

                    default:
                        setStyle("");
                }
            }
        });

        // Colour request status
        statusColumn.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(String status, boolean empty) {

                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(status);

                switch (status.toUpperCase()) {

                    case "CANCELLED":
                        setStyle(
                                "-fx-text-fill: #c62828;" +
                                        "-fx-font-weight: bold;"
                        );
                        break;

                    case "OPEN":
                        setStyle(
                                "-fx-text-fill: #2e7d32;" +
                                        "-fx-font-weight: bold;"
                        );
                        break;

                    case "FULFILLED":
                        setStyle(
                                "-fx-text-fill: #1565c0;" +
                                        "-fx-font-weight: bold;"
                        );
                        break;

                    default:
                        setStyle("");
                }
            }
        });

        table.getColumns().addAll(
                idColumn,
                bloodColumn,
                hospitalColumn,
                cityColumn,
                responseColumn,
                statusColumn,
                dateColumn,
                messageColumn
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        Button refreshButton =
                new Button("Refresh Notifications");

        ObservableList<DonorNotification> notifications =
                FXCollections.observableArrayList();

        table.setItems(notifications);

        // Load notifications
        Runnable loadNotifications = () -> {

            notifications.clear();

            String sql = """
                    SELECT br.request_id,
                           br.blood_group,
                           br.hospital_name,
                           br.city,
                           dr.response_status,
                           br.status AS request_status,
                           dr.responded_at
                    FROM donor_responses dr
                    JOIN blood_requests br
                        ON dr.request_id = br.request_id
                    JOIN donors d
                        ON dr.donor_id = d.donor_id
                    WHERE d.user_id = ?
                      AND dr.response_status IN
                          ('INTERESTED', 'SELECTED', 'NOT_SELECTED')
                    ORDER BY dr.responded_at DESC
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, user.getUserId());

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        notifications.add(new DonorNotification(
                                String.valueOf(
                                        result.getInt("request_id")
                                ),
                                result.getString("blood_group"),
                                result.getString("hospital_name"),
                                result.getString("city"),
                                result.getString("response_status"),
                                result.getString("request_status"),
                                String.valueOf(
                                        result.getTimestamp("responded_at")
                                )
                        ));
                    }
                }

                if (notifications.isEmpty()) {

                    messageLabel.setText(
                            "You don't have any notifications yet."
                    );

                } else {

                    messageLabel.setText(
                            notifications.size() +
                                    " notification(s) found."
                    );
                }

            } catch (SQLException e) {

                messageLabel.setText(
                        "Could not load notifications."
                );

                e.printStackTrace();
            }
        };

        refreshButton.setOnAction(event ->
                loadNotifications.run()
        );

        VBox layout = new VBox(
                15,
                title,
                subtitle,
                table,
                refreshButton,
                messageLabel
        );

        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 1100, 550));

        loadNotifications.run();

        stage.show();
    }
}