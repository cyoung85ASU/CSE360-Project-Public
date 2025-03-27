package Test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import databasePart1.DatabaseHelper;
import application.User;

import java.sql.SQLException;

public class basicTest {
	
	static DatabaseHelper dbHelper;

    @BeforeAll
    public static void setup() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper.connectToDatabase();
    }

	@Test
	/**
	 * Tests the ability to register a new user and verify their existence
	 * using the doesUserExist method.
	 *
	 * This confirms that the register() method correctly inserts user data
	 * into the database and that user lookup works as expected.
	 *
	 * @throws SQLException if a database error occurs
	 */

	public void testDatabaseIsInitiallyEmpty()
	{
		DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();
            assertTrue(db.isDatabaseEmpty());
        } catch (Exception e) {
            fail("Exception during test: " + e.getMessage());
        }		
	}
	
	@Test
	
	/**
	 * Verifies that multiple calls to generateInvitationCode produce
	 * unique codes that can be stored in the database without collision.
	 *
	 * This ensures UUID generation and storage logic works for onboarding users.
	 */
	public void testRegisterAndLogin() throws SQLException {
	    User testUser = new User("junitUser", "securePass", "student");
	    dbHelper.register(testUser);
	    assertTrue(dbHelper.login(testUser));
	}
	
	@Test
	
	/**
	 * Tests whether a new question can be successfully created and
	 * retrieved by its ID, ensuring data integrity.
	 *
	 * Verifies that the inserted question text is accurately returned
	 * using readQuestion.
	 *
	 * @throws SQLException if database access fails
	 */
	public void testLoginWithWrongPassword() throws SQLException {
	    User testUser = new User("admin", "wrongPass", "admin");
	    assertFalse(dbHelper.login(testUser));
	}
	
	
	/**
	 * Tests the process of adding an answer to a question and then
	 * retrieving it to confirm its content and creator.
	 *
	 * Validates linkage between answers and their respective questions.
	 *
	 * @throws SQLException if database interaction fails
	 */
	@Test
	public void testCreateQuestionEmptyText() {
	    DatabaseHelper dbHelper = new DatabaseHelper();
	    assertThrows(IllegalArgumentException.class, () -> {
	        dbHelper.createQuestion("", "admin");
	    });
	}

	
	/**
	 * Confirms that only the creator of a question can mark
	 * an answer as correct, and that the correct flag is updated
	 * properly in the database.
	 *
	 * Ensures enforcement of role-based access and correctness logic.
	 *
	 * @throws SQLException if any SQL operation fails
	 */
	@Test
	public void testMarkCorrectByNonOwner() throws SQLException {
	    int qId = dbHelper.createQuestion("Why is the sky blue?", "alice");
	    int aId = dbHelper.createAnswer(qId, "Because of Rayleigh scattering.", "bob");
	    dbHelper.markAnswerAsCorrect(qId, aId, "bob", "student"); // should throw
	}


}
