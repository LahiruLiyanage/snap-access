package lk.ijse.dep13.snapaccess.client.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ScreenShareSceneController {
    public AnchorPane root;
    public ImageView imgScreen;
    Socket socket;

    public void initialize() throws Exception {

        socket = new Socket("192.168.8.144", 10050);
        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        InputStream is = socket.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bis);

        int screenWidth = ois.readInt();
        int screenHeight = ois.readInt();

        Platform.runLater(() -> {
            Stage stage = (Stage)root.getScene().getWindow();
            stage.setWidth(screenWidth);
            stage.setHeight(screenHeight);
            //stage.setFullScreen(true);
            System.out.println(stage.getWidth());
            System.out.println(stage.getHeight());
            imgScreen.fitWidthProperty().bind(stage.widthProperty());
            imgScreen.fitHeightProperty().bind(stage.heightProperty());
//            System.out.println(imgScreen.getFitHeight());
//            System.out.println(imgScreen.getFitWidth());
            imgScreen.setPreserveRatio(true);
        });

        imgScreen.setOnMouseMoved(mouseEvent -> {
            try{
                oos.writeObject(new Point((int) mouseEvent.getX(), (int) mouseEvent.getY()));
                oos.flush();
            }catch (IOException e){
                e.printStackTrace();
            }

        });

        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                while (true) {
                    byte[] image = (byte[]) ois.readObject();
                    ByteArrayInputStream bais = new ByteArrayInputStream(image);
                    Image screen = new Image(bais);
                    updateValue(screen);
                }
            }
        };

        imgScreen.imageProperty().bind(task.valueProperty());
        new Thread(task).start();
    }
}
