package lk.ijse.dep13.snapaccess.client.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
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

    public void initialize() throws Exception {
        imgScreen.fitWidthProperty().bind(root.widthProperty());
        imgScreen.fitHeightProperty().bind(root.heightProperty());
        imgScreen.setPreserveRatio(true);

        // Connect to the server
        socket = new Socket("127.0.0.1", 5050);
        System.out.println("Connected to the server for screen sharing.");

        // Set up input and output streams
        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        PrintWriter writer = new PrintWriter(bos, true);

        InputStream is = socket.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bis);

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

        // Receiving screen data
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                while (true) {
                    try {
                        byte[] imageBytes = (byte[]) ois.readObject();
                        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                        Image screenImage = new Image(bais);

                        updateValue(screenImage);
                    } catch (Exception e) {
                        System.err.println("Error receiving screen data: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
                return null;
            }
        };

        imgScreen.imageProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Screen sharing connection closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
