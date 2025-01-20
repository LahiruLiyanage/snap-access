package lk.ijse.dep13.snapaccess.client.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import lk.ijse.dep13.snapaccess.client.util.AppRouter;

public class ClientMenuController {

    public Button btnChat;
    public Button btnLogout;
    public Button btnReport;
    public Button btnScreenShare;
    public Button btnVideoCall;
    public Button btnDashBoard;
    public Button btnFileShare;

    private ClientController clientController; // Reference to ClientController

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void btnDashBoardOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.DASHBOARD);
    }

    public void btnScreenShareOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.SCREENSHARE);
    }

    public void btnVideoCallOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.VIDEO_CALL);
    }

    public void btnFileShareOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.FILE_TRANSFER);
    }

    public void btnChatOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.TEXTING);
    }

    public void btnReportOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.DASHBOARD); // Replace with the correct route for "Report"
    }

    private void navigate(AppRouter.Routes route) {
        if (clientController != null) {
            clientController.setCenterContent(route);
        } else {
            System.err.println("ClientController is not set!");
        }
    }
}
