package lk.ijse.dep13.snapaccess.server.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AppRouter {
    public enum Routes {
        DASHBOARD, FILE_TRANSFER, SCREENSHARE, VIDEO_CALL, TEXTING
    }

    public static AnchorPane getContainer(Routes route) throws IOException {
        AnchorPane container = null;
        switch (route) {
            case FILE_TRANSFER -> container = FXMLLoader.load(AppRouter.class.getResource("/scene/ServerFileTransfer.fxml"));
            case SCREENSHARE -> container = FXMLLoader.load(AppRouter.class.getResource("/scene/ScrenShare.fxml"));
            default -> container = FXMLLoader.load(AppRouter.class.getResource("/scene/ServerDashBoard.fxml"));

        }
        return container;
    }
}
