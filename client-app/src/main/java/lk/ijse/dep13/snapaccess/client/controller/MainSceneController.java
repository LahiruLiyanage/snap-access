package lk.ijse.dep13.snapaccess.client.controller;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import javax.sound.sampled.*;

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

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(8080);
            byte[] buffer = new byte[1024];

            SourceDataLine speakerLine = setupSpeaker();    // speaker
            speakerLine.open();
            speakerLine.start();

            System.out.println("Server is ready to receive and play audio...");

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                byte[] audioData = packet.getData();
                System.out.println(new String(audioData));
                int bytesRead = packet.getLength();
                if (bytesRead > 0) {
                    speakerLine.write(audioData, 0, bytesRead);
                    System.out.println("Received and played " + bytesRead + " bytes of audio data.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SourceDataLine setupSpeaker() throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new Exception("Speaker line not supported!");
        }

        SourceDataLine speakerLine = (SourceDataLine) AudioSystem.getLine(info);
        return speakerLine;
    }
}
