
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
import java.util.Collections;
import java.util.List;

public class DonorRequestsScreen {

    private static class BloodRequest {
        int requestId;
        String bloodGroup;
        String hospital;
        String city;
        String area;
        String pincode;
        String phone;
        String units;
        String urgency;
        String date;

        BloodRequest(int requestId, String bloodGroup,
                     String hospital, String city,
                     String area, String pincode, String phone,
                     String units, String urgency, String date) {

            this.requestId = requestId;
            this.bloodGroup = bloodGroup;
            this.hospital = hospital;
            this.city = city;
            this.area = area;
            this.pincode = pincode;
            this.phone = phone;
            this.units = units;
            this.urgency = urgency;
            this.date = date;
        }
    }

    public void show() {

        UserSession user = UserSession.getCurrentUser();

        if (user == null || !"DONOR".equalsIgnoreCase(user.getRole())) {
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("LifeLink - Blood Requests");

        Label title = new Label("Compatible Blood Requests");

        title.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

        TableView<BloodRequest> table = new TableView<>();

        TableColumn<BloodRequest, String> bloodColumn =
                new TableColumn<>("Required Blood Group");

        bloodColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().bloodGroup));

        TableColumn<BloodRequest, String> hospitalColumn =
                new TableColumn<>("Hospital");

        hospitalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().hospital));

        TableColumn<BloodRequest, String> cityColumn =
                new TableColumn<>("City");
        TableColumn<BloodRequest, String> areaColumn =
                new TableColumn<>("Area");

        areaColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().area));

        TableColumn<BloodRequest, String> pincodeColumn =
                new TableColumn<>("Pincode");

        pincodeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().pincode));

        TableColumn<BloodRequest, String> phoneColumn =
                new TableColumn<>("Phone number");

        phoneColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().phone));

        cityColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().city));

        TableColumn<BloodRequest, String> unitsColumn =
                new TableColumn<>("Units");

        unitsColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().units));

        TableColumn<BloodRequest, String> urgencyColumn =
                new TableColumn<>("Urgency");

        urgencyColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().urgency));

        TableColumn<BloodRequest, String> dateColumn =
                new TableColumn<>("Date");

        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().date));

        table.getColumns().clear();

        table.getColumns().addAll(
                bloodColumn,
                hospitalColumn,
                cityColumn,
                areaColumn,
                pincodeColumn,
                phoneColumn,
                unitsColumn,
                urgencyColumn,
                dateColumn
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Highlight emergency and urgent requests

        table.setRowFactory(tv -> new TableRow<>() {

            private void updateRowStyle() {

                BloodRequest request = getItem();

                if (isEmpty() || request == null) {
                    setStyle("");
                    return;
                }

                // Keep selected rows blue, regardless of urgency
                if (isSelected()) {
                    setStyle(
                            "-fx-background-color: #1976d2;" +
                                    "-fx-text-background-color: white;" +
                                    "-fx-font-weight: bold;"
                    );

                } else if ("EMERGENCY".equalsIgnoreCase(request.urgency)) {
                    setStyle(
                            "-fx-background-color: #ffcdd2;" +
                                    "-fx-text-background-color: #b71c1c;" +
                                    "-fx-font-weight: bold;"
                    );

                } else if ("URGENT".equalsIgnoreCase(request.urgency)) {
                    setStyle(
                            "-fx-background-color: #fff3e0;" +
                                    "-fx-text-background-color: #e65100;"
                    );

                } else {
                    setStyle("");
                }
            }

            @Override
            protected void updateItem(BloodRequest request, boolean empty) {
                super.updateItem(request, empty);
                updateRowStyle();
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                updateRowStyle();
            }
        });


        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        Button responseButton = new Button("I'm Interested");
        Button refreshButton = new Button("Refresh Requests");

        responseButton.setDisable(true);

        ObservableList<BloodRequest> requests =
                FXCollections.observableArrayList();

        table.setItems(requests);

        // Get donor ID
        int donorId;

        try (Connection connection = DBConnection.getConnection()) {

            donorId = getDonorId(connection, user.getUserId());

            if (donorId == -1) {
                messageLabel.setText("Donor profile not found.");
                return;
            }

        } catch (SQLException e) {

            messageLabel.setText("Could not load donor profile.");
            e.printStackTrace();
            return;
        }

        // Load only compatible open requests
        Runnable loadRequests = () -> {

            requests.clear();

            messageLabel.setText("");
            responseButton.setText("I'm Interested");
            responseButton.setDisable(true);

            String sql = """
        SELECT br.request_id,
               br.blood_group,
               br.hospital_name,
               br.city,
               u.area,
               u.pincode,
               u.phone,
               br.units_needed,
               br.urgency,
               br.created_at
        FROM blood_requests br
        JOIN users u ON br.seeker_id = u.user_id
        WHERE br.status = 'OPEN'
          AND br.blood_group IN (%s)
        ORDER BY
            CASE br.urgency
                WHEN 'EMERGENCY' THEN 1
                WHEN 'URGENT' THEN 2
                ELSE 3
            END,
            br.created_at DESC
        """;

            try (Connection connection = DBConnection.getConnection()) {

                String donorGroup = getDonorBloodGroup(
                        connection, user.getUserId()
                );

                List<String> compatibleGroups =
                        getCompatibleRecipientGroups(donorGroup);

                if (compatibleGroups.isEmpty()) {

                    messageLabel.setText(
                            "Donor blood group not found or invalid."
                    );

                    return;
                }

                String placeholders = String.join(
                        ",",
                        Collections.nCopies(
                                compatibleGroups.size(), "?"
                        )
                );

                sql = sql.formatted(placeholders);

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    for (int i = 0; i < compatibleGroups.size(); i++) {

                        statement.setString(
                                i + 1,
                                compatibleGroups.get(i)
                        );
                    }

                    try (ResultSet result = statement.executeQuery()) {

                        while (result.next()) {

                            requests.add(new BloodRequest(
                                    result.getInt("request_id"),
                                    result.getString("blood_group"),
                                    result.getString("hospital_name"),
                                    result.getString("city"),
                                    result.getString("area"),
                                    result.getString("pincode"),
                                    result.getString("phone"),
                                    String.valueOf(result.getInt("units_needed")),
                                    result.getString("urgency"),
                                    String.valueOf(result.getTimestamp("created_at"))
                            ));
                        }
                    }
                }

                if (requests.isEmpty()) {

                    messageLabel.setText(
                            "No compatible blood requests available."
                    );
                }

            } catch (SQLException e) {

                messageLabel.setText(
                        "Could not load compatible blood requests."
                );

                e.printStackTrace();
            }
        };

        // Update button according to selected request
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    messageLabel.setText("");

                    if (newValue == null) {

                        responseButton.setDisable(true);
                        responseButton.setText("I'm Interested");

                        return;
                    }

                    try (Connection connection = DBConnection.getConnection()) {

                        updateResponseButton(
                                responseButton,
                                connection,
                                newValue.requestId,
                                donorId
                        );

                    } catch (SQLException e) {

                        messageLabel.setText(
                                "Could not check your response."
                        );

                        e.printStackTrace();
                    }
                });

        // Express interest or withdraw interest
        responseButton.setOnAction(event -> {

            BloodRequest selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {

                messageLabel.setText(
                        "Please select a blood request first."
                );

                return;
            }

            responseButton.setDisable(true);

            try (Connection connection = DBConnection.getConnection()) {

                if (hasExpressedInterest(
                        connection, selected.requestId, donorId)) {

                    String sql = """
                            UPDATE donor_responses
                            SET response_status = 'WITHDRAWN'
                            WHERE request_id = ?
                              AND donor_id = ?
                              AND response_status = 'INTERESTED'
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(sql)) {

                        statement.setInt(1, selected.requestId);
                        statement.setInt(2, donorId);

                        int updated = statement.executeUpdate();

                        if (updated > 0) {

                            messageLabel.setStyle(
                                    "-fx-text-fill: green;"
                            );

                            messageLabel.setText(
                                    "Your interest has been withdrawn."
                            );
                        }
                    }

                } else {

                    String sql = """
                            INSERT INTO donor_responses
                            (request_id, donor_id)
                            VALUES (?, ?)
                            ON DUPLICATE KEY UPDATE
                            response_status = 'INTERESTED',
                            responded_at = CURRENT_TIMESTAMP
                            """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(sql)) {

                        statement.setInt(1, selected.requestId);
                        statement.setInt(2, donorId);

                        statement.executeUpdate();

                        messageLabel.setStyle(
                                "-fx-text-fill: green;"
                        );

                        messageLabel.setText(
                                "Your interest has been recorded successfully!"
                        );
                    }
                }

                // Immediately update the button
                updateResponseButton(
                        responseButton,
                        connection,
                        selected.requestId,
                        donorId
                );

            } catch (SQLException e) {

                messageLabel.setText(
                        "Could not update your response. Please try again."
                );

                e.printStackTrace();

                responseButton.setDisable(false);
            }
        });

        // Refresh requests
        refreshButton.setOnAction(event -> {

            loadRequests.run();

            table.getSelectionModel().clearSelection();
        });

        VBox layout = new VBox(
                15,
                title,
                table,
                responseButton,
                refreshButton,
                messageLabel
        );

        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 850, 550));

        loadRequests.run();

        stage.show();
    }

    // Find donor ID using logged-in user's ID
    private int getDonorId(Connection connection, int userId)
            throws SQLException {

        String sql = """
                SELECT donor_id
                FROM donors
                WHERE user_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("donor_id");
                }
            }
        }

        return -1;
    }

    // Get donor's registered blood group
    private String getDonorBloodGroup(Connection connection, int userId)
            throws SQLException {

        String sql = """
                SELECT blood_group
                FROM donors
                WHERE user_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getString("blood_group");
                }
            }
        }

        return null;
    }

    // Return recipient blood groups compatible with donor
    private List<String> getCompatibleRecipientGroups(String donorGroup) {

        if (donorGroup == null) {
            return Collections.emptyList();
        }

        switch (donorGroup.trim().toUpperCase()) {

            case "O-":
                return List.of(
                        "O-", "O+", "A-", "A+",
                        "B-", "B+", "AB-", "AB+"
                );

            case "O+":
                return List.of("O+", "A+", "B+", "AB+");

            case "A-":
                return List.of("A-", "A+", "AB-", "AB+");

            case "A+":
                return List.of("A+", "AB+");

            case "B-":
                return List.of("B-", "B+", "AB-", "AB+");

            case "B+":
                return List.of("B+", "AB+");

            case "AB-":
                return List.of("AB-", "AB+");

            case "AB+":
                return List.of("AB+");

            default:
                return Collections.emptyList();
        }
    }

    // Check whether donor has expressed interest
    private boolean hasExpressedInterest(
            Connection connection, int requestId, int donorId)
            throws SQLException {

        String sql = """
                SELECT response_id
                FROM donor_responses
                WHERE request_id = ?
                  AND donor_id = ?
                  AND response_status = 'INTERESTED'
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, requestId);
            statement.setInt(2, donorId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // Update button text based on current response
    private void updateResponseButton(
            Button button, Connection connection,
            int requestId, int donorId) throws SQLException {

        if (hasExpressedInterest(connection, requestId, donorId)) {

            button.setText("Withdraw Interest");

        } else {

            button.setText("I'm Interested");
        }

        button.setDisable(false);
    }
}
