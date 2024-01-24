package TP1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class Server {
    private static int dataPort = -1;
    private static ServerSocket pasvSocket = null;
    private static Socket dataSocket = null;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(2121);
            System.out.println("Connected to localhost");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     InputStream in = clientSocket.getInputStream();
                     OutputStream out = clientSocket.getOutputStream();
                     Scanner scanner = new Scanner(in)) {

                    System.out.println("Client connected.");

                    String welcomeMessage = "220 Service ready\r\n";
                    out.write(welcomeMessage.getBytes());
                    System.out.println("Sent: " + welcomeMessage);

                    String userName = scanner.nextLine().trim();
                    System.out.println("Received: " + userName);

                    if ("USER Bouchra".equals(userName)) {
                        out.write("331 User name ok\r\n".getBytes());
                        System.out.println("Sent: 331 User name ok");

                        String password = scanner.nextLine().trim();
                        System.out.println("Received: " + password);

                        if ("PASS Bpass".equals(password)) {
                            out.write("230 User logged in\r\n".getBytes());
                            System.out.println("Sent: 230 User logged in");
                        } else {
                            out.write("530 Not logged in\r\n".getBytes());
                            System.out.println("Sent: 530 Not logged in");
                            break;
                        }
                    } else {
                        out.write("530 Not logged in\r\n".getBytes());
                        System.out.println("Sent: 530 Not logged in");
                        break;
                    }

                   
                    while (true) {
                        String commandLine = scanner.nextLine().trim();
                        System.out.println("Received: " + commandLine);
                        String[] commandParts = commandLine.split(" ");
                        String command = commandParts[0];

                        switch (command.toUpperCase()) {
                            case "QUIT":
                                out.write("221 Goodbye\r\n".getBytes());
                                System.out.println("Sent: 221 Goodbye");
                                break;
                            case "EPRT":
                                handleEprtCommand(commandParts, out);
                                break;
                            case "RETR":
                                if (dataPort != -1) {
                                    handleRetrCommand(commandParts, clientSocket, out);
                                } else {
                                    out.write("503 Bad sequence of commands\r\n".getBytes());
                                    System.out.println("Sent: 503 Bad sequence of commands");
                                }
                                break;
                            case "SYST":
                                out.write("215 UNIX Type: L8\r\n".getBytes());
                                System.out.println("Sent: 215 UNIX Type: L8");
                                break;
                            case "FEAT":
                                out.write("211 End\r\n".getBytes());
                                System.out.println("Sent: 211 End");
                                break;
                            case "SIZE":
                                handleSizeCommand(commandParts, out);
                                break;
                            case "EPSV":
                                handlePasvCommand(clientSocket, out);
                                break;
                            case "PASV":
                                handlePasvCommand(clientSocket, out);
                                break;
                            case "PORT":
                                handlePortCommand(commandParts, out);
                                break;
                            case "TYPE":
                                if (commandParts.length > 1 && "I".equalsIgnoreCase(commandParts[1])) {
                                    out.write("200 Type set to I\r\n".getBytes());
                                    System.out.println("Sent: 200 Type set to I");
                                } else {
                                    out.write("501 Syntax error in parameters or arguments\r\n".getBytes());
                                    System.out.println("Sent: 501 Syntax error in parameters or arguments");
                                }
                                break;
                            default:
                                out.write("500 Syntax error, command unrecognized\r\n".getBytes());
                                System.out.println("Sent: 500 Syntax error, command unrecognized");
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleEprtCommand(String[] commandParts, OutputStream out) throws IOException {
        if (commandParts.length > 1) {
            dataPort = Integer.parseInt(commandParts[1]);
            out.write("200 Port command successful\r\n".getBytes());
            System.out.println("Sent: 200 Port command successful");
        } else {
            out.write("501 Syntax error in parameters or arguments\r\n".getBytes());
            System.out.println("Sent: 501 Syntax error in parameters or arguments");
        }
    }

    private static void handleRetrCommand(String[] commandParts, Socket clientSocket, OutputStream out) throws IOException {
        if (commandParts.length > 1) {
            String fileName = commandParts[1];
            System.out.println("Handling RETR command for file: " + fileName);

            File file = new File("/home/m1ipint/bouchra.hayani.etu/eclipse-workspace/TP1/src/" + fileName); 

            if (!file.exists()) {
                out.write("550 File not found\r\n".getBytes());
                System.out.println("Sent: 550 File not found");
                return;
            }

            try (Socket dataSocket = new Socket(clientSocket.getInetAddress(), dataPort);
                 FileInputStream fis = new FileInputStream(file)) {

                OutputStream dataOut = dataSocket.getOutputStream();
                byte[] buffer = new byte[4096];
                int count;

                while ((count = fis.read(buffer)) > 0) {
                    dataOut.write(buffer, 0, count);
                }

                out.write("226 Transfer complete\r\n".getBytes());
                System.out.println("Sent: 226 Transfer complete");
            }
        } else {
            out.write("501 Syntax error in parameters or arguments\r\n".getBytes());
            System.out.println("Sent: 501 Syntax error in parameters or arguments");
        }
    }

    private static void handleSizeCommand(String[] commandParts, OutputStream out) throws IOException {
        if (commandParts.length > 1) {
            String fileName = commandParts[1];
            File file = new File("/home/m1ipint/bouchra.hayani.etu/eclipse-workspace/TP1/src/" + fileName); 

            if (file.exists()) {
                out.write(("213 " + file.length() + "\r\n").getBytes());
                System.out.println("Sent: 213 " + file.length());
            } else {
                out.write("550 File not found\r\n".getBytes());
                System.out.println("Sent: 550 File not found");
            }
        } else {
            out.write("501 Syntax error in parameters or arguments\r\n".getBytes());
            System.out.println("Sent: 501 Syntax error in parameters or arguments");
        }
    }

    private static void handlePasvCommand(Socket clientSocket, OutputStream out) throws IOException {
        if (pasvSocket != null && !pasvSocket.isClosed()) {
            pasvSocket.close();
        }

        pasvSocket = new ServerSocket(0); 
        int port = pasvSocket.getLocalPort();

        String ipAddress = clientSocket.getLocalAddress().getHostAddress().replace('.', ',');
        int p1 = port / 256;
        int p2 = port % 256;

        String response = "229 Entering Extended Passive Mode (|||" + p1 + "|" + p2 + "|)\r\n";
        out.write(response.getBytes());
        System.out.println("Sent: " + response);

       
        dataSocket = pasvSocket.accept();
    }

    private static void handlePortCommand(String[] commandParts, OutputStream out) throws IOException {
        if (commandParts.length > 1) {
            String[] parts = commandParts[1].split(",");
            if (parts.length == 6) {
                String clientIP = String.join(".", parts[0], parts[1], parts[2], parts[3]);
                int clientPort = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);

               
                if (dataSocket != null && !dataSocket.isClosed()) {
                    dataSocket.close();
                }

               
                dataSocket = new Socket(clientIP, clientPort);

                String response = "200 PORT command successful\r\n";
                out.write(response.getBytes());
                System.out.println("Sent: " + response);
            } else {
                out.write("501 Syntax error in parameters or arguments\r\n".getBytes());
                System.out.println("Sent: 501 Syntax error in parameters or arguments");
            }
        } else {
            out.write("501 Syntax error in parameters or arguments\r\n".getBytes());
            System.out.println("Sent: 501 Syntax error in parameters or arguments");
        }
    }
}