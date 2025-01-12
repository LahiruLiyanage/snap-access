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

                    // Read file name
                    String fileName = reader.readLine();
                    if (fileName == null) {
                        System.err.println("Failed to read file name.");
                        continue;
                    }

                    // Save the file
                    File file = new File(SAVE_DIRECTORY, fileName);
                    try (FileOutputStream fos = new FileOutputStream(file);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                        System.out.println("File received: " + fileName);
                    }
                }
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}