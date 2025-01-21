package lk.ijse.dep13.snapaccess.client.controller;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientVideoCallController {

    private static final AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
    private static final DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
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
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static TargetDataLine targetLine;
    ExecutorService executorService;
    private boolean isAudioMuted = false;
    private boolean isCameraOn = true;
    private static boolean isAudioServerRunning = false;
    final String ipAddress = "127.0.0.1";

    public void initialize() {
        executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> startAudioServer());
        executorService.submit(() -> startVideoServer());

        new Thread(this::startAudioClient).start();
        startVideoClient();
        imgStartAudio.setVisible(false);
        imgStartVideo.setVisible(false);
    }

    private static void startAudioServer() {
        try {
            serverSocket = new ServerSocket(AUDIO_PORT);
            isAudioServerRunning = true;
            System.out.println("Audio Server started on port " + AUDIO_PORT);

            clientSocket = serverSocket.accept();
            System.out.println("Client connected to Audio");

            targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format);
            targetLine.start();

            OutputStream outputStream = clientSocket.getOutputStream();
            byte[] buffer = new byte[1024];
            while (isAudioServerRunning) {
                int bytesRead = targetLine.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void stopAudioServer() {
        if (isAudioServerRunning) {
            try {
                if (targetLine != null && targetLine.isOpen()) {
                    targetLine.stop();
                    targetLine.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    isAudioServerRunning = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startVideoServer() {
        Webcam webcam = Webcam.getDefault();
        webcam.open();
        try (ServerSocket serverSocket = new ServerSocket(VIDEO_PORT)) {
            System.out.println("Video Server started on port " + VIDEO_PORT);
            while (true) {
                System.out.println("Waiting for video client connection...");
                Socket localSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + localSocket.getRemoteSocketAddress());

                new Thread(() -> {
                    try {
                        OutputStream os = localSocket.getOutputStream();
                        BufferedOutputStream bos = new BufferedOutputStream(os);
                        ObjectOutputStream oos = new ObjectOutputStream(bos);

                        while (!localSocket.isClosed()) {
                            try {
                                BufferedImage image = webcam.getImage();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(image, "jpg", baos);
                                oos.writeObject(baos.toByteArray());
                                oos.flush();
                                Thread.sleep(1000 / 27);
                            } catch (IOException e) {
                                break;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void stopVideoServer(){
        Webcam webcam = Webcam.getDefault();
        webcam.close();
    }

    private void startAudioClient() {
        try {
            audioSocket = new Socket(ipAddress, AUDIO_PORT);
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
                videoSocket = new Socket(ipAddress, VIDEO_PORT);
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

    public void imgStopAudioOnMouseClicked(MouseEvent mouseEvent) throws LineUnavailableException {
        if (sourceLine != null && sourceLine.isOpen()) {
            isAudioMuted = true;
            sourceLine.stop();
            System.out.println("Audio Muted");
        }
        imgStopAudio.setVisible(false);
        imgStartAudio.setVisible(true);
    }

    public void imgStartAudioOnMouseClicked(MouseEvent mouseEvent) {
        if (sourceLine != null && sourceLine.isOpen()) {
            isAudioMuted = false;
            sourceLine.start();
            System.out.println("Audio Unmuted");
        }
        imgStartAudio.setVisible(false);
        imgStopAudio.setVisible(true);
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
                    stopVideoServer();
                    System.out.println("Camera Off");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void imgStartVideoOnMouseClicked(MouseEvent mouseEvent) throws InterruptedException {
        startVideoServer();
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

    public void imgDisconnectOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        stopAudioServer();
        stopVideoServer();
        if (Webcam.getDefault().isOpen()) {
            Webcam.getDefault().close();
        }

        if (videoSocket != null && !videoSocket.isClosed()) {
            try {
                videoSocket.close();
                if (videoThread != null) {
                    videoThread.interrupt();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (audioSocket != null && !audioSocket.isClosed()) {
            try {
                audioSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        ((Stage) (root.getScene().getWindow())).setScene(new Scene(FXMLLoader.load(getClass().getResource("/scene/Client.fxml"))));
    }
}
