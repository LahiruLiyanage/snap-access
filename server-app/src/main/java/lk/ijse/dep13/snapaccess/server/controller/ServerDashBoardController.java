package lk.ijse.dep13.snapaccess.server.controller;

import javafx.scene.control.TextField;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class ServerDashBoardController {
    public TextField txtConformation;
    public TextField txtIoAddress;

    public void initialize() {
        setIoAddress();
        setRandomConfirmation();
    }

    private void setIoAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();

            txtIoAddress.setText(ipAddress);
        } catch (UnknownHostException e) {
            txtIoAddress.setText("Unable to fetch IP address");
            e.printStackTrace();
        }
    }

    private void setRandomConfirmation() {
        Random random = new Random();
        int randomNumber = random.nextInt(90) + 10; // Range: 10 to 99

        txtConformation.setText(String.valueOf(randomNumber));
    }
}
