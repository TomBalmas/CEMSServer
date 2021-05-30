package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import common.ActiveTest;
import common.FinishedTest;
import common.Question;
import common.Report;
import common.ScheduledTest;
import common.Test;
import common.TimeExtensionRequest;

public class GeneralQueryMethods {

	/**
	 * creates a question
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new question
	 * @throws SQLException
	 */
	public static Question createQuestion(ResultSet rs) throws SQLException {
		ArrayList<String> answers = new ArrayList<>();
		answers.add(rs.getString("answer1"));
		answers.add(rs.getString("answer2"));
		answers.add(rs.getString("answer3"));
		answers.add(rs.getString("answer4"));
		return new Question(rs.getString("questionId"), rs.getString("authorId"), rs.getString("questionContent"),
				rs.getInt("correctAnswer"), rs.getString("field"), answers);
	}

	/**
	 * creates a test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return - new test
	 * @throws SQLException
	 */
	public static Test createTest(ResultSet rs) throws SQLException {
		return new Test(rs.getString("testId"), Queries.getNameById(rs.getString("authorId")), rs.getString("title"),
				rs.getString("course"), rs.getInt("duration"), rs.getInt("pointsPerQuestion"),
				rs.getString("studentInstructions"), rs.getString("teacherInstructions"),
				rs.getString("questionsInTest"), rs.getString("field"));
	}

	/**
	 * creates a finished test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new finished test
	 * @throws SQLException
	 */
	public static FinishedTest createFinishedTest(ResultSet rs) throws SQLException {
		return new FinishedTest(rs.getString("testId"), rs.getString("author"), rs.getString("Title"),
				rs.getString("Course"), rs.getString("scheduler"), rs.getString("studentSSN"), rs.getString("date"),
				rs.getString("startingTime"), rs.getInt("grade"), rs.getString("status"));
	}

	/**
	 * creates an active test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new active test
	 * @throws SQLException
	 */
	public static ActiveTest createActiveTest(ResultSet rs) throws SQLException {
		return new ActiveTest(rs.getString("testId"), rs.getString("title"), rs.getString("course"),
				rs.getString("author"), rs.getString("field"), rs.getString("startingTime"),
				GeneralQueryMethods.calculateFinishTime(rs.getString("startingTime"), rs.getInt("duration")),
				rs.getString("beginTestCode"));
	}

	/**
	 * creates a scheduled test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new scheduled test
	 * @throws SQLException
	 */
	public static ScheduledTest createScheduledTest(ResultSet rs) throws SQLException {
		return new ScheduledTest(rs.getString("testId"), rs.getString("author"), rs.getString("title"),
				rs.getString("course"), rs.getString("date"), rs.getString("startingTime"), rs.getInt("duration"),
				rs.getString("scheduledByTeacher"), rs.getString("beginTestCode"));
	}

	public static TimeExtensionRequest createTimeExtensionRequest(ResultSet rs) throws SQLException {
		return new TimeExtensionRequest(rs.getString("teacherId"), rs.getString("content"), rs.getString("testCode"));
	}

	/**
	 * calculates starting time + duration
	 * 
	 * @param startingTime - test's starting time
	 * @param duration     - test's duration
	 * @return - finish time as string
	 */
	public static String calculateFinishTime(String startingTime, int duration) {
		String[] time = startingTime.split(":");
		String hours = time[0];
		String minutes = time[1];
		int h, m;
		h = Integer.parseInt(hours);
		m = Integer.parseInt(minutes);
		m += duration;
		while (m >= 60) {
			if (h == 24)
				h = 0;
			h += 1;
			m -= 60;
		}
		hours = Integer.toString(h);
		minutes = Integer.toString(m);
		if (h < 10)
			hours = "0" + hours;
		if (m < 10)
			minutes = "0" + minutes;
		return hours + ":" + minutes;
	}

	/**
	 * creates a report
	 * 
	 * @param rs - result set that has all the arguments for report constructor
	 * @return new report
	 * @throws SQLException
	 */
	public static Report createReport(ResultSet rs) throws SQLException {
		return new Report(rs.getString("reportId"), rs.getString("testId"), rs.getInt("numOfStudents"),
				rs.getDouble("average"), rs.getDouble("median"), rs.getInt("0-54.9"), rs.getInt("55-64"),
				rs.getInt("65-69"), rs.getInt("70-74"), rs.getInt("75-79"), rs.getInt("80-84"), rs.getInt("85-89"),
				rs.getInt("90-94"), rs.getInt("95-100"));
	}

}
