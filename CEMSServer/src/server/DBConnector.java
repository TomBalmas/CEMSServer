package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is a singleton design pattern,
 * it defines the jdbc driver and connect to specific schema of a database
 */
public class DBConnector {

	private String dbIp, schema, userName, password;
	private static DBConnector instance;
	private static Connection conn;

	
	private DBConnector(String dbIp, String schema, String userName, String password) {
		this.dbIp = dbIp;
		this.schema = schema.toLowerCase();
		this.userName = userName;
		this.password = password;
		connectToDB();
	}

	/**
	 * This function makes the connection to the Database
	 */
	private void connectToDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Driver definition succeed");

		} catch (Exception e) {
			System.out.println("Driver definition failed");
		}
		try {
			conn = DriverManager.getConnection(
					"jdbc:mysql://" + dbIp + "/" + schema + "?useSSL=false&serverTimezone=IST", userName, password);
			System.out.println("SQL connection succeed");
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public static Connection getConnection() {
		return conn;
	}

	public String getDbIp() {
		return dbIp;
	}

	public void setDbIp(String dbIp) {
		this.dbIp = dbIp;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * @param dbIp - the ip of mysql server
	 * @param schema - schema name 
	 * @param userName - mysql username
	 * @param password - mysql password
	 * @return single instance for database connection
	 * @throws SQLException
	 */
	public DBConnector getInstance(String dbIp, String schema, String userName, String password) throws SQLException {
		if (instance == null)
			instance = new DBConnector(dbIp, schema, userName, password);
		else {
			conn.close();
			setDbIp(dbIp);
			setSchema(schema);
			setUserName(userName);
			setPassword(password);
			connectToDB();
		}
		return instance;
	}

}
