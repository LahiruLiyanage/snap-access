package lk.ijse.dep13.snapaccess.server;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import com.github.sarxos.webcam.Webcam;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerAppInitializer {

    private static final AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
    private static final DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
    private static final int AUDIO_PORT = 8080;
    private static final int VIDEO_PORT = 8081;

    public static void main(String[] args) throws IOException, LineUnavailableException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> startAudioServer());
        executorService.submit(() -> startVideoServer());
    }

    private static void startAudioServer() {
        try (ServerSocket serverSocket = new ServerSocket(AUDIO_PORT)) {
            System.out.println("Audio Server started on port " + AUDIO_PORT);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected to Audio");

            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format);
            targetLine.start();

            OutputStream outputStream = clientSocket.getOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int bytesRead = targetLine.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
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

                        while (true) {
                            BufferedImage image = webcam.getImage();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "jpg", baos);
                            oos.writeObject(baos.toByteArray());
                            oos.flush();
                            Thread.sleep(1000 / 27);
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
}
