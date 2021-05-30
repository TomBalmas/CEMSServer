package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.ActiveTest;
import common.Course;
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
			String args = null;
			if (str.length > 1)
				args = str[1];
			switch (cases) {
			case "LOGIN":
				String[] details = args.split(","); // details[0] = user name, details[1] = password
				client.sendToClient(Queries.getUser(details[0], details[1])); // sends User
				break;
			case "QUESTION_BANK":
				client.sendToClient(Queries.getQuestionsByFields(args)); // sends ArrayList<Question>
				break;
			case "TEST_BANK":
				client.sendToClient(Queries.getTestsByField(args)); // sends ArrayList<Test>
				break;
			case "DELETE_TEST":
				client.sendToClient(Queries.deleteTestByID(args) ? "deleted" : "notDeleted"); // send String
				break;
			case "ACTIVE_TEST":
				client.sendToClient(Queries.getActiveTestsBySchedulerId(args)); // sends ArrayList<ActiveTest>
				break;
			case "SCHEDULED_TESTS":
				client.sendToClient(Queries.getScheduledTestsBySchedulerID(args)); // sends ArrayList<ScheduledTest>
				break;
			case "SCHEDULE_TEST":
				client.sendToClient(Queries.setTestDate(args) ? "scheduled" : "notScheduled"); // sends String
				break;
			case "GET_COURSES_BY_FIELD":
				client.sendToClient(Queries.getCoursesByField(args)); // sends ArrayList<Course>
			case "FINISHED_TESTS":
				client.sendToClient(Queries.getFinishedTestsBySchedulerSSN(args)); // sends ArrayList<FinishedTest>
			case "ADD_TEST":
				client.sendToClient(Queries.addNewTest(args)); // sends String
			case "DELETE_QUESTION":
				client.sendToClient(Queries.deleteQuestionById(args) ? "deleted" : "notDeleted"); // sends String
			case "ADD_QUESTION":
				client.sendToClient(Queries.addQuestion(args)); // sends String
			case "GET_QUESTIONS_TABLE":
				client.sendToClient(Queries.getQuestionsTable()); // sends ArrayList<Question>
			case "GET_TESTS_TABLE":
				client.sendToClient(Queries.getTestsTable()); // sends ArrayList<Test>
			case "EDIT_QUESTION":
				client.sendToClient(Queries.editQuestion(args) ? "editSuccess" : "editFailed"); // sends String
			case "EDIT_TEST":
				client.sendToClient(Queries.editTest(args) ? "editSuccess" : "editFailed"); // sends String
			case "GET_QUESTIONS_BY_AUTHOR_ID":
				client.sendToClient(Queries.getQuestionsByAuthorId(args)); // sends ArrayList<String>
			case "GET_TESTS_BY_AUTHOR_ID":
				client.sendToClient(Queries.getTestsByAuthorId(args)); // sends ArrayList<String>
			case "GET_NAME_BY_ID":
				client.sendToClient("name:" + Queries.getNameById(args)); // sends String
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
