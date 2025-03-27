package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * UserHomePage, revised to pass the currentUser object.
 */
public class UserHomePage {

    private final DatabaseHelper databaseHelper;
    private final User currentUser;  // store the logged-in User

    public UserHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label userLabel = new Label("Hello, User: " + currentUser.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to navigate to the Q&A page
        Button manageQABtn = new Button("Manage Questions and Answers");
        manageQABtn.setOnAction(e -> {
            new QuestionsAndAnswersPage(databaseHelper, currentUser).show(primaryStage);
        });

        layout.getChildren().addAll(userLabel, manageQABtn);
        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
    }
}
