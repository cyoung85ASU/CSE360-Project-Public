package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // A label to show error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            // Validate userName via FSM
            String userError = UserNameRecognizer.checkForValidUserName(userName);
            if (!userError.isEmpty()) {
                errorLabel.setText("Invalid userName: " + userError);
                return;
            }

            // Validate password via FSM
            String passError = PasswordEvaluator.evaluatePassword(password);
            if (!passError.isEmpty()) {
                errorLabel.setText("Invalid password: " + passError);
                return;
            }

            // If both checks pass, proceed to register
            try {
                User user = new User(userName, password, "admin");
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");

                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, userNameField, passwordField, setupButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}