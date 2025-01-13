package lk.ijse.dep13.snapaccess.client.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ScreenShareController {
    public AnchorPane root;
    public ImageView imgScreen;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private volatile boolean isRunning = true;

    @FXML
    public void initialize() {
        imgScreen.fitWidthProperty().bind(root.widthProperty());
        imgScreen.fitHeightProperty().bind(root.heightProperty());
        imgScreen.setPreserveRatio(true);

        Task<Void> connectionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                initializeConnection();
                return null;
            }
        };

        connectionTask.setOnFailed(e -> {
            System.err.println("Connection failed: " + connectionTask.getException().getMessage());
            Platform.runLater(() -> {
                // Show error dialog or handle the error appropriately
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
            });
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setOnCloseRequest(event -> closeConnection());
        });

        new Thread(connectionTask).start();
    }

    private void initializeConnection() {
        try {
            // Connect to the server
            socket = new Socket("127.0.0.1", 5050);
            System.out.println("Connected to the server for screen sharing.");

            OutputStream os = socket.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            PrintWriter writer = new PrintWriter(bos, true);

            oos = new ObjectOutputStream(bos);
            oos.flush();

            InputStream is = socket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ois = new ObjectInputStream(bis);

            // Send the request type to the server
            writer.println("SCREEN_SHARE");

            // Receive screen dimensions
            int screenWidth = ois.readInt();
            int screenHeight = ois.readInt();

            Platform.runLater(() -> {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.setWidth(screenWidth);
                stage.setHeight(screenHeight);
            });

            startScreenReceiving();

        } catch (Exception e) {
            System.err.println("Error initializing connection: " + e.getMessage());
            e.printStackTrace();
            closeConnection();
        }
    }

    private void startScreenReceiving() {
        Task<Void> receiveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (isRunning && !isCancelled()) {
                    try {
                        byte[] imageBytes = (byte[]) ois.readObject();
                        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                        Image screenImage = new Image(bais);

                        Platform.runLater(() -> imgScreen.setImage(screenImage));
                    } catch (Exception e) {
                        if (isRunning) {
                            System.err.println("Error receiving screen data: " + e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                return null;
            }
        };

        receiveTask.setOnFailed(e -> {
            System.err.println("Screen receiving failed: " + receiveTask.getException().getMessage());
            closeConnection();
        });

        new Thread(receiveTask).start();
    }

    public void closeConnection() {
        isRunning = false;
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Screen sharing connection closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}