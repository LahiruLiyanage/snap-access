package lk.ijse.dep13.snapaccess.client.controller;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import lk.ijse.dep13.snapaccess.client.util.AppRouter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    public BorderPane client_parent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scene/ClientMenu.fxml"));
            Pane menu = loader.load();

            ClientMenuController menuController = loader.getController();

            menuController.setClientController(this);

            client_parent.setLeft(menu);

            setCenterContent(AppRouter.Routes.DASHBOARD);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCenterContent(AppRouter.Routes route) {
        try {
            Pane newContent = AppRouter.getContainer(route);

            client_parent.setCenter(newContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
