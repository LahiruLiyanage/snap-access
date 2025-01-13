package lk.ijse.dep13.snapaccess.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAppInitializer {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(10050);
        System.out.println("Server started on port 10050");

        while (true) {
            System.out.println("Waiting for connection...");
            Socket localSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + localSocket.getRemoteSocketAddress());
            new Thread(() -> {
                try {
                    changeDesktopColor("black");
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
                } finally {
                    try {
                        revertDesktop();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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

    public static void changeDesktopColor(String color) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            String[] command1 = {"gsettings", "set", "org.gnome.desktop.background", "picture-options", "none"};
            Runtime.getRuntime().exec(command1).waitFor();
            String[] command2 = {"gsettings", "set", "org.gnome.desktop.background", "primary-color", "%s".formatted(color)};
            Runtime.getRuntime().exec(command2).waitFor();
        } else if (os.contains("mac")) {
            String script = "tell application \"Finder\" to set desktop picture to POSIX file \"/System/Library/Desktop Pictures/Solid Colors/Solid Black.png\"";
            String[] command = {"osascript", "-e", script};
            Runtime.getRuntime().exec(command).waitFor();
        } else {
            throw new UnsupportedOperationException("Unsupported OS");
        }
    }

    public static void revertDesktop() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            String[] setColor = {"gsettings", "set", "org.gnome.desktop.background", "picture-options", "zoom"};
            Runtime.getRuntime().exec(setColor).waitFor();
        } else if (os.contains("mac")) {
            String script = "tell application \"Finder\" to set desktop picture to POSIX file \"~/Pictures/DefaultWallpaper.jpg\"";
            String[] command = {"osascript", "-e", script};
            Runtime.getRuntime().exec(command).waitFor();
        } else {
            throw new UnsupportedOperationException("Unsupported OS");
        }
    }
}
