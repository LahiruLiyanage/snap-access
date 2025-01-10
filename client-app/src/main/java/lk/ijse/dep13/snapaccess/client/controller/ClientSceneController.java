package lk.ijse.dep13.snapaccess.client.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientSceneController {

    public void initialize() {
        // Run terminal interaction in a separate thread
        new Thread(() -> {
            try {
                Socket socket = new Socket("127.0.0.1", 5050);
                OutputStream os = socket.getOutputStream();
                Scanner scanner = new Scanner(System.in);

                while (true) {
                    System.out.print("Enter your message: ");
                    String message = scanner.nextLine();
                    os.write(("Lahiru Sandhamal: " + message).getBytes());
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
