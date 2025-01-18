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
    private static volatile String selectedFilePath = null; // For tracking selected file to share

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
            PrintWriter writer = new PrintWriter(bos, true);

            String requestType = reader.readLine();
            System.out.println("Received request type: " + requestType);

            oos = new ObjectOutputStream(bos);
            oos.flush();

            switch (requestType.toUpperCase()) {
                case "FILE_TRANSFER":
                    handleFileTransfer(socket, reader, writer);
                    break;
                case "FILE_REQUEST":
                    handleFileRequest(socket, oos, writer);
                    break;
                case "SCREEN_SHARE":
                    handleScreenShare(oos);
                    break;
                default:
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

    private static void handleFileTransfer(Socket socket, BufferedReader reader, PrintWriter writer) {
        try {
            System.out.println("Handling file transfer...");

            String username = reader.readLine();
            if (username != null) {
                System.out.println("Receiving file from user: " + username);
            }

            String fileName = reader.readLine();
            if (fileName == null) {
                System.err.println("Failed to read file name.");
                return;
            }

            File file = new File(SAVE_DIRECTORY, fileName);
            file = ensureUniqueFileName(file);

            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                System.out.println("File received: " + file.getAbsolutePath());
                writer.println("SUCCESS");
            }
        } catch (IOException e) {
            System.err.println("Error during file transfer: " + e.getMessage());
            writer.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleFileRequest(Socket socket, ObjectOutputStream oos, PrintWriter writer) {
        try {
            if (selectedFilePath == null) {
                writer.println("NO_FILE_SELECTED");
                return;
            }

            File fileToSend = new File(selectedFilePath);
            if (!fileToSend.exists()) {
                writer.println("FILE_NOT_FOUND");
                return;
            }

            writer.println("FILE_READY");
            writer.println(fileToSend.getName());
            writer.println(fileToSend.length());

            try (FileInputStream fis = new FileInputStream(fileToSend);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    oos.write(buffer, 0, bytesRead);
                }
                oos.flush();
            }
            System.out.println("File sent: " + fileToSend.getName());
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
            writer.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleScreenShare(ObjectOutputStream oos) {
        // Existing screen share implementation remains the same
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