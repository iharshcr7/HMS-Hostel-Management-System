import gui.LoginContentPanel;
import gui.DatabaseLoginPanel;
import database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            JFrame loginFrame = new JFrame("Hostel Management System - Database Setup");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            DatabaseLoginPanel loginPanel = new DatabaseLoginPanel();
            loginFrame.setContentPane(loginPanel);
            
            loginFrame.setSize(450, 400);
            loginFrame.setResizable(false);
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);

            new Thread(() -> {
                while (!loginPanel.isSetupComplete()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    DatabaseManager.importSampleData();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                        "Error initializing sample data: " + e.getMessage(),
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                }

                SwingUtilities.invokeLater(() -> {
                    loginFrame.dispose();
                    showLoginPanel();
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error starting application: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showLoginPanel() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenRect = ge.getMaximumWindowBounds();
        
        // Create and show login frame
        JFrame loginFrame = new JFrame("Hostel Management System - Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create login panel
        LoginContentPanel loginPanel = new LoginContentPanel();
        loginFrame.setContentPane(loginPanel);
        
        // Set size and position
        loginFrame.setSize(screenRect.width, screenRect.height);
        loginFrame.setLocation(screenRect.x, screenRect.y);
        loginFrame.setVisible(true);
    }
} 