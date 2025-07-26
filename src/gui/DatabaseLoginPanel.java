package gui;

import javax.swing.*;
import java.awt.*;
import database.DatabaseManager;
import java.sql.SQLException;

public class DatabaseLoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private boolean setupComplete = false;
    private JTextArea statusArea;

    public DatabaseLoginPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 240, 255));

        // Create compact login panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Database Setup title
        JLabel setupLabel = new JLabel("Database Setup", SwingConstants.CENTER);
        setupLabel.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(setupLabel, gbc);

        // Username field
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel userLabel = new JLabel("MySQL Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField("root", 15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passLabel = new JLabel("MySQL Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(passwordField, gbc);

        // Instructions
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JTextArea instructions = new JTextArea(
            "Please enter your MySQL credentials.\n" +
            "This will create the hostel_management database\n" +
            "and initialize it with sample data."
        );
        instructions.setEditable(false);
        instructions.setBackground(loginPanel.getBackground());
        instructions.setFont(new Font("Arial", Font.PLAIN, 13));
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setPreferredSize(new Dimension(300, 60));
        loginPanel.add(instructions, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginButton = new JButton("Initialize Database");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.addActionListener(e -> initializeDatabase());
        loginPanel.add(loginButton, gbc);

        // Status area
        gbc.gridx = 0; gbc.gridy = 5;
        statusArea = new JTextArea(3, 25);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setPreferredSize(new Dimension(300, 60));
        loginPanel.add(scrollPane, gbc);

        // Add login panel to center of main panel
        add(loginPanel, BorderLayout.CENTER);
    }

    private void initializeDatabase() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter MySQL username",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Initializing...");
        statusArea.setText("Initializing database...\n");

        // Run database initialization in a background thread
        new Thread(() -> {
            try {
                // Initialize database
                DatabaseManager.initializeDatabase("localhost", "3306", username, password, "hostel_management");
                statusArea.append("Database created successfully!\n");

                setupComplete = true;
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Database setup completed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                String error = e.getMessage();
                SwingUtilities.invokeLater(() -> {
                    statusArea.append("Error: " + error + "\n");
                    JOptionPane.showMessageDialog(this,
                        "Database initialization failed: " + error,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    loginButton.setText("Initialize Database");
                });
            }
        }).start();
    }

    public boolean isSetupComplete() {
        return setupComplete;
    }
} 