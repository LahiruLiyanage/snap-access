package lk.ijse.dep13.snapaccess.server;

import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class ServerAppInitializer {

    public static void main(String[] args) throws IOException {

        Webcam webcam = Webcam.getDefault();
        webcam.open();
        InetAddress serverIP = InetAddress.getByName("127.0.0.1");
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server started on port " + serverSocket.getLocalPort());
        while (true) {
            System.out.println("Waiting for client connection...");
            Socket localSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + localSocket.getRemoteSocketAddress());

            // Video
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

            // Audio
            try {
                DatagramSocket socket = new DatagramSocket();
                
                TargetDataLine microphone = setupMicrophone();   // Set microphone
                byte[] buffer = new byte[1024];
                System.out.println("Capturing audio and sending to server...");

                while (true) {
                    int captureAudio = microphone.read(buffer, 0, buffer.length);

                    if (captureAudio > 0) {
                        DatagramPacket packet = new DatagramPacket(buffer, captureAudio, serverIP, serverSocket.getLocalPort());     // send to server
                        socket.send(packet);
                        System.out.println("Sent " + captureAudio + " bytes of audio data.");
                    }
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static TargetDataLine setupMicrophone() throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);  // 16 kHz, 16-bit mono PCM
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new Exception("Microphone line not supported!");
        }

        TargetDataLine micLine = (TargetDataLine) AudioSystem.getLine(info);
        micLine.open(format);
        micLine.start();

        return micLine;
    }
}
