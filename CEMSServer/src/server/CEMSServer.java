package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import common.Question;
import common.User;
import ocsf.server.ConnectionToClient;
import ocsf.server.ObservableServer;
import util.Queries;

public class CEMSServer extends ObservableServer {

	private List<ConnectionToClient> connectedClients;

	public CEMSServer(int port) {
		super(port);
		connectedClients = new ArrayList<>();
	}

	@Override
	protected void serverStarted() {
		String serverStartedString = SERVER_STARTED.substring(0, SERVER_STARTED.length() - 1);
		sendToLog(serverStartedString + " on port: " + getPort());
	}

	/**
	 * this function
	 * 
	 * @param msg - the string for sending to the server log
	 */
	private void sendToLog(String msg) {
		setChanged();
		notifyObservers(msg);
	}

	@Override
	protected void serverStopped() {

	}

	/**
	 * notify log when a client has connected, adding to connectedClients list
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		String clientConnectedString = CLIENT_CONNECTED.substring(0, CLIENT_CONNECTED.length() - 1);
		connectedClients.add(client);
		sendToLog(clientConnectedString + " from ip: " + client.getInetAddress());
	}

	protected List<ConnectionToClient> getClients() {
		return connectedClients;
	}

	/**
	 * notify log when a client has disconnected,removing from connectedClients list
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		String clientDisconnectedString = CLIENT_DISCONNECTED.substring(0, CLIENT_DISCONNECTED.length() - 1);
		connectedClients.remove(client);
		sendToLog(clientDisconnectedString + " ip: " + client.getInetAddress());
	}

	/**
	 * receives message from client and translates it to switch case to handle it
	 * with connection to the DB
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			
			String[] str = ((String) msg).split("-");
			switch (str[0]) {
			case "LOGIN":
				String[] details = str[1].split(","); // details[0] = user name, details[1] = password
				User user = Queries.getUser(details[0], details[1]);
				if (user == null)
					client.sendToClient("LOGIN-null:");
				client.sendToClient(user);
				break;
			case "QUESTION_BANK":
				String fields =  str[1];
				ArrayList<Question> questions = new ArrayList<>();
				questions=Queries.getQuestions(fields);
				client.sendToClient(questions); 
			default: 
				break;
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
