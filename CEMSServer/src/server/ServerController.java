package server;

import java.io.IOException;
import java.sql.SQLException;

import gui.ServerUIController;

public class ServerController {

	public static CEMSServer server;
	private static ServerUIController uiController;

	public ServerController() {
		uiController = new ServerUIController();
	}

	public ServerUIController getUiController() {
		return uiController;
	}

	public static void runServer(int port) throws IOException {
		server = new CEMSServer(port);
		uiController.setServer(server);
		server.listen(); // Start listening for connections
	}

	public static void connectToDB(String ip, String schema, String user, String pass)
			throws SQLException, ClassNotFoundException {
		DBConnector.getInstance(ip, schema, user, pass);
	}

	public static void closeServer() throws IOException {
		server.close();
	}

	public static CEMSServer getServer() {
		return server;
	}

}
