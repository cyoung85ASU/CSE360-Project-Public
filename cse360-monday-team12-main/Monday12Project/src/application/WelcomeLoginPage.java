package application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * WelcomeLoginPage passes the current User along to whichever page is appropriate.
 */
public class WelcomeLoginPage {
    private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(5);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label welcomeLabel = new Label("Welcome!!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button continueButton = new Button("Continue to your Page");
        continueButton.setOnAction(a -> {
            String role = user.getRole();
            if ("admin".equals(role)) {
                // Pass the user object into AdminHomePage
                new AdminHomePage(databaseHelper, user).show(primaryStage);
            } else {
                new UserHomePage(databaseHelper, user).show(primaryStage);
            }
        });

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit();
        });

        // If admin, can display "Invite" button, etc.
        if ("admin".equals(user.getRole())) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(ev -> {
                new InvitationPage().show(databaseHelper, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }

        layout.getChildren().addAll(welcomeLabel, continueButton, quitButton);
        Scene welcomeScene = new Scene(layout, 800, 400);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}
