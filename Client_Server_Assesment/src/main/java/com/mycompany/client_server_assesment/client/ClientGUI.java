package com.mycompany.client_server_assesment.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JButton connectButton;
    private Client client;

    public ClientGUI(Client client) {
        this.client = client;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat Client");
        setPreferredSize(new Dimension(600, 400));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                client.sendMessage(message);
                messageField.setText("");
            }
        });

        serverPortField = new JTextField(10);
        serverPortField.setPreferredSize(new Dimension(100, 25));
        usernameField = new JTextField(15);
        usernameField.setPreferredSize(new Dimension(150, 25));
        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverAddress = "localhost";
                int serverPort = Integer.parseInt(serverPortField.getText());
                String username = usernameField.getText();
                client.connectToServer(serverAddress, serverPort, username);
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel connectPanel = new JPanel(new FlowLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        connectPanel.add(new JLabel("Server Port:"), gbc);
        gbc.gridx = 1;
        connectPanel.add(serverPortField, gbc);
        gbc.gridx = 2;
        connectPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 3;
        connectPanel.add(usernameField, gbc);
        gbc.gridx = 4;
        connectPanel.add(connectButton, gbc);

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(inputPanel, BorderLayout.SOUTH);
        getContentPane().add(connectPanel, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }
}