package lk.ijse.dep13.snapaccess.server.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import lk.ijse.dep13.snapaccess.server.util.AppRouter;

public class ServerMenuController {
    public Button btnChat;
    public Button btnDashBoard;
    public Button btnFileShare;
    public Button btnLogout;
    public Button btnReport;
    public Button btnScreenShare;
    public Button btnVideoCall;

    private ServerController severController; // Reference to ClientController

    public void setServerController(ServerController clientController) {
        this.severController = clientController;
    }

    public void btnChatOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.TEXTING);
    }

    public void btnDashBoardOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.DASHBOARD);
    }

    public void btnFileShareOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.FILE_TRANSFER);
    }

    public void btnReportOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.DASHBOARD);
    }

    public void btnScreenShareOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.SCREENSHARE);
    }

    public void btnVideoCallOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.VIDEO_CALL);
    }

    private void navigate(AppRouter.Routes route) {
        if (severController != null) {
            severController.setCenterContent(route);
        } else {
            System.err.println("ServerController is not set!");
        }
    }

}
