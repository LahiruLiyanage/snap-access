package lk.ijse.dep13.snapaccess.server.controller;

import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lk.ijse.dep13.snapaccess.server.MultiFunctionServer;

import java.io.File;
import java.util.List;

public class FileTransferController {
    public ImageView imgBackground;
    public AnchorPane root;
    public StackPane send;

    public void sendOnDragDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        if (!files.isEmpty()) {
            MultiFunctionServer.setSelectedFile(files.get(0).getAbsolutePath());
        }
        event.setDropCompleted(true);
        event.consume();
    }

    public void sendOnDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    public void sendOnMouseClicked(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Share");
        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());

        if (selectedFile != null) {
            handleFileSelection(selectedFile);
        }
    }

    private void handleFileSelection(File file) {
        try {
            // Set the selected file in the server
            MultiFunctionServer.setSelectedFile(file.getAbsolutePath());

            // You can add visual feedback here
            System.out.println("File selected for sharing: " + file.getName());

            // Optional: Add some visual feedback
            send.setStyle("-fx-background-color: rgba(0, 255, 0, 0.1);");
            // Reset the style after a brief moment
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() ->
                            send.setStyle(""));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error appropriately
            send.setStyle("-fx-background-color: rgba(255, 0, 0, 0.1);");
        }
    }

}
