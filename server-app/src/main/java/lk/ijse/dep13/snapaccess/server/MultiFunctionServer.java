package lk.ijse.dep13.snapaccess.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;

public class MultiFunctionServer extends Application {

    private static final int SERVER_PORT = 5050;
    private static final String SAVE_DIRECTORY = System.getProperty("user.home") + "/Downloads/snap-access";
    private static volatile String selectedFilePath = null;

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/scene/Server.fxml"))));
        primaryStage.setTitle("Snap Access");
        primaryStage.show();
        primaryStage.centerOnScreen();

        new Thread(this::startServer).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setSelectedFile(String filePath) {
        selectedFilePath = filePath;
        System.out.println("Selected file path set to: " + filePath);
    }

    private void startServer() {
        File saveDir = new File(SAVE_DIRECTORY);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            System.err.println("Failed to create directory: " + SAVE_DIRECTORY);
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String requestType = dis.readUTF();
            System.out.println("Received request type: " + requestType);

            switch (requestType.toUpperCase()) {
                case "FILE_TRANSFER":
                    handleFileTransfer(socket, dis, dos);
                    break;
                case "FILE_REQUEST":
                    handleFileRequest(socket, dis, dos);
                    break;
                case "SCREEN_SHARE":
                    handleScreenShare(socket, dos);
                    break;
                default:
                    System.err.println("Unknown request type: " + requestType);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private static void handleFileTransfer(Socket socket, DataInputStream dis, DataOutputStream dos) {
        try {
            System.out.println("Handling file transfer...");

            String username = dis.readUTF();
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes) from user: " + username);

            File file = new File(SAVE_DIRECTORY, fileName);
            file = ensureUniqueFileName(file);

            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                int bytesRead;

                while (totalBytesRead < fileSize &&
                        (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                bos.flush();

                System.out.println("File received: " + file.getAbsolutePath() + " (" + totalBytesRead + " bytes)");
                dos.writeUTF("SUCCESS");
                dos.flush();
            }
        } catch (IOException e) {
            System.err.println("Error during file transfer: " + e.getMessage());
            try {
                dos.writeUTF("ERROR: " + e.getMessage());
                dos.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private static void handleFileRequest(Socket socket, DataInputStream dis, DataOutputStream dos) {
        try {
            if (selectedFilePath == null) {
                dos.writeUTF("NO_FILE_SELECTED");
                dos.flush();
                return;
            }

            File fileToSend = new File(selectedFilePath);
            if (!fileToSend.exists()) {
                dos.writeUTF("FILE_NOT_FOUND");
                dos.flush();
                return;
            }

            dos.writeUTF("FILE_READY");
            dos.writeUTF(fileToSend.getName());
            dos.writeLong(fileToSend.length());
            dos.flush();

            try (FileInputStream fis = new FileInputStream(fileToSend);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                dos.flush();
            }
            System.out.println("File sent: " + fileToSend.getName());
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
            try {
                dos.writeUTF("ERROR: " + e.getMessage());
                dos.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private static void handleScreenShare(Socket socket, DataOutputStream dos) {
        try {
            System.out.println("Handling screen sharing...");
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            dos.writeInt((int) screenSize.getWidth());
            dos.writeInt((int) screenSize.getHeight());
            dos.flush();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(screenSize));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(screenCapture, "png", baos);
                    byte[] imageBytes = baos.toByteArray();

                    dos.writeInt(imageBytes.length);
                    dos.write(imageBytes);
                    dos.flush();

                    Thread.sleep(100);
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

    public static File ensureUniqueFileName(File file) {
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