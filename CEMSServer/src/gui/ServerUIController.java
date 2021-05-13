package gui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ServerUIController {

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
    private JFXTextArea serverLog;

}
