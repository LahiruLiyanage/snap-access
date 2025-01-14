package lk.ijse.dep13.snapaccess.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class ClientAppInitializer extends Application {



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
      primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/scene/ClientScene.fxml"))));

        primaryStage.setTitle("Snap Access - Client Window");
        primaryStage.show();
        primaryStage.centerOnScreen();
    }
}