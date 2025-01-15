package lk.ijse.dep13.snapaccess.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSceneController {
    public AnchorPane root;
    public TextArea txtSend;
    public Button btnSend;
    public ListView<String> lstView;

    private Socket socket;
    private OutputStream os;

    private String clientIdentifier; // Unique identifier for this client

    public void initialize() {
        new Thread(() -> {
            try {
                connectToServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void connectToServer() throws IOException {
        socket = new Socket("localhost", 5050);
        os = socket.getOutputStream();
        System.out.println("Connected to the server!");

        // Unique identifier for this client
        clientIdentifier = socket.getInetAddress().getHostName();

        // Start a thread to listen for messages from the server
        new Thread(this::handleServerMessages).start();
    }

    private void handleServerMessages() {
        try {
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                String message = new String(buffer, 0, len).trim();

                // Ignore messages originating from this client
                if (!message.startsWith(clientIdentifier + ":")) {
                    Platform.runLater(() -> lstView.getItems().add(message));
                }
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

        // Add "You:" prefix for local display
        String formattedMessage = "You: " + message;

        Platform.runLater(() -> lstView.getItems().add(formattedMessage));

        try {
            os.write(message.getBytes()); // Send raw message to server
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        txtSend.clear();
        txtSend.requestFocus();
    }
}
