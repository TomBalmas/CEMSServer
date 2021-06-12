package server;

import java.io.IOException;
import java.sql.SQLException;

import gui.ServerUIController;
import util.Queries;

/**
 * this class implements the logic of what the server can do function wise.
 * 
 * @author ArtLo
 *
 */
public class ServerController {

	public static CEMSServer server;
	private static ServerUIController uiController;
	private static Queries queries;

	public ServerController() {
		uiController = new ServerUIController();
	}

	/**
	 * @return instance of the UI controller
	 */
	public ServerUIController getUiController() {
		return uiController;
	}

	/**
	 * running the server and start listening to clients connected
	 * 
	 * @param port
	 * @throws IOException
	 */
	public static void runServer(int port) throws IOException {
		server = new CEMSServer(port);
		uiController.setServer(server);
		server.listen(); // Start listening for connections
	}

	/**
	 * connecting to the database.
	 * 
	 * @param ip
	 * @param schema
	 * @param user
	 * @param pass
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void connectToDB(String ip, String schema, String user, String pass)
			throws SQLException, ClassNotFoundException {
		DBConnector.getInstance(ip, schema, user, pass);
		queries = new Queries(DBConnector.getConnection());
		Queries.setGlobalLocalInfile();
		Queries.createTable("users,");
		Queries.createTable("fields");
		Queries.createTable("courses");
		Queries.deleteTableContents("users");
		Queries.deleteTableContents("fields");
		Queries.deleteTableContents("courses");
		Queries.loadTxtFileIntoTable("users,lib/users.txt");
		Queries.loadTxtFileIntoTable("fields,lib/fields.txt");
		Queries.loadTxtFileIntoTable("courses,lib/courses.txt");
	}

	/**
	 * closes the server for connections
	 * 
	 * @throws IOException
	 */
	public static void closeServer() throws IOException {
		server.close();
	}

	/**
	 * @return instance of the server object
	 */
	public static CEMSServer getServer() {
		return server;
	}

}
