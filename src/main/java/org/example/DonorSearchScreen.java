package org.example;

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

public class DonorSearchScreen {

    public void show() {

        Stage stage = new Stage();
        stage.setTitle("LifeLink - Find Blood Donors");

        Label title = new Label("Find Blood Donors");
        title.setStyle(
                "-fx-font-size: 26px; -fx-font-weight: bold; " +
                        "-fx-text-fill: #c62828;"
        );

        // Blood group filter
        ComboBox<String> bloodGroup = new ComboBox<>();
        bloodGroup.getItems().addAll(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        bloodGroup.setPromptText("Select blood group");
        bloodGroup.setMaxWidth(Double.MAX_VALUE);

        // City filter
        TextField city = new TextField();
        city.setPromptText("Enter city");

        // Availability filter
        ComboBox<String> availabilityFilter = new ComboBox<>();
        availabilityFilter.getItems().addAll(
                "All Donors",
                "Available Only",
                "Unavailable Only"
        );
        availabilityFilter.setValue("Available Only");
        availabilityFilter.setMaxWidth(Double.MAX_VALUE);

        Button search = new Button("Search Donors");
        Label result = new Label();

        // Results table
        TableView<ObservableList<String>> table =
                new TableView<>();

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        String[] headings = {
                "Name", "Blood Group", "City",
                "Area", "Email", "Availability"
        };

        for (int i = 0; i < headings.length; i++) {

            final int index = i;

            TableColumn<ObservableList<String>, String> column =
                    new TableColumn<>(headings[i]);

            column.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get(index)
                    )
            );

            table.getColumns().add(column);
        }

        // Search donors
        search.setOnAction(e -> {

            if (bloodGroup.getValue() == null
                    || city.getText().trim().isEmpty()) {

                result.setText(
                        "Select a blood group and enter a city."
                );
                return;
            }

            table.getItems().clear();

            String sql = """
                    SELECT u.full_name,
                           d.blood_group,
                           u.city,
                           COALESCE(u.area, ''),
                           u.email,
                           CASE
                               WHEN d.available = TRUE
                               THEN 'AVAILABLE'
                               ELSE 'UNAVAILABLE'
                           END AS availability
                    FROM donors d
                    JOIN users u ON d.user_id = u.user_id
                    WHERE d.blood_group = ?
                      AND LOWER(u.city) LIKE LOWER(?)
                      AND u.role = 'DONOR'
                      AND (
                          ? = 'All Donors'
                          OR (? = 'Available Only'
                              AND d.available = TRUE)
                          OR (? = 'Unavailable Only'
                              AND d.available = FALSE)
                      )
                    ORDER BY u.full_name
                    """;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, bloodGroup.getValue());

                statement.setString(
                        2,
                        "%" + city.getText().trim() + "%"
                );

                statement.setString(3, availabilityFilter.getValue());
                statement.setString(4, availabilityFilter.getValue());
                statement.setString(5, availabilityFilter.getValue());

                try (ResultSet rs = statement.executeQuery()) {

                    while (rs.next()) {

                        ObservableList<String> row =
                                FXCollections.observableArrayList();

                        for (int i = 1; i <= 6; i++) {
                            row.add(rs.getString(i));
                        }

                        table.getItems().add(row);
                    }
                }

                result.setText(
                        table.getItems().isEmpty()
                                ? "No donors found for your search."
                                : table.getItems().size()
                                + " donor(s) found."
                );

            } catch (SQLException ex) {

                result.setText("Database search failed.");
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(
                15,
                title,
                new Label("Blood Group"),
                bloodGroup,
                new Label("City"),
                city,
                new Label("Donor Availability"),
                availabilityFilter,
                search,
                result,
                table
        );

        layout.setAlignment(javafx.geometry.Pos.CENTER);
        layout.setPadding(new Insets(25));

        stage.setScene(new Scene(layout, 800, 550));
        stage.show();
    }
}