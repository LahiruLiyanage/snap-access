package lk.ijse.dep13.snapaccess.server.controller;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import lk.ijse.dep13.snapaccess.server.util.AppRouter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    public BorderPane server_parent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scene/ServerMenu.fxml"));
            Pane menu = loader.load();

            ServerMenuController menuController = loader.getController();

            menuController.setServerController(this);

            server_parent.setLeft(menu);

            setCenterContent(AppRouter.Routes.DASHBOARD);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setCenterContent(AppRouter.Routes route) {
        try {
            Pane newContent = AppRouter.getContainer(route);

            server_parent.setCenter(newContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
