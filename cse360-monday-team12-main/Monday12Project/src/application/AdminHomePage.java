package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * AdminHomePage, revised to pass the currentUser object.
 */
public class AdminHomePage {

    private final DatabaseHelper databaseHelper;
    private final User currentUser;  // store the logged-in User

    public AdminHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label adminLabel = new Label("Hello, Admin: " + currentUser.getUserName() + "!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to navigate to the Q&A page
        Button manageQABtn = new Button("Manage Questions and Answers");
        manageQABtn.setOnAction(e -> {
            // Pass currentUser so Q&A page knows who we are
            new QuestionsAndAnswersPage(databaseHelper, currentUser).show(primaryStage);
        });

        layout.getChildren().addAll(adminLabel, manageQABtn);
        Scene adminScene = new Scene(layout, 800, 400);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
    }
}
