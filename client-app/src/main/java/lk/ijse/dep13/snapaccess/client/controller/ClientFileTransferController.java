package lk.ijse.dep13.snapaccess.client.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientFileTransferController {

    public StackPane send;
    public ImageView imgBackground;
    public AnchorPane root;
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 5050;

    public void initialize() {
        imgBackground.fitWidthProperty().bind(root.widthProperty());
        imgBackground.fitHeightProperty().bind(root.heightProperty());
        imgBackground.setPreserveRatio(false);
    }

    public void sendOnMouseClicked(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Send");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            sendFileToServer(selectedFile);
        }
    }

    public void sendOnDragOver(DragEvent event) {
        if (event.getGestureSource() != send && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
        }
        event.consume();
    }

    public void sendOnDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;

        if (dragboard.hasFiles()) {
            List<File> files = dragboard.getFiles();
            if (!files.isEmpty()) {
                sendFileToServer(files.get(0));
                success = true;
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    private void sendFileToServer(File file) {
        if (!file.exists() || !file.isFile()) {
            showError("Invalid file selected");
            return;
        }

        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream());
                 FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                // Send request type
                dos.writeUTF("FILE_TRANSFER");

                // Send file metadata
                dos.writeUTF(System.getProperty("user.name")); // username
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
                dos.flush();

                // Send file data
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalSent = 0;
                long fileSize = file.length();

                while ((bytesRead = bis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;

                    final long finalTotalSent = totalSent;
                    Platform.runLater(() -> updateProgress(finalTotalSent, fileSize));
                }
                dos.flush();

                // Get response
                String response = dis.readUTF();
                if (response.startsWith("SUCCESS")) {
                    showSuccess("File sent successfully!");
                } else {
                    showError("Failed to send file: " + response);
                }

            } catch (IOException e) {
                e.printStackTrace();
                showError("Error sending file: " + e.getMessage());
            }
        }).start();
    }

    private void updateProgress(long sent, long total) {
        double progress = (double) sent / total;
        // Update to be done -> Progress bar
        System.out.printf("Progress: %.2f%%\n", progress * 100);
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
