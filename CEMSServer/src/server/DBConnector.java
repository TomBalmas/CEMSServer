package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is a singleton design pattern, it defines the jdbc driver and
 * connect to specific schema of a database
 */
public class DBConnector {

	private static String dbIp, schema, userName, password;
	private static volatile DBConnector instance;
	private static Connection conn;

	private DBConnector(String dbIp, String schema, String userName, String password) throws ClassNotFoundException, SQLException {
		DBConnector.dbIp = dbIp;
		DBConnector.schema = schema.toLowerCase();
		DBConnector.userName = userName;
		DBConnector.password = password;
		connectToDB();
	}

	/**
	 * This function makes the connection to the Database
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	private static void connectToDB() throws ClassNotFoundException, SQLException {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(
					"jdbc:mysql://" + dbIp + "/" + schema + "?useSSL=false&serverTimezone=IST", userName, password);
	}

	public static Connection getConnection() {
		return conn;
	}

	public String getDbIp() {
		return dbIp;
	}

	public static void setDbIp(String dbIp) {
		DBConnector.dbIp = dbIp;
	}

	public String getUserName() {
		return userName;
	}

	public static void setUserName(String userName) {
		DBConnector.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		DBConnector.password = password;
	}

	public String getSchema() {
		return schema;
	}

	public static void setSchema(String schema) {
		DBConnector.schema = schema;
	}

	/**
	 * @param dbIp     - the ip of mysql server
	 * @param schema   - schema name
	 * @param userName - mysql username
	 * @param password - mysql password
	 * @return single instance for database connection
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public static DBConnector getInstance(String dbIp, String schema, String userName, String password)
			throws SQLException, ClassNotFoundException {
		if (instance == null) {
			synchronized (DBConnector.class) {
				instance = new DBConnector(dbIp, schema, userName, password);
			}
		} else {
			if (conn != null)
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
