package TP1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class Server {
    public static void main(String[] args) {

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(2020);
            System.out.println("Connected to localhost");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                OutputStream out = clientSocket.getOutputStream();
                String welcomeMessage = "220 Service ready\r\n";
                out.write(welcomeMessage.getBytes());

                InputStream in = clientSocket.getInputStream();
                Scanner scanner = new Scanner(in);

                String userName = scanner.nextLine().trim();
                if ("USER Bouchra".equals(userName)) {
                    out.write("331 User name ok\r\n".getBytes());

                    String password = scanner.nextLine().trim();

                    if ("PASS Bpass".equals(password)) {
                        out.write("230 User logged in\r\n".getBytes());
                    } else {
                        out.write("530 Not logged in\r\n".getBytes());
                        break;
                    }
                } else {
                    out.write("530 Not logged in\r\n".getBytes());
                    break;
                }

                
                while (true) {
                    String command = scanner.nextLine().trim();

                    if (command.equalsIgnoreCase("quit")) {
                        out.write("221 Goodbye\r\n".getBytes());
                        break; 
                    } else {
                     
                        out.write("500 Syntax error, command unrecognized\r\n".getBytes());
                    }
                }

                scanner.close();
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
