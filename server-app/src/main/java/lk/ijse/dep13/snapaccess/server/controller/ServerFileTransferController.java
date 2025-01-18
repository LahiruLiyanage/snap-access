package lk.ijse.dep13.snapaccess.server.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
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

public class ServerFileTransferController {
    public ImageView imgBackground;
    public AnchorPane root;
    public StackPane send;
    private File lastSelectedFile = null;

    public void initialize() {
        imgBackground.fitWidthProperty().bind(root.widthProperty());
        imgBackground.fitHeightProperty().bind(root.heightProperty());
        imgBackground.setPreserveRatio(false);
    }

    public void sendOnDragDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        if (!files.isEmpty()) {
            File file = files.get(0);
            handleFileSelection(file);
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

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads/snap-access"));

        // Add file filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (selectedFile != null) {
            handleFileSelection(selectedFile);
        }
    }

    private void handleFileSelection(File file) {
        try {
            if (!file.exists() || !file.isFile()) {
                showError("Invalid file selected");
                return;
            }

            lastSelectedFile = file;

            MultiFunctionServer.setSelectedFile(file.getAbsolutePath());

            send.setStyle("-fx-background-color: rgba(0, 255, 0, 0.1);");

            showSuccess("File ready to share: " + file.getName());

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> send.setStyle(""));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error preparing file: " + e.getMessage());
            send.setStyle("-fx-background-color: rgba(255, 0, 0, 0.1);");
        }
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