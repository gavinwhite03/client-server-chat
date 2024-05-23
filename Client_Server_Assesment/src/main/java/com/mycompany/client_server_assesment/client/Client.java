package com.mycompany.client_server_assesment.client;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private ClientGUI gui;

    public Client() {
        gui = new ClientGUI(this);
        gui.setVisible(true);
    }

    public void connectToServer(String serverAddress, int serverPort, String username) {
        try {
            this.username = username;
            socket = new Socket(serverAddress, serverPort);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            listenForMessage();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage(String messageToSend) {
        try {
            if (bufferedWriter != null && messageToSend != null && !messageToSend.isEmpty()) {
                if (messageToSend.equals("/exit")) {
                    bufferedWriter.write("/exit");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                } else {
                    String messageWithUsername = username + ": " + messageToSend;
                    bufferedWriter.write(messageWithUsername);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    gui.appendMessage(messageWithUsername);
                }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    gui.appendMessage(msgFromGroupChat);
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Client();
            }
        });
    }
}