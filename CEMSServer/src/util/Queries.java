package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import common.ActiveTest;
import common.AbstractTest;
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
					return new Student(rs.getInt("SSN"), rs.getString("name"), rs.getString("surname"),
							rs.getString("email"), rs.getString("username"), rs.getString("password"));
				case "Teacher":
					// getting teachers fields from DB and inserting into teachers arrayList

					return new Teacher(rs.getInt("SSN"), rs.getString("name"), rs.getString("surname"),
							rs.getString("email"), rs.getString("username"), rs.getString("password"),
							rs.getString("fields"));
				case "Principle":
					return new Principle(rs.getInt("SSN"), rs.getString("name"), rs.getString("surname"),
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
	 * gets all questions from the given fields
	 * 
	 * @param fields
	 * @return Question array list
	 */
	public static ArrayList<Question> getQuestions(String fields) {
		ArrayList<Question> questions = new ArrayList<>();
		String[] arr;
		String temp = fields.substring(1, fields.length() - 1);
		arr = temp.split(",");
		ArrayList<String> array = new ArrayList<>(Arrays.asList("x", "x", "x", "x", "x", "x"));
		for (int i = 0; i < arr.length; i++)
			array.add(i, arr[i].trim());
		ArrayList<String> answers = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM questions WHERE field = '" + array.get(0) + "' OR field = '"
					+ array.get(1) + "' OR field = '" + array.get(2) + "' OR field = '" + array.get(3)
					+ "' OR field = '" + array.get(4) + "' OR field = '" + array.get(4) + "'");
			while (rs.next()) {
				answers.add(rs.getString("answer1"));
				answers.add(rs.getString("answer2"));
				answers.add(rs.getString("answer3"));
				answers.add(rs.getString("answer4"));
				questions.add(new Question(rs.getInt("ID"), rs.getString("author"),
						rs.getString("instructionsForTeacher"), rs.getString("instructionsForStudent"),
						rs.getString("questionContent"), rs.getInt("correctAnswer"), rs.getString("field"), answers));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return questions;
	}

	/**
	 * gets all the tests of the given fields
	 * 
	 * @param fields
	 * @return Test array list
	 */
	public static ArrayList<Test> getTestsByField(String fields) {
		ArrayList<Test> tests = new ArrayList<>();
		String[] arr;
		String temp = fields.substring(1, fields.length() - 1);
		arr = temp.split(",");
		ArrayList<String> array = new ArrayList<>(Arrays.asList("x", "x", "x", "x", "x", "x"));
		for (int i = 0; i < arr.length; i++)
			array.add(i, arr[i].trim());
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests WHERE field = '" + array.get(0) + "' OR field = '"
					+ array.get(1) + "' OR field = '" + array.get(2) + "' OR field = '" + array.get(3)
					+ "' OR field = '" + array.get(4) + "' OR field = '" + array.get(4) + "'");
			while (rs.next()) {
				tests.add(new Test(rs.getInt("id"), rs.getString("author"), rs.getString("title"),
						rs.getString("course"), rs.getString("testDuartion"), rs.getString("pointsPerQuestion"),
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
					.executeQuery("SELECT * FROM scheduled_tests WHERE 'scheduled by (id)' = '" + authorId + "'");
			while (rs.next()) {
				tests.add(new ScheduledTest(rs.getInt("ID"), null, null, null, rs.getDate("date"),
						rs.getTime("starting time"), rs.getInt("duration"), rs.getInt("scheduled by (id)")));
			}
			for (ScheduledTest test : tests) {
				rs = stmt.executeQuery("SELECT * FROM tests WHERE ID = '" + test.getID() + "'");
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
				test = new Test(rs.getInt("id"), rs.getString("author"), rs.getString("title"), rs.getString("course"),
						rs.getString("testDuartion"), rs.getString("pointsPerQuestion"), rs.getString("instructions"),
						rs.getString("teacherInstructions"), rs.getString("questionsInTest"), rs.getString("field"));
				return test;
			case "ScheduledTest":
				rs.next();
				scheduledTest = new ScheduledTest(rs.getInt("id"), rs.getString("author"), rs.getString("title"),
						rs.getString("course"), null, null, null, null);
				rs = stmt.executeQuery("SELECT * FROM scheduled_tests WHERE ID = '" + scheduledTest.getID() + "'");
				rs.next();
				scheduledTest.setDate(rs.getDate("date"));
				scheduledTest.setStartingTime(rs.getTime("starting time"));
				scheduledTest.setDuration(rs.getInt("duration"));
				scheduledTest.setBelongsTo(rs.getInt("scheduled by (id)"));
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
		ArrayList<Question> questions = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT questionInTest FROM tests WHERE id='" + test.getID() + "'");
			String questionString = rs.getString(0);
			String[] arr = questionString.split(":");
			for (String str : arr) {
				ArrayList<String> answers = new ArrayList<>();
				rs = stmt.executeQuery("SELECT * FROM questions WHERE id='" + str + "'");
				rs.next();
				answers.add(rs.getString("answer1"));
				answers.add(rs.getString("answer2"));
				answers.add(rs.getString("answer3"));
				answers.add(rs.getString("answer4"));
				questions.add(new Question(rs.getInt(0), rs.getString(1), rs.getString(2), rs.getString(3),
						rs.getString(4), rs.getInt(5), rs.getString(6), answers));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questions;
	}

	public static boolean deleteTestByID(String id) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM tests WHERE id='" + id + "'");
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
			while(rs.next()) {
				activeTest.add(new ActiveTest( rs.getInt("ID"), rs.getString("name"), rs.getString("course"), 
						rs.getString("author"),rs.getString("field"),rs.getString("starttime"), rs.getString("andtimetest")));

			}
			
		} catch (SQLException e) {
		}
		return activeTest;
	}

}
