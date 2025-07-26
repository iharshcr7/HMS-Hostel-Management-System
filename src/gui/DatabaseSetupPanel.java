package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import database.DatabaseManager;
import java.sql.SQLException;

public class DatabaseSetupPanel extends JPanel {
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField databaseField;
    private JButton createButton;
    private JButton connectButton;
    private JButton importButton;
    private JTextArea statusArea;
    private boolean setupComplete = false;

    public DatabaseSetupPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Host
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        hostField = new JTextField("localhost", 20);
        inputPanel.add(hostField, gbc);

        // Port
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("3306", 20);
        inputPanel.add(portField, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        inputPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        inputPanel.add(passwordField, gbc);

        // Database
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Database:"), gbc);
        gbc.gridx = 1;
        databaseField = new JTextField("hostel_management", 20);
        inputPanel.add(databaseField, gbc);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        createButton = new JButton("Create Database");
        connectButton = new JButton("Connect");
        importButton = new JButton("Import Sample Data");
        
        buttonPanel.add(createButton);
        buttonPanel.add(connectButton);
        buttonPanel.add(importButton);

        // Create status area
        statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusArea);

        // Add components
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Add action listeners
        createButton.addActionListener(e -> createDatabase());
        connectButton.addActionListener(e -> connectDatabase());
        importButton.addActionListener(e -> importSampleData());
    }

    private void createDatabase() {
        try {
            String host = hostField.getText();
            String port = portField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String database = databaseField.getText();

            DatabaseManager.initializeDatabase(host, port, username, password, database);
            statusArea.append("✓ Database created successfully!\n");
            setupComplete = true;
        } catch (Exception e) {
            statusArea.append("❌ Error creating database: " + e.getMessage() + "\n");
        }
    }

    private void connectDatabase() {
        try {
            String host = hostField.getText();
            String port = portField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String database = databaseField.getText();

            DatabaseManager.connectToDatabase(host, port, username, password, database);
            statusArea.append("✓ Connected to database successfully!\n");
            setupComplete = true;
        } catch (SQLException e) {
            statusArea.append("❌ Error connecting to database: " + e.getMessage() + "\n");
        }
    }

    private void importSampleData() {
        try {
            DatabaseManager.importSampleData();
            statusArea.append("✓ Sample data imported successfully!\n");
        } catch (SQLException e) {
            statusArea.append("❌ Error importing sample data: " + e.getMessage() + "\n");
        }
    }

    public boolean isSetupComplete() {
        return setupComplete;
    }
} 