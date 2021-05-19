package gui;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import server.CEMSServer;
import server.ServerController;

public class ServerUIController implements Observer {

	private int port;
	private FXMLLoader loader;
	private boolean dark = false;
	private Scene scene;
	@SuppressWarnings("unused")
	private CEMSServer server;

	public void setServer(CEMSServer server) {
		this.server = server;
		server.addObserver(loader.getController());
	}

	@FXML
	private JFXTextField portTxt;

	@FXML
	private JFXTextField ipTxt;

	@FXML
	private JFXTextField schemaTxt;

	@FXML
	private JFXTextField usernameTxt;

	@FXML
	private JFXPasswordField passwordTxt;

	@FXML
	private Label connectedLbl;

	@FXML
	private JFXButton connectBtn;
	
	@FXML
	private JFXButton disconnectBtn;

	@FXML
	private JFXTextArea serverLog;

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

	@FXML
	void clickConnect(MouseEvent event) {
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
			} catch (ClassNotFoundException e) {
				writeToLog("Driver definition failed");
			}
		} catch (SQLException ex) {
			writeToLog("SQLException: " + ex.getMessage());
			writeToLog("SQLState: " + ex.getSQLState());
			writeToLog("VendorError: " + ex.getErrorCode());
			disconnectSet();
		}
	}

	@FXML
	void clickDisconnect(MouseEvent event) {
		disconnectSet();
	}

	private void connectSet() {
		connectBtn.setVisible(false);
		disconnectBtn.setVisible(true);
		connectedLbl.setText("(Connected)");
		connectedLbl.setStyle("-fx-text-fill: green;");
	}

	private void disconnectSet() {
		try {
			ServerController.closeServer();
		} catch (IOException e) {
			writeToLog(e.getMessage());
		}
		disconnectBtn.setVisible(false);
		connectBtn.setVisible(true);
		connectedLbl.setText("(Not Connected)");
		connectedLbl.setStyle("-fx-text-fill: red;");
	}

	public FXMLLoader getLoader() {
		return loader;
	}

	public void start(Stage stage) {
		try {
			loader = new FXMLLoader(getClass().getResource("ServerUI.fxml"));
			Parent root = loader.load();
			scene = new Scene(root);
			stage.setTitle("CEMS Server");
			stage.setResizable(false);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeToLog(String msg) {
		serverLog.appendText("> " + msg + "\n");
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		writeToLog((String) arg1);
	}

}
