package lk.ijse.dep13.snapaccess.client.controller;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
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
    public ImageView imgStopAudio;
    public ImageView imgDisconnect;
    public ImageView imgStopVideo;
    public ImageView imgPerson;
    public ImageView imgStartAudio;
    public ImageView imgStartVideo;
    private Thread videoThread;
    private Socket audioSocket;
    private Socket videoSocket;
    private SourceDataLine sourceLine;
    private boolean isAudioMuted = false;
    private boolean isCameraOn = true;

    public void initialize() {
        startVideoClient();
        new Thread(this::startAudioClient).start();
        imgStartAudio.setVisible(false);
        imgStartVideo.setVisible(false);
    }

    private void startAudioClient() {
        try {
            audioSocket = new Socket("127.0.0.1", AUDIO_PORT);
            InputStream inputStream = audioSocket.getInputStream();
            System.out.println("Connected to Audio");

            sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceLine.open(format);
            sourceLine.start();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1 && !audioSocket.isClosed()) {
                if (!isAudioMuted) {
                    sourceLine.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void startVideoClient() {
        if (!isCameraOn) return;

        imgCamera.fitWidthProperty().bind(root.widthProperty());
        imgCamera.fitHeightProperty().bind(root.heightProperty());

        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                videoSocket = new Socket("127.0.0.1", VIDEO_PORT);
                InputStream is = videoSocket.getInputStream();
                System.out.println("Connected to Video");
                BufferedInputStream bis = new BufferedInputStream(is);
                ObjectInputStream ois = new ObjectInputStream(bis);

                while (true) {
                    if (videoSocket.isClosed() || !isCameraOn) {
                        break;
                    }
                    byte[] image = (byte[]) ois.readObject();
                    updateValue(new Image(new ByteArrayInputStream(image)));
                }
                return null;
            }
        };
        imgCamera.imageProperty().bind(task.valueProperty());
        videoThread = new Thread(task);
        videoThread.start();
    }

    public void imgStopAudioOnMouseClicked(MouseEvent mouseEvent) {
        imgStopAudio.setVisible(false);
        imgStartAudio.setVisible(true);
        if (sourceLine != null && sourceLine.isOpen()) {
            isAudioMuted = true;
            System.out.println("Audio Muted");
        }
    }

    public void imgStartAudioOnMouseClicked(MouseEvent mouseEvent) {
        imgStartAudio.setVisible(false);
        imgStopAudio.setVisible(true);
        if (sourceLine != null && sourceLine.isOpen()) {
            isAudioMuted = false;
            System.out.println("Audio Unmuted");
        }
    }

    public void imgStopVideoOnMouseClicked(MouseEvent mouseEvent) {
        if (isCameraOn) {
            isCameraOn = false;
            imgCamera.setVisible(false);
            imgStopVideo.setVisible(false);
            imgStartVideo.setVisible(true);
            if (videoSocket != null && !videoSocket.isClosed()) {
                try {
                    videoSocket.close();
                    videoThread.interrupt();
                    System.out.println("Camera Off");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void imgStartVideoOnMouseClicked(MouseEvent mouseEvent) throws InterruptedException {
        imgStartVideo.setVisible(false);
        imgStopVideo.setVisible(true);
        if (!isCameraOn) {
            isCameraOn = true;
            imgCamera.setVisible(true);
            videoSocket = null;
            startVideoClient();
            System.out.println("Camera On");
        }
    }

    public void imgDisconnectOnMouseClicked(MouseEvent mouseEvent) {
        if (sourceLine != null && sourceLine.isOpen()) {
            isAudioMuted = true;
        }
        imgCamera.setVisible(false);
        ((Stage)(root.getScene().getWindow())).close();
    }

    public void rootOnMouseDragged(MouseEvent mouseEvent) {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setX(mouseEvent.getScreenX() - mouseX);
        stage.setY(mouseEvent.getScreenY() - mouseY);
    }

    public void imgCameraOnMouseDragged(MouseEvent mouseEvent) {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setX(mouseEvent.getScreenX() - mouseX);
        stage.setY(mouseEvent.getScreenY() - mouseY);
    }

    double mouseX, mouseY;
    public void rootOnMousePressed(MouseEvent mouseEvent) {
        mouseX = mouseEvent.getSceneX();
        mouseY = mouseEvent.getSceneY();
    }

    public void imgCameraOnMousePressed(MouseEvent mouseEvent) {
        mouseX = mouseEvent.getSceneX();
        mouseY = mouseEvent.getSceneY();
    }
}
