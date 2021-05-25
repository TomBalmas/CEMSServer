package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import common.Question;

public class GeneralQueryMethods {

	/**
	 * creates a question
	 * 
	 * @param rs - result set of a query from questions table
	 * @return Question
	 * @throws SQLException
	 */
	public static Question createQuestion(ResultSet rs) throws SQLException {
		ArrayList<String> answers = new ArrayList<>();
		answers.add(rs.getString("answer1"));
		answers.add(rs.getString("answer2"));
		answers.add(rs.getString("answer3"));
		answers.add(rs.getString("answer4"));
		return new Question(rs.getString("questionId"), rs.getString("author"), rs.getString("questionContent"),
				rs.getInt("correctAnswer"), rs.getString("field"), answers);
	}

	/**
	 * calculates starting time + duration
	 * 
	 * @param startingTime - test's starting time
	 * @param duration - test's duration
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
}
