package lk.ijse.dep13.snapaccess.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSceneController {

    public AnchorPane root;
    public TextArea txtSend;
    public Button btnSend;
    public ListView<String> lstView;

    private Socket socket;
    private OutputStream outputStream;

    public void initialize() {
        try {
            // Establish a connection to the server
            socket = new Socket("127.0.0.1", 5050);
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            showError("Failed to connect to the server.");
            e.printStackTrace();
        }
    }

    public void btnSendOnAction(ActionEvent actionEvent) {
        String message = txtSend.getText();

        if (message.isEmpty()) {
            txtSend.requestFocus();
            return;
        }

        sendData(message);
        updateListView("You: " + message);
        txtSend.clear();
        txtSend.requestFocus();
    }

    private void sendData(String message) {
        if (outputStream != null) {
            try {
                outputStream.write(message.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                showError("Failed to send the message.");
                e.printStackTrace();
            }
        } else {
            showError("No connection to the server.");
        }
    }

    private void updateListView(String message) {
        Platform.runLater(() -> lstView.getItems().add(message));
    }

    private void showError(String errorMessage) {
        Platform.runLater(() -> {
            lstView.getItems().add("Error: " + errorMessage);
        });
    }

    public void closeConnection() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
