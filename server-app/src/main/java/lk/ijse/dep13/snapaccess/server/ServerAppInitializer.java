package lk.ijse.dep13.snapaccess.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAppInitializer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(10050);
        System.out.println("Server started on port 10050");

        while (true) {
            System.out.println("Waiting for connection...");
            Socket localSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + localSocket.getRemoteSocketAddress());
            new Thread(() -> {
                try {
                    OutputStream os = localSocket.getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);

                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    System.out.println(screenSize.height);
                    System.out.println(screenSize.width);
                    int screenWidth = screenSize.width;
                    int screenHeight = screenSize.height;

                    oos.writeInt(screenWidth);
                    oos.writeInt(screenHeight);
                    oos.flush();

                    while (true) {
                        Robot robot = new Robot();
                        BufferedImage screen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(screen, "jpeg", baos);
                        oos.writeObject(baos.toByteArray());
                        oos.flush();
                        Thread.sleep(1000 / 27);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    InputStream is = localSocket.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ObjectInputStream ois = new ObjectInputStream(bis);

                    Point coordinates = (Point) ois.readObject();
                    Robot robot = new Robot();
                    robot.mouseMove(coordinates.x, coordinates.y);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }
}
