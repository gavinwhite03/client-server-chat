package com.mycompany.client_server_assesment.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame {
    private JTextArea serverLogArea;
    private JButton startButton;
    private JButton stopButton;
    private Server server;

    public ServerGUI(Server server) {
        this.server = server;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat Server");
        setPreferredSize(new Dimension(400, 400));

        serverLogArea = new JTextArea();
        serverLogArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(serverLogArea);

        startButton = new JButton("Start Server");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Prompt the user to enter the port number
                String portString = JOptionPane.showInputDialog(ServerGUI.this, "Enter the server port (default: 5555):", "Server Port", JOptionPane.QUESTION_MESSAGE);
                int port = 5555; // Default port
                if (portString != null && !portString.isEmpty()) {
                    try {
                        port = Integer.parseInt(portString);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(ServerGUI.this, "Invalid port number. Using default port 5555.", "Invalid Port", JOptionPane.WARNING_MESSAGE);
                    }
                }
                server.startServer(port);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.stopServer();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public void appendLog(String log) {
        serverLogArea.append(log + "\n");
    }
}