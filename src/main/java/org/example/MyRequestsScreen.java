
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

public class MyRequestsScreen {

    // Donor information for the seeker
    private static class DonorInfo {
        int donorId;
        String name;
        String bloodGroup;
        String city;
        String available;
        String responseStatus;

        DonorInfo(int donorId, String name, String bloodGroup,
                  String city, String available, String responseStatus) {
            this.donorId = donorId;
            this.name = name;
            this.bloodGroup = bloodGroup;
            this.city = city;
            this.available = available;
            this.responseStatus = responseStatus;
        }
    }

    public void show() {

        UserSession user = UserSession.getCurrentUser();

        if (user == null || !"SEEKER".equalsIgnoreCase(user.getRole())) {
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("LifeLink - My Blood Requests");

        Label title = new Label("My Blood Requests");
        title.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;"
        );

        Button viewButton = new Button("Refresh My Requests");
        Button cancelButton = new Button("Cancel Selected Request");
        Button donorsButton = new Button("View Interested Donors");

        TableView<ObservableList<String>> table = new TableView<>();

        String[] columns = {
                "Request ID", "Blood Group", "Hospital",
                "City", "Units", "Urgency", "Status"
        };

        for (int i = 0; i < columns.length; i++) {

            final int index = i;

            TableColumn<ObservableList<String>, String> column =
                    new TableColumn<>(columns[i]);

            column.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(index))
            );

            column.setPrefWidth(105);

            if (index == 6) {
                column.setCellFactory(col -> new TableCell<>() {

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
                            case "OPEN":
                                setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                                break;

                            case "FULFILLED":
                                setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
                                break;

                            case "CANCELLED":
                                setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                                break;

                            default:
                                setStyle("");
                        }
                    }
                });
            }

            table.getColumns().add(column);
        }

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        viewButton.setOnAction(event ->
                loadRequests(user.getUserId(), table, messageLabel)
        );

        // Cancel selected request
        cancelButton.setOnAction(event -> {

            ObservableList<String> selectedRow =
                    table.getSelectionModel().getSelectedItem();

            if (selectedRow == null) {
                messageLabel.setText(
                        "Please select a blood request first."
                );
                return;
            }

            int requestId = Integer.parseInt(selectedRow.get(0));

            if (!"OPEN".equalsIgnoreCase(selectedRow.get(6))) {
                messageLabel.setText(
                        "Only OPEN requests can be cancelled."
                );
                return;
            }

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION
            );

            confirmation.setTitle("Cancel Blood Request");
            confirmation.setHeaderText("Confirm cancellation");
            confirmation.setContentText(
                    "Are you sure you want to cancel request #"
                            + requestId + "?"
            );

            if (confirmation.showAndWait().orElse(
                    ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            String sql = """
                    UPDATE blood_requests
                    SET status = 'CANCELLED'
                    WHERE request_id = ?
                      AND seeker_id = ?
                      AND status = 'OPEN'
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, requestId);
                statement.setInt(2, user.getUserId());

                int updated = statement.executeUpdate();

                if (updated == 1) {
                    messageLabel.setText(
                            "Blood request cancelled successfully."
                    );

                    loadRequests(user.getUserId(), table, messageLabel);

                } else {
                    messageLabel.setText(
                            "Request could not be cancelled."
                    );
                }

            } catch (SQLException e) {
                messageLabel.setText("Could not cancel the request.");
                e.printStackTrace();
            }
        });

        // View interested donors
        donorsButton.setOnAction(event -> {

            ObservableList<String> selectedRow =
                    table.getSelectionModel().getSelectedItem();

            if (selectedRow == null) {
                messageLabel.setText(
                        "Please select a blood request first."
                );
                return;
            }

            int requestId = Integer.parseInt(selectedRow.get(0));

            if (!"OPEN".equalsIgnoreCase(selectedRow.get(6))) {
                messageLabel.setText(
                        "Donors can only be selected for OPEN requests."
                );
                return;
            }

            showInterestedDonors(requestId, user.getUserId());
        });

        VBox layout = new VBox(
                15,
                title,
                viewButton,
                table,
                cancelButton,
                donorsButton,
                messageLabel
        );

        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 850, 550));
        stage.show();

        loadRequests(user.getUserId(), table, messageLabel);
    }

    // Load seeker's requests
    private void loadRequests(
            int seekerId,
            TableView<ObservableList<String>> table,
            Label messageLabel) {

        ObservableList<ObservableList<String>> rows =
                FXCollections.observableArrayList();

        String sql = """
                SELECT request_id, blood_group, hospital_name,
                       city, units_needed, urgency, status
                FROM blood_requests
                WHERE seeker_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, seekerId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    ObservableList<String> row =
                            FXCollections.observableArrayList();

                    row.add(String.valueOf(result.getInt("request_id")));
                    row.add(result.getString("blood_group"));
                    row.add(result.getString("hospital_name"));
                    row.add(result.getString("city"));
                    row.add(String.valueOf(result.getInt("units_needed")));
                    row.add(result.getString("urgency"));
                    row.add(result.getString("status"));

                    rows.add(row);
                }
            }

            table.setItems(rows);

            messageLabel.setText(
                    rows.isEmpty()
                            ? "No blood requests found."
                            : "Found " + rows.size() + " blood request(s)."
            );

        } catch (SQLException e) {
            messageLabel.setText("Could not load requests.");
            e.printStackTrace();
        }
    }

    // Show interested donors and allow seeker to select one
    private void showInterestedDonors(int requestId, int seekerId) {

        Stage donorStage = new Stage();
        donorStage.setTitle("Select a Donor");

        Label title = new Label(
                "Donors Interested in Request #" + requestId
        );

        title.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

        TableView<DonorInfo> donorTable = new TableView<>();

        TableColumn<DonorInfo, String> nameColumn =
                new TableColumn<>("Donor Name");
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().name));

        TableColumn<DonorInfo, String> bloodColumn =
                new TableColumn<>("Blood Group");
        bloodColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().bloodGroup));

        TableColumn<DonorInfo, String> cityColumn =
                new TableColumn<>("City");
        cityColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().city));

        TableColumn<DonorInfo, String> availableColumn =
                new TableColumn<>("Available");
        availableColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().available));

        TableColumn<DonorInfo, String> statusColumn =
                new TableColumn<>("Response");
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().responseStatus));

        donorTable.getColumns().addAll(
                nameColumn, bloodColumn, cityColumn,
                availableColumn, statusColumn
        );

        donorTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        Label message = new Label();
        message.setWrapText(true);

        Button selectButton = new Button("Select Donor");
        Button refreshButton = new Button("Refresh Donors");

        ObservableList<DonorInfo> donorList =
                FXCollections.observableArrayList();

        donorTable.setItems(donorList);

        // Load donors who responded to this request
        Runnable loadDonors = () -> {

            donorList.clear();
            selectButton.setDisable(true);

            String sql = """
                    SELECT d.donor_id, u.full_name,
                           d.blood_group, d.city, d.available,
                           dr.response_status,
                           br.selected_donor_id
                    FROM donor_responses dr
                    JOIN donors d ON dr.donor_id = d.donor_id
                    JOIN users u ON d.user_id = u.user_id
                    JOIN blood_requests br
                        ON dr.request_id = br.request_id
                    WHERE dr.request_id = ?
                      AND br.seeker_id = ?
                      AND dr.response_status IN
                          ('INTERESTED', 'SELECTED', 'NOT_SELECTED')
                    ORDER BY dr.responded_at DESC
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, requestId);
                statement.setInt(2, seekerId);

                try (ResultSet result = statement.executeQuery()) {

                    Integer selectedDonorId = null;

                    while (result.next()) {

                        int id = result.getInt("donor_id");

                        int selectedId =
                                result.getInt("selected_donor_id");

                        if (!result.wasNull()) {
                            selectedDonorId = selectedId;
                        }

                        donorList.add(new DonorInfo(
                                id,
                                result.getString("full_name"),
                                result.getString("blood_group"),
                                result.getString("city"),
                                result.getBoolean("available")
                                        ? "Yes" : "No",
                                result.getString("response_status")
                        ));
                    }

                    if (selectedDonorId != null) {
                        message.setText(
                                "A donor has already been selected for this request."
                        );
                    } else if (donorList.isEmpty()) {
                        message.setText(
                                "No donors have expressed interest yet."
                        );
                    } else {
                        message.setText(
                                donorList.size() + " interested donor(s) found. Select one."
                        );
                    }

                    selectButton.setDisable(selectedDonorId != null);
                }

            } catch (SQLException e) {
                message.setText("Could not load interested donors.");
                e.printStackTrace();
            }
        };

        donorTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    if (newValue == null) {
                        selectButton.setDisable(true);
                    } else {
                        selectButton.setDisable(
                                !"INTERESTED".equalsIgnoreCase(
                                        newValue.responseStatus
                                )
                        );
                    }
                });

        // Select the chosen donor
        selectButton.setOnAction(event -> {

            DonorInfo selected =
                    donorTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                message.setText("Please select a donor first.");
                return;
            }

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION
            );

            confirmation.setTitle("Confirm Donor Selection");
            confirmation.setHeaderText("Select " + selected.name + "?");

            confirmation.setContentText(
                    "This donor will be selected for blood request #"
                            + requestId +
                            ". Other interested donors will be marked as not selected."
            );

            if (confirmation.showAndWait().orElse(
                    ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            selectButton.setDisable(true);

            Connection connection = null;

            try {
                connection = DBConnection.getConnection();
                connection.setAutoCommit(false);

                // Verify ownership, request status and availability of selection
                String requestSql = """
                        UPDATE blood_requests
                        SET selected_donor_id = ?
                        WHERE request_id = ?
                          AND seeker_id = ?
                          AND status = 'OPEN'
                          AND selected_donor_id IS NULL
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(requestSql)) {

                    statement.setInt(1, selected.donorId);
                    statement.setInt(2, requestId);
                    statement.setInt(3, seekerId);

                    int updated = statement.executeUpdate();

                    if (updated != 1) {
                        throw new SQLException(
                                "A donor may already have been selected, or this request is no longer open."
                        );
                    }
                }

                // Mark the chosen donor SELECTED and others NOT_SELECTED
                String responseSql = """
                        UPDATE donor_responses
                        SET response_status =
                            CASE
                                WHEN donor_id = ? THEN 'SELECTED'
                                ELSE 'NOT_SELECTED'
                            END
                        WHERE request_id = ?
                          AND response_status = 'INTERESTED'
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(responseSql)) {

                    statement.setInt(1, selected.donorId);
                    statement.setInt(2, requestId);

                    int updated = statement.executeUpdate();

                    if (updated == 0) {
                        throw new SQLException(
                                "No interested donor responses were found."
                        );
                    }
                }

                connection.commit();

                message.setStyle("-fx-text-fill: green;");
                message.setText(
                        selected.name + " has been selected successfully!"
                );

                loadDonors.run();

            } catch (SQLException e) {

                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackError) {
                        rollbackError.printStackTrace();
                    }
                }

                message.setStyle("-fx-text-fill: red;");
                message.setText(
                        "Could not select donor: " + e.getMessage()
                );

                e.printStackTrace();

                selectButton.setDisable(false);

            } finally {

                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        refreshButton.setOnAction(event -> {
            donorTable.getSelectionModel().clearSelection();
            loadDonors.run();
        });

        VBox layout = new VBox(
                15,
                title,
                donorTable,
                selectButton,
                refreshButton,
                message
        );

        layout.setPadding(new Insets(20));

        donorStage.setScene(new Scene(layout, 750, 450));

        loadDonors.run();

        donorStage.show();
    }
}