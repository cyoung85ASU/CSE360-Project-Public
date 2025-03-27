package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;


public class QuestionsAndAnswersPage {
    private final DatabaseHelper dbHelper;
    private final User currentUser;

    public QuestionsAndAnswersPage(DatabaseHelper dbHelper, User currentUser) {
        this.dbHelper = dbHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        Tab createQTab = new Tab("Create Question");
        createQTab.setClosable(false);

        VBox createQBox = new VBox(10);
        createQBox.setPadding(new Insets(20));

        Label createQLabel = new Label("Create a new question:");
        TextField questionTextField = new TextField();
        questionTextField.setPromptText("Enter your question text");
        Button createQuestionButton = new Button("Create Question");
        Label createQuestionStatus = new Label();

        createQuestionButton.setOnAction(e -> {
            String qText = questionTextField.getText();
            try {
                int qId = dbHelper.createQuestion(qText, currentUser.getUserName());
                createQuestionStatus.setText("Question created with ID: " + qId);
                questionTextField.clear();
            } catch (Exception ex) {
                createQuestionStatus.setText("Error: " + ex.getMessage());
            }
        });

        createQBox.getChildren().addAll(createQLabel, questionTextField, createQuestionButton, createQuestionStatus);
        createQTab.setContent(createQBox);

        Tab updateDeleteQTab = new Tab("Update/Delete Q");
        updateDeleteQTab.setClosable(false);

        VBox updateDeleteQBox = new VBox(10);
        updateDeleteQBox.setPadding(new Insets(20));

        Label updateQLabel = new Label("Update a question:");
        TextField qIdToUpdateField = new TextField();
        qIdToUpdateField.setPromptText("Question ID to update");
        TextField qNewTextField = new TextField();
        qNewTextField.setPromptText("New text");
        Button updateQBtn = new Button("Update Question");
        Label updateQStatus = new Label();

        updateQBtn.setOnAction(e -> {
            try {
                int qId = Integer.parseInt(qIdToUpdateField.getText());
                boolean success = dbHelper.updateQuestion(
                        qId,
                        qNewTextField.getText(),
                        currentUser.getUserName(),
                        currentUser.getRole()
                );
                updateQStatus.setText(success ? "Question updated." : "No question updated.");
            } catch (Exception ex) {
                updateQStatus.setText("Error: " + ex.getMessage());
            }
        });

        // Delete
        Label deleteQLabel = new Label("Delete a question:");
        TextField qIdToDeleteField = new TextField();
        qIdToDeleteField.setPromptText("Question ID to delete");
        Button deleteQBtn = new Button("Delete Question");
        Label deleteQStatus = new Label();

        deleteQBtn.setOnAction(e -> {
            try {
                int qId = Integer.parseInt(qIdToDeleteField.getText());
                boolean success = dbHelper.deleteQuestion(
                        qId,
                        currentUser.getUserName(),
                        currentUser.getRole()
                );
                deleteQStatus.setText(success ? "Question deleted." : "No question deleted.");
            } catch (Exception ex) {
                deleteQStatus.setText("Error: " + ex.getMessage());
            }
        });

        updateDeleteQBox.getChildren().addAll(
            updateQLabel, qIdToUpdateField, qNewTextField, updateQBtn, updateQStatus,
            deleteQLabel, qIdToDeleteField, deleteQBtn, deleteQStatus
        );
        updateDeleteQTab.setContent(updateDeleteQBox);

        Tab createATab = new Tab("Create Answer");
        createATab.setClosable(false);

        VBox createABox = new VBox(10);
        createABox.setPadding(new Insets(20));

        Label createALabel = new Label("Create an answer:");
        TextField aQIdField = new TextField();
        aQIdField.setPromptText("Question ID");
        TextField aTextField = new TextField();
        aTextField.setPromptText("Answer text");
        Button createAnswerButton = new Button("Create Answer");
        Label createAnswerStatus = new Label();

        createAnswerButton.setOnAction(e -> {
            try {
                int qId = Integer.parseInt(aQIdField.getText());
                String text = aTextField.getText();
                int aId = dbHelper.createAnswer(qId, text, currentUser.getUserName());
                createAnswerStatus.setText("Answer created with ID: " + aId);
                aTextField.clear();
            } catch (Exception ex) {
                createAnswerStatus.setText("Error: " + ex.getMessage());
            }
        });

        createABox.getChildren().addAll(createALabel, aQIdField, aTextField, createAnswerButton, createAnswerStatus);
        createATab.setContent(createABox);

        Tab updateDeleteATab = new Tab("Update/Delete A");
        updateDeleteATab.setClosable(false);

        VBox updateDeleteABox = new VBox(10);
        updateDeleteABox.setPadding(new Insets(20));

        Label updateALabel = new Label("Update an answer:");
        TextField aIdToUpdateField = new TextField();
        aIdToUpdateField.setPromptText("Answer ID");
        TextField aNewTextField = new TextField();
        aNewTextField.setPromptText("New answer text");
        Button updateAnswerButton = new Button("Update Answer");
        Label updateAnswerStatus = new Label();

        updateAnswerButton.setOnAction(e -> {
            try {
                int aId = Integer.parseInt(aIdToUpdateField.getText());
                boolean success = dbHelper.updateAnswer(
                        aId,
                        aNewTextField.getText(),
                        currentUser.getUserName(),
                        currentUser.getRole()
                );
                updateAnswerStatus.setText(success ? "Answer updated." : "No answer updated.");
            } catch (Exception ex) {
                updateAnswerStatus.setText("Error: " + ex.getMessage());
            }
        });

        // Delete Answer
        Label deleteALabel = new Label("Delete an answer:");
        TextField aIdToDeleteField = new TextField();
        aIdToDeleteField.setPromptText("Answer ID to delete");
        Button deleteAnswerButton = new Button("Delete Answer");
        Label deleteAnswerStatus = new Label();

        deleteAnswerButton.setOnAction(e -> {
            try {
                int aId = Integer.parseInt(aIdToDeleteField.getText());
                boolean success = dbHelper.deleteAnswer(
                        aId,
                        currentUser.getUserName(),
                        currentUser.getRole()
                );
                deleteAnswerStatus.setText(success ? "Answer deleted." : "No answer deleted.");
            } catch (Exception ex) {
                deleteAnswerStatus.setText("Error: " + ex.getMessage());
            }
        });

        updateDeleteABox.getChildren().addAll(
            updateALabel, aIdToUpdateField, aNewTextField, updateAnswerButton, updateAnswerStatus,
            deleteALabel, aIdToDeleteField, deleteAnswerButton, deleteAnswerStatus
        );
        updateDeleteATab.setContent(updateDeleteABox);

        Tab correctTab = new Tab("Mark Correct");
        correctTab.setClosable(false);

        VBox correctBox = new VBox(10);
        correctBox.setPadding(new Insets(20));

        Label correctLabel = new Label("Mark an answer correct for a question:");
        TextField correctQIdField = new TextField();
        correctQIdField.setPromptText("Question ID");
        TextField correctAIdField = new TextField();
        correctAIdField.setPromptText("Answer ID");
        Button markCorrectBtn = new Button("Mark Correct");
        Label markCorrectStatus = new Label();

        markCorrectBtn.setOnAction(e -> {
            try {
                int qId = Integer.parseInt(correctQIdField.getText());
                int aId = Integer.parseInt(correctAIdField.getText());
                boolean success = dbHelper.markAnswerAsCorrect(
                        qId,
                        aId,
                        currentUser.getUserName(),
                        currentUser.getRole()
                );
                markCorrectStatus.setText(success ? "Answer marked correct." : "Failed to mark correct.");
            } catch (Exception ex) {
                markCorrectStatus.setText("Error: " + ex.getMessage());
            }
        });

        correctBox.getChildren().addAll(
            correctLabel, correctQIdField, correctAIdField, markCorrectBtn, markCorrectStatus
        );
        correctTab.setContent(correctBox);

        //  Clarifications
        Tab clarificationsTab = new Tab("Clarifications");
        clarificationsTab.setClosable(false);

        VBox clarificationsBox = new VBox(10);
        clarificationsBox.setPadding(new Insets(20));

        Label createReplyLabel = new Label("Create reply to an answer:");
        TextField cAnswerIdField = new TextField();
        cAnswerIdField.setPromptText("Answer ID to clarify");
        TextField cReplyTextField = new TextField();
        cReplyTextField.setPromptText("Reply text");
        Button createReplyButton = new Button("Create Reply");
        Label createReplyStatus = new Label();

        createReplyButton.setOnAction(e -> {
            try {
                int ansId = Integer.parseInt(cAnswerIdField.getText());
                int rId = dbHelper.createReply(ansId, cReplyTextField.getText(), currentUser.getUserName());
                createReplyStatus.setText("Reply created with ID: " + rId);
                cReplyTextField.clear();
            } catch (Exception ex) {
                createReplyStatus.setText("Error: " + ex.getMessage());
            }
        });

        clarificationsBox.getChildren().addAll(
            createReplyLabel, cAnswerIdField, cReplyTextField, createReplyButton, createReplyStatus
        );

        clarificationsTab.setContent(clarificationsBox);

        Tab searchTab = new Tab("Search Q&A");
        searchTab.setClosable(false);

        VBox searchBox = new VBox(10);
        searchBox.setPadding(new Insets(20));

        Label searchLabel = new Label("Search Questions (and see answers):");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter search term (keyword in question text)");

        Button searchButton = new Button("Search");
        TextArea searchResultsArea = new TextArea();
        searchResultsArea.setEditable(false);

        searchButton.setOnAction(e -> {
            searchResultsArea.clear();
            String term = searchField.getText().trim();
            if (term.isEmpty()) {
                searchResultsArea.setText("Please enter a search term.");
                return;
            }
            try {
                // 1) Find matching questions
                List<String[]> questions = dbHelper.searchQuestions(term);
                if (questions.isEmpty()) {
                    searchResultsArea.setText("No questions found for term: " + term);
                    return;
                }

                // 2) For each question, show question text & any answers
                StringBuilder sb = new StringBuilder();
                for (String[] q : questions) {
                    // q = { questionId, questionText, created_by }
                    int qId = Integer.parseInt(q[0]);
                    String qText = q[1];
                    String qCreator = q[2];
                    
                    sb.append("Question #").append(qId).append(" (by ").append(qCreator).append("): ")
                      .append(qText).append("\n");

                    // Get answers for that question
                    List<String[]> answers = dbHelper.getAnswersForQuestion(qId);
                    if (answers.isEmpty()) {
                        sb.append("   No answers yet.\n");
                    } else {
                        for (String[] ans : answers) {
                            // ans = { answerId, answerText, created_by, is_correct }
                            int aId = Integer.parseInt(ans[0]);
                            String aText = ans[1];
                            String aCreator = ans[2];
                            boolean isCorrect = Boolean.parseBoolean(ans[3]);

                            sb.append("   Answer #").append(aId)
                              .append(" (by ").append(aCreator).append(")")
                              .append(isCorrect ? " [CORRECT]" : "")
                              .append(": ").append(aText).append("\n");
                        }
                    }
                    sb.append("\n");
                }

                searchResultsArea.setText(sb.toString());
            } catch (Exception ex) {
                searchResultsArea.setText("Error: " + ex.getMessage());
            }
        });

        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, searchResultsArea);
        searchTab.setContent(searchBox);

        tabPane.getTabs().addAll(
            createQTab,
            updateDeleteQTab,
            createATab,
            updateDeleteATab,
            correctTab,
            clarificationsTab,
            searchTab
        );

        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> {
            if ("admin".equals(currentUser.getRole())) {
                new AdminHomePage(dbHelper, currentUser).show(primaryStage);
            } else {
                new UserHomePage(dbHelper, currentUser).show(primaryStage);
            }
        });

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().addAll(tabPane, backButton);

        // Show everything
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Q&A Management");
        primaryStage.show();
    }
}
