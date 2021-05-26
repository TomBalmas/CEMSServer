package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.ActiveTest;
import common.FinishedTest;
import common.Question;
import common.ScheduledTest;
import common.Test;
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

	/**
	 * catch exceptions and write them to the log
	 *
	 */
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {

		try {
			sendToLog("Client Exception: " + exception.toString());
			client.close();
		} catch (Exception e) {

		}
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
			String cases = str[0];
			String args = str[1];
			boolean deleted;
			switch (cases) {
			case "LOGIN":
				String[] details = args.split(","); // details[0] = user name, details[1] = password
				client.sendToClient(Queries.getUser(details[0], details[1]));
				break;
			case "QUESTION_BANK":
				ArrayList<Question> questions = Queries.getQuestionsByFields(args);
				client.sendToClient(questions);
				break;
			case "TEST_BANK":
				ArrayList<Test> tests = Queries.getTestsByField(args);
				client.sendToClient(tests);
				break;
			case "DELETE_TEST":
				deleted = Queries.deleteTestByID(args);
				client.sendToClient(deleted ? "deleted" : "notDeleted");
				break;
			case "ACTIVE_TEST":
				ArrayList<ActiveTest> activeTests = Queries.getActiveTestsByAuthorId(args);
				client.sendToClient(activeTests);
				break;
			case "SCHEDULED_TESTS":
				ArrayList<ScheduledTest> scheduledTests = Queries.getScheduledTestsByAuthorID(args);
				client.sendToClient(scheduledTests);
				break;
			case "SCHEDULE_TEST":
				boolean scheduled = Queries.setTestDate(args);
				client.sendToClient(scheduled ? "scheduled" : "notScheduled");
				break;
			case "GET_COURSES_BY_FIELD":
				ArrayList<String> courses = Queries.getCoursesByField(args);
				client.sendToClient(courses);
			case "FINISHED_TESTS":
				ArrayList<FinishedTest> finishedTests = Queries.getFinishedTestsBySchedulerSSN(args);
				client.sendToClient(finishedTests);
			case "ADD_TEST":
				String testId = Queries.addNewTest(args);
				client.sendToClient(testId);
			case "DELETE_QUESTION":
				deleted = Queries.deleteQuestionById(args);
				client.sendToClient(deleted ? "deleted" : "notDeleted");
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
