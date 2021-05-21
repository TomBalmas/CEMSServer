package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import entities.Principle;
import entities.Student;
import entities.Teacher;
import entities.User;

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
			switch (rs.getString("Role")) {
			case "Student":
				return new Student(rs.getLong(0), rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5));
			case "Teacher":
				return new Teacher(rs.getLong(0), rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5));
			case "Principle":
				return new Principle(rs.getLong(0), rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5));
			default:
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
