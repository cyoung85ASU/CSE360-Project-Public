package databasePart1;

import application.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }


    private void createTables() throws SQLException {
    	
        //statement.execute("DROP ALL OBJECTS");
    	
        String userTable = """
           CREATE TABLE IF NOT EXISTS cse360users (
               id INT AUTO_INCREMENT PRIMARY KEY,
               userName VARCHAR(255) UNIQUE,
               password VARCHAR(255),
               role VARCHAR(20)
           )
        """;
        statement.execute(userTable);

        String invitationCodesTable = """
           CREATE TABLE IF NOT EXISTS InvitationCodes (
               code VARCHAR(10) PRIMARY KEY,
               isUsed BOOLEAN DEFAULT FALSE
           )
        """;
        statement.execute(invitationCodesTable);

        String questionsTable = """
           CREATE TABLE IF NOT EXISTS questions (
               question_id INT AUTO_INCREMENT PRIMARY KEY,
               question_text VARCHAR(255) NOT NULL,
               created_by VARCHAR(255) NOT NULL
           )
        """;
        statement.execute(questionsTable);

        // Each answer has a creator and "is_correct" flag
        String answersTable = """
           CREATE TABLE IF NOT EXISTS answers (
               answer_id INT AUTO_INCREMENT PRIMARY KEY,
               question_id INT NOT NULL,
               answer_text VARCHAR(255) NOT NULL,
               created_by VARCHAR(255) NOT NULL,
               is_correct BOOLEAN DEFAULT FALSE,
               FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
           )
        """;
        statement.execute(answersTable);

        String clarificationsTable = """
           CREATE TABLE IF NOT EXISTS clarifications (
               reply_id INT AUTO_INCREMENT PRIMARY KEY,
               answer_id INT NOT NULL,
               reply_text VARCHAR(255) NOT NULL,
               created_by VARCHAR(255) NOT NULL,
               FOREIGN KEY (answer_id) REFERENCES answers(answer_id) ON DELETE CASCADE
           )
        """;
        statement.execute(clarificationsTable);
    }


    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        try (ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        }
        return true;
    }

    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.executeUpdate();
        }
    }

    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateInvitationCode() {
        String code = java.util.UUID.randomUUID().toString().substring(0, 4);
        String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                markInvitationCodeAsUsed(code);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public int createQuestion(String questionText, String createdBy) throws SQLException {
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be empty.");
        }
        String insertQuestion =
            "INSERT INTO questions (question_text, created_by) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuestion, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, questionText.trim());
            pstmt.setString(2, createdBy);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public String readQuestion(int questionId) throws SQLException {
        String selectQuestion = "SELECT question_text FROM questions WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuestion)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("question_text");
                }
            }
        }
        return null;
    }

    /**
     * Admin can update any question, else only the creator can update.
     */
    public boolean updateQuestion(int questionId, String newText, String currentUser, String currentUserRole)
        throws SQLException
    {
        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("New question text cannot be empty.");
        }

        // Check who created the question
        String checkCreator = "SELECT created_by FROM questions WHERE question_id = ?";
        String creator = null;
        try (PreparedStatement pstmt = connection.prepareStatement(checkCreator)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    creator = rs.getString("created_by");
                }
            }
        }
        if (creator == null) {
            return false; // No such question
        }

        // If not admin and not the creator, no permission
        if (!currentUserRole.equals("admin") && !creator.equals(currentUser)) {
            throw new SecurityException("No permission to update this question.");
        }

        String updateQuery = "UPDATE questions SET question_text = ? WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, newText.trim());
            pstmt.setInt(2, questionId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    /**
     * Admin can delete any question, else only the creator can delete.
     */
    public boolean deleteQuestion(int questionId, String currentUser, String currentUserRole)
        throws SQLException
    {
        // Check who created the question
        String checkCreator = "SELECT created_by FROM questions WHERE question_id = ?";
        String creator = null;
        try (PreparedStatement pstmt = connection.prepareStatement(checkCreator)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    creator = rs.getString("created_by");
                }
            }
        }
        if (creator == null) {
            return false;
        }

        // If not admin and not the creator, no permission
        if (!currentUserRole.equals("admin") && !creator.equals(currentUser)) {
            throw new SecurityException("No permission to delete this question.");
        }

        String deleteQuery = "DELETE FROM questions WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<String[]> getAllQuestions() throws SQLException {
        List<String[]> results = new ArrayList<>();
        String query = "SELECT question_id, question_text, created_by FROM questions";
        try (ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                results.add(new String[]{
                    String.valueOf(rs.getInt("question_id")),
                    rs.getString("question_text"),
                    rs.getString("created_by")
                });
            }
        }
        return results;
    }

    public List<String[]> searchQuestions(String term) throws SQLException {
        List<String[]> matches = new ArrayList<>();
        String query = "SELECT question_id, question_text, created_by FROM questions WHERE LOWER(question_text) LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + term.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(new String[]{
                        String.valueOf(rs.getInt("question_id")),
                        rs.getString("question_text"),
                        rs.getString("created_by")
                    });
                }
            }
        }
        return matches;
    }

    // Answers

    public int createAnswer(int questionId, String answerText, String createdBy) throws SQLException {
        if (answerText == null || answerText.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer text cannot be empty.");
        }
        // Confirm question exists
        if (readQuestion(questionId) == null) {
            throw new IllegalArgumentException("Question with ID " + questionId + " does not exist.");
        }

        String insertAnswer = """
            INSERT INTO answers (question_id, answer_text, created_by)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(insertAnswer, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, answerText.trim());
            pstmt.setString(3, createdBy);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public String[] readAnswer(int answerId) throws SQLException {
        String selectAnswer = """
            SELECT question_id, answer_text, created_by, is_correct
            FROM answers WHERE answer_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(selectAnswer)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("question_id")),
                        rs.getString("answer_text"),
                        rs.getString("created_by"),
                        String.valueOf(rs.getBoolean("is_correct"))
                    };
                }
            }
        }
        return null;
    }

    /**
     * Admin or the answer’s creator can update the answer.
     */
    public boolean updateAnswer(int answerId, String newText, String currentUser, String currentUserRole)
        throws SQLException
    {
        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("New answer text cannot be empty.");
        }
        // Find who created this answer
        String checkCreator = "SELECT created_by FROM answers WHERE answer_id = ?";
        String creator = null;
        try (PreparedStatement pstmt = connection.prepareStatement(checkCreator)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    creator = rs.getString("created_by");
                }
            }
        }
        if (creator == null) {
            return false;
        }
        if (!currentUserRole.equals("admin") && !creator.equals(currentUser)) {
            throw new SecurityException("No permission to update this answer.");
        }

        String updateQuery = "UPDATE answers SET answer_text = ? WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, newText.trim());
            pstmt.setInt(2, answerId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Admin or the answer’s creator can delete the answer.
     */
    public boolean deleteAnswer(int answerId, String currentUser, String currentUserRole)
        throws SQLException
    {
        // Check who created the answer
        String checkCreator = "SELECT created_by FROM answers WHERE answer_id = ?";
        String creator = null;
        try (PreparedStatement pstmt = connection.prepareStatement(checkCreator)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    creator = rs.getString("created_by");
                }
            }
        }
        if (creator == null) {
            return false;
        }
        if (!currentUserRole.equals("admin") && !creator.equals(currentUser)) {
            throw new SecurityException("No permission to delete this answer.");
        }

        String deleteQuery = "DELETE FROM answers WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, answerId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<String[]> getAnswersForQuestion(int questionId) throws SQLException {
        List<String[]> answers = new ArrayList<>();
        String query = """
            SELECT answer_id, answer_text, created_by, is_correct
            FROM answers
            WHERE question_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(new String[]{
                        String.valueOf(rs.getInt("answer_id")),
                        rs.getString("answer_text"),
                        rs.getString("created_by"),
                        String.valueOf(rs.getBoolean("is_correct"))
                    });
                }
            }
        }
        return answers;
    }

    public boolean markAnswerAsCorrect(int questionId, int answerId, String currentUser, String currentUserRole)
        throws SQLException
    {
        // Lookup the question’s creator
        String checkCreator = "SELECT created_by FROM questions WHERE question_id = ?";
        String questionCreator = null;
        try (PreparedStatement pstmt = connection.prepareStatement(checkCreator)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    questionCreator = rs.getString("created_by");
                }
            }
        }
        if (questionCreator == null) {
            throw new IllegalArgumentException("No such question exists.");
        }

        if (!questionCreator.equals(currentUser)) {
            throw new SecurityException("Only the question’s creator can mark an answer correct.");
        }

        // Mark all answers for that question false
        String resetAll = "UPDATE answers SET is_correct = FALSE WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(resetAll)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }

        // Now set the chosen answer to true
        String markCorrect = "UPDATE answers SET is_correct = TRUE WHERE answer_id = ? AND question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(markCorrect)) {
            pstmt.setInt(1, answerId);
            pstmt.setInt(2, questionId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Clarifications

    public int createReply(int answerId, String replyText, String createdBy) throws SQLException {
        if (replyText == null || replyText.trim().isEmpty()) {
            throw new IllegalArgumentException("Reply text cannot be empty.");
        }
        // Check the answer exists
        String checkAns = "SELECT answer_id FROM answers WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkAns)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Answer " + answerId + " does not exist.");
                }
            }
        }

        String insertReply = """
            INSERT INTO clarifications (answer_id, reply_text, created_by)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(insertReply, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, answerId);
            pstmt.setString(2, replyText.trim());
            pstmt.setString(3, createdBy);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<String[]> getRepliesForAnswer(int answerId) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT reply_id, reply_text, created_by FROM clarifications WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        String.valueOf(rs.getInt("reply_id")),
                        rs.getString("reply_text"),
                        rs.getString("created_by")
                    });
                }
            }
        }
        return list;
    }


    public boolean updateReply(int replyId, String newText, String currentUser, String currentUserRole)
        throws SQLException
    {
        String checkCreator = "SELECT created_by FROM clarifications WHERE reply_id = ?";
        String creator = null;
        try (PreparedStatement pstmt = connection.prepareStatement(checkCreator)) {
            pstmt.setInt(1, replyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    creator = rs.getString("created_by");
                }
            }
        }
        if (creator == null) {
            return false;
        }
        if (!currentUserRole.equals("admin") && !creator.equals(currentUser)) {
            throw new SecurityException("No permission to update this reply.");
        }

        String update = "UPDATE clarifications SET reply_text = ? WHERE reply_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(update)) {
            pstmt.setString(1, newText.trim());
            pstmt.setInt(2, replyId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a reply
     */
    public boolean deleteReply(int replyId, String currentUser, String currentUserRole)
        throws SQLException
    {
        String checkCreator = "SELECT created_by FROM clarifications WHERE reply_id = ?";
        String creator = null;
        try (PreparedStatement pstmt = connection.prepareStatement(checkCreator)) {
            pstmt.setInt(1, replyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    creator = rs.getString("created_by");
                }
            }
        }
        if (creator == null) {
            return false;
        }
        if (!currentUserRole.equals("admin") && !creator.equals(currentUser)) {
            throw new SecurityException("No permission to delete this reply.");
        }

        String delete = "DELETE FROM clarifications WHERE reply_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(delete)) {
            pstmt.setInt(1, replyId);
            return pstmt.executeUpdate() > 0;
        }
    }


    // Close

    public void closeConnection() {
        try {
            if(statement!=null) statement.close();
        } catch(SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if(connection!=null) connection.close();
        } catch(SQLException se) {
            se.printStackTrace();
        }
    }
}
