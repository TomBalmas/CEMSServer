package server;

import java.io.IOException;
import java.sql.SQLException;

import gui.ServerUIController;
import util.Queries;

/**
 * This class implements the logic of what the server can do function wise.
 *  @version 1.0
 *  @author Group 1
 * 
 */
public class ServerController {

	public static CEMSServer server;
	private static ServerUIController uiController;
	private static Queries queries;

	public ServerController() {
		uiController = new ServerUIController();
	}

	
	public ServerUIController getUiController() {
		return uiController;
	}

	/**
	 * Running the server and start listening to clients connected.
	 * 
	 * @param port of server
	 * @throws IOException
	 */
	public static void runServer(int port) throws IOException {
		server = new CEMSServer(port);
		uiController.setServer(server);
		server.listen(); // Start listening for connections
	}

	/**
	 * Connecting to the database.
	 * 
	 * @param ip of SQLserver.
	 * @param schema
	 * @param user of MYSQL.
	 * @param pass of MYSQL.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void connectToDB(String ip, String schema, String user, String pass)
			throws SQLException, ClassNotFoundException {
		DBConnector.getInstance(ip, schema, user, pass);
		queries = new Queries(DBConnector.getConnection());
		Queries.setGlobalLocalInfile();
	}

	/**
	 * Closes the server for connections.
	 * 
	 * @throws IOException
	 */
	public static void closeServer() throws IOException {
		server.close();
	}

	
	public static CEMSServer getServer() {
		return server;
	}

}
