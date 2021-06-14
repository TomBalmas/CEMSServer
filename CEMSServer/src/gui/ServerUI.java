package gui;

import javafx.application.Application;
import javafx.stage.Stage;
import server.ServerController;

/**
 * This class starts the server UI.
 * 
 * @version 1.0
 * @author Group 1
 */
public class ServerUI extends Application {
	protected ServerController serverController;
	
	/**
	 *This methods start the server UI.
	 *
	 *@param primaryStage the first screen that appears.
	 *@throws Exception
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		serverController = new ServerController();
		serverController.getUiController().start(primaryStage);
	}

	public static void main(String args[]) throws Exception {
		launch(args);
	}

}