package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import common.ActiveTest;
import common.Course;
import common.FinishedTest;
import common.Principle;
import common.Question;
import common.Report;
import common.ScheduledTest;
import common.Student;
import common.StudentGrade;
import common.Teacher;
import common.Test;
import common.TimeExtensionRequest;
import common.User;
import javafx.util.Pair;

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
	 * @param schedulerId
	 * @return scheduled tests array list
	 */
	public static ArrayList<ScheduledTest> getScheduledTestsBySchedulerID(String schedulerId) {
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
	 * gets all active tests of the given schedulerId id
	 * 
	 * @param schedulerId
	 * @return array of active tests
	 */
	public static ArrayList<ActiveTest> getActiveTestsBySchedulerId(String schedulerId) {
		Statement stmt1;
		ArrayList<ActiveTest> activeTests = new ArrayList<>();
		ArrayList<ActiveTest> temp = new ArrayList<>();
		try {
			stmt1 = conn.createStatement();
			ResultSet rs = stmt1
					.executeQuery("SELECT * FROM scheduled_tests s1, active_tests a1 WHERE s1.scheduledByTeacher = '"
							+ schedulerId + "' AND a1.testId = s1.testId AND a1.startingTime = s1.startingTime");
			while (rs.next())
				activeTests.add(GeneralQueryMethods.createActiveTest(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (ActiveTest activeTest : activeTests) {
			if (!temp.contains(activeTest))
				temp.add(activeTest);
		}
		return temp;
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
		Statement stmt;
		ResultSet rs;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM scheduled_tests WHERE beginTestCode = '" + code + "'");
			if (rs.next())
				return false;
			rs = stmt.executeQuery("SELECT duration FROM tests WHERE testId = '" + testId + "'");
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
			ResultSet rs = stmt.executeQuery("SELECT * FROM courses WHERE field = '" + field + "'");
			while (rs.next())
				courses.add(GeneralQueryMethods.createCourse(rs));
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
	public static ArrayList<FinishedTest> getFinishedTestsBySchedulerSSN(String schedulerSSN) {
		ArrayList<FinishedTest> finishedTests = new ArrayList<>();
		String ssn = schedulerSSN;
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
	 *             authorId,title,course,duration,pointsPerQuestion,studentInstructions,TeacherInstructions,questions,field
	 * @return test id as string
	 */
	public static String addNewTest(String args) {
		String[] details = args.split(",");
		String authorId = details[0];
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
		String testId = Queries.getAvailableIdForTestOrQuestion("tests,course,testId," + course);
		if (testId == null)
			return null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO tests VALUES ('" + testId + "', '" + authorId + "', '" + title + "', '"
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
	 * @param args - tableName,columnToCompare,iDColumn,argument argument is
	 *             courseName or fieldName
	 * @return - the available id as a string
	 */
	private static String getAvailableIdForTestOrQuestion(String args) {
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
				rs = stmt.executeQuery("SELECT fieldId, courseId FROM fields f, courses c WHERE (f.fieldName = '" + arg
						+ "') OR (c.courseName = '" + arg + "' AND f.fieldName = c.field);");
				rs.next();
				if (tableName.equals("questions"))
					newId = rs.getString("fieldId") + "000";
				else if (tableName.equals("tests"))
					newId = rs.getString("fieldId") + rs.getString("courseId") + "00";
				return newId;
			}
			// check if there is a lower id available
			if (tableName.equals("tests")) { // tests
				currentId = rs.getInt("testId");
				tempId = currentId - 1;
				if (!tempId.toString().endsWith("99")) {
					newId = tempId.toString();
					newId = newId.substring(0, newId.length() - 2);
					newId = newId + "00";
					while (newId.length() < lengthOfTestId)
						newId = "0" + newId;
					return newId;
				}
			} else if (tableName.equals("questions")) { // questions
				currentId = rs.getInt("questionId");
				tempId = currentId - 1;
				if (!tempId.toString().endsWith("999")) {
					newId = tempId.toString();
					newId = newId.substring(0, newId.length() - 3);
					newId = newId + "000";
					while (newId.length() < lengthOfQuestionId)
						newId = "0" + newId;
					return newId;
				}
			}

			// check if there is a gap between id numbers
			while (rs.next()) {
				if (tableName.equals("tests")) { // tests
					lastId = rs.getInt("testId");
					if (lastId - currentId != 1) {
						currentId += 1;
						newId = currentId.toString();
						while (newId.length() < lengthOfTestId)
							newId = "0" + newId;
						rs.close();
						return newId;
					}

				} else if (tableName.equals("questions")) {// questions
					lastId = rs.getInt("questionId");
					if (lastId - currentId != 1) {
						currentId += 1;
						newId = currentId.toString();
						while (newId.length() < lengthOfQuestionId)
							newId = "0" + newId;
						rs.close();
						return newId;
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
		Statement stmt1, stmt2;
		String tempQuestions = null;
		try {
			stmt1 = conn.createStatement();
			stmt2 = conn.createStatement();
			ResultSet rs = stmt1.executeQuery(
					"SELECT testId, questionsInTest FROM tests WHERE questionsInTest LIKE '%" + questionId + "%'");
			while (rs.next()) {
				tempQuestions = rs.getString("questionsInTest");
				tempQuestions = tempQuestions.replace("~" + questionId + "~", "~");
				stmt2.executeUpdate("UPDATE tests SET questionsInTest = '" + tempQuestions + "' WHERE testId = '"
						+ rs.getString("testId") + "'");
			}
			stmt1.executeUpdate("DELETE FROM questions WHERE questionId='" + questionId + "'");
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
	 *             authorId,questionContent,correctAnswer,field,answer1,answer2,answer3,answer4
	 * @return - question id as string
	 */
	public static String addQuestion(String args) {
		Statement stmt;
		String[] details = args.split(",");
		String authorId = details[0];
		String questionContent = details[1];
		Integer correctAnswer = Integer.valueOf(details[2]);
		String field = details[3];
		String answer1 = details[4];
		String answer2 = details[5];
		String answer3 = details[6];
		String answer4 = details[7];
		String questionId = Queries.getAvailableIdForTestOrQuestion("questions,field,questionId," + field);
		if (questionId == null)
			return null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO questions VALUES ('" + questionId + "', '" + questionContent + "', '"
					+ authorId + "', '" + field + "', '" + answer1 + "', '" + answer2 + "', '" + answer3 + "', '"
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
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests");
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
	 *             questionId,authorId,questionContent,correctAnswer,field,answer1,answer2,answer3,answer4
	 * @return true if the question was edited successfully
	 */
	public static boolean editQuestion(String args) {
		String[] details = args.split(",");
		String questionId = details[0];
		String authorId = details[1];
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
			stmt.executeUpdate("UPDATE questions SET authorId = '" + authorId + "', questionContent = '"
					+ questionContent + "', correctAnswer = " + correctAnswer + ", field = '" + field + "', answer1 = '"
					+ answer1 + "', answer2 = '" + answer2 + "', answer3 = '" + answer3 + "', answer4 = '" + answer4
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
	 *             testId,authorId,title,course,duration,pointsPerQuestion,studentInstructions,teacherInstructions,questionsString,field
	 * @return true if the test was edited successfully
	 */
	public static boolean editTest(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String authorId = details[1];
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
			stmt.executeUpdate("UPDATE tests SET authorId = '" + authorId + "', title = '" + title + "', course = '"
					+ course + "', duration = " + duration + ", pointsPerQuestion = " + pointsPerQuestion
					+ ", studentInstructions = '" + studentInstructions + "', teacherInstructions = '"
					+ teacherInstructions + "', questions = '" + questions + "', field = '" + field
					+ "' WHERE testId = '" + testId + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * gets all the questions of the given authorId
	 * 
	 * @param authorId - ssn of the author
	 * @return string array list of question IDs
	 */
	public static ArrayList<String> getQuestionsByAuthorId(String authorId) {
		ArrayList<String> questions = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT questionId FROM questions WHERE authorId = '" + authorId + "'");
			while (rs.next())
				questions.add(rs.getString("questionId"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questions;
	}

	/**
	 * gets all the tests of the given authorId
	 * 
	 * @param authorId - ssn of the author
	 * @return string array list of test IDs
	 */
	public static ArrayList<String> getTestsByAuthorId(String authorId) {
		ArrayList<String> tests = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT testId FROM test WHERE authorId = '" + authorId + "'");
			while (rs.next())
				tests.add(rs.getString("testId"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

	/**
	 * gets the name of the given ssn
	 * 
	 * @param userId - ssn of the user
	 * @return string with the name of the user
	 */
	public static String getNameById(String userId) {
		String name = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name FROM users WHERE ssn = '" + userId + "'");
			if (rs.next())
				name = rs.getString("name");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * gets all the time extension requests
	 * 
	 * @return array list of time extension requests
	 */
	public static ArrayList<TimeExtensionRequest> getTimeExtensionRequests() {
		ArrayList<TimeExtensionRequest> requests = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM time_extension_requests");
			while (rs.next())
				requests.add(GeneralQueryMethods.createTimeExtensionRequest(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return requests;
	}

	/**
	 * adds a time request to the time_extension_requests table if the test is
	 * already in the table, it updates its content
	 * 
	 * @param args - teacherSSN,content,testCode,minutes
	 * @return true if request was successfully added
	 */
	public static boolean addTimeExtensionRequest(String args) {
		String[] details = args.split(",");
		String teacherSSN = details[0];
		String content = details[1];
		String code = details[2];
		int minutes = Integer.parseInt(details[3]);
		Statement stmt1;
		Statement stmt2;
		ResultSet rs;
		try {
			stmt1 = conn.createStatement();
			stmt2 = conn.createStatement();
			rs = stmt1.executeQuery("SELECT * FROM time_extension_requests WHERE testCode = '" + code + "'");
			if (rs.next())
				stmt2.executeUpdate("UPDATE time_extension_requests SET content = '" + content + "', minutes = "
						+ minutes + " WHERE testCode = '" + code + "'");
			else
				stmt1.executeUpdate("INSERT INTO time_extension_requests VALUES ('" + teacherSSN + "', '" + content
						+ "', '" + code + "', " + minutes + ");");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * deletes a request from the time_extension_requests table
	 * 
	 * @param code - the test's code
	 * @return true if the request was deleted
	 */
	public static boolean deleteTimeExtensionRequest(String code) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM time_extension_requests WHERE testCode = '" + code + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * gets the reports table
	 * 
	 * @return report array list
	 */
	public static ArrayList<Report> getReportsTable() {
		ArrayList<Report> reports = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM statistic_reports");
			while (rs.next())
				reports.add(GeneralQueryMethods.createTestReport(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reports;
	}

	/**
	 * deletes a report from the reports table
	 * 
	 * @param args - report id
	 * @return true if the report was deleted
	 */
	public static boolean deleteReport(String reportId) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM statistic_reports WHERE reportId = '" + reportId + "'");
			stmt.executeUpdate("DELETE FROM course_statistics WHERE reportId = '" + reportId + "'");
			stmt.executeUpdate("DELETE FROM student_statistics WHERE reportId = '" + reportId + "'");
			stmt.executeUpdate("DELETE FROM teacher_statistics WHERE reportId = '" + reportId + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * gets author name given a test's id
	 * 
	 * @param testId
	 * @return the name of the author
	 */
	public static String getAuthorNameByTestId(String testId) {
		String name = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT authorId FROM tests WHERE testId = '" + testId + "'");
			rs.next();
			name = getNameById(rs.getString("authorId"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * gets all the questions belong to the test id
	 * 
	 * @param testId
	 * @return array list of questions
	 */
	public static ArrayList<Question> getQuestionsFromTest(String testId) {
		String questionsString;
		String[] questionsArray;
		ArrayList<Question> questions = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT questionsInTest FROM tests WHERE testId = '" + testId + "'");
			rs.next();
			questionsString = rs.getString("questionsInTest");
			questionsArray = questionsString.split("~");
			for (String question : questionsArray) {
				rs = stmt.executeQuery("SELECT * FROM questions WHERE questionId = '" + question + "'");
				rs.next();
				questions.add(GeneralQueryMethods.createQuestion(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questions;
	}

	/**
	 * removes a scheduled test from the DB
	 * 
	 * @param args - testCode
	 * @return true if the test was removed from the scheduled_tests
	 */
	public static boolean removeScheduledTest(String testCode) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM scheduled_tests WHERE beginTestCode = '" + testCode + "'");
			if (!rs.next())
				return false;
			stmt.executeUpdate("DELETE FROM scheduled_tests WHERE beginTestCode = '" + testCode + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * gets the test given its code
	 * 
	 * @param testCode
	 * @return test
	 */
	public static Test getTestByCode(String testCode) {
		Test test = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests t, scheduled_tests st WHERE st.beginTestCode = '"
					+ testCode + "' AND t.testId = st.testId");
			if (rs.next())
				test = GeneralQueryMethods.createTest(rs);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return test;
	}

	/**
	 * gets the grades that belong to the given SSN
	 * 
	 * @param studentSSN
	 * @return student grade array list
	 */
	public static ArrayList<StudentGrade> getGradesBySSN(String studentSSN) {
		ArrayList<StudentGrade> grades = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT g.testId, course, title, grade FROM grades g, tests t WHERE g.ssn = '"
							+ studentSSN + "' AND g.testId = t.testId");
			if (rs.next())
				do {
					grades.add(GeneralQueryMethods.createStudentGrade(rs));
				} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(grades.toString());
		return grades;
	}

	/**
	 * saves answers of a student in the DB answers is a string, example: answers =
	 * 1~2~1~0~3~3~4~3
	 * 
	 * @param args - studentSSN,testId,answers
	 * @return
	 */
	public static boolean saveStudentAnswers(String args) {
		String[] details = args.split(",");
		String studentSSN = details[0];
		String testId = details[1];
		String answers = details[3];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(
					"INSERT INTO students_answers VALUES ('" + studentSSN + "', '" + testId + "', '" + answers + "');");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * gets test with student's answers
	 * 
	 * @param args - testId,studentSSN
	 * @return pair of test and array list of the student's answers
	 */
	public static Pair<Test, ArrayList<String>> getStudentTest(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String studentSSN = details[1];
		Test test = null;
		String[] answers;
		ArrayList<String> studentAnswers = new ArrayList<>();
		Pair<Test, ArrayList<String>> studentTest = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests t, students_answers sa WHERE sa.studentSSN = '"
					+ studentSSN + "', AND sa.testId = '" + testId + "', AND t.testId = '" + testId + "'");
			rs.next();
			test = GeneralQueryMethods.createTest(rs);
			answers = rs.getString("studentAnswers").split("~");
			for (String answer : answers)
				studentAnswers.add(answer);
			studentTest = new Pair<>(test, studentAnswers);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return studentTest;
	}

	/**
	 * reschedule date and time for a test
	 * 
	 * @param args - testCode,date,startingTime
	 * @return - true if the date was rescheduled
	 */
	public static boolean rescheduleTest(String args) {
		String[] details = args.split(",");
		String testCode = details[0];
		String date = details[1];
		String startingTime = details[2];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE scheduled_tests SET date = '" + date + "', startingTime = '" + startingTime
					+ "' WHERE beginTestCode = '" + testCode + "';");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * calculates the grade of a student's test
	 * 
	 * @param testId
	 * @param studentId
	 * @return grade as integer
	 */
	private static int calculateStudentGrade(String testId, String studentId) {
		int grade = 0;
		String questionsInTest;
		String studentAnswers;
		String[] questions;
		String[] answers;
		int pointsPerQuestion;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT pointsPerQuestion, questionsInTest, studentAnswers FROM tests t, students_answers sa WHERE t.testId = '"
							+ testId + "' AND sa.testId = '" + testId + "'");
			rs.next();
			questionsInTest = rs.getString("questionsInTest");
			pointsPerQuestion = rs.getInt("pointsPerQuestion");
			studentAnswers = rs.getString("studentAnswers");
			questions = questionsInTest.split("~");
			answers = studentAnswers.split("~");
			if (questions.length != answers.length)
				return -1;
			for (int i = 0; i < questions.length; i++) {
				rs = stmt.executeQuery("SELECT correctAnswer FROM questions WHERE questionId = '" + questions[i] + "'");
				if (rs.getInt("correctAnswer") == Integer.parseInt(answers[i]))
					grade += pointsPerQuestion;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return grade;
	}

	/**
	 * adds a finished test to the finished_tests table
	 * 
	 * @param args -
	 *             studentSSN,testId,code,startingTime,timeTaken,presentationMethod,title,course,status
	 * @return true if the finished test was added to the finished_tests table
	 */
	public static boolean addFinishedTest(String args) {
		String[] details = args.split(",");
		String studentSSN = details[0];
		String testId = details[1];
		String code = details[2];
		String startingTime = details[3];
		int timeTaken = Integer.parseInt(details[4]);
		String presentationMethod = details[5];
		String title = details[6];
		String course = details[7];
		int grade = Queries.calculateStudentGrade(testId, studentSSN);
		String status = details[8];
		String date = Queries.getDateByCode(code);
		String scheduler = Queries.getSchedulerIdByCode(code);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO finished_tests VALUES ('" + scheduler + "', '" + studentSSN + "', '"
					+ testId + "', '" + date + "', '" + startingTime + "', '" + timeTaken + "', '" + presentationMethod
					+ "', '" + title + "', '" + course + "', '" + grade + "', '" + status + "');");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * gets all students in the system
	 * 
	 * @return array list of students
	 */
	public static ArrayList<Student> getStudents() {
		ArrayList<Student> students = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE role = 'Student'");
			while (rs.next())
				students.add(GeneralQueryMethods.createStudent(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return students;
	}

	/**
	 * gets all the courses of a given student
	 * 
	 * @param studentSSN
	 * @return array list of courses
	 */
	public static ArrayList<Course> getCoursesByStudentSSN(String studentSSN) {
		ArrayList<Course> courses = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM finished_tests ft, courses c  WHERE ft.studentSSN = '"
					+ studentSSN + "' AND c.courseName = ft.course");
			if (rs.next())
				do {
					courses.add(GeneralQueryMethods.createCourse(rs));
				} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return courses;
	}

	/**
	 * gets all the teachers
	 * 
	 * @return array list of teachers
	 */
	public static ArrayList<Teacher> getTeachers() {
		ArrayList<Teacher> teachers = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE role = 'Teacher'");
			while (rs.next())
				teachers.add(GeneralQueryMethods.createTeacher(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return teachers;
	}

	/**
	 * gets all the courses
	 * 
	 * @return array list of courses
	 */
	public static ArrayList<Course> getCourses() {
		ArrayList<Course> courses = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM courses");
			while (rs.next())
				courses.add(GeneralQueryMethods.createCourse(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return courses;
	}

	/**
	 * gets all tests that were already taken by students that belong to a single
	 * teacher
	 * 
	 * @param teacherSSN
	 * @return array list of tests
	 */
	public static ArrayList<Test> getTestsByTeacherSSN(String teacherSSN) {
		ArrayList<Test> tests = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests t, finished_tests ft WHERE ft.scheduler = '"
					+ teacherSSN + "' AND ft.testId = t.testId");
			while (rs.next())
				tests.add(GeneralQueryMethods.createTest(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

	/**
	 * gets date of a test
	 * 
	 * @param testCode
	 * @return date string
	 */
	private static String getDateByCode(String testCode) {
		String date = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT date FROM scheduled_tests WHERE beginTestCode = '" + testCode + "'");
			rs.next();
			date = rs.getString("date");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * get schedulerId of a test
	 * 
	 * @param testCode
	 * @return ID string
	 */
	private static String getSchedulerIdByCode(String testCode) {
		String schedulerId = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT scheduledByTeacher FROM scheduled_tests WHERE beginTestCode = '" + testCode + "'");
			rs.next();
			schedulerId = rs.getString("scheduledByTeacher");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return schedulerId;
	}

	/**
	 * loads text file into table
	 * 
	 * @param args - tableName,path example: questions,c:/folder/MYSQL/text_file.txt
	 * @return true if the file was loaded
	 */
	public static boolean loadTxtFileIntoTable(String args) {
		String[] details = args.split(",");
		String tableName = details[0];
		String path = details[1];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("LOAD DATA LOCAL INFILE '" + path + "' INTO TABLE " + tableName);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * sets MYSQL to be able to use local files
	 * 
	 * @return true if the local files permission was set
	 */
	public static boolean setGlobalLocalInfile() {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.execute("SET GLOBAL local_infile = 1");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * creates a student report
	 * 
	 * @param args - studentSSN,courses example: 483285796,Java~Hedva
	 * @return report with student constructor
	 */
	public static Report createStudentReportBySSNAndCourses(String args) {
		Pair<String, Integer> testAndGrade;
		ArrayList<Pair<String, Integer>> testsAndGrades = new ArrayList<>();
		Report report = null;
		String[] details = args.split(",");
		String studentSSN = details[0];
		String[] courses = details[1].split("~");
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs;
			for (String course : courses) {
				rs = stmt.executeQuery("SELECT testId, grade FROM finished_tests WHERE studentSSN = '" + studentSSN
						+ "' AND course = '" + course + "'");
				if (rs.next())
					do {
						testAndGrade = new Pair<String, Integer>(rs.getString("testId"), rs.getInt("grade"));
						testsAndGrades.add(testAndGrade);
					} while (rs.next());
			}
			report = GeneralQueryMethods.createStudentReport(testsAndGrades);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return report;
	}

	/**
	 * gets a test given its ID
	 * 
	 * @param testId
	 * @return test
	 */
	public static Test getTestByTestId(String testId) {
		Test test = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests WHERE testId = '" + testId + "'");
			if (rs.next())
				test = GeneralQueryMethods.createTest(rs);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return test;
	}

	/**
	 * creates report of a teacher, includes all their scheduled tests
	 * 
	 * @param teacherSSN
	 * @return report of all the teacher's tests
	 */
	public static Report createTeacherReportBySSN(String teacherSSN) {
		ArrayList<Pair<String, Pair<Double, Double>>> testsAveragesMedians = new ArrayList<>();
		Pair<Double, Double> averageMedian;
		Pair<String, Pair<Double, Double>> testAverageMedian;
		Report teacherReport = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM teacher_statistics ts, statistic_reports sr WHERE teacherSSN = '"
							+ teacherSSN + "' AND ts.reportId = sr.reportId");
			while (rs.next()) {
				averageMedian = new Pair<Double, Double>(rs.getDouble("average"), rs.getDouble("median"));
				testAverageMedian = new Pair<String, Pair<Double, Double>>(rs.getString("testId"), averageMedian);
				testsAveragesMedians.add(testAverageMedian);
			}
			teacherReport = GeneralQueryMethods.createTeacherOrCourseReport(testsAveragesMedians);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return teacherReport;
	}

	/**
	 * adds a test report to the all the report tables
	 * 
	 * @param testCode
	 * @return reportId as string
	 */
	public static String addTestReport(String testCode) {
		ArrayList<Integer> grades = new ArrayList<>();
		Integer numberOfStudents = 0;
		Double median;
		Double average;
		String reportId = null;
		String testId = null;
		String teacherSSN = null;
		String courseName = null;
		String courseId = null;
		boolean initializeOnceFlag = true;
		int F = 0; // 0-54.9
		int DMinus = 0; // 55-64
		int DPlus = 0; // 65 - 69
		int CMinus = 0; // 70-74
		int CPlus = 0; // 75-79
		int BMinus = 0; // 80-84
		int BPlus = 0; // 85-89
		int AMinus = 0; // 90-94
		int APlus = 0; // 95-100
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT testId, scheduledByTeacher, grades, course FROM scheduled_tests st, finished_tests ft WHERE st.date = ft.date AND st.beginTestCode = '"
							+ testCode + "' AND st.starting_time = ft.starting_time AND st.testId = ft.testId");
			while (rs.next()) {
				grades.add(rs.getInt("grade"));
				numberOfStudents += 1;
				if (initializeOnceFlag) {
					testId = rs.getString("testId");
					teacherSSN = rs.getString("scheduledByTeacher");
					courseName = rs.getString("course");
					initializeOnceFlag = false;
				}
			}
			average = GeneralQueryMethods.getAverage(grades);
			median = GeneralQueryMethods.getMedian(grades);
			reportId = Queries.getAvailableIdForReport();
			Collections.sort(grades);
			for (Integer grade : grades)
				if (grade < 55)
					F += 1;
				else if (grade < 65)
					DMinus += 1;
				else if (grade < 70)
					DPlus += 1;
				else if (grade < 75)
					CMinus += 1;
				else if (grade < 80)
					CPlus += 1;
				else if (grade < 85)
					BMinus += 1;
				else if (grade < 90)
					BPlus += 1;
				else if (grade < 95)
					AMinus += 1;
				else
					APlus += 1;
			stmt.executeUpdate("INSERT INTO reports VALUES ('" + reportId + "', '" + testId + "', " + numberOfStudents
					+ ", " + average + ", " + median + ", " + F + ", " + DMinus + ", " + DPlus + ", " + CMinus + ", "
					+ CPlus + ", " + BMinus + ", " + BPlus + ", " + AMinus + ", " + APlus + ");");
			stmt.executeUpdate("INSERT INTO teacher_statistics VALUES ('" + reportId + "', '" + teacherSSN + "'");
			rs = stmt.executeQuery("SELECT courseId FROM courses WHERE courseName = '" + courseName + "'");
			rs.next();
			courseId = rs.getString("courseId");
			stmt.executeUpdate("INSERT INTO course_statistics VALUES ('" + reportId + "', '" + courseId + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reportId;
	}

	/**
	 * gets the next available ID for a report in an ascending order
	 * 
	 * @return report ID as string
	 * @throws SQLException
	 */
	private static String getAvailableIdForReport() throws SQLException {
		Integer currentId;
		Integer lastId;
		Integer tempId;
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT reportId FROM reports ORDER BY reportId");

		// case of empty bank
		if (!rs.next())
			return "1";
		// check if there is a lower id available
		currentId = rs.getInt("reportId");
		tempId = currentId - 1;
		if (!tempId.equals(0))
			return "1";
		// check if there is a gap between id numbers
		while (rs.next()) {
			lastId = rs.getInt("reportId");
			if (lastId - currentId != 1) {
				currentId += 1;
				rs.close();
				return currentId.toString();
			}
			currentId = lastId;
		}
		// return highest id + 1;
		currentId += 1;
		return currentId.toString();
	}

	/**
	 * gets all the reports of tests that their author is the teacher with the given
	 * SSN
	 * 
	 * @param teacherSSN
	 * @return report array list
	 */
	public static ArrayList<Report> getReportsByTeacherSSN(String teacherSSN) {
		ArrayList<Report> reports = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM statistic_reports sr, tests t WHERE t.authorId = '"
					+ teacherSSN + "' AND t.testId = sr.testId");
			while (rs.next())
				reports.add(GeneralQueryMethods.createTestReport(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reports;
	}

	/**
	 * creates report of a course
	 * 
	 * @param courseId
	 * @return report of all the courses' tests
	 */
	public static Report createCourseReportById(String courseId) {
		ArrayList<Pair<String, Pair<Double, Double>>> testsAveragesMedians = new ArrayList<>();
		Pair<Double, Double> averageMedian;
		Pair<String, Pair<Double, Double>> testAverageMedian;
		Report courseReport = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM statistic_reports sr, course_statistics cs WHERE sr.reportId = cs.reportId");
			while (rs.next()) {
				averageMedian = new Pair<Double, Double>(rs.getDouble("average"), rs.getDouble("meidan"));
				testAverageMedian = new Pair<String, Pair<Double, Double>>(rs.getString("testId"), averageMedian);
				testsAveragesMedians.add(testAverageMedian);
			}
			courseReport = GeneralQueryMethods.createTeacherOrCourseReport(testsAveragesMedians);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return courseReport;
	}
}