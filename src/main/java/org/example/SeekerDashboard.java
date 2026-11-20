
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

        Label title = new Label("Find Your Lifeline ❤️");
        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #c62828;"
        );

        Label welcome = new Label(
                "Hello, " + user.getFullName() + "! ✨"
        );
        welcome.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #333333;"
        );

        Label info = new Label(
                "Every second counts. Find the help you need, " +
                        "connect with donors, and bring hope closer. 🩸"
        );
        info.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-text-fill: #666666;"
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
