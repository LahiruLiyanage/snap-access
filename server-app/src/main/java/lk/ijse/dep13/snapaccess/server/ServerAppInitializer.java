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

    public static void main(String[] args) {
        final int PORT = 5050;
        String home = System.getProperty("user.home");
        File downloadDir = new File(home + "/Downloads/");

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port: " + PORT);
        } catch (BindException e) {
            System.err.println("Port " + PORT + " is already in use. Starting on a random port...");
            try {
                serverSocket = new ServerSocket(0);
                System.out.println("Server started on port: " + serverSocket.getLocalPort());
            } catch (IOException ex) {
                System.err.println("Failed to start server.");
                ex.printStackTrace();
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Infinite loop to accept and handle connections
        while (true) {
            try {
                System.out.println("Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from: " + clientSocket.getRemoteSocketAddress());

                // Create threads for screen capture and file upload
                new Thread(() -> handleScreenCapture(clientSocket)).start();
                new Thread(() -> handleFileUpload(clientSocket, downloadDir)).start();

            } catch (IOException e) {
                System.err.println("Error accepting connection.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles screen capture and streaming to the client.
     */
    private static void handleScreenCapture(Socket clientSocket) {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            oos.writeInt(screenSize.width);
            oos.writeInt(screenSize.height);
            oos.flush();

            Robot robot = new Robot();
            while (!clientSocket.isClosed()) {
                BufferedImage screen = robot.createScreenCapture(new Rectangle(screenSize));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screen, "jpeg", baos);

                oos.writeObject(baos.toByteArray());
                oos.flush();
                Thread.sleep(1000 / 27); // Maintain frame rate
            }
        } catch (Exception e) {
            System.err.println("Screen capture error.");
            e.printStackTrace();
        }
    }

    /**
     * Handles file upload from the client.
     */
    private static void handleFileUpload(Socket clientSocket, File downloadDir) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            // Read the username
            String username = br.readLine().strip();
            if (username == null || username.length() < 3 || username.length() > 10) {
                System.err.println("Invalid username: " + username);
                return;
            }

            // Create a timestamped folder for the files
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            File userFolder = new File(downloadDir, username + "-" + now.format(formatter));
            if (!userFolder.mkdirs()) {
                System.err.println("Failed to create user folder: " + userFolder.getAbsolutePath());
                return;
            }

            // Read the file name
            String fileName = br.readLine().strip();
            if (fileName == null || fileName.isEmpty()) {
                System.err.println("Invalid file name.");
                return;
            }

            File uploadedFile = new File(userFolder, fileName);
            try (BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(uploadedFile))) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                System.out.printf("File %s uploaded successfully to %s%n", fileName, userFolder.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("File upload error.");
            e.printStackTrace();
        }
    }
}
