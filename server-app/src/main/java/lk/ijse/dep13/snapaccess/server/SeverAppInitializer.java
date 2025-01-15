package lk.ijse.dep13.snapaccess.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SeverAppInitializer extends Application {

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML and set up the scene
        primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/scene/ServerScene.fxml"))));
        primaryStage.setTitle("Snap Access - Server Window");
        primaryStage.show();
        primaryStage.centerOnScreen();


    }
}