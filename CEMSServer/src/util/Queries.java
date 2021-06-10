package util;

import java.sql.Blob;
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
import common.TestFile;
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
	 * gets all the questions of the given authorId
	 * 
	 * @param authorId - SSN of the author
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
			ResultSet rs = stmt.executeQuery(
					"SELECT g.testId, course, title, grade, teacherNotes FROM grades g, tests t WHERE g.ssn = '"
							+ studentSSN + "' AND g.testId = t.testId");
			if (rs.next())
				do {
					grades.add(GeneralQueryMethods.createStudentGrade(rs));
				} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		String answers = details[2];
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
	 * @param studentSSN
	 * @return grade as integer
	 */
	private static int calculateStudentGrade(String studentSSN) {
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
					"SELECT pointsPerQuestion, questionsInTest, studentAnswers FROM tests t, students_answers sa WHERE sa.studentSSN = '"
							+ studentSSN + "' AND t.testId = sa.testId");
			rs.next();
			questionsInTest = rs.getString("questionsInTest");
			pointsPerQuestion = rs.getInt("pointsPerQuestion");
			studentAnswers = rs.getString("studentAnswers");
			questions = questionsInTest.split("~");
			answers = studentAnswers.split("~");
			for (int i = 0; i < questions.length; i++) {
				rs = stmt.executeQuery("SELECT correctAnswer FROM questions WHERE questionId = '" + questions[i] + "'");
				rs.next();
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
	 *             studentSSN,testId,code,timeTaken,presentationMethod,title,course,status
	 * @return true if the finished test was added to the finished_tests table
	 */
	public static boolean addFinishedTest(String args) {
		String[] details = args.split(",");
		String studentSSN = details[0];
		String testId = details[1];
		String code = details[2];
		int timeTaken = Integer.parseInt(details[3]);
		String presentationMethod = details[4];
		String title = details[5];
		String course = details[6];
		String status = details[7];
		String startingTime = Queries.getStartingTimeByTestCode(code);
		int grade = Queries.calculateStudentGrade(studentSSN);
		String date = Queries.getDateByCode(code);
		String scheduler = Queries.getSchedulerIdByCode(code);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO finished_tests VALUES ('" + scheduler + "', '" + studentSSN + "', '"
					+ testId + "', '" + date + "', '" + startingTime + "', '" + timeTaken + "', '" + title + "', '"
					+ course + "', '" + grade + "', '" + status + "', '" + presentationMethod + "');");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * gets all students in the data base
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
					if (!courses.contains(GeneralQueryMethods.createCourse(rs)))
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
		String testId = null;
		String courseId = null;
		ArrayList<String> coursesIds = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs;
			for (String course : courses) {
				rs = stmt.executeQuery("SELECT courseId FROM courses WHERE courseName = '" + course + "'");
				if (rs.next())
					coursesIds.add(rs.getString("courseId"));
			}
			rs = stmt.executeQuery("SELECT * FROM grades WHERE ssn = '" + studentSSN + "'");
			if (rs.next())
				do {
					testId = rs.getString("testId");
					courseId = testId.substring(2, 4);
					System.out.println(courseId);
					if (coursesIds.contains(courseId)) {
						testAndGrade = new Pair<String, Integer>(rs.getString("testId"), rs.getInt("grade"));
						testsAndGrades.add(testAndGrade);
					}
				} while (rs.next());
			if (testsAndGrades.isEmpty())
				return new Report();
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
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM teacher_statistics ts, statistic_reports sr WHERE teacherSSN = '"
							+ teacherSSN + "' AND ts.reportId = sr.reportId");
			while (rs.next()) {
				averageMedian = new Pair<Double, Double>(rs.getDouble("average"), rs.getDouble("median"));
				testAverageMedian = new Pair<String, Pair<Double, Double>>(rs.getString("testId"), averageMedian);
				testsAveragesMedians.add(testAverageMedian);
			}
			if (testsAveragesMedians.isEmpty())
				return new Report();
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
					"SELECT * FROM statistic_reports sr, course_statistics cs WHERE sr.reportId = cs.reportId AND cs.courseId = '"
							+ courseId + "'");
			if (rs.next()) {
				do {
					averageMedian = new Pair<Double, Double>(rs.getDouble("average"), rs.getDouble("median"));
					testAverageMedian = new Pair<String, Pair<Double, Double>>(rs.getString("testId"), averageMedian);
					testsAveragesMedians.add(testAverageMedian);
				} while (rs.next());
				courseReport = GeneralQueryMethods.createTeacherOrCourseReport(testsAveragesMedians);
			} else
				courseReport = new Report();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return courseReport;
	}

	/**
	 * gets a scheduled test by its code
	 * 
	 * @param testCode
	 * @return scheduled test
	 */
	public static ScheduledTest getScheduledTestByCode(String testCode) {
		ScheduledTest scheduledTest = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM scheduled_tests st, tests t WHERE beginTestCode = '"
					+ testCode + "' AND t.testId = st.testId");
			rs.next();
			scheduledTest = GeneralQueryMethods.createScheduledTest(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return scheduledTest;
	}

	/**
	 * gets all the students that are currently in a test, given the test's code
	 * 
	 * @param testCode
	 * @return array list of students
	 */
	public static ArrayList<Student> getStudentSSNByTestCode(String testCode) {
		ArrayList<Student> students = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM students_in_test sit, users u WHERE sit.testCode = '"
					+ testCode + "' AND sit.studentSSN = u.ssn");
			if (rs.next())
				do {
					students.add(GeneralQueryMethods.createStudent(rs));
				} while (rs.next());
			else
				students.add(new Student());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return students;
	}

	/**
	 * gets a specific time extension request using the test's code
	 * 
	 * @param testCode
	 * @return time extension request
	 */
	public static TimeExtensionRequest getTimeExtensionRequestByTestCode(String testCode) {
		TimeExtensionRequest request = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM time_extension_requests WHERE testCode = '" + testCode + "'");
			if (rs.next())
				request = GeneralQueryMethods.createTimeExtensionRequest(rs);
			else
				request = new TimeExtensionRequest();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return request;
	}

	/**
	 * gets the starting time of a test given its code
	 * 
	 * @param testCode
	 * @return starting time string
	 */
	private static String getStartingTimeByTestCode(String testCode) {
		String startingTime = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT startingTime FROM scheduled_tests WHERE beginTestCode = '" + testCode + "'");
			if (rs.next())
				startingTime = rs.getString("startingTime");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return startingTime;
	}

	/**
	 * adds a student in test to the DB
	 * 
	 * @param args - studentSSN,testCode
	 * @return true if the student was added
	 */
	public static boolean addStudentInTest(String args) {
		String[] details = args.split(",");
		String studentSSN = details[0];
		String testCode = details[1];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO students_in_test VALUES ('" + studentSSN + "', '" + testCode + "');");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * deletes a student from the students_in_test table using studentSSN
	 * 
	 * @param studentSSN
	 * @return true if the student was deleted
	 */
	public static boolean deleteStudentInTest(String studentSSN) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM students_in_test WHERE studentSSN = '" + studentSSN + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * checks if a test is currently active
	 * 
	 * @param testCode
	 * @return true if the test is active
	 */
	public static boolean isActiveTest(String testCode) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM active_tests WHERE beginTestCode = '" + testCode + "'");
			if (rs.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * checks if the scheduled test needs to be active if it needs to be active,
	 * inserts it into the active_tests table
	 * 
	 * @param args - localDate,localTime,testCode example: 03/08/2021,09:30,Xf82 all
	 *             Strings
	 * @return true if the test needs to be active
	 */
	public static boolean isTimeForTest(String args) {
		String[] details = args.split(",");
		String localDate = details[0];
		String localTime = details[1];
		String testCode = details[2];
		String finishTime;
		Statement stmt1;
		Statement stmt2;
		try {
			stmt1 = conn.createStatement();
			stmt2 = conn.createStatement();
			ResultSet rs1;
			ResultSet rs2;
			rs1 = stmt1.executeQuery("SELECT * FROM scheduled_tests WHERE date = '" + localDate
					+ "' AND beginTestCode = '" + testCode + "'");
			if (!rs1.next())
				return false;
			finishTime = GeneralQueryMethods.calculateFinishTime(rs1.getString("startingTime"), rs1.getInt("duration"));
			if (GeneralQueryMethods.isArgTimeBetweenStartAndFinishTimes(rs1.getString("startingTime"), finishTime,
					localTime)) {
				rs2 = stmt2.executeQuery("SELECT * FROM active_tests WHERE beginTestCode = '" + testCode + "'");
				if (!rs2.next()) {
					rs2 = stmt2.executeQuery("SELECT * FROM tests WHERE testId = '" + rs1.getString("testId") + "'");
					rs2.next();
					stmt2.executeUpdate("INSERT INTO active_tests VALUES ('" + rs1.getString("testId") + "', '"
							+ Queries.getAuthorNameByTestId(rs1.getString("testId")) + "', '" + rs2.getString("title")
							+ "', '" + rs2.getString("course") + "', '" + rs2.getString("field") + "', '"
							+ rs1.getString("startingTime") + "', '" + rs1.getString("beginTestCode") + "');");
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * gets course using a test's id
	 * 
	 * @param testId
	 * @return course entity
	 */
	public static Course getCourseByTestId(String testId) {
		Course course = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM tests t, courses c WHERE t.testId = '" + testId + "' AND t.course = c.courseName");
			if (rs.next())
				course = GeneralQueryMethods.createCourse(rs);
			else
				course = new Course();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return course;
	}

	/**
	 * checks if students copied of of each other in a test
	 * 
	 * @param testCode
	 * @return array list of pairs of students who might have copied
	 */
	public static ArrayList<Pair<Student, Student>> checkTestForCopyingByTestCode(String testCode) {
		ArrayList<Pair<Student, Student>> studentsSuscpectedCopying = new ArrayList<>();
		ArrayList<String> studentsSSN = new ArrayList<>();
		ScheduledTest test;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			test = Queries.getScheduledTestByCode(testCode);
			ResultSet rs = stmt.executeQuery("SELECT studentSSN FROM finished_tests WHERE testId = '" + test.getID()
					+ "' AND date = '" + test.getDate() + "' AND startingTime = '" + test.getStartingTime() + "'");
			while (rs.next())
				studentsSSN.add(rs.getString("studentSSN"));
			for (int i = 0; i < studentsSSN.size() - 1; i++)
				for (int j = i + 1; j < studentsSSN.size(); j++)
					if (Queries.checkCopyingBetweenTwoStudentsBySSNAndTestId(
							studentsSSN.get(i) + studentsSSN.get(j) + test.getID()))
						studentsSuscpectedCopying
								.add(new Pair<Student, Student>(Queries.getStudentBySSN(studentsSSN.get(i)),
										Queries.getStudentBySSN(studentsSSN.get(j))));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return studentsSuscpectedCopying;
	}

	/**
	 * gets a student using their SSN
	 * 
	 * @param studentSSN
	 * @return student
	 */
	public static Student getStudentBySSN(String studentSSN) {
		Student student = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE ssn = '" + studentSSN + "'");
			student = GeneralQueryMethods.createStudent(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return student;
	}

	/**
	 * checks if the students have 50% or more similar wrong answers in a test
	 * 
	 * @param args - studentSSNOne,studentSSNTwo,testId
	 * @return true if the students have at least 50% similar wrong answers
	 */
	private static boolean checkCopyingBetweenTwoStudentsBySSNAndTestId(String args) {
		String[] details = args.split(",");
		String studentOne = details[0];
		String studentTwo = details[1];
		String testId = details[2];
		ArrayList<Pair<String, Integer>> answersOfStudentOne;
		ArrayList<Pair<String, Integer>> answersOfStudentTwo;
		ArrayList<Question> questions;
		ArrayList<Pair<String, Integer>> similarAnswers = new ArrayList<>();
		int numberOfSimilarIncorrectAnswers = 0;
		answersOfStudentOne = Queries.getStudentAnswersByTestIdAndSSN(testId + studentOne);
		answersOfStudentTwo = Queries.getStudentAnswersByTestIdAndSSN(testId + studentTwo);
		if (answersOfStudentOne == null || answersOfStudentTwo == null)
			return false;
		if (answersOfStudentOne.get(0).getKey().equals("testQuestionsChanged"))
			return false;
		questions = Queries.getQuestionsFromTest(testId);
		for (Pair<String, Integer> answerOne : answersOfStudentOne)
			for (Pair<String, Integer> answerTwo : answersOfStudentTwo) {
				if (answerOne.getKey().equals(answerTwo.getKey())) {
					if (answerOne.getValue().equals(answerTwo.getValue()))
						similarAnswers.add(answerOne);
					break;
				}
			}
		for (Question question : questions)
			for (Pair<String, Integer> answer : similarAnswers) {
				if (question.getID().equals(answer.getKey())) {
					if (question.getCorrectAnswer() != answer.getValue())
						numberOfSimilarIncorrectAnswers += 1;
					break;
				}
			}
		if (questions.size() / 2 <= numberOfSimilarIncorrectAnswers)
			return true;
		return false;
	}

	/**
	 * gets a student's answers to a test's questions as pair of question+answer
	 * 
	 * @param args - testId,studentSSN
	 * @return array list of pairs of question and answer
	 */
	public static ArrayList<Pair<String, Integer>> getStudentAnswersByTestIdAndSSN(String args) {
		ArrayList<Pair<String, Integer>> studentAnswers = new ArrayList<>();
		String[] details = args.split(",");
		String testId = details[0];
		String studentSSN = details[1];
		String[] studentAnswersString;
		ArrayList<Question> questions;
		int i = 0;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT studentAnswers FROM students_answers WHERE studentSSN = '"
					+ studentSSN + "' AND testId = '" + testId + "'");
			if (!rs.next()) {
				studentAnswers.add(new Pair<String, Integer>("studentDidn'tTakeTest", 0));
				return studentAnswers;
			}
			studentAnswersString = rs.getString("studentAnswers").split("~");
			questions = Queries.getQuestionsFromTest(testId);
			if (questions.size() != studentAnswersString.length) {
				studentAnswers.add(new Pair<String, Integer>("testQuestionsChanged", 0));
				return studentAnswers;
			}
			for (String answer : studentAnswersString) {
				studentAnswers.add(new Pair<String, Integer>(questions.get(i).getID(), Integer.parseInt(answer)));
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return studentAnswers;
	}

	/**
	 * locks a test, deletes it from scheduled_tests and active_tests tables and
	 * checks for students who might have copied of of each other and fills
	 * copy_suspects table with them
	 * 
	 * @param testCode
	 * @return true if the test was locked
	 */
	public static boolean lockTest(String testCode) {
		ArrayList<Pair<Student, Student>> studentsSuspectedCopying = new ArrayList<>();
		Test test;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			test = Queries.getTestByCode(testCode);
			studentsSuspectedCopying = Queries.checkTestForCopyingByTestCode(testCode);
			for (Pair<Student, Student> students : studentsSuspectedCopying)
				stmt.executeUpdate("INSERT INTO copy_suspects VALUES ('" + students.getKey().getSSN() + "', '"
						+ students.getValue().getSSN() + "', '" + test.getID() + "', '"
						+ Queries.getDateByCode(testCode) + "', '" + Queries.getStartingTimeByTestCode(testCode)
						+ "');");
			stmt.executeUpdate("DELETE FROM active_tests WHERE beginTestCode = '" + testCode + "'");
			stmt.executeUpdate("DELETE FROM scheduled_tests WHERE beginTestCode = '" + testCode + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * adds a grade to the grades table
	 * 
	 * @param args - testId,studentSSN,teacherNotes
	 * @return true if the grade was added to the grades table
	 */
	public static boolean addGrade(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String studentSSN = details[1];
		String teacherNotes = details[2];
		int grade;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT grade FROM finished_tests WHERE studentSSN = '" + studentSSN
					+ "' AND testId = '" + testId + "'");
			rs.next();
			grade = rs.getInt("grade");
			stmt.executeUpdate("UPDATE finished_tests SET status = Checked WHERE studentSSN = '" + studentSSN
					+ "' AND testId = '" + testId + "'");
			stmt.executeUpdate("INSERT INTO grades VALUES ('" + testId + "', '" + studentSSN + "', " + grade + ", '"
					+ teacherNotes + "');");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * deletes the contents of an entire table given its name
	 * 
	 * @param tableName
	 * @return true if the contents were deleted
	 */
	public static boolean deleteTableContents(String tableName) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM " + tableName);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * checks if there is only one more student in the test
	 * 
	 * @param testCode
	 * @return true if there is only one student in the test
	 */
	public static boolean isLastStudentInTest(String testCode) {
		int numberOfStudents = 0;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM students_in_test WHERE testCode = '" + testCode + "'");
			while (rs.next())
				numberOfStudents += 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		if (numberOfStudents > 1)
			return false;
		return true;
	}

	/**
	 * gets all the tests of a students inside the range of dates given
	 * 
	 * @param args - startDate,finishDate,studentSSN example:
	 *             21/03/2021,23/03/2021,654255167
	 * @return array list of tests
	 */
	public static ArrayList<StudentGrade> getTestsByDateRange(String args) {
		String[] details = args.split(",");
		String startDate = details[0];
		String finishDate = details[1];
		String studentSSN = details[2];
		String testDate;
		ArrayList<StudentGrade> grades = new ArrayList<>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs1 = stmt
					.executeQuery("SELECT testId, date FROM finished_tests WHERE studentSSN = '" + studentSSN + "'");
			if (rs1.next())
				do {
					testDate = rs1.getString("date");
					if (GeneralQueryMethods.isDateInRange(startDate, finishDate, testDate)) {
						ResultSet rs2 = stmt.executeQuery(
								"SELECT * FROM tests t, grades g WHERE t.testId = '" + rs1.getString("testId")
										+ "' AND t.testId = g.testId AND g.ssn = '" + studentSSN + "'");
						rs2.next();
						grades.add(GeneralQueryMethods.createStudentGrade(rs2));
					}
				} while (rs1.next());
			else
				grades.add(new StudentGrade());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return grades;
	}

	/**
	 * gets all the copying suspects of a given test as pairs
	 * 
	 * @param args - testId,date,startingTime
	 * @return
	 */
	public static ArrayList<Pair<String, String>> getCopySuspectByTestIdAndDate(String args) {
		ArrayList<Pair<String, String>> suspects = new ArrayList<>();
		String[] details = args.split(",");
		String testId = details[0];
		String date = details[1];
		String startingTime = details[2];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM copy_suspects WHERE testId = '" + testId + "' AND date = '"
					+ date + "' AND startingTime = '" + startingTime + "'");
			if (rs.next())
				do {
					suspects.add(
							new Pair<String, String>(rs.getString("studentOneSSN"), rs.getString("studentTwoSSN")));
				} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return suspects;
	}

	/**
	 * changes details of a test in the DB
	 * 
	 * @param args -
	 *             testId,title,duration,pointsPerQuestion,studentInstructions,teacherInstructions,questionsInTest
	 * @return true if the test was updated successfully
	 */
	public static boolean editTest(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String title = details[1];
		int duration = Integer.parseInt(details[2]);
		String pointsPerQuestion = details[3];
		String studentInstructions = details[4];
		String teacherInstructions = details[5];
		String questionsInTest = details[6];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE tests SET title = '" + title + "', duration = " + duration
					+ ", pointsPerQuerstion = '" + pointsPerQuestion + "', studentInstructions = '"
					+ studentInstructions + "', teacherInstructions = '" + teacherInstructions
					+ "', questionsInTest = '" + questionsInTest + "' WHERE testId = '" + testId + "';");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * adds a manual test to the DB
	 * 
	 * to call this query from server, send Pair<TestFile,String> that includes the
	 * word file as TestFile and all the other arguments except for path as string
	 * example: Pair<TestFile,String> =
	 * [wordFile=ADD_MANUAL_TEST-testId,studentSSN,scheduler,date,startingTime,path]
	 * 
	 * path is /lib/fileName
	 * 
	 * @param args - testId,studentSSN,scheduler,date,startingTime,path
	 * @return
	 */
	public static boolean addManualTest(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String studentSSN = details[1];
		String scheduler = details[2];
		String date = details[3];
		String startingTime = details[4];
		String path = details[5];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(
					"INSERT INTO manual_tests (testId, studentSSN, scheduler, date, startingTime, status, presentationMethod) VALUES ('"
							+ testId + "', '" + studentSSN + "', '" + scheduler + "', '" + date + "', '" + startingTime
							+ "', 'UnChecked', 'Self'" + ");");
			if (!path.equals("null"))
				stmt.executeUpdate("UPDATE manual_tests SET word = LOAD_FILE('" + path + "') WHERE testId = '" + testId
						+ "' AND studentSSN = '" + studentSSN + "';");
			else
				stmt.executeUpdate("UPDATE manual_tests SET presentationMethod = 'Forced'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * updates the grade and comments of a manual test
	 * 
	 * @param args - testId,studentSSN,grade,comments
	 * @return true if the test was updated
	 */
	public static boolean updateManualTest(String args) {
		String[] details = args.split(",");
		String testId = details[0];
		String studentSSN = details[1];
		int grade = Integer.parseInt(details[2]);
		String comments = details[3];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE manual_tests SET grade = " + grade + ", comments = '" + comments
					+ "', status = 'Checked' WHERE studentSSN = '" + studentSSN + "' AND testId = '" + testId + "';");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * locks a test, deletes it from scheduled_tests and active_tests tables
	 * 
	 * @param testCode
	 * @return true if the test was locked
	 */
	public static boolean lockManualTest(String testCode) {
		ScheduledTest test;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT studentSSN FROM students_in_test WHERE testCode = '" + testCode + "'");
			test = Queries.getScheduledTestByCode(testCode);
			if (rs.next()) {
				do {
					Queries.addManualTest(test.getID() + rs.getString("studentSSN") + test.getBelongsToID()
							+ test.getDate() + test.getStartingTime() + "null");
				} while (rs.next());
				stmt.executeUpdate("DELETE FROM students_in_test WHERE testCode = '" + testCode + "'");
			}
			stmt.executeUpdate("DELETE FROM active_tests WHERE beginTestCode = '" + testCode + "'");
			stmt.executeUpdate("DELETE FROM scheduled_tests WHERE beginTestCode = '" + testCode + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * gets the manual test of a student
	 * 
	 * @param args - testId,studentSSN
	 * @return test file
	 */
	public static TestFile getManualTestByStudentSSN(String args) {
		TestFile test = new TestFile();
		Blob word;
		String[] details = args.split(",");
		String testId = details[0];
		String studentSSN = details[1];
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM manual_tests WHERE studentSSN = '" + studentSSN + "' AND testId = '" + testId + "'");
			rs.next();
			if (rs.getString("presentationMethod").equals("Self")) {
				word = rs.getBlob("word");
				test.setSize((int) word.length());
				test.initArray((int) word.length());
				test.setByteArray(word.getBytes(1, test.getSize()));
			} else
				test.setFlag(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return test;
	}

	/**
	 * gets the test code of a student who takes the test right now
	 * 
	 * @param studentSSN
	 * @return test code as string
	 */
	public static String getTestCodeByStudentSSN(String studentSSN) {
		String testCode = null;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT testCode FROM students_in_test WHERE studentSSN = '" + studentSSN + "'");
			rs.next();
			testCode = rs.getString("testCode");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return testCode;
	}

	/**
	 * updates a finished test in the DB
	 * 
	 * @param args - testId,studentSSN,grade - String,String,integer
	 * @return true if the test was updated
	 */
	public static boolean updateFinishedTest(String args) {
		String[] details = args.split(args);
		String testId = details[0];
		String studentSSN = details[1];
		int grade = Integer.parseInt(details[3]);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE finished_tests SET grade = " + grade + ", status = Checked WHERE testId = '"
					+ testId + "' AND studentSSN = '" + studentSSN + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}