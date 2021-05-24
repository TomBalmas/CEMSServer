package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import common.Principle;
import common.Question;
import common.Student;
import common.Teacher;
import common.Test;
import common.User;

public class Queries {

	private static Connection conn = null;

	public Queries(Connection conn) {
		Queries.conn = conn;
	}

	public static User getUser(String username, String password) {
		Statement stmt;
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
				
					return new Teacher(rs.getInt("SSN"), rs.getString("name"), rs.getString("surname"),
							rs.getString("email"), rs.getString("username"), rs.getString("password"), rs.getString("fields"));
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
	 * this method returns questions that are of the teachers fields
	 * 
	 * @return
	 */
	public static ArrayList<Question> getQuestions(String fields) {
		Statement stmt;
		ArrayList<Question> questions = new ArrayList<>();
		String temp = fields.substring(1, fields.length() - 1);
		String[] arr = temp.split(",");
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

	public static ArrayList<Test> getTests(String fields) {
		Statement stmt;
		ArrayList<Test> tests = new ArrayList<>();
		String[] arr;
		String temp = fields.substring(1, fields.length() - 1);
		arr = temp.split(",");
		ArrayList<String> array = new ArrayList<>(Arrays.asList("x", "x", "x", "x", "x", "x"));
		for (int i = 0; i < arr.length; i++)
			array.add(i, arr[i].trim());
		try {
			System.out.println("1");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tests WHERE field = '" + array.get(0) + "' OR field = '"
					+ array.get(1) + "' OR field = '" + array.get(2) + "' OR field = '" + array.get(3)
					+ "' OR field = '" + array.get(4) + "' OR field = '" + array.get(4) + "'");
			while (rs.next()) {
				System.out.println(rs.getString("autour"));
				tests.add(new Test(rs.getInt("id"), rs.getString("autour"), rs.getString("testName"),
						rs.getString("course"), rs.getString("testDuartion"), rs.getString("pointsPerQuestion"),
						rs.getString("instructions"), rs.getString("teacherInstructions"), rs.getString("field")));
				System.out.println(tests.get(0));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tests;
	}

}
