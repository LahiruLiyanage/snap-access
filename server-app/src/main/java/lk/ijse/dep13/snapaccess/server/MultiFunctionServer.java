package lk.ijse.dep13.snapaccess.server;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

public class MultiFunctionServer {

    private static final int SERVER_PORT = 5050;
    private static final String SAVE_DIRECTORY = System.getProperty("user.home") + "/Downloads/snap-access";

    public static void main(String[] args) {
        // Creating snap-access directory
        File saveDir = new File(SAVE_DIRECTORY);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            System.err.println("Failed to create directory: " + SAVE_DIRECTORY);
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        ObjectOutputStream oos = null;
        try {
            InputStream is = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            OutputStream os = socket.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);

            // Read the client request type first
            String requestType = reader.readLine();
            System.out.println("Received request type: " + requestType);

            // Create ObjectOutputStream after reading request type
            oos = new ObjectOutputStream(bos);
            oos.flush();

            if ("FILE_TRANSFER".equalsIgnoreCase(requestType)) {
                handleFileTransfer(socket, reader);
            } else if ("SCREEN_SHARE".equalsIgnoreCase(requestType)) {
                handleScreenShare(oos);
            } else {
                System.err.println("Unknown request type: " + requestType);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) oos.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private static void handleFileTransfer(Socket socket, BufferedReader reader) {
        try {
            System.out.println("Handling file transfer...");

            // Read username
            String username = reader.readLine();
            if (username != null) {
                System.out.println("Receiving file from user: " + username);
            }

            // Read file name
            String fileName = reader.readLine();
            if (fileName == null) {
                System.err.println("Failed to read file name.");
                return;
            }

            File file = new File(SAVE_DIRECTORY, fileName);
            file = ensureUniqueFileName(file);

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                System.out.println("File received: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error during file transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleScreenShare(ObjectOutputStream oos) {
        try {
            System.out.println("Handling screen sharing...");
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            // Send screen dimensions to the client
            oos.writeInt((int) screenSize.getWidth());
            oos.writeInt((int) screenSize.getHeight());
            oos.flush();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(screenSize));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(screenCapture, "png", baos);
                    byte[] imageBytes = baos.toByteArray();

                    oos.writeObject(imageBytes);
                    oos.flush();
                    oos.reset(); // Prevent memory leak

                    Thread.sleep(100); // Adjust screen capture rate as needed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error capturing screen: " + e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during screen sharing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static File ensureUniqueFileName(File file) {
        String name = file.getName();
        String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";

        int counter = 1;
        while (file.exists()) {
            file = new File(file.getParent(), baseName + "_" + counter + extension);
            counter++;
        }
        return file;
    }
}