package lk.ijse.dep13.snapaccess.client.controller;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MainSceneController {

    public ImageView imgCamera;
    public AnchorPane root;

    public void initialize() {
        imgCamera.fitWidthProperty().bind(root.widthProperty());
        imgCamera.fitHeightProperty().bind(root.heightProperty());

        Task<Image> task = new Task<>() {
            protected Image call() throws Exception {
                Socket socket = new Socket("127.0.0.1", 8080);
                InputStream is = socket.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ObjectInputStream ois = new ObjectInputStream(bis);

                while (true){
                    byte[] image = (byte[]) ois.readObject();
                    updateValue(new Image(new ByteArrayInputStream(image)));
                }
            }
        };
        imgCamera.imageProperty().bind(task.valueProperty());
        new Thread(task).start();
        task.setOnFailed(e -> System.out.println(e.getSource()));
    }
}
