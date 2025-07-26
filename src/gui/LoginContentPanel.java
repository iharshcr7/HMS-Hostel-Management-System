package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

public class LoginContentPanel extends JPanel {
    private JRadioButton adminButton;
    private JRadioButton studentButton;
    private JButton continueButton;
    private ButtonGroup roleGroup;
    
    public LoginContentPanel() {
        setLayout(null);
        initializeUI();
    }
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable antialiasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(41, 128, 185),
            getWidth(), getHeight(), new Color(44, 62, 80)
        );
        g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
        // Draw hexagon pattern
        drawHexagonPattern(g2d);
        
        g2d.dispose();
    }

    private void drawHexagonPattern(Graphics2D g2d) {
        int hexSize = 30;
        int spacing = 60;
        g2d.setColor(new Color(255, 255, 255, 20));
        g2d.setStroke(new BasicStroke(1.0f));

        for (int y = -hexSize; y < getHeight() + hexSize; y += spacing) {
            for (int x = -hexSize; x < getWidth() + hexSize; x += spacing) {
                drawHexagon(g2d, x, y, hexSize);
                    }
                }
    }

    private void drawHexagon(Graphics2D g2d, int x, int y, int size) {
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            xPoints[i] = x + (int) (size * Math.cos(angle));
            yPoints[i] = y + (int) (size * Math.sin(angle));
        }
        g2d.drawPolygon(xPoints, yPoints, 6);
    }

    private void initializeUI() {
        // Create white rounded panel
        JPanel whitePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.dispose();
            }
        };
        whitePanel.setLayout(null);
        whitePanel.setOpaque(false);
        
        // Calculate center position for white panel
        int whitePanelWidth = 500;
        int whitePanelHeight = 400;
        whitePanel.setBounds(
            (getWidth() - whitePanelWidth) / 2,
            (getHeight() - whitePanelHeight) / 2,
            whitePanelWidth,
            whitePanelHeight
        );
        
        // Title
        JLabel titleLabel = new JLabel("Welcome to StayFlow Hub");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(20, 50, whitePanelWidth - 40, 40);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Please select your role to continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBounds(20, 100, whitePanelWidth - 40, 30);
        
        // Radio buttons
        adminButton = new JRadioButton("ADMIN");
        studentButton = new JRadioButton("STUDENT");
        roleGroup = new ButtonGroup();
        roleGroup.add(adminButton);
        roleGroup.add(studentButton);
        
        // Style radio buttons
        Font radioFont = new Font("Segoe UI", Font.BOLD, 14);
        adminButton.setFont(radioFont);
        studentButton.setFont(radioFont);
        adminButton.setOpaque(false);
        studentButton.setOpaque(false);
        
        // Position radio buttons
        adminButton.setBounds(100, 170, 300, 40);
        studentButton.setBounds(100, 220, 300, 40);
        
        // Continue button with green background
        continueButton = new JButton("Continue");
        continueButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        continueButton.setForeground(Color.WHITE);
        continueButton.setBackground(new Color(40, 167, 69));  // Bootstrap success green
        continueButton.setOpaque(true);  // Make button opaque to show background
        continueButton.setBorderPainted(false);
        continueButton.setFocusPainted(false);
        continueButton.setContentAreaFilled(true);  // Ensure the content area is filled
        continueButton.setBounds(75, 300, whitePanelWidth - 150, 45);
        continueButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Custom button UI to ensure solid background
        continueButton.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw solid background
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 5, 5);
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(c.getFont());
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(((JButton)c).getText(), g2d);
                int x = (c.getWidth() - (int) r.getWidth()) / 2;
                int y = (c.getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(((JButton)c).getText(), x, y);
                
                g2d.dispose();
            }
        });
        
        // Add hover effect to continue button
        continueButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                continueButton.setBackground(new Color(33, 136, 56));  // Darker green on hover
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                continueButton.setBackground(new Color(40, 167, 69));  // Return to original green
            }
        });
        
        // Add components to white panel
        whitePanel.add(titleLabel);
        whitePanel.add(subtitleLabel);
        whitePanel.add(adminButton);
        whitePanel.add(studentButton);
        whitePanel.add(continueButton);
        
        // Add white panel to main panel
        add(whitePanel);
        
        // Add component listener to handle resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                whitePanel.setBounds(
                    (getWidth() - whitePanelWidth) / 2,
                    (getHeight() - whitePanelHeight) / 2,
                    whitePanelWidth,
                    whitePanelHeight
                );
            }
        });
                
        // Add continue button action
        continueButton.addActionListener(e -> {
            if (!adminButton.isSelected() && !studentButton.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Please select a role to continue",
                    "Role Required",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String role = adminButton.isSelected() ? "admin" : "student";
                    
            // Get the parent window
            Window window = SwingUtilities.getWindowAncestor(this);
            
            // Create and show the dashboard
            JFrame dashboardFrame = new DashboardFrame("", role);
            dashboardFrame.setVisible(true);
            
            // Close the login window
            if (window != null) {
                window.dispose();
            }
        });
    }
    
    public String getSelectedRole() {
        return adminButton.isSelected() ? "admin" : "student";
    }
}