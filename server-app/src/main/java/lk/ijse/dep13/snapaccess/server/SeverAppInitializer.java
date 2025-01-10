package lk.ijse.dep13.snapaccess.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SeverAppInitializer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5050);
        System.out.println("Server started on port 5050");
        while (true) {
            System.out.println("Waiting for connection..."  );
            Socket localsocket = serverSocket.accept();
            new Thread(() -> {
                try{
                    System.out.println("Accepted connection from ");
                    InputStream is = localsocket.getInputStream();
                    while (true){
                        byte[] buffer = new byte[1024];
                        int len = is.read(buffer);
                        System.out.println(new String(buffer,0,len));
                    }

                } catch (Exception e) {

                }
            }).start();






        }
    }
}
