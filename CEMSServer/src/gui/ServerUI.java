package gui;

import javafx.application.Application;
import javafx.stage.Stage;
import server.ServerController;

public class ServerUI extends Application {
	protected ServerController serverController;

	@Override
	public void start(Stage primaryStage) throws Exception {
		serverController = new ServerController();
		serverController.getUiController().start(primaryStage);
	}

	public static void main(String args[]) throws Exception {
		launch(args);
	}

}
