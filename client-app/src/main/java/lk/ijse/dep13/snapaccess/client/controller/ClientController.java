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
            // Load the ClientMenu.fxml into the left section of the BorderPane
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scene/ClientMenu.fxml"));
            Pane menu = loader.load(); // Use Pane instead of AnchorPane to handle any layout

            // Get the ClientMenuController instance
            ClientMenuController menuController = loader.getController();

            // Set a reference to this ClientController in the ClientMenuController
            menuController.setClientController(this);

            // Add the menu to the left section of the BorderPane
            client_parent.setLeft(menu);

            // Optionally, load the initial center content (e.g., dashboard)
            setCenterContent(AppRouter.Routes.DASHBOARD);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dynamically set the center content of the BorderPane.
     */
    public void setCenterContent(AppRouter.Routes route) {
        try {
            // Load the new content for the center
            Pane newContent = AppRouter.getContainer(route);

            // Set the new content to the center of the BorderPane
            client_parent.setCenter(newContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
