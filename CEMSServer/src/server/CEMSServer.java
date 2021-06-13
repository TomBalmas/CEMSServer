package server;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import common.EmptyUser;
import common.Student;
import common.TestFile;
import common.User;
import javafx.util.Pair;
import ocsf.server.ConnectionToClient;
import ocsf.server.ObservableServer;
import util.Queries;
import util.Stopwatch;

public class CEMSServer extends ObservableServer {

	private List<ClientIdentifier> connectedClients;
	private ArrayList<Pair<Stopwatch, String>> testTimers = new ArrayList<>();

	private class ClientIdentifier {

		private ConnectionToClient clientConnection;
		private String clientType = null;
		private String clientID = null;
		private String clientIp = null;

		public ClientIdentifier(ConnectionToClient clientConnection) {
			this.clientConnection = clientConnection;
		}

		public String getClientIp() {
			return clientIp;
		}

		public void setClientIp(String clientIp) {
			this.clientIp = clientIp;
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
		String clientDisconnectedString = CLIENT_DISCONNECTED.substring(0, CLIENT_DISCONNECTED.length() - 1);
		try {
			for (ClientIdentifier c : connectedClients)
				if (c.getClientConnection().equals(client)) {
					sendToLog(clientDisconnectedString + " from ip: " + c.getClientIp());
					connectedClients.remove(c);
				}
			client.close();
		} catch (EOFException e) {
			return;
		} catch (IOException e) {
			sendToLog("Client Exception: " + exception.toString());
			exception.printStackTrace();
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
		for (ClientIdentifier c : connectedClients) {
			if (c.getClientConnection().equals(client))
				connectedClients.remove(c);
		}
	}

	/**
	 * receives message from client and translates it to switch case to handle it
	 * with connection to the DB
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			// case when a manual test needs to be loaded into the DB
			if (msg instanceof Pair<?, ?>) {
				TestFile word = ((Pair<TestFile, String>) msg).getKey();
				File newWordFile = new File("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/" + word.getFileName());
				byte[] wordFileByteArray = word.getByteArray();
				FileOutputStream fos = new FileOutputStream(newWordFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				bos.write(wordFileByteArray, 0, word.getSize());
				bos.close();
				fos.close();
				String path = "C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/" + word.getFileName();
				msg = ((Pair<byte[], String>) msg).getValue();
				msg = msg + "," + path;
			}
			String[] str = ((String) msg).split("-");
			String cases = str[0];
			String args = null;
			String[] details;
			String[] studentsSSN;
			if (str.length > 1)
				args = str[1];

			switch (cases) {
			case "LOGIN":
				details = args.split(","); // details[0] = user name, details[1] = password
				User user = Queries.getUser(details[0], details[1]);
				if (!(user instanceof EmptyUser)) {
					for (ClientIdentifier c : connectedClients)
						if (user.getSSN().equals(c.getClientID())) {
							client.sendToClient("userAlreadyConnected"); // sends String
							return;
						}
					for (ClientIdentifier c : connectedClients)
						if (c.getClientID() == null && c.getClientType() == null) {
							c.setClientID(user.getSSN());
							c.setClientType(user.getClass().getSimpleName());
							c.setClientIp(client.getInetAddress().toString());
						}
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
				details = args.split(",");
				String status = details[0]; // approved or disapproved
				String teacherSSN = details[1];
				for (ClientIdentifier c : connectedClients) {
					if (c.getClientType().equals("Teacher"))
						if (c.getClientID().equals(teacherSSN))
							c.getClientConnection().sendToClient("notifyTeacher:" + status); // sends String
					break;
				}
				client.sendToClient("teacherNotified"); // sends String
				break;
			// notifies the principle
			case "NOTIFY_PRINCIPLE":
				for (ClientIdentifier c : connectedClients)
					if (c.getClientType().equals("Principle")) {
						c.getClientConnection().sendToClient("notifyPrinciple"); // sends String
						break;
					}
				client.sendToClient("principleNotified"); // sends String
				break;
			/*
			 * notifies and lock test students given their id
			 * 
			 * @param - studentSSN,studentSSN,studentSSN...
			 */
			case "NOTIFY_STUDENTS_BY_SSN":
				studentsSSN = args.split(",");
				for (ClientIdentifier c : connectedClients)
					for (String ssn : studentsSSN)
						if (c.getClientID().equals(ssn)) {
							c.getClientConnection().sendToClient("notifyStudent"); // sends string
							break;
						}
				client.sendToClient("studentsNotifiedLocked"); // sends String
				break;
			/*
			 * notify students of how many minutes were extended in a test given their SSN
			 * and minutes
			 * 
			 * @param - minutes~studentSSN,studentSSN,studentSSN...
			 */
			case "NOTIFY_STUDENTS_TIME_EXTENSION":
				details = args.split("~");
				String minutes = details[0];
				studentsSSN = details[1].split(",");
				String testCode = Queries.getTestCodeByStudentSSN(studentsSSN[0]);
				for (Pair<Stopwatch, String> pair : testTimers)
					if (pair.getValue().equals(testCode))
						pair.getKey().addMinutes(Integer.parseInt(minutes)); // adds minutes to the test timer
				for (ClientIdentifier c : connectedClients)
					for (String ssn : studentsSSN)
						if (c.getClientID().equals(ssn)) {
							c.getClientConnection().sendToClient("timeExtension:" + minutes); // sends string
							break;
						}
				client.sendToClient("studentsNotified"); // sends String
				break;
			case "REMOVE_SCHEDULED_TEST":
				client.sendToClient(Queries.removeScheduledTest(args) ? "testRemoved" : "testNotRemoved"); // sends
																											// String
				break;
			case "GET_TEST_BY_CODE":
				client.sendToClient(Queries.getTestByCode(args)); // sends Test
				break;
			case "GET_GRADES_BY_SSN":
				client.sendToClient(Queries.getGradesBySSN(args)); // sends ArrayList<StudentGrade>
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
			case "CREATE_STUDENT_REPORT":
				client.sendToClient(Queries.createStudentReportBySSNAndCourses(args)); // sends Report
				break;
			case "GET_TEST_BY_ID":
				client.sendToClient(Queries.getTestByTestId(args)); // sends Test
				break;
			case "CREATE_TEACHER_REPORT":
				client.sendToClient(Queries.createTeacherReportBySSN(args)); // sends Report
				break;
			case "GET_REPORTS_BY_TEACHER_SSN":
				client.sendToClient(Queries.getReportsByTeacherSSN(args)); // sends ArrayList<Report>
				break;
			case "CREATE_COURSE_REPORT":
				client.sendToClient(Queries.createCourseReportById(args)); // sends Report
				break;
			case "GET_SCHEDULED_TEST_BY_CODE":
				client.sendToClient(Queries.getScheduledTestByCode(args)); // sends ScheduledTest
				break;
			case "GET_STUDENTS_IN_TEST_BY_CODE":
				client.sendToClient(Queries.getStudentSSNByTestCode(args)); // sends ArrayList<Student>
				break;
			case "GET_TIME_EXTENSION_REQUEST_BY_CODE":
				client.sendToClient(Queries.getTimeExtensionRequestByTestCode(args)); // sends TimeExtensionRequest
				break;
			case "ADD_STUDENT_IN_TEST":
				client.sendToClient(Queries.addStudentInTest(args) ? "studentAdded" : "studentNotAdded"); // sends
																											// String
				break;
			case "DELETE_STUDENT_FROM_TEST":
				client.sendToClient(
						Queries.deleteStudentInTest(args) ? "studentRemovedFromTest" : "studentNotRemovedFromTest"); // sends
																														// String
				break;
			case "IS_TEST_ACTIVE":
				client.sendToClient(Queries.isActiveTest(args) ? "testActive" : "testNotActive"); // sends String
				break;
			case "IS_TIME_FOR_TEST":
				boolean isFirstStudentInTest = Queries.isTimeForTest(args);
				if (isFirstStudentInTest) {
					String[] split = args.split(",");
					String code = split[2];
					String[] timeSplitter = split[1].split(":");
					LocalTime studentBegan = LocalTime.of(Integer.parseInt(timeSplitter[0]),
							Integer.parseInt(timeSplitter[1]));
					timeSplitter = Queries.getScheduledTestByCode(code).getStartingTime().split(":");
					LocalTime startTest = LocalTime.of(Integer.parseInt(timeSplitter[0]),
							Integer.parseInt(timeSplitter[1]));
					int duration = Queries.getTestByCode(code).getTestDuration();
					Long timeDiff = ChronoUnit.MINUTES.between(studentBegan, startTest) % 60;
					testTimers.add(new Pair<>(new Stopwatch(duration - (timeDiff.intValue())), code));
					testTimers.get(testTimers.size() - 1).getKey().startTimer(new TimerTask() {
						@Override
						public void run() {
							ArrayList<Student> studentsInTest = Queries.getStudentSSNByTestCode(code);
							for (ClientIdentifier c : connectedClients)
								for (Student student : studentsInTest)
									if (c.getClientID().equals(student.getSSN())) {
										try {
											c.getClientConnection().sendToClient("notifyStudent");
											client.sendToClient("studentsNotifiedLocked");
										} catch (IOException e) {
											e.printStackTrace();
										}
										break;
									}
						}

					});
				}
				client.sendToClient(isFirstStudentInTest ? "timeForTest" : "notTimeForTest"); // sends String
				break;
			case "GET_COURSE_BY_TEST_ID":
				client.sendToClient(Queries.getCourseByTestId(args)); // sends Course
				break;
			case "ADD_GRADE":
				client.sendToClient(Queries.addGrade(args) ? "gradeAdded" : "gradeNotAdded"); // sends String
				break;
			case "LOCK_TEST":
				client.sendToClient(Queries.lockTest(args) ? "testLocked" : "testNotLocked"); // sends String
				for (Pair<Stopwatch, String> pair : testTimers)
					if (pair.getValue().equals(args)) {
						pair.getKey().stopTimer();
						testTimers.remove(pair);
					}
				break;
			case "IS_LAST_STUDENT_IN_TEST":
				client.sendToClient(Queries.isLastStudentInTest(args) ? "lastStudent" : "notLastStudent"); // sends
																											// String
				break;
			case "GET_TESTS_BY_DATE_RANGE":
				client.sendToClient(Queries.getTestsByDateRange(args)); // sends ArrayList<StudentGrade>
				break;
			case "GET_COPY_SUSPECTS":
				client.sendToClient(Queries.getCopySuspectByTestIdAndDate(args)); // sends
																					// ArrayList<Pair<String,String>>
				break;
			case "ADD_MANUAL_TEST":
				client.sendToClient(Queries.addManualTest(args) ? "manualTestAdded" : "manualTestNotAdded"); // sends
																												// String
				break;
			case "UPDATE_MANUAL_TEST":
				client.sendToClient(Queries.updateManualTest(args) ? "manualTestUpdated" : "manualTestNotUpdated"); // sends
																													// String
				break;
			case "LOCK_MANUAL_TEST":
				client.sendToClient(Queries.lockManualTest(args) ? "manualTestLocked" : "manualTestNotLocked"); // sends
																												// String
				break;
			case "GET_MANUAL_TEST_BY_STUDENT_SSN":
				client.sendToClient(Queries.getManualTestByStudentSSNAndTestId(args)); // sends TestFile
				break;
			case "UPDATE_FINISHED_TEST":
				client.sendToClient(
						Queries.updateFinishedTest(args) ? "finishedTestUpdated" : "finishedTestNotUpdated"); // sends
																												// String
				break;
			case "GET_STUDENT_ANSWERS_BY_SSN_AND_TEST_ID":
				client.sendToClient(Queries.getStudentAnswersByTestIdAndSSN(args)); // sends ArrayList<Pair<String,
																					// Integer>>
				break;
			case "GET_MANUAL_TESTS_BY_SCHEDULER_SSN":
				client.sendToClient(Queries.getManualTestsBySchedulerSSN(args)); // sends ArrayList<FinishedTest>
				break;
			case "GET_NUMBER_OF_STUDENTS_DETAILS_BY_TEST_REPORT_ID":
				client.sendToClient(Queries.getNumberOfStudentsByTestReportId(args)); // sends ArrayList<Integer>
				break;
			default:
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
