package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import server.CEMSServer;
import server.ServerController;
import util.Queries;

public class ServerUIController implements Observer, Initializable {

	private int port;
	private FXMLLoader loader;
	@SuppressWarnings("unused")
	private CEMSServer server;
	private boolean connection = false;

	public void setServer(CEMSServer server) {
		this.server = server;
		server.addObserver(loader.getController());
	}

	@FXML
	private AnchorPane serverConnectionAnchor;

	@FXML
	private AnchorPane insideserverConnectionAnchor;

	@FXML
	private JFXTextField portTxt;

	@FXML
	private AnchorPane dataBaseConnectionAnchor;

	@FXML
	private AnchorPane insidedataBaseConnectionAnchor;

	@FXML
	private VBox databaseConVBox;

	@FXML
	private JFXTextField ipTxt;

	@FXML
	private JFXTextField schemaTxt;

	@FXML
	private JFXTextField usernameTxt;

	@FXML
	private JFXPasswordField passwordTxt;

	@FXML
	private JFXButton connectBtn;

	@FXML
	private JFXButton disconnectBtn;

	@FXML
	private JFXTextArea serverLog;

	@FXML
	private JFXButton importBtn;

	public JFXTextField getIpTxt() {
		return ipTxt;
	}

	public JFXTextField getSchemaTxt() {
		return schemaTxt;
	}

	public JFXTextField getUsernameTxt() {
		return usernameTxt;
	}

	public JFXPasswordField getPasswordTxt() {
		return passwordTxt;
	}

	/**
	 * when clicking on connect the server will start and start listen to
	 * connections
	 * 
	 * @param event
	 */
	@FXML
	void clickConnect(Event event) {
		port = Integer.parseInt(portTxt.getText());
		try {
			ServerController.runServer(port);
		} catch (IOException e) {
			writeToLog("Could not run server");
			disconnectSet();
		}
		try {
			try {
				ServerController.connectToDB(ipTxt.getText(), schemaTxt.getText(), usernameTxt.getText(),
						passwordTxt.getText());
				writeToLog("Driver definition succeed");
				writeToLog("SQL connection succeed");
				connectSet();
				connection = true;
				importBtn.setDisable(false);
			} catch (ClassNotFoundException e) {
				writeToLog("Driver definition failed");
				disconnectSet();
			}
		} catch (SQLException ex) {
			writeToLog("SQLException: " + ex.getMessage());
			writeToLog("SQLState: " + ex.getSQLState());
			writeToLog("VendorError: " + ex.getErrorCode());
			disconnectSet();
		}
	}

	/**
	 * imports data from text files to DB
	 * 
	 * @param event
	 */
	@FXML
	void importClicked(MouseEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File file = directoryChooser.showDialog(new Stage());
		String path = file.getAbsolutePath();
		path = path.replaceAll("\\\\", "/");
		Queries.setGlobalLocalInfile();
		Queries.deleteTableContents("users");
		Queries.deleteTableContents("fields");
		Queries.deleteTableContents("courses");
		Queries.loadTxtFileIntoTable("users," + path + "/users.txt");
		Queries.loadTxtFileIntoTable("fields," + path + "/fields.txt");
		Queries.loadTxtFileIntoTable("courses," + path + "/courses.txt");
		writeToLog("Data imported");
	}

	/**
	 * when clicking disconnect it will close the connection to the server
	 * 
	 * @param event
	 */
	@FXML
	void clickDisconnect(MouseEvent event) {
		importBtn.setDisable(true);
		disconnectSet();
	}

	/**
	 * this function contains the visual changes when clicking connect
	 */
	private void connectSet() {
		databaseConVBox.setDisable(true);
		connectBtn.setVisible(false);
		disconnectBtn.setVisible(true);
		disconnectBtn.setDisable(false);
		portTxt.setDisable(true);
		serverLog.requestFocus();
	}

	/**
	 * this function contains the visual and functional changes when clicking
	 * disconnect
	 */
	private void disconnectSet() {
		try {
			ServerController.closeServer();
		} catch (IOException e) {
			writeToLog(e.getMessage());
		}
		if (connection)
			serverLog.clear();
		connection = false;
		databaseConVBox.setDisable(false);
		passwordTxt.requestFocus();
		disconnectBtn.setVisible(false);
		connectBtn.setVisible(true);
		portTxt.setDisable(false);
	}

	/**
	 * @return instance of the fxml loader
	 */
	public FXMLLoader getLoader() {
		return loader;
	}

	public void start(Stage stage) {
		try {
			loader = new FXMLLoader(getClass().getResource("ServerUI.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add("util/style.css");
			stage.setTitle("CEMS Server");
			stage.setResizable(false);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param msg - this message will be written to the server log when called
	 */
	private void writeToLog(String msg) {
		serverLog.appendText("> " + msg + "\n");
	}

	/**
	 * this function will get the messages from the server to write them to the log
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		writeToLog((String) arg1);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		importBtn.setDisable(true);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				passwordTxt.requestFocus();
			}
		});
	}

}