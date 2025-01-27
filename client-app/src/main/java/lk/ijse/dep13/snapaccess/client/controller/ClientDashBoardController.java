package lk.ijse.dep13.snapaccess.client.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ClientDashBoardController {
    public Button btnConnect;
    public TextField txtConformation;
    public TextField txtIoAddress;
    public FontAwesomeIconView connectionStatus;

    public void initialize() {
        connectionStatus.setStyle("-fx-fill: #acacac;");
    }


    public void btnConnectOnAction(ActionEvent event) {


        connectionStatus.setStyle("-fx-fill: green;");
    }
}
