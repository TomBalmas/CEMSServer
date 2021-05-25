package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import common.Question;

public class GeneralQueryMethods {
	
	/**
	 * cuts the edges of a given string,
	 * example: [hello world] -> hello world
	 * 
	 * @param string
	 * @return string without the edges
	 * 
	 */
	public static String trimEdges(String string) {
		return string.substring(1, string.length() - 1);
	}
	
	public static Question createQuestion(ResultSet rs) throws SQLException {
		ArrayList<String> answers = new ArrayList<>();
		answers.add(rs.getString("answer1"));
		answers.add(rs.getString("answer2"));
		answers.add(rs.getString("answer3"));
		answers.add(rs.getString("answer4"));
		return new Question(rs.getString("questionId"), rs.getString("author"),
				rs.getString("instructionsForTeacher"), rs.getString("instructionsForStudent"),
				rs.getString("questionContent"), rs.getInt("correctAnswer"), rs.getString("field"),
				answers);
	}

}
