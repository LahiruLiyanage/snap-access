package lk.ijse.dep13.snapaccess.server.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerSceneController {
    public AnchorPane root;
    public TextArea txtSend;
    public Button btnSend;
    public ListView<String> lstView;
    private final List<Socket> clients = new ArrayList<>();

    public void initialize() {
        // Start the server in a new thread
        new Thread(() -> {
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(5050);
        System.out.println("Server started at port 5050");
        while (true) {
            Socket localSocket = serverSocket.accept();
            clients.add(localSocket);
            System.out.println("Client connected!");
            new Thread(() -> handleClient(localSocket)).start();
        }
    }

    private void handleClient(Socket socket) {
        try {
            String clientHostName = socket.getInetAddress().getHostName();
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                String message = new String(buffer, 0, len).trim();

                // Format the message with the client's hostname
                String formattedMessage = clientHostName + ": " + message;

                // Update the server's ListView
                Platform.runLater(() -> lstView.getItems().add(formattedMessage));

                // Broadcast the formatted message to all clients
                broadcastMessage(formattedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnSendOnAction(ActionEvent actionEvent) {
        String message = txtSend.getText();

        if (message.isEmpty()) {
            txtSend.requestFocus();
            return;
        }

        String serverHostName = getLocalHostName();
        String formattedMessage = serverHostName + ": " + message;

        // Update the ListView on the server
        Platform.runLater(() -> lstView.getItems().add(formattedMessage));

        // Broadcast the message to all clients
        broadcastMessage(formattedMessage);

        // Clear the text area
        txtSend.clear();
        txtSend.requestFocus();
    }

    private void broadcastMessage(String message) {
        for (Socket client : clients) {
            try {
                OutputStream os = client.getOutputStream();
                os.write(message.getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getLocalHostName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown Host";
        }
    }
}
