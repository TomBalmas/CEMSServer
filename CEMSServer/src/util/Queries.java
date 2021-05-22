package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import common.Principle;
import common.Student;
import common.Teacher;
import common.User;

public class Queries {

	private static Connection conn = null;

	public Queries(Connection conn) {
		this.conn = conn;
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
					// getting teachers fields from DB and inserting into teachers arrayList
					String[] arr = null;
					arr = rs.getString("fields").split(" ");
					ArrayList<String> fields = new ArrayList<>();
					for (int i = 0; i < arr.length; i++) {
						fields.add(arr[i]);
					}
					return new Teacher(rs.getInt("SSN"), rs.getString("name"), rs.getString("surname"),
							rs.getString("email"), rs.getString("username"), rs.getString("password"), fields);
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
}
