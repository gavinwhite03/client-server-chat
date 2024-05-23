package com.mycompany.client_server_assesment.client;

import com.mycompany.client_server_assesment.server.Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<String> chatLog;
    private String username;
    private Server server;

    public ClientHandler(Socket socket, List<String> chatLog, Server server) {
        this.socket = socket;
        this.chatLog = chatLog;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            username = in.readLine();
            for (String message : chatLog) {
                out.println(message);
            }

            Server.broadcastMessage("Server: " + username + " has connected!", this);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("/exit")) {
                    break;
                }
                server.gui.appendLog("Received message from client: " + inputLine);
                server.broadcastMessage(inputLine, this);
            }

            server.broadcastMessage("Server: " + username + " has left the chat!", this);
        } catch (IOException e) {
            if (!server.isRunning()) {
                // Server has stopped, close the connection gracefully
                closeConnection();
                return;
            }
            server.gui.appendLog("Error in client handler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public static void saveChat(String fileName, List<String> chatLog) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String message : chatLog) {
                writer.write(message);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public void stopClient() {
        if (out != null) {
            out.println("Server has stopped. Closing the connection.");
            out.flush();
        }
        closeConnection();
    }

    private void closeConnection() {
        try {
            server.removeClientHandler(this);
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            server.gui.appendLog("Error closing client connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}