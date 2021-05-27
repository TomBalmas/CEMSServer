package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.table.TableModel;

import common.ActiveTest;
import common.Course;
import common.FinishedTest;
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
		String[] fields = fieldsString.split("~");
		try {
			stmt = conn.createStatement();
			ResultSet rs;
			for (String field : fields) {
				rs = stmt.executeQuery("SELECT * FROM tests WHERE field = '" + field + "'");
				while (rs.next())
					tests.add(GeneralQueryMethods.createTest(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

	/**
	 * gets all the scheduled tests of a given scheduler id
	 * 
	 * @param authorId
	 * @return scheduled tests array list
	 */
	public static ArrayList<ScheduledTest> getScheduledTestsByAuthorID(String schedulerId) {
		ArrayList<ScheduledTest> tests = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM scheduled_tests st, tests t WHERE st.scheduledByTeacher = '"
					+ schedulerId + "' AND t.testId = st.testId");
			while (rs.next()) {
				tests.add(GeneralQueryMethods.createScheduledTest(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

	// "SELECT * FROM scheduled_tests st, tests t WHERE st.scheduledByTeacher = '" +
	// schedulerId + "' AND t.testId = st.testId"

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
				if (rs.next())
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
	public static boolean deleteTestByID(String testId) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM tests WHERE testId='" + testId + "'");
			stmt.executeUpdate("DELETE FROM scheduled_tests WHERE testId = '" + testId + "'");
			stmt.executeUpdate("DELETE FROM active_tests WHERE testId = '" + testId + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	/**
	 * gets all active tests of the given author id
	 * 
	 * @param authorId
	 * @return array of active tests
	 */
	public static ArrayList<ActiveTest> getActiveTestsByAuthorId(String authorId) {
		Statement stmt;
		ArrayList<ActiveTest> activeTests = new ArrayList<>();
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM scheduled_tests s1, active_tests a1 WHERE s1.scheduledByTeacher = '"
							+ authorId + "' AND a1.testId = s1.testId AND a1.startingTime = s1.startingTime");
			while (rs.next()) {
				activeTests.add(GeneralQueryMethods.createActiveTest(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return activeTests;
	}

	/**
	 * sets date and time for a test (adds it to the scheduled_tests table)
	 * 
	 * @param args - testId,date,startingTime,scheduledBy,code
	 * @return - true if the date was set
	 */
	public static boolean setTestDate(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String date = details[1];
		String startingTime = details[2];
		String scheduledBy = details[3];
		String code = details[4];
		try {
			LocalDate now = LocalDate.now();
			Date today = new SimpleDateFormat("dd-mm-yyyy").parse(now.toString());
			Date testDate = new SimpleDateFormat("dd-mm-yyyy").parse(date);
			if (testDate.compareTo(today) >= 0)
				return false;
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT duration FROM tests WHERE testId = '" + testId + "'");
			rs.next();
			stmt.executeUpdate("INSERT INTO scheduled_tests VALUES ('" + testId + "', '" + date + "', '" + startingTime
					+ "', '" + rs.getInt("duration") + "', '" + scheduledBy + "', '" + code + "')");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * gets all the courses of the given field
	 * 
	 * @param field
	 * @return courses that belong to the given field
	 */
	public static ArrayList<Course> getCoursesByField(String field) {
		ArrayList<Course> courses = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT courseName FROM courses WHERE field = '" + field + "'");
			while (rs.next())
				courses.add(new Course(rs.getString("courseName")));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return courses;
	}

	/**
	 * gets all finished tests of the given scheduler id
	 * 
	 * @param args - id of the teacher who scheduled the tests
	 * @return - array list of finished tests
	 */
	public static ArrayList<FinishedTest> getFinishedTestsBySchedulerSSN(String args) {
		ArrayList<FinishedTest> finishedTests = new ArrayList<>();
		String ssn = args;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM finished_tests ft, tests t WHERE scheduler = '" + ssn
					+ "' AND t.testId = ft.testId");
			while (rs.next())
				finishedTests.add(GeneralQueryMethods.createFinishedTest(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return finishedTests;
	}

	/**
	 * adds a new test given the details in the right order, test id will be added
	 * automatically
	 * 
	 * @param args -
	 *             author,title,course,duration,pointsPerQuestion,studentInstructions,TeacherInstructions,questions,field
	 * @return test id as string
	 */
	public static String addNewTest(String args) {
		String[] details = args.split(",");
		String author = details[0];
		String title = details[1];
		String course = details[2];
		Integer duration = Integer.parseInt(details[3]);
		Integer pointsPerQuestion = Integer.parseInt(details[4]);
		String studentInstructions = null;
		String teacherInstructions = null;
		if (!details[5].equals("null"))
			studentInstructions = details[5];
		if (!details[6].equals("null"))
			teacherInstructions = details[6];
		String questions = details[7];
		String field = details[8];
		String testId = Queries.getAvailableId("tests,course,testId," + course);
		if (testId == null)
			return null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO tests VALUES ('" + testId + "', '" + author + "', '" + title + "', '"
					+ course + "', " + duration + ", " + pointsPerQuestion + ", '" + studentInstructions + "', '"
					+ teacherInstructions + "', '" + questions + "', '" + field + "');");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "testAdded:" + testId;
	}

	/**
	 * returns an available id
	 * 
	 * @param args - tableName,columnToCompare,iDColumn,argument
	 * @return - the available id as a string
	 */
	private static String getAvailableId(String args) {
		Integer lastId = null;
		Integer currentId = null;
		String[] details = args.split(",");
		String tableName = details[0];
		String columnToCompare = details[1];
		String iDColumn = details[2];
		String arg = details[3];
		String newId = null;
		int lengthOfTestId = 6;
		int lengthOfQuestionId = 5;
		Integer tempId;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT " + iDColumn + " FROM " + tableName + " WHERE " + columnToCompare
					+ " = '" + arg + "' " + "ORDER BY " + iDColumn);

			// case of empty bank
			if (!rs.next()) {
				rs = stmt.executeQuery("SELECT fieldId, courseId FROM fields f, courses c WHERE (f.fieldName = '"
						+ arg + "') OR (c.courseName = '" + arg + "' AND f.fieldName = c.field);" );
				rs.next();
				if (tableName.equals("questions"))
					newId = rs.getString("fieldId") + "000";
				else if (tableName.equals("tests"))
					newId = rs.getString("fieldId") + rs.getString("courseId") + "00";
				return newId;
			}
			// check if there is a lower id available
			if (tableName.equals("tests")) {
				currentId = rs.getInt("testId");
				tempId = currentId - 1;
				if (!tempId.toString().endsWith("99")) {
					newId = tempId.toString();
					while (newId.length() < lengthOfTestId)
						newId = "0" + newId;
					return newId;
				}
			} else if (tableName.equals("questions")) {
				currentId = rs.getInt("questionId");
				tempId = currentId - 1;
				if (!tempId.toString().endsWith("999")) {
					newId = tempId.toString();
					while (newId.length() < lengthOfQuestionId)
						newId = "0" + newId;
					return newId;
				}
			}

			// check if there is a gap between id numbers
			while (rs.next()) {
				lastId = rs.getInt("testId");
				if (lastId - currentId != 1) {
					currentId += 1;
					if (tableName.equals("tests")) {
						newId = currentId.toString();
						while (newId.length() < lengthOfTestId)
							newId = "0" + newId;
						rs.close();
						return newId;
					} else if (tableName.equals("questions")) {
						newId = currentId.toString();
						while (newId.length() < lengthOfQuestionId)
							newId = "0" + newId;
					}

				}
				currentId = lastId;
			}

			// check if there is room to add id as the highest number
			if (tableName.equals("tests")) {
				if (currentId.toString().endsWith("99"))
					return null;
				else {
					currentId += 1;
					newId = currentId.toString();
					while (newId.length() < lengthOfTestId)
						newId = "0" + newId;
				}
			} else if (tableName.equals("questions"))
				if (currentId.toString().endsWith("999"))
					return null;
				else {
					currentId += 1;
					newId = currentId.toString();
					while (newId.length() < lengthOfQuestionId)
						newId = "0" + newId;
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newId;
	}

	/**
	 * deletes a question from the DB given its id
	 * 
	 * @param questionId
	 * @return - true if the question was deleted
	 */
	public static boolean deleteQuestionById(String questionId) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM questions WHERE questionId='" + questionId + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * adds a new question given the details in the right order, question id will be
	 * added automatically
	 * 
	 * @param args -
	 *             author,questionContent,correctAnswer,field,answer1,answer2,answer3,answer4
	 * @return - question id as string
	 */
	public static String addQuestion(String args) {
		Statement stmt;
		String[] details = args.split(",");
		String author = details[0];
		String questionContent = details[1];
		Integer correctAnswer = Integer.valueOf(details[2]);
		String field = details[3];
		String answer1 = details[4];
		String answer2 = details[5];
		String answer3 = details[6];
		String answer4 = details[7];
		String questionId = Queries.getAvailableId("questions,field,questionId," + field);
		if (questionId == null)
			return null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO questions VALUES ('" + questionId + "', '" + questionContent + "', '"
					+ author + "', '" + field + "', '" + answer1 + "', '" + answer2 + "', '" + answer3 + "', '"
					+ answer4 + "', " + correctAnswer + ");");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return "questionAdded:" + questionId;
	}

	/**
	 * getter for questions table
	 * 
	 * @return the entire questions table as array list
	 */
	public static ArrayList<Question> getQuestionsTable() {
		ArrayList<Question> questions = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM questions");
			while (rs.next())
				questions.add(GeneralQueryMethods.createQuestion(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questions;
	}

	/**
	 * getter for tests table
	 * 
	 * @return the entire tests table as array list
	 */
	public static ArrayList<Test> getTestsTable() {
		ArrayList<Test> tests = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM questions");
			while (rs.next())
				tests.add(GeneralQueryMethods.createTest(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

	/**
	 * changes details of a question in the DB
	 * 
	 * @param args -
	 *             questionId,author,questionContent,correctAnswer,field,answer1,answer2,answer3,answer4
	 * @return true if the question was edited successfully
	 */
	public static boolean editQuestion(String args) {
		String[] details = args.split(",");
		String questionId = details[0];
		String author = details[1];
		String questionContent = details[2];
		Integer correctAnswer = Integer.parseInt(details[3]);
		String field = details[4];
		String answer1 = details[5];
		String answer2 = details[6];
		String answer3 = details[7];
		String answer4 = details[8];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE questions SET author = '" + author + "', questionContent = '" + questionContent
					+ "', correctAnswer = " + correctAnswer + ", field = '" + field + "', answer1 = '" + answer1
					+ "', answer2 = '" + answer2 + "', answer3 = '" + answer3 + "', answer4 = '" + answer4
					+ "' WHERE questionId = '" + questionId + "';");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * changes details of a test in the DB
	 * 
	 * @param args -
	 *             testId,authorName,title,course,testDuration,pointsPerQuestion,studentInstructions,teacherInstructions,questionsString,field
	 * @return true if the test was edited successfully
	 */
	public static boolean editTest(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String author = details[1];
		String title = details[2];
		String course = details[3];
		Integer duration = Integer.parseInt(details[4]);
		Integer pointsPerQuestion = Integer.parseInt(details[5]);
		String studentInstructions = null;
		String teacherInstructions = null;
		if (!details[5].equals("null"))
			studentInstructions = details[5];
		if (!details[6].equals("null"))
			teacherInstructions = details[6];
		String questions = details[8];
		String field = details[9];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE tests SET author = '" + author + "', title = '" + title + "', course = '"
					+ course + "', testDuration = " + duration + ", pointsPerQuestion = " + pointsPerQuestion
					+ ", studentInstructions = '" + studentInstructions + "', teacherInstructions = '"
					+ teacherInstructions + "', questions = '" + questions + "', field = '" + field
					+ "' WHERE testId = '" + testId + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
