package lk.ijse.dep13.snapaccess.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerAppInitializer {

    public static void main(String[] args) throws IOException {
        final int PORT = 5050;

        String home = System.getProperty("user.home");
        File downDir = new File(home+"/Downloads/");

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port :" + PORT);
        } catch (BindException e) {
            System.out.println("6060 port is already in use");
            serverSocket = new ServerSocket(0);
            System.out.println("Re-try: server started on port " + serverSocket.getLocalPort());
        }

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

            new Thread(() -> {
                try {
                    InputStream is = localSocket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String username = br.readLine().strip();
                    if (!(username.length() >= 3 && username.length() <= 10)){
                        System.out.println(username);
                        System.out.println("Invalid username");
                        return;
                    }

                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    String date = now.format(formatter);

                    File downFolder = new File(downDir, date);
                    downFolder.mkdirs();

                    String fileName = br.readLine().strip();
                    if (fileName.isEmpty()){
                        System.out.println("Invalid filename");
                        return;
                    }

                    File file = new File(downFolder, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    BufferedInputStream bis = new BufferedInputStream(is);

                    while (true){
                        byte[] buffer = new byte[1024];
                        int read = bis.read(buffer);
                        if (read == -1){
                            break;
                        }
                        bos.write(buffer, 0, read);
                    }
                    System.out.printf("File: %s uploaded successfully%n", fileName);
                    bos.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
