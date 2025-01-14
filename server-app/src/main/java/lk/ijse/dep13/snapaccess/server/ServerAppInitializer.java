package lk.ijse.dep13.snapaccess.server;

import dto.Request;
import util.EventType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAppInitializer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(10050);
        System.out.println("Server started on port 10050");

        int leftButton = InputEvent.BUTTON1_DOWN_MASK;
        int rightButton = InputEvent.BUTTON2_DOWN_MASK;

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
                    Robot robot = new Robot();
                    while (true) {
                        Request request = (Request) ois.readObject();
                        if(request.getEvent() == EventType.MOVE){
                            Point coordinates = (Point) request.getPayload();
                            //robot.mouseMove(coordinates.x, coordinates.y);
                        }else if (request.getEvent() == EventType.LEFT_CLICK){
                            Point coordinates = (Point) request.getPayload();
                            robot.mouseMove(coordinates.x, coordinates.y);
                            robot.mousePress(leftButton);
                            Thread.sleep(50);
                            robot.mouseRelease(leftButton);
                            System.out.println(request.getPayload());
                        }else if (request.getEvent() == EventType.RIGHT_CLICK){
                            robot.mousePress(rightButton);
                            Thread.sleep(50);
                            robot.mouseRelease(rightButton);
                            System.out.println(request.getPayload());
                        } else if (request.getEvent() == EventType.KEY_PRESS) {
                            //robot.keyPress(request.getPayload());
                            System.out.println("Key pressed");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }
}
