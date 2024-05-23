package com.mycompany.client_server_assesment.server;

import com.mycompany.client_server_assesment.client.ClientHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static final List<String> chatLog = new ArrayList<>();
    private static final List<ClientHandler> clientHandlers = new ArrayList<>();
    public ServerGUI gui;
    private ServerSocket serverSocket;
    private HttpServer httpServer;
    private boolean running;

    public static void main(String[] args) {
        Server server = new Server();
        ServerGUI gui = new ServerGUI(server);
        gui.setVisible(true);
        server.setGUI(gui);
    }
    
    public void setGUI(ServerGUI gui) {
        this.gui = gui;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            gui.appendLog("Server started on port " + port);

            Thread acceptThread = new Thread(() -> {
                while (running) {
                    try {
                        Socket socket = serverSocket.accept();
                        gui.appendLog("A new client has connected!");

                        ClientHandler clientHandler = new ClientHandler(socket, chatLog, this);
                        clientHandlers.add(clientHandler);
                        new Thread(clientHandler).start();
                    } catch (IOException e) {
                        if (!running) {
                            break;
                        }
                        gui.appendLog("Error in server socket: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            acceptThread.start();

            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/", new chatPageHandler());
            httpServer.createContext("/download-chat", new downloadChatHandler());
            httpServer.setExecutor(null);
            httpServer.start();
            gui.appendLog("HTTP server started on port 8080");
        } catch (Exception e) {
            gui.appendLog("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                gui.appendLog("Server socket closed.");
            }
            if (httpServer != null) {
                httpServer.stop(0);
                gui.appendLog("HTTP server stopped.");
            }
            List<ClientHandler> clientHandlersCopy = new ArrayList<>(clientHandlers);
            for (ClientHandler clientHandler : clientHandlersCopy) {
                clientHandler.stopClient();
            }
            gui.appendLog("Server stopped.");
        } catch (IOException e) {
            gui.appendLog("Error stopping the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    private static class chatPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Received request for /");
            String response = renderChatPage();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static class downloadChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String clientId = exchange.getRequestURI().getQuery().split("=")[1];
                String chatLogString = getClientChatLog(clientId);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"chat.txt\"");
                exchange.sendResponseHeaders(200, chatLogString.length());
                OutputStream os = exchange.getResponseBody();
                os.write(chatLogString.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    private static String renderChatPage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h1>Chat Log</h1>");
        html.append("<div id='chatLog'>");
        for (String message : chatLog) {
            html.append("<p>").append(message).append("</p>");
        }
        html.append("</div>");
        html.append("<h2>Connected Clients</h2>");
        html.append("<ul id='connectedClients'>");
        for (ClientHandler clientHandler : clientHandlers) {
            html.append("<li>").append(clientHandler.getUsername()).append("</li>");
        }
        html.append("</ul>");
        html.append("<button onclick=\"downloadChat()\">Download Chat</button>");
        html.append("<script>");
        html.append("function downloadChat() {");
        html.append("  var clientId = prompt('Enter your username:');");
        html.append("  if (clientId) {");
        html.append("    var xhr = new XMLHttpRequest();");
        html.append("    xhr.open('POST', '/download-chat?clientId=' + encodeURIComponent(clientId), true);");
        html.append("    xhr.responseType = 'blob';");
        html.append("    xhr.onload = function() {");
        html.append("      if (xhr.status === 200) {");
        html.append("        var blob = new Blob([xhr.response], { type: 'text/plain' });");
        html.append("        var link = document.createElement('a');");
        html.append("        link.href = window.URL.createObjectURL(blob);");
        html.append("        link.download = 'chat.txt';");
        html.append("        link.click();");
        html.append("      }");
        html.append("    };");
        html.append("    xhr.send();");
        html.append("  }");
        html.append("}");
        html.append("</script>");
        html.append("</body></html>");
        String chatPage = html.toString();
        return chatPage;
    }

    public static void broadcastMessage(String message, ClientHandler excludeClient) {
        chatLog.add(message);
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != excludeClient) {
                clientHandler.sendMessage(message);
            }
        }
    }

    private static String getClientChatLog(String clientId) {
        StringBuilder chatLogBuilder = new StringBuilder();
        for (String message : chatLog) {
            if (!message.startsWith("Server: " + clientId)) {
                chatLogBuilder.append(message).append("\n");
            }
        }
        return chatLogBuilder.toString();
    }
}