package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import common.AbstractTest;
import common.ActiveTest;
import common.Principle;
import common.Question;
import common.ScheduledTest;
import common.Student;
import common.Teacher;
import common.Test;
import common.User;

public class Queries {

	private static Connection conn = null;
	private static Statement stmt;

	public Queries(Connection conn) {
		this.conn = conn;
	}

	/**
	 * gets the user with the given user name and password
	 * 
	 * @param username
	 * @param password
	 * @return User
	 */
	public static User getUser(String username, String password) {
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'");
			if (rs.next())
				switch (rs.getString("role")) {
				case "Student":
					return new Student(rs.getString("ssn"), rs.getString("name"), rs.getString("surname"),
							rs.getString("email"), rs.getString("username"), rs.getString("password"));
				case "Teacher":
					// getting teachers fields from DB and inserting into teachers arrayList

					return new Teacher(rs.getString("ssn"), rs.getString("name"), rs.getString("surname"),
							rs.getString("email"), rs.getString("username"), rs.getString("password"),
							rs.getString("fields"));
				case "Principle":
					return new Principle(rs.getString("ssn"), rs.getString("name"), rs.getString("surname"),
							rs.getString("email"), rs.getString("username"), rs.getString("password"));
				default:
					return null;
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * query that returns all questions from the given fields
	 * 
	 * @param fieldsString a string of fields split by ~
	 * @return Question array list
	 */
	public static ArrayList<Question> getQuestionsByFields(String fieldsString) {
		ArrayList<Question> questions = new ArrayList<>();
		fieldsString = GeneralQueryMethods.trimEdges(fieldsString);
		String[] fields = fieldsString.split("~");
		try {
			stmt = conn.createStatement();
			ResultSet rs;
			for (String field : fields) {
				rs = stmt.executeQuery("SELECT * FROM questions WHERE field = '" + field + "'");
				while (rs.next())
					questions.add(GeneralQueryMethods.createQuestion(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questions;
	}

	/**
	 * query that returns all test from the given fields
	 * 
	 * @param fieldsString a string of fields split by ~
	 * @return Test array list
	 */
	public static ArrayList<Test> getTestsByField(String fieldsString) {
		ArrayList<Test> tests = new ArrayList<>();
		fieldsString = GeneralQueryMethods.trimEdges(fieldsString);
		String[] fields = fieldsString.split("~");
		try {
			stmt = conn.createStatement();
			ResultSet rs;
			for (String field : fields) {
				rs = stmt.executeQuery("SELECT * FROM tests WHERE field = '" + field + "'");
				while (rs.next())
					tests.add(new Test(rs.getString("testId"), rs.getString("author"), rs.getString("title"),
							rs.getString("course"), rs.getInt("testDuration"), rs.getInt("pointsPerQuestion"),
							rs.getString("instructions"), rs.getString("teacherInstructions"),
							rs.getString("questionsInTest"), rs.getString("field")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

	/**
	 * gets all the scheduled tests of a given author id
	 * 
	 * @param authorId
	 * @return scheduled tests array list
	 */
	public static ArrayList<ScheduledTest> getScheduledTestsByAuthorID(String authorId) {
		ArrayList<ScheduledTest> tests = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM scheduled_tests WHERE scheduledByTeacher = '" + authorId + "'");
			while (rs.next()) {
				tests.add(new ScheduledTest(rs.getString("testId"), null, null, null, rs.getDate("date"),
						rs.getTime("startingTime"), rs.getInt("duration"), rs.getString("scheduledByTeacher")));
			}
			for (ScheduledTest test : tests) {
				rs = stmt.executeQuery("SELECT * FROM tests WHERE testId = '" + test.getID() + "'");
				rs.next();
				test.setAuthorName(rs.getString("author"));
				test.setCourse(rs.getString("course"));
				test.setTitle(rs.getString("title"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

	/**
	 * gets a test given the test id
	 * 
	 * @param testType - which type of tests to return
	 * @param testID
	 * @return - returns the test with the given id
	 */
	public static AbstractTest getTestByID(String testType, String testID) {
		Test test;
		ScheduledTest scheduledTest;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests WHERE id = '" + Integer.parseInt(testID) + "'");
			switch (testType) {
			case "Test":
				rs.next();
				test = new Test(rs.getString("id"), rs.getString("author"), rs.getString("title"),
						rs.getString("course"), rs.getInt("testDuartion"), rs.getInt("pointsPerQuestion"),
						rs.getString("instructions"), rs.getString("teacherInstructions"),
						rs.getString("questionsInTest"), rs.getString("field"));
				return test;
			case "ScheduledTest":
				rs.next();
				scheduledTest = new ScheduledTest(rs.getString("testId"), rs.getString("author"), rs.getString("title"),
						rs.getString("course"), null, null, null, null);
				rs = stmt.executeQuery("SELECT * FROM scheduled_tests WHERE ID = '" + scheduledTest.getID() + "'");
				rs.next();
				scheduledTest.setDate(rs.getDate("date"));
				scheduledTest.setStartingTime(rs.getTime("starting time"));
				scheduledTest.setDuration(rs.getInt("duration"));
				scheduledTest.setBelongsToID(rs.getString("scheduledByTeacher"));
				return scheduledTest;
			case "ActiveTest":
				break;
			default:
				break;
			}
		} catch (

		SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * gets all the questions that belong to the given test question
	 * 
	 * @param test
	 * @return Question array list
	 */
	public static ArrayList<Question> getQuestionFromTest(Test test) {
		ArrayList<Question> questionsArray = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT questionInTest FROM tests WHERE testId='" + test.getID() + "'");
			rs.next();
			String questionString = rs.getString("questionsInTest");
			String[] questions = questionString.split("~");
			for (String question : questions) {
				rs = stmt.executeQuery("SELECT * FROM questions WHERE questionId='" + question + "'");
				rs.next();
				questionsArray.add(GeneralQueryMethods.createQuestion(rs));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questionsArray;
	}

	/**
	 * deletes a test from the DB given the test's id
	 * 
	 * @param id - test's id
	 * @return true if the test was deleted,
	 */
	public static boolean deleteTestByID(String id) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM tests WHERE testId='" + id + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public static ArrayList<ActiveTest> getActiveTests() {
		Statement stmt;
		ArrayList<ActiveTest> activeTest = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM activetest");
			while (rs.next()) {
				activeTest.add(new ActiveTest(rs.getString("testId"), rs.getString("name"), rs.getString("course"),
						rs.getString("author"), rs.getString("field"), rs.getString("starttime"),
						rs.getString("andtimetest")));

			}

		} catch (SQLException e) {
		}
		return activeTest;
	}

}
