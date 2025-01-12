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

    /**
     * Navigate to the Dashboard scene.
     */
    public void btnDashBoardOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.DASHBOARD);
    }

    /**
     * Navigate to the Screen Share scene.
     */
    public void btnScreenShareOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.SCREENSHARE);
    }

    /**
     * Navigate to the Video Call scene.
     */
    public void btnVideoCallOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.VIDEO_CALL);
    }

    /**
     * Navigate to the File Share scene.
     */
    public void btnFileShareOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.FILE_TRANSFER);
    }

    /**
     * Navigate to the Chat scene.
     */
    public void btnChatOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.TEXTING);
    }

    /**
     * Navigate to the Report scene.
     */
    public void btnReportOnAction(ActionEvent event) {
        navigate(AppRouter.Routes.DASHBOARD); // Replace with the correct route for "Report"
    }

    /**
     * Handles navigation to a specified route by updating the center content of the BorderPane.
     */
    private void navigate(AppRouter.Routes route) {
        if (clientController != null) {
            clientController.setCenterContent(route);
        } else {
            System.err.println("ClientController is not set!");
        }
    }
}
