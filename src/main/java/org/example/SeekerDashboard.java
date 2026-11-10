
package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SeekerDashboard {

    public void show() {

        UserSession user = UserSession.getCurrentUser();

        if (user == null) {
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("LifeLink - Seeker Dashboard");

        Label title = new Label("Seeker Dashboard");
        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

        Label welcome = new Label(
                "Welcome, " + user.getFullName() + "!"
        );
        welcome.setStyle("-fx-font-size: 18px;");

        Label info = new Label(
                "Find blood donors and manage your blood requests."
        );

        Button searchButton = new Button("Find Blood Donors");
        Button requestButton = new Button("Request Blood");
        Button historyButton = new Button("My Blood Requests");
        Button logoutButton = new Button("Logout");

        searchButton.setPrefWidth(220);
        requestButton.setPrefWidth(220);
        historyButton.setPrefWidth(220);
        logoutButton.setPrefWidth(220);

        searchButton.setOnAction(event ->
                new DonorSearchScreen().show()
        );

        requestButton.setOnAction(event ->
                new BloodRequestScreen().show()
        );

        historyButton.setOnAction(event ->
                new MyRequestsScreen().show()
        );

        logoutButton.setOnAction(event -> {
            UserSession.logout();
            stage.close();
        });

        VBox layout = new VBox(
                18,
                title,
                welcome,
                info,
                searchButton,
                requestButton,
                historyButton,
                logoutButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #fffafa;");

        Scene scene = new Scene(layout, 550, 500);

        stage.setScene(scene);
        stage.show();
    }
}
