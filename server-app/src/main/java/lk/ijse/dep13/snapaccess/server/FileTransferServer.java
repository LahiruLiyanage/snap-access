package lk.ijse.dep13.snapaccess.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer {

    private static final int SERVER_PORT = 5050;
    private static final String SAVE_DIRECTORY = System.getProperty("user.home") + "/Downloads/snap-access";

    public static void main(String[] args) {
        // Create the snap-access directory if it doesn't exist
        File saveDir = new File(SAVE_DIRECTORY);
        if (!saveDir.exists()) {
            if (saveDir.mkdirs()) {
                System.out.println("Created directory: " + SAVE_DIRECTORY);
            } else {
                System.err.println("Failed to create directory: " + SAVE_DIRECTORY);
                return;
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);
            System.out.println("Files will be saved to: " + SAVE_DIRECTORY);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected.");

                try (InputStream is = socket.getInputStream();
                     BufferedInputStream bis = new BufferedInputStream(is);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(bis))) {

                    // Read username (optional, can be used for logging)
                    String username = reader.readLine();
                    if (username != null) {
                        System.out.println("Receiving file from user: " + username);
                    }

                    // Read file name
                    String fileName = reader.readLine();
                    if (fileName == null) {
                        System.err.println("Failed to read file name.");
                        continue;
                    }

                    // Ensure the file name is unique
                    File file = new File(SAVE_DIRECTORY, fileName);
                    file = ensureUniqueFileName(file);

                    // Save the file
                    try (FileOutputStream fos = new FileOutputStream(file);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }

                        System.out.println("File received: " + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    socket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensures the file name is unique by appending a numeric suffix if a file with the same name exists.
     */
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
