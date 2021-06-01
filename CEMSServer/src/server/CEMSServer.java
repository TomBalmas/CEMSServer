package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.User;
import ocsf.server.ConnectionToClient;
import ocsf.server.ObservableServer;
import util.Queries;

public class CEMSServer extends ObservableServer {

	private List<ClientIdentifier> connectedClients;

	private static class ClientIdentifier {
		private static int counter = 0;

		private ConnectionToClient clientConnection;
		private String clientType = null;
		private String clientID = null;
		private int identifier;

		public ClientIdentifier(ConnectionToClient clientConnection) {
			this.clientConnection = clientConnection;
			identifier = counter;
			counter++;
		}

		public int getIdentifier() {
			return identifier;
		}

		public ConnectionToClient getClientConnection() {
			return clientConnection;
		}

		public String getClientType() {
			return clientType;
		}

		public String getClientID() {
			return clientID;
		}

		public void setClientType(String clientType) {
			this.clientType = clientType;
		}

		public void setClientID(String clientID) {
			this.clientID = clientID;
		}

		@Override
		public String toString() {
			return "ClientIdentifier [clientType=" + clientType + ", clientID=" + clientID + ", identifier="
					+ identifier + "]";
		}

	}

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
		connectedClients.add(new ClientIdentifier(client));
		sendToLog(clientConnectedString + " from ip: " + client.getInetAddress());
	}

	/**
	 * catch exceptions and write them to the log
	 *
	 */
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {

		try {
			for (ClientIdentifier c : connectedClients)
				if (c.getClientConnection().equals(client))
					connectedClients.remove(c);
			sendToLog("Client Exception: " + exception.toString());
			client.close();
		} catch (Exception e) {

		}
	}

	protected List<ClientIdentifier> getClients() {
		return connectedClients;
	}

	/**
	 * notify log when a client has disconnected,removing from connectedClients list
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		String clientDisconnectedString = CLIENT_DISCONNECTED.substring(0, CLIENT_DISCONNECTED.length() - 1);
		for (ClientIdentifier c : connectedClients)
			if (c.getClientConnection().equals(client))
				connectedClients.remove(c);
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
				User user = Queries.getUser(details[0], details[1]);
				if (user != null)
					for (ClientIdentifier c : connectedClients)
						if (c.getClientID() == null && c.getClientType() == null) {
							c.setClientID(user.getSSN());
							c.setClientType(user.getClass().getSimpleName());
						}
				client.sendToClient(user); // sends User

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
				System.out.println(Queries.getActiveTestsBySchedulerId(args).toString());
				client.sendToClient(Queries.getActiveTestsBySchedulerId(args)); // sends ArrayList<ActiveTest>
				break;
			case "SCHEDULED_TESTS":
				client.sendToClient(Queries.getScheduledTestsBySchedulerID(args)); // sends ArrayList<ScheduledTest>
				break;
			case "SET_TEST_DATE":
				client.sendToClient(Queries.setTestDate(args) ? "scheduled" : "notScheduled"); // sends String
				break;
			case "GET_COURSES_BY_FIELD":
				client.sendToClient(Queries.getCoursesByField(args)); // sends ArrayList<Course>
				break;
			case "FINISHED_TESTS":
				client.sendToClient(Queries.getFinishedTestsBySchedulerSSN(args)); // sends ArrayList<FinishedTest>
				break;
			case "ADD_TEST":
				client.sendToClient(Queries.addNewTest(args)); // sends String
				break;
			case "DELETE_QUESTION":
				client.sendToClient(Queries.deleteQuestionById(args) ? "deleted" : "notDeleted"); // sends String
				break;
			case "ADD_QUESTION":
				client.sendToClient(Queries.addQuestion(args)); // sends String
				break;
			case "GET_QUESTIONS_TABLE":
				client.sendToClient(Queries.getQuestionsTable()); // sends ArrayList<Question>
				break;
			case "GET_TESTS_TABLE":
				client.sendToClient(Queries.getTestsTable()); // sends ArrayList<Test>
				break;
			case "EDIT_QUESTION":
				client.sendToClient(Queries.editQuestion(args) ? "editSuccess" : "editFailed"); // sends String
				break;
			case "EDIT_TEST":
				client.sendToClient(Queries.editTest(args) ? "editSuccess" : "editFailed"); // sends String
				break;
			case "GET_QUESTIONS_BY_AUTHOR_ID":
				client.sendToClient(Queries.getQuestionsByAuthorId(args)); // sends ArrayList<String>
				break;
			case "GET_TESTS_BY_AUTHOR_ID":
				client.sendToClient(Queries.getTestsByAuthorId(args)); // sends ArrayList<String>
				break;
			case "GET_NAME_BY_ID":
				client.sendToClient("name:" + Queries.getNameById(args)); // sends String
				break;
			case "GET_TIME_EXTENSION_REQUESTS":
				client.sendToClient(Queries.getTimeExtensionRequests()); // sends ArrayList<TimeExtensionRequest>
				break;
			case "ADD_TIME_EXTENSION_REQUEST":
				client.sendToClient(Queries.addTimeExtensionRequest(args) ? "requestAdded" : "requestNotAdded"); // sends
																													// String
				break;
			case "DELETE_TIME_EXTENSION_REQUEST":
				client.sendToClient(Queries.deleteTimeExtensionRequest(args) ? "requestDeleted" : "requestNotDeleted"); // sends
																														// String
				break;
			case "GET_REPORTS":
				client.sendToClient(Queries.getReportsTable()); // sends ArrayList<Report>
				break;
			case "GET_QUESTIONS_FROM_TEST":
				client.sendToClient(Queries.getQuestionsFromTest(args)); // sends ArrayList<Question>
				break;
			case "DELETE_REPORT":
				client.sendToClient(Queries.deleteReport(args) ? "reportDeleted" : "reportNotDeleted"); // sends String
				break;
			// notifies a teacher
			case "NOTIFY_TEACHER_BY_SSN":
				for (ClientIdentifier c : connectedClients) {
					if (c.getClientType().equals("Teacher"))
						if (c.getClientID().equals(args))
							c.getClientConnection().sendToClient("notify"); // sends String
					break;
				}
				client.sendToClient("teacherNotified"); // sends String
				break;
			// notifies the principle
			case "NOTIFY_PRINCIPLE":
				for (ClientIdentifier c : connectedClients)
					if (c.getClientType().equals("Principle")) {
						c.getClientConnection().sendToClient("notify"); // sends String
						break;
					}
				client.sendToClient("principleNotified"); // sends String
				break;
			/*
			 * notifies students given their id
			 * 
			 * @param - studentSSN,studentSSN,studentSSN...
			 */
			case "NOTIFY_STUDENTS_BY_SSN":
				String[] studentsSSN = args.split(",");
				for (ClientIdentifier c : connectedClients)
					for (String ssn : studentsSSN)
						if (c.getClientID().equals(ssn)) {
							c.getClientConnection().sendToClient("notify"); // sends string
							break;
						}
				client.sendToClient("studentsNotifies"); // sends String
				break;
			case "REMOVE_SCHEDULED_TEST":
				client.sendToClient(Queries.removeScheduledTest(args) ? "testRemoved" : "testNotRemoved"); // sends
																											// String
				break;
			case "GET_TEST_BY_CODE":
				client.sendToClient(Queries.getTestByCode(args)); // sends Test
				break;
			case "GET_GRADES_BY_SSN":
				client.sendToClient(Queries.getGradesBySSN(args)); // sends ArrayList<Integer>
				break;
			case "SAVE_STUDENT_ANSWERS":
				client.sendToClient(Queries.saveStudentAnswers(args) ? "answersSaved" : "answersNotSaved"); // sends
																											// String
				break;
			case "RESCHEDULE_TEST":
				client.sendToClient(Queries.rescheduleTest(args) ? "testRescheduled" : "testNotRescheduled"); // sends
																												// String
				break;
			case "GET_STUDENT_TEST":
				client.sendToClient(Queries.getStudentTest(args)); // sends Pair<Test,ArrayList<String>>
				break;
			case "ADD_FINISHED_TEST":
				client.sendToClient(Queries.addFinishedTest(args) ? "finishedTestAdded" : "finishedTestNotAdded"); // sends
																													// String
				break;
			case "GET_STUDENTS":
				client.sendToClient(Queries.getStudents()); // sends ArrayList<Student>
				break;
			case "GET_COURSES_BY_STUDENT":
				client.sendToClient(Queries.getCoursesByStudentSSN(args)); // sends ArrayList<Course>
				break;
			case "GET_TEACHERS":
				client.sendToClient(Queries.getTeachers()); // sends ArrayList<Teacher>
				break;
			case "GET_COURSES":
				client.sendToClient(Queries.getCourses()); // sends ArrayList<Course>
				break;
			case "GET_TESTS_BY_TEACHER_SSN":
				client.sendToClient(Queries.getTestsByTeacherSSN(args)); // sends ArrayList<Test>
				break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
