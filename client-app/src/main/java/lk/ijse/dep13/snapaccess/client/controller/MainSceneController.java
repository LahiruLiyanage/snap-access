package lk.ijse.dep13.snapaccess.client.controller;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainSceneController {

    private static final AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
    private static final DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
    private static final int AUDIO_PORT = 8080;
    private static final int VIDEO_PORT = 8081;

    public ImageView imgCamera;
    public AnchorPane root;

    public void initialize() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> startAudioClient());
        executorService.submit(() -> startVideoClient());
    }

    private void startAudioClient() {
        try (Socket socket = new Socket("127.0.0.1", AUDIO_PORT);
             InputStream inputStream = socket.getInputStream()) {
            System.out.println("Connected to Audio server");

            SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceLine.open(format);
            sourceLine.start();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                sourceLine.write(buffer, 0, bytesRead);
            }

        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void startVideoClient() {
        imgCamera.fitWidthProperty().bind(root.widthProperty());
        imgCamera.fitHeightProperty().bind(root.heightProperty());

        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                Socket socket = new Socket("127.0.0.1", VIDEO_PORT);
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
