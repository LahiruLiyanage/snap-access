package lk.ijse.dep13.snapaccess.client.controller;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class FileTransferController {

    public StackPane send;

    public void sendOnMouseClicked(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Send");

        // Directory to Downloads
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));

        // Add file filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif")
        );

        // File chooser
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
            sendFileToServer(selectedFile);
        } else {
            System.out.println("No file selected.");
        }
    }

    public void sendOnDragOver(DragEvent event) {
        if (event.getGestureSource() != send && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
        }
        event.consume();
    }

    // Drag and Drop
    public void sendOnDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            List<File> files = dragboard.getFiles();
            File file = files.get(0);
            System.out.println("File dropped: " + file.getAbsolutePath());
            sendFileToServer(file);
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
    }

    // Files to the server
    private void sendFileToServer(File file) {
        final String SERVER_HOST = "127.0.0.1";
        final int SERVER_PORT = 5050;

        if (!file.exists() || !file.isFile()) {
            System.err.println("Invalid file selected. Please choose a valid file.");
            return;
        }

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to the server.");

            // Send the request type
            writer.println("FILE_TRANSFER");

            // Send username
            String username = "user123";
            writer.println(username);

            // Send the file name
            writer.println(file.getName());

            // Send file data
            byte[] buffer = new byte[1024];
            int bytesRead;

            System.out.println("Sending file: " + file.getName());
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            System.out.println("File transfer completed successfully.");

        } catch (IOException e) {
            System.err.println("Error during file transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
