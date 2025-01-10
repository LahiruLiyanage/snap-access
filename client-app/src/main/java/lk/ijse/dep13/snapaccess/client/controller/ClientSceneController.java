package lk.ijse.dep13.snapaccess.client.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class MainSceneController {

    public void initialize() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9090);
        OutputStream os = socket.getOutputStream();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter your message: ");
            String message = scanner.nextLine();
            os.write(("Lahiru Sandhamal: " + message).getBytes());
            os.flush();


        }
    }
}
