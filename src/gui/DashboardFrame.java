package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import database.DatabaseManager;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DashboardFrame extends JFrame {
    private JPanel mainPanel;
    private JPanel contentPanel;
    private String currentUserRole;
    private String currentUsername;
    private DefaultTableModel roomTableModel;  // Add this field
    
    public DashboardFrame(String username, String role) {
        this.currentUsername = username;
        this.currentUserRole = role;
        initUI();
    }
    
    private void initUI() {
        // Get screen dimensions
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        
        // Set window to full screen size
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setBounds(0, 0, width, height);
        setName("mainFrame");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        JPanel container = new JPanel();
        container.setLayout(null);
        container.setBounds(0, 0, width, height);
        add(container);
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, width, height/6);
        headerPanel.setBackground(new Color(0, 199, 0));
        
        // Add centered title text
        JLabel titleLabel = new JLabel("Welcome To StayFlow Hub");
        titleLabel.setFont(titleLabel.getFont().deriveFont(45.0f));
        titleLabel.setForeground(Color.white);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 0, width, height/6);
        headerPanel.add(titleLabel);
        
        // Add user info and logout button in header
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        userInfoPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel(currentUserRole.substring(0, 1).toUpperCase() + 
                                    currentUserRole.substring(1) + ": " + currentUsername);
        userLabel.setFont(userLabel.getFont().deriveFont(16.0f));
        userLabel.setForeground(Color.white);
        userInfoPanel.add(userLabel);
        
        // Add logout button with oval shape styling
        JButton logoutButton = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(new Color(180, 25, 41)); // Darker when pressed
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(200, 35, 51)); // Darker on hover
                } else {
                    g.setColor(new Color(220, 53, 69)); // Bootstrap red
                }
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
            }
        };
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setOpaque(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        logoutButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutButton.repaint();
            }
            public void mouseExited(MouseEvent e) {
                logoutButton.repaint();
            }
            public void mousePressed(MouseEvent e) {
                logoutButton.repaint();
            }
            public void mouseReleased(MouseEvent e) {
                logoutButton.repaint();
            }
        });
        
        logoutButton.addActionListener(e -> handleLogout());
        userInfoPanel.add(logoutButton);
        
        userInfoPanel.setBounds(20, height/6 - 50, width - 40, 40);
        headerPanel.add(userInfoPanel);
        
        container.add(headerPanel);
        
        // Calculate main content area height
        int contentHeight = height - (height/6) - (height/6);
        
        // Left Menu Panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(null);
        menuPanel.setBackground(Color.orange);
        int menuWidth = width/4;
        menuPanel.setBounds(0, height/6, menuWidth, contentHeight);
        container.add(menuPanel);
        
        // Calculate button heights
        int buttonHeight = contentHeight/6;
        
        // Menu Buttons - Different for Admin and Student
        String[] adminMenuItems = {
            "Dashboard",
            "Room Management",
            "Warden Management",
            "Student Management",
            "Complaint Management",
            "Cafeteria Management",
            "Hostel Rules"
        };
        
        String[] studentMenuItems = {
            "Dashboard",
            "View My Details",
            "File Complaint",
            "View Room Details",
            "View Meal Plan",
            "Hostel Rules"
        };
        
        String[] menuItems = currentUserRole.equals("admin") ? adminMenuItems : studentMenuItems;
        
        for (int i = 0; i < menuItems.length; i++) {
            final int index = i;
            JButton button = new JButton(menuItems[i]);
            button.setBounds(0, buttonHeight * index, menuWidth, buttonHeight);
            button.setFont(button.getFont().deriveFont(24f));
            button.addActionListener(e -> handleMenuAction(menuItems[index]));
            
            // Add hover effect
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(255, 200, 100));
                }
                public void mouseExited(MouseEvent e) {
                    button.setBackground(UIManager.getColor("Button.background"));
                }
            });
            
            menuPanel.add(button);
        }
        
        // Content Panel (for image and other content)
        contentPanel = new JPanel();
        // Change layout to BoxLayout for vertical stacking
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBounds(menuWidth, height/6, width - menuWidth, contentHeight);
        
        // Add the hostel image to the content panel
        try {
            ImageIcon icon = new ImageIcon("Hostel.jpg");
            Image img = icon.getImage();
            int contentWidth = width - menuWidth; // Use the calculated content width
            int imageHeight = 450; // Keep target height around this value

            // Scale image with horizontal padding, keeping height approximately fixed
            int horizontalPadding = 40; // Padding on left and right (20px each side)
            int targetWidth = contentWidth - horizontalPadding;
            int targetHeight = imageHeight;

            // Ensure target dimensions are not negative or zero
            if (targetWidth <= 0) targetWidth = 1;
            if (targetHeight <= 0) targetHeight = 1;

            Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
            // Center the image horizontally
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(imageLabel);
        } catch (Exception e) {
            // Fallback to welcome message if image loading fails
            JLabel welcomeLabel = new JLabel("Welcome to Hostel Management System", SwingConstants.CENTER);
            welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(30f));
            welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(welcomeLabel);
        }
        
        container.add(contentPanel);

        // Load dashboard content initially based on role
        if (currentUserRole.equals("admin") || currentUserRole.equals("student")) {
            handleMenuAction("Dashboard");
        }
        
        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBounds(0, height - height/6, width, height/6);
        footerPanel.setBackground(Color.pink);
        container.add(footerPanel);
        
        JLabel footerLabel = new JLabel("<html><p>Developers: Pranav Mistry,Henisha Kandoi,Harsh Chauhan,Dhanvin Patel<br>Address: Bhayli,Vadodara,Gujarat-390018<br>Mobile: +91 9876543210</p></html>");
        footerLabel.setFont(footerLabel.getFont().deriveFont(22f));
        footerLabel.setVerticalTextPosition(JLabel.BOTTOM);
        footerLabel.setVerticalAlignment(JLabel.BOTTOM);
        footerPanel.add(footerLabel);
    }
    
    private void handleMenuAction(String menuItem) {
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
        
        switch (menuItem) {
            case "Dashboard":
                contentPanel.removeAll();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setBackground(Color.WHITE);

                // Add the hostel image to the content panel for both roles
                try {
                    ImageIcon icon = new ImageIcon("Hostel.jpg");
                    Image img = icon.getImage();
                    int contentWidth = contentPanel.getWidth();
                    int imageHeight = 450;

                    int horizontalPadding = 40;
                    int targetWidth = contentWidth - horizontalPadding;
                    int targetHeight = imageHeight;

                    if (targetWidth <= 0) targetWidth = 1;
                    if (targetHeight <= 0) targetHeight = 1;

                    Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                    imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    contentPanel.add(imageLabel);
                } catch (Exception e) {
                    JLabel welcomeLabel = new JLabel("Welcome to Hostel Management System", SwingConstants.CENTER);
                    welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(30f));
                    welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    contentPanel.add(welcomeLabel);
                }

                contentPanel.add(Box.createVerticalStrut(30)); // Space below the image

                if (currentUserRole.equals("admin")) {
                    // Admin dashboard content (statistics panels)
                    Map<String, Integer> dashboardData = getDashboardData();
                    int totalStudents = dashboardData.get("totalStudents");
                    int totalRooms = dashboardData.get("totalRooms");
                    int totalWardens = dashboardData.get("totalWardens");
                    int pendingComplaints = dashboardData.get("pendingComplaints");

                    JPanel dataPanel = new JPanel(new GridLayout(2, 2, 20, 20));
                    dataPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JPanel studentsPanel = createDashboardDataPanel("Total Students", totalStudents, new Color(52, 152, 219));
                    JPanel roomsPanel = createDashboardDataPanel("Total Rooms", totalRooms, new Color(46, 204, 113));
                    JPanel wardensPanel = createDashboardDataPanel("Total Wardens", totalWardens, new Color(255, 112, 67));
                    JPanel complaintsPanel = createDashboardDataPanel("Pending Complaints", pendingComplaints, new Color(230, 126, 34));

                    dataPanel.add(studentsPanel);
                    dataPanel.add(roomsPanel);
                    dataPanel.add(wardensPanel);
                    dataPanel.add(complaintsPanel);

                    contentPanel.add(dataPanel);
                } else {
                    // Student dashboard content (Room Availability and Fees Structure)
                    JLabel welcomeLabelText = new JLabel("Welcome to DHHP Hostel", SwingConstants.CENTER);
                    welcomeLabelText.setFont(new Font("Arial", Font.BOLD, 24));
                    welcomeLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);
                   // contentPanel.add(welcomeLabelText);
                   // contentPanel.add(Box.createVerticalStrut(20));

                    // Room Availability Section
                    JPanel roomAvailabilityPanel = new JPanel(new BorderLayout()); // Use BorderLayout for title and table
                    roomAvailabilityPanel.setBackground(Color.WHITE);
                    roomAvailabilityPanel.setBorder(BorderFactory.createTitledBorder("Room Availability"));
                    roomAvailabilityPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    try {
                        String sql = "SELECT r.room_no, r.room_type, r.sharing_type, r.block_name, r.floor_no, " +
                                   "r.capacity, COUNT(s.roll_no) as current_occupancy, (r.capacity - COUNT(s.roll_no)) as available_space " +
                                   "FROM rooms r LEFT JOIN students s ON r.room_no = s.room_no " + // Join with students table
                                   "GROUP BY r.room_no, r.room_type, r.sharing_type, r.block_name, r.floor_no, r.capacity " + // Group by room details
                                   "ORDER BY r.block_name, r.floor_no, r.room_no";
                        PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery();

                        String[] columns = {"Room No", "Type", "Sharing", "Block", "Floor", "Available/Total"};
                        DefaultTableModel model = new DefaultTableModel(columns, 0) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };

                        while (rs.next()) {
                            model.addRow(new Object[]{
                                rs.getString("room_no"),
                                rs.getString("room_type"),
                                rs.getString("sharing_type"),
                                rs.getString("block_name"),
                                rs.getInt("floor_no"),
                                rs.getInt("available_space") + "/" + rs.getInt("capacity")
                            });
                        }

                        JTable roomTable = new JTable(model);
                        roomTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Allow columns to adjust width
                        roomTable.getTableHeader().setReorderingAllowed(false);
                        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        roomTable.setRowHeight(25);

                        // int[] columnWidths = {80, 100, 100, 80, 60, 100};
                        // for (int i = 0; i < columnWidths.length; i++) {
                        //     roomTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
                        // }

                        JScrollPane scrollPane = new JScrollPane(roomTable);
                        scrollPane.setPreferredSize(new Dimension(contentPanel.getWidth() - 80, 300)); // Make it wider and taller
                        roomAvailabilityPanel.add(scrollPane, BorderLayout.CENTER);
                    } catch (SQLException ex) {
                        JLabel errorLabel = new JLabel("Error loading room data: " + ex.getMessage());
                        roomAvailabilityPanel.add(errorLabel, BorderLayout.CENTER);
                    }

                    // Fees Structure Section (Removed from dashboard)
                    // JPanel feesPanel = new JPanel(new BorderLayout());
                    // feesPanel.setBackground(Color.WHITE);
                    // feesPanel.setBorder(BorderFactory.createTitledBorder("Fees Structure"));
                    // feesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    // try {
                    //     String sql = "SELECT DISTINCT room_type, sharing_type FROM rooms ORDER BY room_type, sharing_type";
                    //     PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                    //     ResultSet rs = pstmt.executeQuery();
                    //     String[] columns = {"Room Type", "Sharing Type", "Annual Fee (₹)"};
                    //     DefaultTableModel model = new DefaultTableModel(columns, 0) {
                    //         @Override
                    //         public boolean isCellEditable(int row, int column) {
                    //             return false;
                    //         }
                    //     };
                    //     while (rs.next()) {
                    //         String roomType = rs.getString("room_type");
                    //         String sharingType = rs.getString("sharing_type");
                    //         double annualFeePerPerson = DatabaseManager.calculateRoomFee(roomType, sharingType);
                    //         model.addRow(new Object[]{
                    //             roomType,
                    //             sharingType,
                    //             String.format("%,.0f", annualFeePerPerson)
                    //         });
                    //     }
                    //     JTable feesTable = new JTable(model);
                    //     feesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    //     feesTable.getTableHeader().setReorderingAllowed(false);
                    //     feesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    //     feesTable.setRowHeight(25);
                    //     int[] columnWidths = {100, 100, 150};
                    //     for (int i = 0; i < columnWidths.length; i++) {
                    //         feesTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
                    //     }
                    //     JScrollPane scrollPane = new JScrollPane(feesTable);
                    //     scrollPane.setPreferredSize(new Dimension(370, 180));
                    //     feesPanel.add(scrollPane, BorderLayout.CENTER);
                    // } catch (SQLException ex) {
                    //     JLabel errorLabel = new JLabel("Error loading fees data: " + ex.getMessage());
                    //     feesPanel.add(errorLabel, BorderLayout.CENTER);
                    // }

                    // Add panels to the content
                    contentPanel.add(roomAvailabilityPanel);
                    contentPanel.add(Box.createVerticalGlue()); // Push everything to the top
                }

                contentPanel.revalidate();
                contentPanel.repaint();
                break;
            case "Room Management":
                if (currentUserRole.equals("admin")) {
                    showRoomManagementPanel();
                }
                break;
            case "Warden Management":
                if (currentUserRole.equals("admin")) {
                    JPanel wardenPanel = new JPanel(new BorderLayout(5, 5));
                    wardenPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                    
                    // Create table model for wardens
                    String[] wardenColumns = {"Warden ID", "Name", "Age", "Mobile", "Hostel", "Block", "Joining Date"};
                    DefaultTableModel wardenModel = new DefaultTableModel(wardenColumns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    JTable wardenTable = new JTable(wardenModel);
                    
                    // Configure table properties
                    wardenTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    wardenTable.getTableHeader().setReorderingAllowed(false);
                    wardenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    wardenTable.setRowHeight(25);
                    
                    // Set column widths
                    int[] wardenColumnWidths = {80, 150, 50, 100, 100, 80, 120};
                    for (int i = 0; i < wardenColumnWidths.length; i++) {
                        wardenTable.getColumnModel().getColumn(i).setPreferredWidth(wardenColumnWidths[i]);
                    }
                    
                    // Top panel for controls
                    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                    JButton refreshButton = new JButton("Refresh");
                    JButton addButton = new JButton("Add Warden");
                    JButton updateButton = new JButton("Update Warden");
                    JButton deleteButton = new JButton("Delete Warden");
                    JTextField searchField = new JTextField(20);
                    JButton searchButton = new JButton("Search");
                    
                    controlPanel.add(refreshButton);
                    controlPanel.add(addButton);
                    controlPanel.add(updateButton);
                    controlPanel.add(deleteButton);
                    controlPanel.add(new JLabel("Search:"));
                    controlPanel.add(searchField);
                    controlPanel.add(searchButton);
                    
                    // Create scroll pane
                    JScrollPane scrollPane = new JScrollPane(wardenTable);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    
                    // Refresh action to load warden data
                    refreshButton.addActionListener(e -> {
                        try {
                            String sql = "SELECT * FROM wardens ORDER BY warden_id";
                            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                            ResultSet rs = pstmt.executeQuery();
                            
                            wardenModel.setRowCount(0);
                            while (rs.next()) {
                                wardenModel.addRow(new Object[]{
                                    rs.getString("warden_id"),
                                    rs.getString("name"),
                                    rs.getInt("age"),
                                    rs.getString("mobile"),
                                    rs.getString("assigned_hostel"),
                                    rs.getString("block_name"),
                                    rs.getDate("joining_date")
                                });
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error loading wardens: " + ex.getMessage());
                        }
                    });
                    
                    // Add Warden action
                    addButton.addActionListener(e -> {
                        JPanel addPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                        addPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                        
                        JTextField wardenIdField = new JTextField();
                        JTextField nameField = new JTextField();
                        JTextField ageField = new JTextField();
                        JTextField mobileField = new JTextField();
                        JTextField hostelField = new JTextField();
                        JTextField blockField = new JTextField();
                        
                        addPanel.add(new JLabel("Warden ID:*"));
                        addPanel.add(wardenIdField);
                        addPanel.add(new JLabel("Name:*"));
                        addPanel.add(nameField);
                        addPanel.add(new JLabel("Age:*"));
                        addPanel.add(ageField);
                        addPanel.add(new JLabel("Mobile:* (10 digits)"));
                        addPanel.add(mobileField);
                        addPanel.add(new JLabel("Hostel:*"));
                        addPanel.add(hostelField);
                        addPanel.add(new JLabel("Block:*"));
                        addPanel.add(blockField);
                        
                        int result = JOptionPane.showConfirmDialog(null, addPanel, 
                            "Add New Warden", JOptionPane.OK_CANCEL_OPTION);
                            
                        if (result == JOptionPane.OK_OPTION) {
                            try {
                                // Validate fields
                                if (wardenIdField.getText().trim().isEmpty() || 
                                    nameField.getText().trim().isEmpty() ||
                                    ageField.getText().trim().isEmpty() ||
                                    mobileField.getText().trim().isEmpty() ||
                                    hostelField.getText().trim().isEmpty() ||
                                    blockField.getText().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(null, "All fields are required!");
                                    return;
                                }
                                
                                // Validate mobile number
                                if (!mobileField.getText().trim().matches("\\d{10}")) {
                                    JOptionPane.showMessageDialog(null, "Mobile number must be exactly 10 digits!");
                                    return;
                                }
                                
                                // Validate age
                                int age = Integer.parseInt(ageField.getText().trim());
                                if (age < 25 || age > 65) {
                                    JOptionPane.showMessageDialog(null, "Age must be between 25 and 65!");
                                    return;
                                }
                                
                                String sql = "INSERT INTO wardens (warden_id, name, age, mobile, assigned_hostel, block_name, joining_date) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, NOW())";
                                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                                pstmt.setString(1, wardenIdField.getText().trim());
                                pstmt.setString(2, nameField.getText().trim());
                                pstmt.setInt(3, age);
                                pstmt.setString(4, mobileField.getText().trim());
                                pstmt.setString(5, hostelField.getText().trim());
                                pstmt.setString(6, blockField.getText().trim());
                                
                                pstmt.executeUpdate();
                                refreshButton.doClick();
                                JOptionPane.showMessageDialog(null, "Warden added successfully!");
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(null, "Error adding warden: " + ex.getMessage());
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Please enter a valid age!");
                            }
                        }
                    });
                    
                    // Update Warden action
                    updateButton.addActionListener(e -> {
                        int selectedRow = wardenTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(null, "Please select a warden to update");
                            return;
                        }
                        
                        String wardenId = (String) wardenTable.getValueAt(selectedRow, 0);
                        try {
                            String sql = "SELECT * FROM wardens WHERE warden_id = ?";
                            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                            pstmt.setString(1, wardenId);
                            ResultSet rs = pstmt.executeQuery();
                            
                            if (rs.next()) {
                                JPanel updatePanel = new JPanel(new GridLayout(0, 2, 5, 5));
                                updatePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                                
                                JTextField wardenIdField = new JTextField(rs.getString("warden_id"));
                                wardenIdField.setEditable(false);
                                JTextField nameField = new JTextField(rs.getString("name"));
                                JTextField ageField = new JTextField(String.valueOf(rs.getInt("age")));
                                JTextField mobileField = new JTextField(rs.getString("mobile"));
                                JTextField hostelField = new JTextField(rs.getString("assigned_hostel"));
                                JTextField blockField = new JTextField(rs.getString("block_name"));
                                
                                updatePanel.add(new JLabel("Warden ID:"));
                                updatePanel.add(wardenIdField);
                                updatePanel.add(new JLabel("Name:*"));
                                updatePanel.add(nameField);
                                updatePanel.add(new JLabel("Age:*"));
                                updatePanel.add(ageField);
                                updatePanel.add(new JLabel("Mobile:* (10 digits)"));
                                updatePanel.add(mobileField);
                                updatePanel.add(new JLabel("Hostel:*"));
                                updatePanel.add(hostelField);
                                updatePanel.add(new JLabel("Block:*"));
                                updatePanel.add(blockField);
                                
                                int result = JOptionPane.showConfirmDialog(null, updatePanel, 
                                    "Update Warden", JOptionPane.OK_CANCEL_OPTION);
                                    
                                if (result == JOptionPane.OK_OPTION) {
                                    // Validate fields
                                    if (nameField.getText().trim().isEmpty() ||
                                        ageField.getText().trim().isEmpty() ||
                                        mobileField.getText().trim().isEmpty() ||
                                        hostelField.getText().trim().isEmpty() ||
                                        blockField.getText().trim().isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "All fields are required!");
                                        return;
                                    }
                                    
                                    // Validate mobile number
                                    if (!mobileField.getText().trim().matches("\\d{10}")) {
                                        JOptionPane.showMessageDialog(null, "Mobile number must be exactly 10 digits!");
                                        return;
                                    }
                                    
                                    // Validate age
                                    int age = Integer.parseInt(ageField.getText().trim());
                                    if (age < 25 || age > 65) {
                                        JOptionPane.showMessageDialog(null, "Age must be between 25 and 65!");
                                        return;
                                    }
                                    
                                    String updateSql = "UPDATE wardens SET name = ?, age = ?, mobile = ?, " +
                                                     "assigned_hostel = ?, block_name = ? WHERE warden_id = ?";
                                    PreparedStatement updateStmt = DatabaseManager.getConnection().prepareStatement(updateSql);
                                    updateStmt.setString(1, nameField.getText().trim());
                                    updateStmt.setInt(2, age);
                                    updateStmt.setString(3, mobileField.getText().trim());
                                    updateStmt.setString(4, hostelField.getText().trim());
                                    updateStmt.setString(5, blockField.getText().trim());
                                    updateStmt.setString(6, wardenId);
                                    
                                    updateStmt.executeUpdate();
                                    refreshButton.doClick();
                                    JOptionPane.showMessageDialog(null, "Warden updated successfully!");
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error updating warden: " + ex.getMessage());
                        }
                    });
                    
                    // Delete Warden action
                    deleteButton.addActionListener(e -> {
                        int selectedRow = wardenTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(null, "Please select a warden to delete");
                            return;
                        }
                        
                        String wardenId = (String) wardenTable.getValueAt(selectedRow, 0);
                        int confirm = JOptionPane.showConfirmDialog(null,
                            "Are you sure you want to delete this warden?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                            
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                String sql = "DELETE FROM wardens WHERE warden_id = ?";
                                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                                pstmt.setString(1, wardenId);
                                pstmt.executeUpdate();
                                
                                refreshButton.doClick();
                                JOptionPane.showMessageDialog(null, "Warden deleted successfully!");
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(null, "Error deleting warden: " + ex.getMessage());
                            }
                        }
                    });
                    
                    // Search action
                    searchButton.addActionListener(e -> {
                        String searchTerm = searchField.getText().trim().toLowerCase();
                        if (searchTerm.isEmpty()) {
                            showRoomManagementPanel();
                            return;
                        }
                        
                        for (int i = wardenModel.getRowCount() - 1; i >= 0; i--) {
                            boolean found = false;
                            for (int j = 0; j < wardenModel.getColumnCount(); j++) {
                                Object value = wardenModel.getValueAt(i, j);
                                if (value != null && value.toString().toLowerCase().contains(searchTerm)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                wardenModel.removeRow(i);
                            }
                        }
                    });
                    
                    wardenPanel.add(controlPanel, BorderLayout.NORTH);
                    wardenPanel.add(scrollPane, BorderLayout.CENTER);
                    contentPanel.add(wardenPanel);
                    
                    // Initial load
                    refreshButton.doClick();
                }
                break;
            case "Student Management":
                if (currentUserRole.equals("admin")) {
                    JPanel studentPanel = new JPanel(new BorderLayout(5, 5));
                    studentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                    
                    // Basic table model with custom column widths
                    String[] columns = {"Roll No", "Name", "College", "Department", "Semester", "Age", "Contact", "Room", "Room Type", "Sharing Type", "Block", "Floor", "Amount Paid", "Amount Due", "Payment Method", "Password"};
                    DefaultTableModel model = new DefaultTableModel(columns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    JTable table = new JTable(model);
                    
                    // Configure table properties
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    table.getTableHeader().setReorderingAllowed(false);
                    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    table.setRowHeight(25);
                    
                    // Set specific column widths
                    int[] columnWidths = {80, 120, 100, 100, 70, 50, 100, 60, 80, 80, 60, 50, 90, 90, 100, 100};
                    for (int i = 0; i < columnWidths.length; i++) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
                    }
                    
                    // Add data
                    refreshStudentTable(model);
                    
                    // Create scroll pane with both scrollbars always visible
                    JScrollPane scrollPane = new JScrollPane(table);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    
                    // Add controls with better spacing
                    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                    JButton addButton = new JButton("Add Student");
                    JButton editButton = new JButton("Edit Student");
                    JButton deleteButton = new JButton("Delete Student");
                    JButton refreshButton = new JButton("Refresh");
                    controls.add(addButton);
                    controls.add(editButton);
                    controls.add(deleteButton);
                    controls.add(refreshButton);
                    controls.add(new JLabel("Search:"));
                    JTextField searchField = new JTextField(15);
                    controls.add(searchField);
                    JButton searchButton = new JButton("Search");
                    controls.add(searchButton);
                    
                    // Add Student Action
                    addButton.addActionListener(e -> {
                        // First show available rooms with costs
                        try {
                            String roomQuery = "SELECT r.room_no, r.room_type, r.sharing_type, r.block_name, r.floor_no, " +
                                             "r.capacity, r.current_occupancy, " +
                                             "(r.capacity - r.current_occupancy) as available_space " +
                                             "FROM rooms r " +
                                             "WHERE r.current_occupancy < r.capacity " +
                                             "ORDER BY r.block_name, r.floor_no, r.room_no";
                            PreparedStatement roomStmt = DatabaseManager.getConnection().prepareStatement(roomQuery);
                            ResultSet roomRs = roomStmt.executeQuery();

                            // Create a table to display available rooms with costs
                            String[] roomColumns = {"Room No", "Type", "Sharing", "Block", "Floor", "Available/Total", "Total Cost"};
                            DefaultTableModel roomModel = new DefaultTableModel(roomColumns, 0) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return false;
                                }
                            };

                            while (roomRs.next()) {
                                String roomType = roomRs.getString("room_type");
                                String sharingType = roomRs.getString("sharing_type");
                                double totalCost = calculateRoomCost(roomType, sharingType);
                                
                                roomModel.addRow(new Object[]{
                                    roomRs.getString("room_no"),
                                    roomType,
                                    sharingType,
                                    roomRs.getString("block_name"),
                                    roomRs.getInt("floor_no"),
                                    roomRs.getInt("available_space") + "/" + roomRs.getInt("capacity"),
                                    String.format("₹%.0f", totalCost)
                                });
                            }

                            // Show room selection dialog with costs
                            JDialog roomDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Available Rooms", true);
                            roomDialog.setLayout(new BorderLayout(5, 5));
                            
                            JLabel explainLabel = new JLabel("<html>Available rooms and costs are shown below.<br>" +
                                "Room pricing is calculated based on total room cost divided by number of occupants:<br>" +
                                "• Luxury rooms: Total room cost ₹60,000<br>" +
                                "  - 1 Sharing: ₹60,000 per person<br>" +
                                "  - 2 Sharing: ₹30,000 per person (₹60,000 total)<br>" +
                                "  - 4 Sharing: ₹15,000 per person (₹60,000 total)<br>" +
                                "• Standard rooms: Total room cost ₹40,000<br>" +
                                "  - 1 Sharing: ₹40,000 per person<br>" +
                                "  - 2 Sharing: ₹20,000 per person (₹40,000 total)<br>" +
                                "  - 4 Sharing: ₹10,000 per person (₹40,000 total)<br>" +
                                "Note: The amount shown is per person. Total room cost remains same regardless of sharing type.</html>");
                            explainLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
                            roomDialog.add(explainLabel, BorderLayout.NORTH);

                            JTable roomTable = new JTable(roomModel);
                            roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            JScrollPane roomScroll = new JScrollPane(roomTable);
                            roomDialog.add(roomScroll, BorderLayout.CENTER);

                            JButton closeButton = new JButton("Continue to Add Student");
                            JPanel buttonPanel = new JPanel();
                            buttonPanel.add(closeButton);
                            roomDialog.add(buttonPanel, BorderLayout.SOUTH);

                            closeButton.addActionListener(event -> roomDialog.dispose());

                            roomDialog.setSize(800, 500);
                            roomDialog.setLocationRelativeTo(this);
                            roomDialog.setVisible(true);

                            // After showing available rooms, proceed with student addition
                            JPanel addPanel = new JPanel();
                            addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
                            addPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                            
                            // Create form panel
                            JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                            
                            JTextField rollNoField = new JTextField();
                            JTextField nameField = new JTextField();
                            JTextField collegeField = new JTextField();
                            JTextField deptField = new JTextField();
                            JTextField semesterField = new JTextField();
                            JTextField ageField = new JTextField();
                            JTextField mobileField = new JTextField();
                            JTextField roomNoField = new JTextField();
                            JTextField passwordField = new JTextField();
                            JTextField amountPaidField = new JTextField("0");
                            
                            formPanel.add(new JLabel("Roll No:*"));
                            formPanel.add(rollNoField);
                            formPanel.add(new JLabel("Name:*"));
                            formPanel.add(nameField);
                            formPanel.add(new JLabel("College:*"));
                            formPanel.add(collegeField);
                            formPanel.add(new JLabel("Department:*"));
                            formPanel.add(deptField);
                            formPanel.add(new JLabel("Semester:*"));
                            formPanel.add(semesterField);
                            formPanel.add(new JLabel("Age:* (must be 16+)"));
                            formPanel.add(ageField);
                            formPanel.add(new JLabel("Contact:* (10 digits)"));
                            formPanel.add(mobileField);
                            formPanel.add(new JLabel("Room No:* (from available rooms)"));
                            formPanel.add(roomNoField);
                            formPanel.add(new JLabel("Password:*"));
                            formPanel.add(passwordField);
                            formPanel.add(new JLabel("Amount Paid:*"));
                            formPanel.add(amountPaidField);
                            
                            addPanel.add(formPanel);
                            
                            // Room verification panel
                            JPanel roomVerifyPanel = new JPanel();
                            roomVerifyPanel.setLayout(new BoxLayout(roomVerifyPanel, BoxLayout.Y_AXIS));
                            roomVerifyPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
                            
                            JButton verifyButton = new JButton("Verify Room");
                            JPanel verifyButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                            verifyButtonPanel.add(verifyButton);
                            roomVerifyPanel.add(verifyButtonPanel);
                            
                            JLabel roomStatusLabel = new JLabel(" ");
                            roomStatusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                            roomStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                            statusPanel.add(roomStatusLabel);
                            roomVerifyPanel.add(statusPanel);
                            
                            addPanel.add(roomVerifyPanel);
                            
                            // Add note about required fields
                            JLabel noteLabel = new JLabel("* All fields are required");
                            noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                            noteLabel.setForeground(Color.RED);
                            addPanel.add(noteLabel);

                            verifyButton.addActionListener(verifyEvent -> {
                                String roomNo = roomNoField.getText().trim();
                                try {
                                    String verifyQuery = "SELECT room_type, sharing_type, capacity, current_occupancy, block_name, floor_no " +
                                                       "FROM rooms WHERE room_no = ?";
                                    PreparedStatement verifyStmt = DatabaseManager.getConnection().prepareStatement(verifyQuery);
                                    verifyStmt.setString(1, roomNo);
                                    ResultSet verifyRs = verifyStmt.executeQuery();
                                    
                                    if (verifyRs.next()) {
                                        int capacity = verifyRs.getInt("capacity");
                                        int occupancy = verifyRs.getInt("current_occupancy");
                                        String roomType = verifyRs.getString("room_type");
                                        String sharingType = verifyRs.getString("sharing_type");
                                        double totalCost = calculateRoomCost(roomType, sharingType);
                                        
                                        if (occupancy < capacity) {
                                            roomStatusLabel.setText("<html><font color='green'>✓ Room Available<br>" + 
                                                "Type: " + roomType + "<br>" +
                                                "Sharing: " + sharingType + "<br>" +
                                                "Block: " + verifyRs.getString("block_name") + "<br>" +
                                                "Floor: " + verifyRs.getInt("floor_no") + "<br>" +
                                                "Spaces: " + (capacity - occupancy) + " of " + capacity + " available<br>" +
                                                "Total Cost: ₹" + String.format("%.0f", totalCost) + "</font></html>");
                                        } else {
                                            roomStatusLabel.setText("<html><font color='red'>✗ Room is full!<br>" +
                                                "Please select a different room.</font></html>");
                                        }
                                    } else {
                                        roomStatusLabel.setText("<html><font color='red'>✗ Invalid room number!<br>" +
                                            "Please check the available rooms list.</font></html>");
                                    }
                                    SwingUtilities.getWindowAncestor(addPanel).pack();
                                } catch (SQLException ex) {
                                    roomStatusLabel.setText("<html><font color='red'>✗ Error verifying room!</font></html>");
                                }
                            });
                            
                            int result = JOptionPane.showConfirmDialog(this, addPanel, 
                                "Add New Student", JOptionPane.OK_CANCEL_OPTION);
                                
                            if (result == JOptionPane.OK_OPTION) {
                                // Validate all fields
                                String validationError = validateFields(
                                    rollNoField.getText().trim(),
                                    nameField.getText().trim(),
                                    collegeField.getText().trim(),
                                    deptField.getText().trim(),
                                    semesterField.getText().trim(),
                                    ageField.getText().trim(),
                                    mobileField.getText().trim(),
                                    roomNoField.getText().trim(),
                                    passwordField.getText().trim(),
                                    amountPaidField.getText().trim()
                                );
                                
                                if (validationError != null) {
                                    JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                try {
                                    // Get room details and calculate due amount
                                    String roomCheck = "SELECT room_type, sharing_type, capacity, current_occupancy FROM rooms WHERE room_no = ?";
                                    PreparedStatement checkStmt = DatabaseManager.getConnection().prepareStatement(roomCheck);
                                    checkStmt.setString(1, roomNoField.getText().trim());
                                    ResultSet rs = checkStmt.executeQuery();
                                    
                                    if (!rs.next()) {
                                        JOptionPane.showMessageDialog(this, "Room does not exist!");
                                        return;
                                    }
                                    
                                    int capacity = rs.getInt("capacity");
                                    int currentOccupancy = rs.getInt("current_occupancy");
                                    
                                    if (currentOccupancy >= capacity) {
                                        JOptionPane.showMessageDialog(this, "Room is already full!");
                                        return;
                                    }
                                    
                                    // Calculate total cost and due amount
                                    double totalCost = calculateRoomCost(rs.getString("room_type"), rs.getString("sharing_type"));
                                    double amountPaid = Double.parseDouble(amountPaidField.getText().trim());
                                    double amountDue = totalCost - amountPaid;
                                    
                                    if (amountPaid > totalCost) {
                                        JOptionPane.showMessageDialog(this, "Amount paid cannot be greater than total cost: ₹" + 
                                            String.format("%.0f", totalCost));
                                        return;
                                    }
                                    
                                    // Get payment method
                                    String[] paymentMethods = {"Cash", "UPI", "Card"};
                                    String paymentMethod = (String) JOptionPane.showInputDialog(
                                        this,
                                        "Select Payment Method:",
                                        "Payment Method",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        paymentMethods,
                                        paymentMethods[0]
                                    );

                                    if (paymentMethod == null) {
                                        return; // User cancelled
                                    }

                                    // Get additional payment details if needed
                                    String paymentDetails = "";
                                    if ("UPI".equals(paymentMethod)) {
                                        JTextField upiIdField = new JTextField();
                                        Object[] message = {
                                            "Enter UPI ID:", upiIdField
                                        };
                                        int option = JOptionPane.showConfirmDialog(
                                            this, message, "UPI Payment", JOptionPane.OK_CANCEL_OPTION);
                                        if (option != JOptionPane.OK_OPTION || upiIdField.getText().trim().isEmpty()) {
                                            JOptionPane.showMessageDialog(this, "Valid UPI ID is required!");
                                            return;
                                        }
                                    } else if ("Card".equals(paymentMethod)) {
                                        JPanel cardPanel = new JPanel(new GridLayout(0, 1));
                                        JTextField cardNumberField = new JTextField();
                                        JTextField expiryField = new JTextField();
                                        JTextField cvvField = new JTextField();
                                        
                                        cardPanel.add(new JLabel("Card Number (16 digits):"));
                                        cardPanel.add(cardNumberField);
                                        cardPanel.add(new JLabel("Expiry (MM/YY):"));
                                        cardPanel.add(expiryField);
                                        cardPanel.add(new JLabel("CVV:"));
                                        cardPanel.add(cvvField);
                                        
                                        int option = JOptionPane.showConfirmDialog(
                                            this, cardPanel, "Card Payment", JOptionPane.OK_CANCEL_OPTION);
                                        
                                        if (option != JOptionPane.OK_OPTION) {
                                            return;
                                        }
                                        
                                        // Validate card details
                                        String cardNumber = cardNumberField.getText().trim();
                                        String expiry = expiryField.getText().trim();
                                        String cvv = cvvField.getText().trim();
                                        
                                        if (!cardNumber.matches("\\d{16}")) {
                                            JOptionPane.showMessageDialog(this, "Invalid card number!");
                                            return;
                                        }
                                        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                                            JOptionPane.showMessageDialog(this, "Invalid expiry date! Use MM/YY format");
                                            return;
                                        }
                                        if (!cvv.matches("\\d{3}")) {
                                            JOptionPane.showMessageDialog(this, "Invalid CVV!");
                                            return;
                                        }
                                    }
                                    
                                    // Insert student with calculated due amount and payment method
                                    String sql = "INSERT INTO students (roll_no, name, college, department, semester, " +
                                               "age, mobile, room_no, room_type, sharing_type, block_name, floor_no, password, amount_paid, amount_due, payment_method) " +
                                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                    PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                                    
                                    // Get room details first
                                    String roomDetailsSql = "SELECT room_type, sharing_type, block_name, floor_no FROM rooms WHERE room_no = ?";
                                    PreparedStatement roomDetailsStmt = DatabaseManager.getConnection().prepareStatement(roomDetailsSql);
                                    roomDetailsStmt.setString(1, roomNoField.getText().trim());
                                    ResultSet roomDetails = roomDetailsStmt.executeQuery();
                                    
                                    if (!roomDetails.next()) {
                                        JOptionPane.showMessageDialog(this, "Error: Room details not found!");
                                        return;
                                    }
                                    
                                    pstmt.setString(1, rollNoField.getText().trim());
                                    pstmt.setString(2, nameField.getText().trim());
                                    pstmt.setString(3, collegeField.getText().trim());
                                    pstmt.setString(4, deptField.getText().trim());
                                    pstmt.setString(5, semesterField.getText().trim());
                                    pstmt.setInt(6, Integer.parseInt(ageField.getText().trim()));
                                    pstmt.setString(7, mobileField.getText().trim());
                                    pstmt.setString(8, roomNoField.getText().trim());
                                    pstmt.setString(9, roomDetails.getString("room_type"));
                                    pstmt.setString(10, roomDetails.getString("sharing_type"));
                                    pstmt.setString(11, roomDetails.getString("block_name"));
                                    pstmt.setInt(12, roomDetails.getInt("floor_no"));
                                    pstmt.setString(13, passwordField.getText().trim());
                                    pstmt.setDouble(14, amountPaid);
                                    pstmt.setDouble(15, amountDue);
                                    pstmt.setString(16, paymentMethod);
                                    
                                    pstmt.executeUpdate();
                                    
                                    // Update room occupancy
                                    String updateRoom = "UPDATE rooms SET current_occupancy = current_occupancy + 1 WHERE room_no = ?";
                                    PreparedStatement updateStmt = DatabaseManager.getConnection().prepareStatement(updateRoom);
                                    updateStmt.setString(1, roomNoField.getText().trim());
                                    updateStmt.executeUpdate();
                                    
                                    refreshStudentTable(model);
                                    JOptionPane.showMessageDialog(this, 
                                        String.format("Student added successfully!\nTotal Cost: ₹%.0f\nAmount Paid: ₹%.0f\nAmount Due: ₹%.0f", 
                                        totalCost, amountPaid, amountDue));
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(this, "Error adding student: " + ex.getMessage());
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, "Error loading available rooms: " + ex.getMessage());
                        }
                    });
                    
                    // Edit Student Action
                    editButton.addActionListener(e -> {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(this, "Please select a student to edit");
                            return;
                        }
                        
                        String rollNo = (String) table.getValueAt(selectedRow, 0);
                        try {
                            String sql = "SELECT * FROM students WHERE roll_no = ?";
                            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                            pstmt.setString(1, rollNo);
                            ResultSet rs = pstmt.executeQuery();
                            
                            if (rs.next()) {
                                JPanel editPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                                editPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                                
                                JTextField nameField = new JTextField(rs.getString("name"));
                                JTextField collegeField = new JTextField(rs.getString("college"));
                                JTextField deptField = new JTextField(rs.getString("department"));
                                JTextField semesterField = new JTextField(rs.getString("semester"));
                                JTextField ageField = new JTextField(String.valueOf(rs.getInt("age")));
                                JTextField mobileField = new JTextField(rs.getString("mobile"));
                                JTextField roomNoField = new JTextField(rs.getString("room_no"));
                                JTextField amountPaidField = new JTextField(String.valueOf(rs.getDouble("amount_paid")));
                                
                                editPanel.add(new JLabel("Name:"));
                                editPanel.add(nameField);
                                editPanel.add(new JLabel("College:"));
                                editPanel.add(collegeField);
                                editPanel.add(new JLabel("Department:"));
                                editPanel.add(deptField);
                                editPanel.add(new JLabel("Semester:"));
                                editPanel.add(semesterField);
                                editPanel.add(new JLabel("Age:"));
                                editPanel.add(ageField);
                                editPanel.add(new JLabel("Contact:"));
                                editPanel.add(mobileField);
                                editPanel.add(new JLabel("Room No:"));
                                editPanel.add(roomNoField);
                                editPanel.add(new JLabel("Amount Paid:"));
                                editPanel.add(amountPaidField);
                                
                                int result = JOptionPane.showConfirmDialog(this, editPanel, 
                                    "Edit Student", JOptionPane.OK_CANCEL_OPTION);
                                    
                                if (result == JOptionPane.OK_OPTION) {
                                    String oldRoomNo = rs.getString("room_no");
                                    String newRoomNo = roomNoField.getText();
                                    
                                    // Check room capacity if room is changed
                                    if (!oldRoomNo.equals(newRoomNo)) {
                                        String roomCheck = "SELECT capacity, current_occupancy FROM rooms WHERE room_no = ?";
                                        PreparedStatement checkStmt = DatabaseManager.getConnection().prepareStatement(roomCheck);
                                        checkStmt.setString(1, newRoomNo);
                                        ResultSet roomRs = checkStmt.executeQuery();
                                        
                                        if (!roomRs.next()) {
                                            JOptionPane.showMessageDialog(this, "New room does not exist!");
                                            return;
                                        }
                                        
                                        int capacity = roomRs.getInt("capacity");
                                        int currentOccupancy = roomRs.getInt("current_occupancy");
                                        
                                        if (currentOccupancy >= capacity) {
                                            JOptionPane.showMessageDialog(this, "New room is already full!");
                                            return;
                                        }
                                    }
                                    
                                    // Update student
                                    String updateSql = "UPDATE students SET name = ?, college = ?, department = ?, " +
                                                     "semester = ?, age = ?, mobile = ?, room_no = ?, amount_paid = ? " +
                                                     "WHERE roll_no = ?";
                                    PreparedStatement updateStmt = DatabaseManager.getConnection().prepareStatement(updateSql);
                                    updateStmt.setString(1, nameField.getText());
                                    updateStmt.setString(2, collegeField.getText());
                                    updateStmt.setString(3, deptField.getText());
                                    updateStmt.setString(4, semesterField.getText());
                                    updateStmt.setInt(5, Integer.parseInt(ageField.getText()));
                                    updateStmt.setString(6, mobileField.getText());
                                    updateStmt.setString(7, roomNoField.getText());
                                    updateStmt.setDouble(8, Double.parseDouble(amountPaidField.getText()));
                                    updateStmt.setString(9, rollNo);
                                    
                                    updateStmt.executeUpdate();
                                    
                                    // Update room occupancy if room changed
                                    if (!oldRoomNo.equals(newRoomNo)) {
                                        // Decrease old room occupancy
                                        String decreaseOld = "UPDATE rooms SET current_occupancy = current_occupancy - 1 WHERE room_no = ?";
                                        PreparedStatement decreaseStmt = DatabaseManager.getConnection().prepareStatement(decreaseOld);
                                        decreaseStmt.setString(1, oldRoomNo);
                                        decreaseStmt.executeUpdate();
                                        
                                        // Increase new room occupancy
                                        String increaseNew = "UPDATE rooms SET current_occupancy = current_occupancy + 1 WHERE room_no = ?";
                                        PreparedStatement increaseStmt = DatabaseManager.getConnection().prepareStatement(increaseNew);
                                        increaseStmt.setString(1, newRoomNo);
                                        increaseStmt.executeUpdate();
                                    }
                                    
                                    refreshStudentTable(model);
                                    JOptionPane.showMessageDialog(this, "Student updated successfully!");
                                }
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, "Error updating student: " + ex.getMessage());
                        }
                    });
                    
                    // Delete Student Action
                    deleteButton.addActionListener(e -> {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(this, "Please select a student to delete");
                            return;
                        }
                        
                        int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete this student?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                            
                        if (confirm == JOptionPane.YES_OPTION) {
                            String rollNo = (String) table.getValueAt(selectedRow, 0);
                            String roomNo = (String) table.getValueAt(selectedRow, 7);
                            
                            try {
                                // First decrease room occupancy
                                String updateRoom = "UPDATE rooms SET current_occupancy = current_occupancy - 1 WHERE room_no = ?";
                                PreparedStatement updateStmt = DatabaseManager.getConnection().prepareStatement(updateRoom);
                                updateStmt.setString(1, roomNo);
                                updateStmt.executeUpdate();
                                
                                // Then delete student
                                String sql = "DELETE FROM students WHERE roll_no = ?";
                                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                                pstmt.setString(1, rollNo);
                                pstmt.executeUpdate();
                                
                                refreshStudentTable(model);
                                JOptionPane.showMessageDialog(this, "Student deleted successfully!");
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(this, "Error deleting student: " + ex.getMessage());
                            }
                        }
                    });
                    
                    // Refresh button action
                    refreshButton.addActionListener(e -> refreshStudentTable(model));
                    
                    // Search button action
                    searchButton.addActionListener(e -> {
                        String searchTerm = searchField.getText().trim().toLowerCase();
                        if (searchTerm.isEmpty()) {
                            showRoomManagementPanel();
                            return;
                        }
                        
                        for (int i = model.getRowCount() - 1; i >= 0; i--) {
                            boolean found = false;
                            for (int j = 0; j < model.getColumnCount(); j++) {
                                Object value = model.getValueAt(i, j);
                                if (value != null && value.toString().toLowerCase().contains(searchTerm)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                model.removeRow(i);
                            }
                        }
                    });
                    
                    studentPanel.add(controls, BorderLayout.NORTH);
                    studentPanel.add(scrollPane, BorderLayout.CENTER);
                    
                    contentPanel.removeAll();
                    contentPanel.add(studentPanel);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
                break;
            case "Complaint Management":
                if (currentUserRole.equals("admin")) {
                    JPanel complaintPanel = new JPanel(new BorderLayout(5, 5));
                    complaintPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                    
                    // Create table model for complaints
                    String[] complaintColumns = {"ID", "Student", "Roll No", "Complaint", "Status", "Filed On", "Resolved On"};
                    DefaultTableModel complaintModel = new DefaultTableModel(complaintColumns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    JTable complaintTable = new JTable(complaintModel);
                    
                    // Configure table properties
                    complaintTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    complaintTable.getTableHeader().setReorderingAllowed(false);
                    complaintTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    complaintTable.setRowHeight(25);
                    
                    // Set column widths
                    int[] complaintColumnWidths = {50, 100, 80, 300, 80, 150, 150};
                    for (int i = 0; i < complaintColumnWidths.length; i++) {
                        complaintTable.getColumnModel().getColumn(i).setPreferredWidth(complaintColumnWidths[i]);
                    }
                    
                    // Top panel for controls
                    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                    JButton refreshButton = new JButton("Refresh");
                    JButton viewPendingButton = new JButton("View Pending");
                    JButton resolveButton = new JButton("Resolve Complaint");
                    JButton deleteButton = new JButton("Delete Complaint");  // Add delete button
                    JTextField searchField = new JTextField(20);
                    JButton searchButton = new JButton("Search");
                    
                    controlPanel.add(refreshButton);
                    controlPanel.add(viewPendingButton);
                    controlPanel.add(resolveButton);
                    controlPanel.add(deleteButton);  // Add delete button to panel
                    controlPanel.add(new JLabel("Search:"));
                    controlPanel.add(searchField);
                    controlPanel.add(searchButton);
                    
                    // Create scroll pane
                    JScrollPane scrollPane = new JScrollPane(complaintTable);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    
                    // Refresh action to load all complaints
                    refreshButton.addActionListener(e -> {
                        try {
                            String sql = "SELECT DISTINCT c.complaint_id, s.name as student_name, c.student_roll_no, " +
                                       "c.complaint_text, c.status, c.filing_date, c.resolution_date " +
                                       "FROM complaints c " +
                                       "JOIN students s ON c.student_roll_no = s.roll_no " +
                                       "ORDER BY c.filing_date DESC";
                            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                            ResultSet rs = pstmt.executeQuery();
                            
                            complaintModel.setRowCount(0);
                            while (rs.next()) {
                                complaintModel.addRow(new Object[]{
                                    rs.getInt("complaint_id"),
                                    rs.getString("student_name"),
                                    rs.getString("student_roll_no"),
                                    rs.getString("complaint_text"),
                                    rs.getString("status"),
                                    rs.getTimestamp("filing_date"),
                                    rs.getTimestamp("resolution_date")
                                });
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error loading complaints: " + ex.getMessage());
                        }
                    });
                    
                    // View Pending action
                    viewPendingButton.addActionListener(e -> {
                        try {
                            String sql = "SELECT DISTINCT c.complaint_id, s.name as student_name, c.student_roll_no, " +
                                       "c.complaint_text, c.status, c.filing_date, c.resolution_date " +
                                       "FROM complaints c " +
                                       "JOIN students s ON c.student_roll_no = s.roll_no " +
                                       "WHERE c.status = 'Pending' " +
                                       "ORDER BY c.filing_date DESC";
                            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                            ResultSet rs = pstmt.executeQuery();
                            
                            complaintModel.setRowCount(0);
                            while (rs.next()) {
                                complaintModel.addRow(new Object[]{
                                    rs.getInt("complaint_id"),
                                    rs.getString("student_name"),
                                    rs.getString("student_roll_no"),
                                    rs.getString("complaint_text"),
                                    rs.getString("status"),
                                    rs.getTimestamp("filing_date"),
                                    rs.getTimestamp("resolution_date")
                                });
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error loading pending complaints: " + ex.getMessage());
                        }
                    });
                    
                    // Resolve action
                    resolveButton.addActionListener(e -> {
                        int selectedRow = complaintTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(null, "Please select a complaint to resolve");
                            return;
                        }
                        
                        int complaintId = (Integer) complaintTable.getValueAt(selectedRow, 0);
                        try {
                            String sql = "UPDATE complaints SET status = 'Resolved', resolution_date = NOW() " +
                                       "WHERE complaint_id = ?";
                            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                            pstmt.setInt(1, complaintId);
                            
                            int result = pstmt.executeUpdate();
                            if (result > 0) {
                                refreshButton.doClick();
                                JOptionPane.showMessageDialog(null, "Complaint resolved successfully!");
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error resolving complaint: " + ex.getMessage());
                        }
                    });
                    
                    // Search action
                    searchButton.addActionListener(e -> {
                        String searchTerm = searchField.getText().trim().toLowerCase();
                        if (searchTerm.isEmpty()) {
                            showRoomManagementPanel();
                            return;
                        }
                        
                        for (int i = complaintModel.getRowCount() - 1; i >= 0; i--) {
                            boolean found = false;
                            for (int j = 0; j < complaintModel.getColumnCount(); j++) {
                                Object value = complaintModel.getValueAt(i, j);
                                if (value != null && value.toString().toLowerCase().contains(searchTerm)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                complaintModel.removeRow(i);
                            }
                        }
                    });
                    
                    // Add Delete action
                    deleteButton.addActionListener(e -> {
                        int selectedRow = complaintTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(null, "Please select a complaint to delete");
                            return;
                        }
                        
                        int complaintId = (Integer) complaintTable.getValueAt(selectedRow, 0);
                        String status = (String) complaintTable.getValueAt(selectedRow, 4);
                        
                        // Ask for confirmation before deleting
                        int confirm = JOptionPane.showConfirmDialog(null,
                            "Are you sure you want to delete this complaint?\nThis action cannot be undone.",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                            
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                String sql = "DELETE FROM complaints WHERE complaint_id = ?";
                                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                                pstmt.setInt(1, complaintId);
                                
                                int result = pstmt.executeUpdate();
                                if (result > 0) {
                                    refreshButton.doClick();
                                    JOptionPane.showMessageDialog(null, "Complaint deleted successfully!");
                                }
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(null, "Error deleting complaint: " + ex.getMessage());
                            }
                        }
                    });
                    
                    complaintPanel.add(controlPanel, BorderLayout.NORTH);
                    complaintPanel.add(scrollPane, BorderLayout.CENTER);
                    contentPanel.add(complaintPanel);
                    
                    // Initial load
                    refreshButton.doClick();
                }
                break;
            case "View My Details":
                if (currentUserRole.equals("student")) {
                    JPanel studentSelectionPanel = new JPanel(new BorderLayout(10, 10));
                    studentSelectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                    // Create top panel with instructions
                    JPanel topPanel = new JPanel(new BorderLayout());
                    JLabel instructionLabel = new JLabel("Select your Roll Number to view details:");
                    instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                    topPanel.add(instructionLabel, BorderLayout.NORTH);

                    // Create table model for students list
                    String[] columns = {"Roll No", "Name", "Department"};
                    DefaultTableModel model = new DefaultTableModel(columns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    JTable studentTable = new JTable(model);
                    studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    
                    // Load student data
                    try {
                        String sql = "SELECT roll_no, name, department FROM students ORDER BY roll_no";
                        PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery();
                        
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                rs.getString("roll_no"),
                                rs.getString("name"),
                                rs.getString("department")
                            });
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Error loading students: " + ex.getMessage());
                    }

                    // Add table to scroll pane
                    JScrollPane scrollPane = new JScrollPane(studentTable);
                    topPanel.add(scrollPane, BorderLayout.CENTER);

                    // Create view button
                    JButton viewButton = new JButton("View Details");
                    viewButton.addActionListener(e -> {
                        int selectedRow = studentTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(null, "Please select a student first");
                            return;
                        }

                        String selectedRollNo = (String) studentTable.getValueAt(selectedRow, 0);
                        currentUsername = selectedRollNo; // Update current username to selected student
                        
                        // Show student details
                        try {
                            String sql = "SELECT s.*, r.room_type, r.sharing_type, r.block_name, r.floor_no FROM students s " +
                                       "LEFT JOIN rooms r ON s.room_no = r.room_no " +
                                       "WHERE s.roll_no = ?";
                            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                            pstmt.setString(1, selectedRollNo);
                            ResultSet rs = pstmt.executeQuery();
                            
                            if (rs.next()) {
                                // Main container with padding
                                JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
                                mainContainer.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
                                mainContainer.setBackground(new Color(245, 245, 245));
                                
                                // Title panel
                                JPanel titlePanel = new JPanel();
                                titlePanel.setBackground(new Color(33, 150, 243));
                                titlePanel.setLayout(new BorderLayout());
                                JLabel titleLabel = new JLabel("Student Details", SwingConstants.CENTER);
                                titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                                titleLabel.setForeground(Color.WHITE);
                                titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                                titlePanel.add(titleLabel, BorderLayout.CENTER);
                                
                                // Content Panel with sections
                                JPanel contentPanel = new JPanel();
                                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                                contentPanel.setBackground(Color.WHITE);
                                contentPanel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                                    BorderFactory.createEmptyBorder(25, 30, 25, 30)
                                ));
                                
                                // Add sections
                                addDetailsSection(contentPanel, "Personal Information", new String[][] {
                                    {"Roll No:", rs.getString("roll_no")},
                                    {"Name:", rs.getString("name")},
                                    {"Age:", String.valueOf(rs.getInt("age"))},
                                    {"Contact:", rs.getString("mobile")}
                                });
                                
                                addDetailsSection(contentPanel, "Academic Information", new String[][] {
                                    {"College:", rs.getString("college")},
                                    {"Department:", rs.getString("department")},
                                    {"Semester:", rs.getString("semester")}
                                });
                                
                                addDetailsSection(contentPanel, "Room Information", new String[][] {
                                    {"Room No:", rs.getString("room_no")},
                                    {"Room Type:", rs.getString("room_type")},
                                    {"Sharing Type:", rs.getString("sharing_type")},
                                    {"Block:", rs.getString("block_name")},
                                    {"Floor:", String.valueOf(rs.getInt("floor_no"))}
                                });
                                
                                addDetailsSection(contentPanel, "Payment Information", new String[][] {
                                    {"Amount Paid:", "₹" + String.format("%.2f", rs.getDouble("amount_paid"))},
                                    {"Amount Due:", "₹" + String.format("%.2f", rs.getDouble("amount_due"))}
                                });
                                
                                // Add Meal Plan Information section
                                try {
                                    String mealPlanSql = "SELECT m.plan_type, m.start_date, m.end_date, m.payment_status " +
                                                        "FROM meal_plans m " +
                                                        "WHERE m.student_roll_no = ?";
                                    PreparedStatement mealPlanStmt = DatabaseManager.getConnection().prepareStatement(mealPlanSql);
                                    mealPlanStmt.setString(1, currentUsername);
                                    ResultSet mealPlanRs = mealPlanStmt.executeQuery();

                                    String[][] mealPlanDetails;
                                    if (mealPlanRs.next()) {
                                        mealPlanDetails = new String[][] {
                                            {"Plan Type", mealPlanRs.getString("plan_type")},
                                            {"Start Date", mealPlanRs.getDate("start_date").toString()},
                                            {"End Date", mealPlanRs.getDate("end_date").toString()},
                                            {"Payment Status", mealPlanRs.getString("payment_status")}
                                        };
                                    } else {
                                        mealPlanDetails = new String[][] {
                                            {"Status", "No Meal Plan Assigned/Selected Yet."}
                                        };
                                    }
                                    addDetailsSection(contentPanel, "Meal Plan Information", mealPlanDetails);
                                } catch (SQLException ex) {
                                    System.err.println("Error loading meal plan details: " + ex.getMessage());
                                    String[][] errorDetails = {{"Error", "No Meal Plan Assigned/Selected Yet."}};
                                    addDetailsSection(contentPanel, "Meal Plan Information", errorDetails);
                                }
                                
                                // Add Cafeteria Feedback button
                                JPanel feedbackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                feedbackPanel.setBackground(Color.WHITE);
                                JButton giveFeedbackButton = new JButton("Give Cafeteria Feedback");
                                giveFeedbackButton.setFont(new Font("Arial", Font.BOLD, 14));
                                giveFeedbackButton.setBackground(new Color(255, 165, 0)); // Orange color
                                giveFeedbackButton.setForeground(Color.BLACK); // Black text for contrast
                                giveFeedbackButton.setFocusPainted(false);
                                giveFeedbackButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                                giveFeedbackButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

                                giveFeedbackButton.addActionListener(feedbackEvent -> {
                                    JPanel feedbackInputPanel = new JPanel(new BorderLayout(10, 10));
                                    feedbackInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                                    JTextArea feedbackArea = new JTextArea(5, 30);
                                    feedbackArea.setLineWrap(true);
                                    feedbackArea.setWrapStyleWord(true);
                                    JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);

                                    feedbackInputPanel.add(new JLabel("Your Feedback:"), BorderLayout.NORTH);
                                    feedbackInputPanel.add(feedbackScrollPane, BorderLayout.CENTER);

                                    int result = JOptionPane.showConfirmDialog(
                                        mainPanel,
                                        feedbackInputPanel,
                                        "Cafeteria Feedback",
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.PLAIN_MESSAGE
                                    );

                                    if (result == JOptionPane.OK_OPTION) {
                                        String feedbackText = feedbackArea.getText().trim();
                                        if (!feedbackText.isEmpty()) {
                                            try {
                                                // First check if student has already given feedback today
                                                String checkSql = "SELECT COUNT(*) as count FROM meal_feedback " +
                                                                "WHERE student_roll_no = ? AND DATE(feedback_date) = CURRENT_DATE";
                                                PreparedStatement checkStmt = DatabaseManager.getConnection().prepareStatement(checkSql);
                                                checkStmt.setString(1, currentUsername);
                                                ResultSet checkRs = checkStmt.executeQuery();
                                                checkRs.next();
                                                int count = checkRs.getInt("count");
                                                
                                                if (count > 0) {
                                                    JOptionPane.showMessageDialog(mainPanel, "You have already submitted feedback today. Please wait until tomorrow to submit another feedback.");
                                                    return;
                                                }
                                                
                                                String feedbackSql = "INSERT INTO meal_feedback (student_roll_no, feedback_text, feedback_date) " +
                                                                    "VALUES (?, ?, CURRENT_TIMESTAMP)";
                                                PreparedStatement feedbackStmt = DatabaseManager.getConnection().prepareStatement(feedbackSql);
                                                feedbackStmt.setString(1, currentUsername);
                                                feedbackStmt.setString(2, feedbackText);
                                                feedbackStmt.executeUpdate();
                                                JOptionPane.showMessageDialog(mainPanel, "Thank you for your feedback!");
                                            } catch (SQLException ex) {
                                                JOptionPane.showMessageDialog(mainPanel, "Error submitting feedback: " + ex.getMessage());
                                            }
                                        } else {
                                            JOptionPane.showMessageDialog(mainPanel, "Please enter your feedback before submitting.");
                                        }
                                    }
                                });

                                feedbackPanel.add(giveFeedbackButton);
                                contentPanel.add(feedbackPanel);
                                
                                // Add back button
                                JButton backButton = new JButton("Back to Student List");
                                backButton.addActionListener(backEvent -> {
                                    contentPanel.removeAll();
                                    contentPanel.add(studentSelectionPanel);
                                    contentPanel.revalidate();
                                    contentPanel.repaint();
                                });
                                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                buttonPanel.setBackground(Color.WHITE);
                                buttonPanel.add(backButton);
                                contentPanel.add(buttonPanel);
                                
                                // Add to scroll pane
                                JScrollPane detailsScrollPane = new JScrollPane(contentPanel);
                                detailsScrollPane.setBorder(null);
                                detailsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
                                
                                // Add components to main container
                                mainContainer.add(titlePanel, BorderLayout.NORTH);
                                mainContainer.add(detailsScrollPane, BorderLayout.CENTER);
                                
                                // Update the main content panel
                                this.contentPanel.removeAll();
                                this.contentPanel.add(mainContainer);
                                this.contentPanel.revalidate();
                                this.contentPanel.repaint();
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error fetching student details: " + ex.getMessage());
                        }
                    });

                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonPanel.add(viewButton);
                    topPanel.add(buttonPanel, BorderLayout.SOUTH);

                    studentSelectionPanel.add(topPanel, BorderLayout.CENTER);
                    contentPanel.add(studentSelectionPanel);
                }
                break;
            case "File Complaint":
                if (currentUserRole.equals("student")) {
                    JPanel studentSelectionPanel = new JPanel(new BorderLayout(10, 10));
                    studentSelectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                    // Create top panel with instructions
                    JPanel topPanel = new JPanel(new BorderLayout());
                    JLabel instructionLabel = new JLabel("Select your Roll Number to file/view complaints:");
                    instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                    topPanel.add(instructionLabel, BorderLayout.NORTH);

                    // Create table model for students list
                    String[] columns = {"Roll No", "Name", "Department"};
                    DefaultTableModel model = new DefaultTableModel(columns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    JTable studentTable = new JTable(model);
                    studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    
                    // Load student data
                    try {
                        String sql = "SELECT roll_no, name, department FROM students ORDER BY roll_no";
                        PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery();
                        
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                rs.getString("roll_no"),
                                rs.getString("name"),
                                rs.getString("department")
                            });
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Error loading students: " + ex.getMessage());
                    }

                    // Add table to scroll pane
                    JScrollPane scrollPane = new JScrollPane(studentTable);
                    topPanel.add(scrollPane, BorderLayout.CENTER);

                    // Create continue button
                    JButton continueButton = new JButton("Continue");
                    continueButton.addActionListener(e -> {
                        int selectedRow = studentTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(null, "Please select a student first");
                            return;
                        }

                        String selectedRollNo = (String) studentTable.getValueAt(selectedRow, 0);
                        String selectedName = (String) studentTable.getValueAt(selectedRow, 1);
                        currentUsername = selectedRollNo; // Update current username to selected student

                        // Create complaint panel
                        JPanel complaintPanel = new JPanel(new BorderLayout(10, 10));
                        complaintPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        
                        // Selected student info panel
                        JPanel studentInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JLabel studentLabel = new JLabel("Filing complaint as: " + selectedName + " (" + selectedRollNo + ")");
                        studentLabel.setFont(new Font("Arial", Font.BOLD, 14));
                        studentInfoPanel.add(studentLabel);
                        
                        // Complaint form
                        JPanel formPanel = new JPanel(new GridBagLayout());
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        gbc.anchor = GridBagConstraints.WEST;
                        gbc.insets = new Insets(5, 5, 5, 5);
                        
                        JLabel titleLabel = new JLabel("New Complaint");
                        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
                        formPanel.add(titleLabel, gbc);
                        
                        gbc.gridy++;
                        JTextArea complaintText = new JTextArea(10, 40);
                        complaintText.setLineWrap(true);
                        complaintText.setWrapStyleWord(true);
                        JScrollPane textScrollPane = new JScrollPane(complaintText);
                        formPanel.add(textScrollPane, gbc);
                        
                        // Buttons panel
                        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JButton submitButton = new JButton("Submit Complaint");
                        JButton backButton = new JButton("Back to Student List");
                        
                        submitButton.addActionListener(submitEvent -> {
                            if (complaintText.getText().trim().isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Please enter your complaint");
                                return;
                            }
                            
                            try {
                                String sql = "INSERT INTO complaints (student_roll_no, complaint_text, status, filing_date) VALUES (?, ?, 'Pending', NOW())";
                                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                                pstmt.setString(1, selectedRollNo);
                                pstmt.setString(2, complaintText.getText().trim());
                                
                                int result = pstmt.executeUpdate();
                                if (result > 0) {
                                    JOptionPane.showMessageDialog(null, "Complaint filed successfully!");
                                    complaintText.setText("");
                                    loadComplaints(selectedRollNo, complaintPanel); // Refresh complaints list
                                }
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(null, "Error filing complaint: " + ex.getMessage());
                            }
                        });
                        
                        backButton.addActionListener(backEvent -> {
                            contentPanel.removeAll();
                            contentPanel.add(studentSelectionPanel);
                            contentPanel.revalidate();
                            contentPanel.repaint();
                        });
                        
                        buttonsPanel.add(submitButton);
                        buttonsPanel.add(backButton);
                        
                        // Main layout
                        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
                        mainPanel.add(studentInfoPanel, BorderLayout.NORTH);
                        mainPanel.add(formPanel, BorderLayout.CENTER);
                        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
                        
                        complaintPanel.add(mainPanel, BorderLayout.NORTH);
                        
                        // Load existing complaints
                        loadComplaints(selectedRollNo, complaintPanel);
                        
                        // Update the main content panel
                        contentPanel.removeAll();
                        contentPanel.add(complaintPanel);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    });

                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonPanel.add(continueButton);
                    topPanel.add(buttonPanel, BorderLayout.SOUTH);

                    studentSelectionPanel.add(topPanel, BorderLayout.CENTER);
                    contentPanel.add(studentSelectionPanel);
                }
                break;
            case "View Room Details":
                if (currentUserRole.equals("student")) {
                    if (currentUsername == null || currentUsername.isEmpty()) {
                        // Main panel with white background
                        JPanel guidancePanel = new JPanel(new BorderLayout(20, 20));
                        guidancePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
                        guidancePanel.setBackground(Color.WHITE);
                        
                        // Message panel with darker green border
                        JPanel messagePanel = new JPanel();
                        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
                        messagePanel.setBackground(Color.WHITE);
                        messagePanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
                            BorderFactory.createEmptyBorder(35, 40, 35, 40)
                        ));
                        
                        // Title with darker green color for better visibility
                        JLabel titleLabel = new JLabel("Please Select a Student First");
                        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
                        titleLabel.setForeground(new Color(0, 100, 0));
                        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
                        
                        // Instructions with darker text for better readability
                        JLabel messageLabel = new JLabel("<html><div style='text-align: center; width: 400px; color: #333333;'>" +
                            "<p style='margin: 10px 0; font-size: 16px;'>To view your room details, please follow these steps:</p>" +
                            "<ol style='margin-left: 30px; margin-top: 15px;'>" +
                            "<li style='margin: 8px 0;'>Click the button below to go to \"View My Details\"</li>" +
                            "<li style='margin: 8px 0;'>Select your student record</li>" +
                            "<li style='margin: 8px 0;'>Then return to \"View Room Details\"</li>" +
                            "</ol></div></html>");
                        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        
                        // Button with blue color scheme for better visibility
                        JButton goToDetailsButton = new JButton("Go to View My Details");
                        goToDetailsButton.setFont(new Font("Arial", Font.BOLD, 18));  // Larger font
                        goToDetailsButton.setBackground(new Color(220, 220, 220)); // Light gray background
                        goToDetailsButton.setForeground(Color.BLACK); // Black text
                        goToDetailsButton.setFocusPainted(false);
                        goToDetailsButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(40, 96, 144), 1),  // Darker blue border
                            BorderFactory.createEmptyBorder(15, 40, 15, 40)
                        ));
                        goToDetailsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                        goToDetailsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        
                        // Make button appear raised
                        goToDetailsButton.setBorderPainted(true);
                        goToDetailsButton.setContentAreaFilled(true);
                        goToDetailsButton.setOpaque(true); // Explicitly set opaque

                        // Request a repaint to ensure the background color is displayed
                        goToDetailsButton.revalidate();
                        goToDetailsButton.repaint();
                        
                        // Enhanced hover effect with brighter colors
                        goToDetailsButton.addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                goToDetailsButton.setBackground(new Color(180, 180, 180));  // Slightly darker gray on hover
                            }
                            public void mouseExited(MouseEvent e) {
                                goToDetailsButton.setBackground(new Color(220, 220, 220));  // Back to original light gray
                            }
                            public void mousePressed(MouseEvent e) {
                                goToDetailsButton.setBackground(new Color(180, 180, 180));  // Darker gray when pressed
                            }
                            public void mouseReleased(MouseEvent e) {
                                goToDetailsButton.setBackground(new Color(220, 220, 220));  // Back to original
                            }
                        });
                        
                        goToDetailsButton.addActionListener(e -> handleMenuAction("View My Details"));
                        
                        // Add components with proper spacing
                        messagePanel.add(titleLabel);
                        messagePanel.add(Box.createRigidArea(new Dimension(0, 20)));
                        messagePanel.add(messageLabel);
                        messagePanel.add(Box.createRigidArea(new Dimension(0, 35)));
                        messagePanel.add(goToDetailsButton);
                        
                        // Revalidate and repaint the container to ensure button is drawn correctly
                        messagePanel.revalidate();
                        messagePanel.repaint();
                        
                        // Center the message panel
                        guidancePanel.add(messagePanel, BorderLayout.CENTER);
                        contentPanel.add(guidancePanel);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                        return;
                    }
                    
                    try {
                        String sql = "SELECT r.*, " +
                                   "(SELECT COUNT(*) FROM students s2 WHERE s2.room_no = r.room_no) as current_occupancy " +
                                   "FROM rooms r " +
                                   "JOIN students s ON r.room_no = s.room_no " +
                                   "WHERE s.roll_no = ?";
                        PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                        pstmt.setString(1, currentUsername);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            // Main container with padding
                            JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
                            mainContainer.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
                            mainContainer.setBackground(new Color(245, 245, 245));
                            
                            // Title panel
                            JPanel titlePanel = new JPanel();
                            titlePanel.setBackground(new Color(46, 125, 50)); // Material Green
                            titlePanel.setLayout(new BorderLayout());
                            JLabel titleLabel = new JLabel("Room Details", SwingConstants.CENTER);
                            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                            titleLabel.setForeground(Color.WHITE);
                            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                            titlePanel.add(titleLabel, BorderLayout.CENTER);
                            
                            // Content Panel
                            JPanel contentPanel = new JPanel();
                            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                            contentPanel.setBackground(Color.WHITE);
                            contentPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                                BorderFactory.createEmptyBorder(25, 30, 25, 30)
                            ));
                            
                            // Add Room Information section
                            addDetailsSection(contentPanel, "Room Information", new String[][] {
                                {"Room Number:", rs.getString("room_no")},
                                {"Room Type:", rs.getString("room_type")},
                                {"Sharing Type:", rs.getString("sharing_type")},
                                {"Block:", rs.getString("block_name")},
                                {"Floor:", String.valueOf(rs.getInt("floor_no"))},
                                {"Capacity:", String.valueOf(rs.getInt("capacity"))},
                                {"Current Occupancy:", String.valueOf(rs.getInt("current_occupancy"))}
                            });
                            
                            // Add Roommates section
                            JPanel roommatesPanel = new JPanel();
                            roommatesPanel.setLayout(new BoxLayout(roommatesPanel, BoxLayout.Y_AXIS));
                            roommatesPanel.setBackground(Color.WHITE);
                            
                            // Section header
                            JPanel headerPanel = new JPanel(new BorderLayout());
                            headerPanel.setBackground(Color.WHITE);
                            JLabel headerLabel = new JLabel("Roommate Information");
                            headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
                            headerLabel.setForeground(new Color(33, 33, 33));
                            headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                            headerPanel.add(headerLabel, BorderLayout.CENTER);
                            roommatesPanel.add(headerPanel);
                            
                            // Get roommates
                            String roommateSql = "SELECT name, roll_no, department, semester FROM students WHERE room_no = ? AND roll_no != ? ORDER BY name";
                            PreparedStatement rmStmt = DatabaseManager.getConnection().prepareStatement(roommateSql);
                            rmStmt.setString(1, rs.getString("room_no"));
                            rmStmt.setString(2, currentUsername);
                            ResultSet rmRs = rmStmt.executeQuery();
                            
                            boolean hasRoommates = false;
                            while (rmRs.next()) {
                                hasRoommates = true;
                                JPanel roommatePanel = new JPanel(new BorderLayout());
                                roommatePanel.setBackground(Color.WHITE);
                                
                                String roommateInfo = String.format("<html>• %s (%s)<br/>&nbsp;&nbsp;&nbsp;%s, Semester %s</html>",
                                    rmRs.getString("name"),
                                    rmRs.getString("roll_no"),
                                    rmRs.getString("department"),
                                    rmRs.getString("semester")
                                );
                                
                                JLabel roommateLabel = new JLabel(roommateInfo);
                                roommateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                                roommateLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
                                roommatePanel.add(roommateLabel, BorderLayout.CENTER);
                                
                                // Add hover effect
                                roommatePanel.addMouseListener(new MouseAdapter() {
                                    public void mouseEntered(MouseEvent e) {
                                        roommatePanel.setBackground(new Color(245, 245, 245));
                                    }
                                    public void mouseExited(MouseEvent e) {
                                        roommatePanel.setBackground(Color.WHITE);
                                    }
                                });
                                
                                roommatesPanel.add(roommatePanel);
                            }
                            
                            if (!hasRoommates) {
                                JLabel noRoommatesLabel = new JLabel("No roommates currently assigned");
                                noRoommatesLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                                noRoommatesLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
                                roommatesPanel.add(noRoommatesLabel);
                            }
                            
                            contentPanel.add(roommatesPanel);
                            
                            // Add to scroll pane
                            JScrollPane scrollPane = new JScrollPane(contentPanel);
                            scrollPane.setBorder(null);
                            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                            
                            // Add components to main container
                            mainContainer.add(titlePanel, BorderLayout.NORTH);
                            mainContainer.add(scrollPane, BorderLayout.CENTER);
                            
                            this.contentPanel.add(mainContainer);
                        }
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this, "Error fetching room details: " + e.getMessage());
                    }
                }
                break;
            case "Hostel Rules":
                // Main container with padding
                JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
                mainContainer.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
                
                // Title Panel
                JPanel titlePanel = new JPanel();
                titlePanel.setBackground(new Color(70, 130, 180)); // Steel blue color
                JLabel titleLabel = new JLabel("Hostel Rules and Regulations");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
                titleLabel.setForeground(Color.WHITE);
                titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                titlePanel.add(titleLabel);
                
                // Rules Panel
                JPanel rulesPanel = new JPanel();
                rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
                rulesPanel.setBackground(Color.WHITE);
                rulesPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(20, 25, 20, 25)
                ));
                
                // Rules sections
                String[][] sections = {
                    {
                        "1. Entry/Exit Timing",
                        "• Entry closes at 10:00 PM\n" +
                        "• No entry/exit allowed between 10:00 PM to 6:00 AM"
                    },
                    {
                        "2. Room Types and Pricing",
                        "• Luxury Rooms (Total Cost: ₹60,000)\n" +
                        "  - 1 Sharing: ₹60,000 per person\n" +
                        "  - 2 Sharing: ₹30,000 per person\n" +
                        "  - 4 Sharing: ₹15,000 per person\n" +
                        "• Standard Rooms (Total Cost: ₹40,000)\n" +
                        "  - 1 Sharing: ₹40,000 per person\n" +
                        "  - 2 Sharing: ₹20,000 per person\n" +
                        "  - 4 Sharing: ₹10,000 per person\n" +
                        "• Room cost is divided equally among occupants\n" +
                        "• All rooms include basic furniture and utilities"
                    },
                    {
                        "3. Room Maintenance",
                        "• Keep rooms clean and tidy\n" +
                        "• No modifications to room structure\n" +
                        "• Report damages immediately"
                    },
                    {
                        "4. Visitors",
                        "• Visitors allowed only in common areas\n" +
                        "• Visiting hours: 9:00 AM to 6:00 PM"
                    },
                    {
                        "5. Common Areas",
                        "• Maintain cleanliness\n" +
                        "• Follow noise regulations\n" +
                        "• Respect other residents"
                    },
                    {
                        "6. Safety",
                        "• No prohibited items\n" +
                        "• Follow fire safety guidelines\n" +
                        "• Report suspicious activities"
                    },
                    {
                        "7. Payments",
                        "• Pay hostel fees on time\n" +
                        "• Late payment penalties apply\n" +
                        "• Security deposit required\n" +
                        "• Refunds processed after room inspection"
                    },
                    {
                        "8. Discipline",
                        "• Maintain proper conduct\n" +
                        "• Follow hostel guidelines\n" +
                        "• Respect staff and fellow residents"
                    }
                };
                
                for (String[] section : sections) {
                    // Section Panel
                    JPanel sectionPanel = new JPanel();
                    sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
                    sectionPanel.setBackground(Color.WHITE);
                    sectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                    sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    // Section Title
                    JLabel sectionTitle = new JLabel(section[0]);
                    sectionTitle.setFont(new Font("Arial", Font.BOLD, 16));
                    sectionTitle.setForeground(new Color(70, 130, 180));
                    sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                    sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    // Section Content
                    JTextArea content = new JTextArea(section[1]);
                    content.setFont(new Font("Arial", Font.PLAIN, 14));
                    content.setForeground(new Color(51, 51, 51));
                    content.setBackground(Color.WHITE);
                    content.setEditable(false);
                    content.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 0));
                    content.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    // Add hover effect with a light background
                    sectionPanel.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            sectionPanel.setBackground(new Color(240, 248, 255)); // Alice Blue
                            content.setBackground(new Color(240, 248, 255));
                        }
                        public void mouseExited(MouseEvent e) {
                            sectionPanel.setBackground(Color.WHITE);
                            content.setBackground(Color.WHITE);
                        }
                    });
                    
                    // Add components to section panel
                    sectionPanel.add(sectionTitle);
                    sectionPanel.add(content);
                    
                    // Add separator except for the last section
                    if (!section[0].startsWith("8")) {
                        JSeparator separator = new JSeparator();
                        separator.setForeground(new Color(200, 200, 200));
                        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
                        sectionPanel.add(separator);
                    }
                    
                    rulesPanel.add(sectionPanel);
                }
                
                // Add components to main container
                mainContainer.add(titlePanel, BorderLayout.NORTH);
                
                // Wrap rules panel in scroll pane
                JScrollPane scrollPane = new JScrollPane(rulesPanel);
                scrollPane.setBorder(null);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.setBackground(Color.WHITE);
                
                mainContainer.add(scrollPane, BorderLayout.CENTER);
                contentPanel.add(mainContainer);
                break;
            case "Cafeteria Management":
                if (currentUserRole.equals("admin")) {
                    try {
                        // Create required tables if they don't exist
                        Statement stmt = DatabaseManager.getConnection().createStatement();
                        
                        // Create meal_plans table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS meal_plans (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "student_roll_no VARCHAR(20), " +
                            "plan_type ENUM('Basic', 'Standard', 'Premium'), " +
                            "start_date DATE, " +
                            "end_date DATE, " +
                            "payment_status ENUM('Pending', 'Paid') DEFAULT 'Pending', " +
                            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, " +
                            "FOREIGN KEY (plan_type) REFERENCES meal_plan_details(plan_type))"
                        );
                        
                        // Create daily_menu table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS daily_menu (" +
                            "menu_date DATE PRIMARY KEY, " +
                            "breakfast TEXT, " +
                            "lunch TEXT, " +
                            "dinner TEXT)"
                        );
                        
                        // Create meal_feedback table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS meal_feedback (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "student_roll_no VARCHAR(20), " +
                            "feedback_text TEXT, " +
                            "feedback_date DATE, " +
                            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, " +
                            "FOREIGN KEY (plan_type) REFERENCES meal_plan_details(plan_type))"
                        );
                        
                        // Show cafeteria management panel
                        showCafeteriaManagementPanel();
                        
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error setting up cafeteria management: " + ex.getMessage());
                    }
                }
                break;
            case "View Meal Plan":
                if (currentUserRole.equals("student")) {
                    try {
                        // Create required tables if they don't exist (same as above)
                        Statement stmt = DatabaseManager.getConnection().createStatement();
                        
                        // Create meal_plans table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS meal_plans (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "student_roll_no VARCHAR(20), " +
                            "plan_type ENUM('Basic', 'Standard', 'Premium'), " +
                            "start_date DATE, " +
                            "end_date DATE, " +
                            "payment_status ENUM('Pending', 'Paid') DEFAULT 'Pending', " +
                            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, " +
                            "FOREIGN KEY (plan_type) REFERENCES meal_plan_details(plan_type))"
                        );
                        
                        // Create daily_menu table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS daily_menu (" +
                            "menu_date DATE PRIMARY KEY, " +
                            "breakfast TEXT, " +
                            "lunch TEXT, " +
                            "dinner TEXT)"
                        );
                        
                        // Create meal_feedback table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS meal_feedback (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "student_roll_no VARCHAR(20), " +
                            "feedback_text TEXT, " +
                            "feedback_date DATE, " +
                            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, " +
                            "FOREIGN KEY (plan_type) REFERENCES meal_plan_details(plan_type))"
                        );
                        
                        // Show student meal plan panel
                        showStudentMealPlanPanel();
                        
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error setting up meal plan view: " + ex.getMessage());
                    }
                }
                break;
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void handleLogout() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (response == JOptionPane.YES_OPTION) {
            dispose();
            // Create and show login frame
            JFrame loginFrame = new JFrame("Hostel Management System - Login");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setContentPane(new LoginContentPanel());
            
            // Set size and position
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screenRect = ge.getMaximumWindowBounds();
            loginFrame.setSize(screenRect.width, screenRect.height);
            loginFrame.setLocation(screenRect.x, screenRect.y);
            loginFrame.setVisible(true);
        }
    }

    // Helper method to display complaints in the text area
    private void displayComplaints(List<Map<String, Object>> complaints, JTextArea textArea) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID | Student | Roll No | Complaint | Status | Filed On | Resolved On\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        
        if (complaints.isEmpty()) {
            sb.append("No complaints found.\n");
        } else {
            for (Map<String, Object> complaint : complaints) {
                sb.append(String.format("%-3d | %-7s | %-8s | %-9s | %-6s | %-8s | %s\n",
                    complaint.get("id"),
                    complaint.get("studentName"),
                    complaint.get("rollNo"),
                    ((String)complaint.get("text")).substring(0, Math.min(9, ((String)complaint.get("text")).length())),
                    complaint.get("status"),
                    complaint.get("filedOn"),
                    complaint.get("resolvedOn") != null ? complaint.get("resolvedOn") : "Not resolved"));
            }
        }
        
        textArea.setText(sb.toString());
    }

    // Helper method to display rooms in the text area
    private void displayRooms(List<Map<String, Object>> rooms, JTextArea textArea) {
        StringBuilder sb = new StringBuilder();
        sb.append("Room No | Type     | Capacity | Occupied | Block    | Floor\n");
        sb.append("----------------------------------------------------------------\n");
        
        if (rooms.isEmpty()) {
            sb.append("No rooms found.\n");
        } else {
            for (Map<String, Object> room : rooms) {
                sb.append(String.format("%-8s | %-9s | %-8d | %-9d | %-9s | %-5d\n",
                    room.get("roomNo"),
                    room.get("type"),
                    room.get("capacity"),
                    room.get("currentOccupancy"),
                    room.get("block"),
                    room.get("floor")));
            }
        }
        
        textArea.setText(sb.toString());
    }

    // Helper method to display wardens in the text area
    private void displayWardens(List<Map<String, Object>> wardens, JTextArea textArea) {
        StringBuilder sb = new StringBuilder();
        sb.append("Warden ID | Name         | Age | Mobile       | Hostel      | Block    | Joining Date\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        
        if (wardens.isEmpty()) {
            sb.append("No wardens found.\n");
        } else {
            for (Map<String, Object> warden : wardens) {
                sb.append(String.format("%-9s | %-11s | %-3d | %-11s | %-11s | %-8s | %s\n",
                    warden.get("id"),
                    warden.get("name"),
                    warden.get("age"),
                    warden.get("mobile"),
                    warden.get("hostel"),
                    warden.get("block"),
                    warden.get("joiningDate")));
            }
        }
        
        textArea.setText(sb.toString());
    }

    // Add this helper method at the end of the class
    private void refreshStudentTable(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT s.*, r.room_type, r.sharing_type, r.block_name, r.floor_no " +
                        "FROM students s " +
                        "LEFT JOIN rooms r ON s.room_no = r.room_no " +
                        "ORDER BY s.roll_no";
            Statement stmt = DatabaseManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("college"),
                    rs.getString("department"),
                    rs.getString("semester"),
                    rs.getInt("age"),
                    rs.getString("mobile"),
                    rs.getString("room_no"),
                    rs.getString("room_type"),
                    rs.getString("sharing_type"),
                    rs.getString("block_name"),
                    rs.getInt("floor_no"),
                    String.format("₹%.0f", rs.getDouble("amount_paid")),
                    String.format("₹%.0f", rs.getDouble("amount_due")),
                    rs.getString("payment_method"),
                    rs.getString("password")
                });
            }
            
            // Update room occupancy counts
            String updateOccupancy = "UPDATE rooms r SET current_occupancy = (" +
                                   "SELECT COUNT(*) FROM students s WHERE s.room_no = r.room_no)";
            stmt.executeUpdate(updateOccupancy);
            
            // Refresh room table if it exists
            if (roomTableModel != null) {
                refreshRoomTable(roomTableModel);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error refreshing table: " + e.getMessage());
        }
    }

    private void refreshRoomTable(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT * FROM rooms ORDER BY room_no";
            Statement stmt = DatabaseManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("room_no"),
                    rs.getString("room_type"),
                    rs.getString("sharing_type"),
                    rs.getInt("capacity"),
                    rs.getInt("current_occupancy"),
                    rs.getString("block_name"),
                    rs.getInt("floor_no")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error refreshing room table: " + e.getMessage());
        }
    }

    // Calculate room cost using the centralized method
    private double calculateRoomCost(String roomType, String sharingType) {
        return DatabaseManager.calculateRoomFee(roomType, sharingType);
    }

    // Add validation method
    private String validateFields(String rollNo, String name, String college, 
                               String department, String semester, String age, 
                               String mobile, String roomNo, String password, 
                               String amountPaid) {
        if (rollNo.isEmpty() || name.isEmpty() || college.isEmpty() || 
            department.isEmpty() || semester.isEmpty() || age.isEmpty() || 
            mobile.isEmpty() || roomNo.isEmpty() || password.isEmpty() || 
            amountPaid.isEmpty()) {
            return "All fields are required. Please fill in all the details.";
        }

        // Validate mobile number (exactly 10 digits)
        if (!mobile.matches("\\d{10}")) {
            return "Mobile number must be exactly 10 digits.";
        }

        // Validate age (must be 16+)
        try {
            int ageValue = Integer.parseInt(age);
            if (ageValue < 16) {
                return "Student must be at least 16 years old.";
            }
        } catch (NumberFormatException e) {
            return "Please enter a valid age.";
        }

        // Validate amount paid (must be a valid number)
        try {
            double amount = Double.parseDouble(amountPaid);
            if (amount < 0) {
                return "Amount paid cannot be negative.";
            }
        } catch (NumberFormatException e) {
            return "Please enter a valid amount paid.";
        }

        return null; // null means validation passed
    }

    // Helper method to add section headers
    private void addSectionHeader(JPanel panel, String text, GridBagConstraints gbc) {
        gbc.gridy++;
        JLabel header = new JLabel(text);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(new Color(51, 51, 51));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        
        GridBagConstraints headerGbc = (GridBagConstraints) gbc.clone();
        headerGbc.gridwidth = 2;
        headerGbc.insets = new Insets(15, 10, 8, 10);
        panel.add(header, headerGbc);
        gbc.gridy++;
    }
    
    // Helper method to add detail rows
    private void addDetailRow(JPanel panel, String label, String value, GridBagConstraints gbc) {
        // Label
        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Arial", Font.BOLD, 14));
        lblField.setForeground(new Color(102, 102, 102));
        panel.add(lblField, gbc);
        
        // Value
        gbc.gridx = 1;
        JLabel lblValue = new JLabel(value != null ? value : "N/A");
        lblValue.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblValue, gbc);
        
        // Reset for next row
        gbc.gridx = 0;
        gbc.gridy++;
    }

    // Helper method to add a section of details
    private void addDetailsSection(JPanel container, String title, String[][] details) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(Color.WHITE);
        sectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Section header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(new Color(33, 33, 33));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        sectionPanel.add(headerPanel);
        
        // Add details
        for (String[] detail : details) {
            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setBackground(Color.WHITE);
            
            JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            labelPanel.setBackground(Color.WHITE);
            JLabel label = new JLabel(detail[0]);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setForeground(new Color(95, 99, 104));
            labelPanel.add(label);
            
            JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            valuePanel.setBackground(Color.WHITE);
            JLabel value = new JLabel(detail[1] != null ? detail[1] : "N/A");
            value.setFont(new Font("Arial", Font.PLAIN, 14));
            valuePanel.add(value);
            
            rowPanel.add(labelPanel, BorderLayout.WEST);
            rowPanel.add(valuePanel, BorderLayout.CENTER);
            
            // Add hover effect
            rowPanel.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    rowPanel.setBackground(new Color(245, 245, 245));
                    labelPanel.setBackground(new Color(245, 245, 245));
                    valuePanel.setBackground(new Color(245, 245, 245));
                }
                public void mouseExited(MouseEvent e) {
                    rowPanel.setBackground(Color.WHITE);
                    labelPanel.setBackground(Color.WHITE);
                    valuePanel.setBackground(Color.WHITE);
                }
            });
            
            sectionPanel.add(rowPanel);
        }
        
        container.add(sectionPanel);
    }

    // Add this helper method at the end of the class
    private void loadComplaints(String rollNo, JPanel container) {
        try {
            String sql = "SELECT DISTINCT complaint_id, complaint_text, status, filing_date, resolution_date " +
                        "FROM complaints WHERE student_roll_no = ? " +
                        "ORDER BY filing_date DESC";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            // Create table model
            String[] columns = {"ID", "Complaint", "Status", "Filed On", "Resolved On"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("complaint_id"),
                    rs.getString("complaint_text"),
                    rs.getString("status"),
                    rs.getTimestamp("filing_date"),
                    rs.getTimestamp("resolution_date")
                });
            }
            
            // Create table
            JTable complaintsTable = new JTable(model);
            complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(300);
            
            // Create panel for previous complaints
            JPanel previousPanel = new JPanel(new BorderLayout(10, 10));
            previousPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            
            JLabel previousLabel = new JLabel("Previous Complaints");
            previousLabel.setFont(new Font("Arial", Font.BOLD, 16));
            previousPanel.add(previousLabel, BorderLayout.NORTH);
            
            JScrollPane tableScrollPane = new JScrollPane(complaintsTable);
            previousPanel.add(tableScrollPane, BorderLayout.CENTER);
            
            // Add to container
            container.add(previousPanel, BorderLayout.CENTER);
            container.revalidate();
            container.repaint();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error loading complaints: " + ex.getMessage());
        }
    }

    private void showRoomManagementPanel() {
        contentPanel.removeAll();
        
        // Create room table model
        String[] columns = {"Room No", "Type", "Sharing", "Capacity", "Occupied", "Block", "Floor"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Initialize room table
        JTable roomTable = new JTable(model);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Refresh room data with current occupancy
        try {
            String sql = "SELECT r.*, " +
                        "(SELECT COUNT(*) FROM students s WHERE s.room_no = r.room_no) as actual_occupancy " +
                        "FROM rooms r ORDER BY r.room_no";
            Statement stmt = DatabaseManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
                while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("room_no"),
                        rs.getString("room_type"),
                        rs.getString("sharing_type"),
                        rs.getInt("capacity"),
                    rs.getInt("actual_occupancy"),
                        rs.getString("block_name"),
                        rs.getInt("floor_no")
                    });
                
                // Update the current_occupancy in rooms table
                String updateSql = "UPDATE rooms SET current_occupancy = ? WHERE room_no = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(updateSql);
                pstmt.setInt(1, rs.getInt("actual_occupancy"));
                pstmt.setString(2, rs.getString("room_no"));
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage());
        }

        // Add buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Room");
        JButton updateButton = new JButton("Update Room");
        JButton vacateButton = new JButton("Vacant Room");
        JButton historyButton = new JButton("Room History");
        
        // Add search field
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(vacateButton);
        buttonsPanel.add(historyButton);
        buttonsPanel.add(new JLabel("Search:"));
        buttonsPanel.add(searchField);
        buttonsPanel.add(searchButton);

        // Add action listeners
        historyButton.addActionListener(e -> showRoomHistory());
        refreshButton.addActionListener(e -> showRoomManagementPanel());
        
        addButton.addActionListener(e -> {
            JPanel addRoomPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            
            JTextField roomNoField = new JTextField();
            JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"Standard", "Luxury"});
            JComboBox<String> sharingTypeBox = new JComboBox<>(new String[]{"4-sharing", "2-sharing", "1-sharing"});
            JTextField blockNameField = new JTextField();
            JTextField floorNoField = new JTextField();
            
            addRoomPanel.add(new JLabel("Room Number:*"));
            addRoomPanel.add(roomNoField);
            addRoomPanel.add(new JLabel("Room Type:*"));
            addRoomPanel.add(roomTypeBox);
            addRoomPanel.add(new JLabel("Sharing Type:*"));
            addRoomPanel.add(sharingTypeBox);
            addRoomPanel.add(new JLabel("Block Name:*"));
            addRoomPanel.add(blockNameField);
            addRoomPanel.add(new JLabel("Floor Number:*"));
            addRoomPanel.add(floorNoField);
            
            int result = JOptionPane.showConfirmDialog(this, addRoomPanel, 
                "Add New Room", JOptionPane.OK_CANCEL_OPTION);
                
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String roomNo = roomNoField.getText().trim();
                    String roomType = (String) roomTypeBox.getSelectedItem();
                    String sharingType = (String) sharingTypeBox.getSelectedItem();
                    String blockName = blockNameField.getText().trim();
                    String floorNo = floorNoField.getText().trim();
                    
                    if (roomNo.isEmpty() || blockName.isEmpty() || floorNo.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "All fields are required!");
                        return;
                    }
                    
                    // Get capacity from sharing type
                    int capacity;
                    switch(sharingType) {
                        case "4-sharing":
                            capacity = 4;
                            break;
                        case "2-sharing":
                            capacity = 2;
                            break;
                        case "1-sharing":
                            capacity = 1;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid sharing type");
                    }
                    
                    // Insert new room
                    String sql = "INSERT INTO rooms (room_no, room_type, sharing_type, capacity, current_occupancy, floor_no, block_name) " +
                               "VALUES (?, ?, ?, ?, 0, ?, ?)";
                    PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                    pstmt.setString(1, roomNo);
                    pstmt.setString(2, roomType);
                    pstmt.setString(3, sharingType);
                    pstmt.setInt(4, capacity);
                    pstmt.setInt(5, Integer.parseInt(floorNo));
                    pstmt.setString(6, blockName);
                    
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Room added successfully!");
                    showRoomManagementPanel(); // Refresh the panel
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding room: " + ex.getMessage());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Floor number must be a valid integer!");
                }
            }
        });
        
        // Update Room button action listener
        updateButton.addActionListener(e -> {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a room to update");
                return;
            }
            
            String roomNo = (String) model.getValueAt(selectedRow, 0);
            String currentRoomType = (String) model.getValueAt(selectedRow, 1);
            String currentSharingType = (String) model.getValueAt(selectedRow, 2);
            String currentBlock = (String) model.getValueAt(selectedRow, 5);
            int currentFloor = (int) model.getValueAt(selectedRow, 6);
            
            JPanel updateRoomPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            
            JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"Standard", "Luxury"});
            roomTypeBox.setSelectedItem(currentRoomType);
            
            JComboBox<String> sharingTypeBox = new JComboBox<>(new String[]{"4-sharing", "2-sharing", "1-sharing"});
            sharingTypeBox.setSelectedItem(currentSharingType);
            
            JTextField blockNameField = new JTextField(currentBlock);
            JTextField floorNoField = new JTextField(String.valueOf(currentFloor));
            
            updateRoomPanel.add(new JLabel("Room Type:"));
            updateRoomPanel.add(roomTypeBox);
            updateRoomPanel.add(new JLabel("Sharing Type:"));
            updateRoomPanel.add(sharingTypeBox);
            updateRoomPanel.add(new JLabel("Block Name:"));
            updateRoomPanel.add(blockNameField);
            updateRoomPanel.add(new JLabel("Floor Number:"));
            updateRoomPanel.add(floorNoField);
            
            int result = JOptionPane.showConfirmDialog(this, updateRoomPanel, 
                        "Update Room", JOptionPane.OK_CANCEL_OPTION);
                        
                    if (result == JOptionPane.OK_OPTION) {
                try {
                    String newRoomType = (String) roomTypeBox.getSelectedItem();
                    String newSharingType = (String) sharingTypeBox.getSelectedItem();
                    String newBlockName = blockNameField.getText().trim();
                    String newFloorNo = floorNoField.getText().trim();
                    
                    if (newBlockName.isEmpty() || newFloorNo.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Block name and floor number cannot be empty!");
                        return;
                    }
                    
                    // Get new capacity from sharing type
                    int newCapacity;
                    switch(newSharingType) {
                        case "4-sharing":
                            newCapacity = 4;
                            break;
                        case "2-sharing":
                            newCapacity = 2;
                            break;
                        case "1-sharing":
                            newCapacity = 1;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid sharing type");
                    }
                    
                    // Check if new capacity is less than current occupancy
                    int currentOccupancy = (int) model.getValueAt(selectedRow, 4);
                    if (newCapacity < currentOccupancy) {
                        JOptionPane.showMessageDialog(this, 
                            "Cannot reduce capacity below current occupancy (" + currentOccupancy + " students)");
                        return;
                    }
                    
                    // Update room
                    String sql = "UPDATE rooms SET room_type = ?, sharing_type = ?, capacity = ?, " +
                                "block_name = ?, floor_no = ? WHERE room_no = ?";
                    PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                    pstmt.setString(1, newRoomType);
                    pstmt.setString(2, newSharingType);
                    pstmt.setInt(3, newCapacity);
                    pstmt.setString(4, newBlockName);
                    pstmt.setInt(5, Integer.parseInt(newFloorNo));
                    pstmt.setString(6, roomNo);
                    
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Room updated successfully!");
                    showRoomManagementPanel(); // Refresh the panel
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating room: " + ex.getMessage());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Floor number must be a valid integer!");
                }
            }
        });
        
        vacateButton.addActionListener(e -> {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a room to vacate");
                return;
            }
            
            String roomNo = (String) model.getValueAt(selectedRow, 0);
            String roomType = (String) model.getValueAt(selectedRow, 1);
            int currentOccupancy = (int) model.getValueAt(selectedRow, 4);
            
            if (currentOccupancy == 0) {
                JOptionPane.showMessageDialog(this, "Room is already vacant");
                return;
            }

            try {
                // Get students in the room
                String sql = "SELECT roll_no, name FROM students WHERE room_no = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                pstmt.setString(1, roomNo);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    String studentId = rs.getString("roll_no");
                    String studentName = rs.getString("name");
                    
                    // Ask for reason first
            String reason = JOptionPane.showInputDialog(this, 
                        "Enter reason for vacating (e.g., Semester End, Transfer Request, etc.):",
                        "Vacating Reason",
                JOptionPane.QUESTION_MESSAGE);
            
            if (reason == null || reason.trim().isEmpty()) {
                        reason = "No reason provided";
                    }
                    
                    // Ask if student should be transferred
                    int choice = JOptionPane.showConfirmDialog(this,
                        "Do you want to transfer student " + studentName + " (ID: " + studentId + ") to another room?",
                        "Transfer Student",
                JOptionPane.YES_NO_OPTION);
                
                    if (choice == JOptionPane.YES_OPTION) {
                        // Show available rooms for transfer
                        String availableRoomsSql = 
                            "SELECT r.room_no, r.room_type, r.sharing_type, r.capacity, r.current_occupancy, r.block_name, r.floor_no " +
                            "FROM rooms r " +
                            "WHERE r.room_no != ? AND r.current_occupancy < r.capacity " +
                            "ORDER BY r.room_no";
                            
                        PreparedStatement availRoomsPstmt = DatabaseManager.getConnection().prepareStatement(availableRoomsSql);
                        availRoomsPstmt.setString(1, roomNo);
                        ResultSet availRoomsRs = availRoomsPstmt.executeQuery();
                        
                        // Create room selection dialog
                        JDialog roomDialog = new JDialog(this, "Select New Room", true);
                        JPanel roomPanel = new JPanel(new BorderLayout());
                        
                        String[] roomColumns = {"Room No", "Type", "Sharing", "Capacity", "Occupied", "Block", "Floor", "Price Difference"};
                        DefaultTableModel roomModel = new DefaultTableModel(roomColumns, 0);
                        JTable roomSelectionTable = new JTable(roomModel);
                        
                        while (availRoomsRs.next()) {
                            String newRoomType = availRoomsRs.getString("room_type");
                            double priceDiff = calculatePriceDifference(roomType, newRoomType);
                            String priceDiffStr = String.format("₹%.2f", priceDiff);
                            
                            roomModel.addRow(new Object[]{
                                availRoomsRs.getString("room_no"),
                                newRoomType,
                                availRoomsRs.getString("sharing_type"),
                                availRoomsRs.getInt("capacity"),
                                availRoomsRs.getInt("current_occupancy"),
                                availRoomsRs.getString("block_name"),
                                availRoomsRs.getInt("floor_no"),
                                priceDiffStr
                            });
                        }
                        
                        roomPanel.add(new JScrollPane(roomSelectionTable), BorderLayout.CENTER);
                        
                        JPanel buttonPanel = new JPanel();
                        JButton selectButton = new JButton("Select");
                        JButton cancelButton = new JButton("Cancel");
                        buttonPanel.add(selectButton);
                        buttonPanel.add(cancelButton);
                        roomPanel.add(buttonPanel, BorderLayout.SOUTH);
                        
                        final String[] selectedNewRoom = {null};
                        final double[] selectedPriceDiff = {0.0};
                        
                        selectButton.addActionListener(evt -> {
                            int selectedRoomRow = roomSelectionTable.getSelectedRow();
                            if (selectedRoomRow != -1) {
                                selectedNewRoom[0] = (String) roomModel.getValueAt(selectedRoomRow, 0);
                                String priceDiffStr = ((String) roomModel.getValueAt(selectedRoomRow, 7)).replace("₹", "");
                                selectedPriceDiff[0] = Double.parseDouble(priceDiffStr);
                                roomDialog.dispose();
                            } else {
                                JOptionPane.showMessageDialog(roomDialog, "Please select a room");
                            }
                        });
                        
                        cancelButton.addActionListener(evt -> {
                            selectedNewRoom[0] = null;
                            roomDialog.dispose();
                        });
                        
                        roomDialog.setContentPane(roomPanel);
                        roomDialog.setSize(800, 400);
                        roomDialog.setLocationRelativeTo(this);
                        roomDialog.setVisible(true);
                        
                        // Process room transfer if a room was selected
                        if (selectedNewRoom[0] != null) {
                            // Handle price difference
                            if (selectedPriceDiff[0] > 0) {
                                int paymentChoice = JOptionPane.showConfirmDialog(this,
                                    String.format("Additional payment of ₹%.2f is required for the upgrade. Collect payment?",
                                        selectedPriceDiff[0]),
                                    "Payment Required",
                                    JOptionPane.YES_NO_OPTION);
                                        
                                if (paymentChoice != JOptionPane.YES_OPTION) {
                                    continue; // Skip to next student if payment not confirmed
                                }
                            } else if (selectedPriceDiff[0] < 0) {
                                JOptionPane.showMessageDialog(this,
                                    String.format("Note: Student is eligible for a refund of ₹%.2f",
                                        -selectedPriceDiff[0]));
                            }
                            
                            // Close the current room history entry
                            String updateHistorySql = 
                                "UPDATE room_history " +
                                "SET check_out = CURRENT_TIMESTAMP " +
                                "WHERE student_id = ? AND room_no = ? AND check_out IS NULL";
                            PreparedStatement updateHistoryPstmt = DatabaseManager.getConnection().prepareStatement(updateHistorySql);
                            updateHistoryPstmt.setString(1, studentId);
                            updateHistoryPstmt.setString(2, roomNo);
                            updateHistoryPstmt.executeUpdate();
                            
                            // Update student's room
                            String updateStudentSql = "UPDATE students SET room_no = ? WHERE roll_no = ?";
                            PreparedStatement updateStudentPstmt = DatabaseManager.getConnection().prepareStatement(updateStudentSql);
                            updateStudentPstmt.setString(1, selectedNewRoom[0]);
                            updateStudentPstmt.setString(2, studentId);
                            updateStudentPstmt.executeUpdate();
                            
                            // Create new room history entry
                            String insertHistorySql = 
                                "INSERT INTO room_history " +
                                "(student_id, room_no, room_type, sharing_type, block, floor, reason) " +
                                "SELECT ?, r.room_no, r.room_type, r.sharing_type, r.block_name, r.floor_no, ? " +
                                "FROM rooms r WHERE r.room_no = ?";
                            PreparedStatement insertHistoryPstmt = DatabaseManager.getConnection().prepareStatement(insertHistorySql);
                            insertHistoryPstmt.setString(1, studentId);
                            insertHistoryPstmt.setString(2, reason + " (Transfer)");
                            insertHistoryPstmt.setString(3, selectedNewRoom[0]);
                            insertHistoryPstmt.executeUpdate();
                            
                            // Update room occupancy counts
                            String updateOccupancySql = 
                                "UPDATE rooms r SET r.current_occupancy = " +
                                "(SELECT COUNT(*) FROM students s WHERE s.room_no = r.room_no) " +
                                "WHERE r.room_no IN (?, ?)";
                            PreparedStatement updateOccupancyPstmt = DatabaseManager.getConnection().prepareStatement(updateOccupancySql);
                            updateOccupancyPstmt.setString(1, roomNo);
                            updateOccupancyPstmt.setString(2, selectedNewRoom[0]);
                            updateOccupancyPstmt.executeUpdate();
                            
                            JOptionPane.showMessageDialog(this, "Room transfer completed successfully!");
                        }
                    } else {
                        // Close the current room history entry
                        String updateHistorySql = 
                            "UPDATE room_history " +
                            "SET check_out = CURRENT_TIMESTAMP " +
                            "WHERE student_id = ? AND room_no = ? AND check_out IS NULL";
                        PreparedStatement updateHistoryPstmt = DatabaseManager.getConnection().prepareStatement(updateHistorySql);
                        updateHistoryPstmt.setString(1, studentId);
                        updateHistoryPstmt.setString(2, roomNo);
                        updateHistoryPstmt.executeUpdate();
                        
                        // If not transferring, just remove the student
                        String updateStudentSql = "UPDATE students SET room_no = NULL WHERE roll_no = ?";
                        PreparedStatement updateStudentPstmt = DatabaseManager.getConnection().prepareStatement(updateStudentSql);
                        updateStudentPstmt.setString(1, studentId);
                        updateStudentPstmt.executeUpdate();
                        
                        // Update room occupancy
                        String updateOccupancySql = 
                            "UPDATE rooms r SET r.current_occupancy = " +
                            "(SELECT COUNT(*) FROM students s WHERE s.room_no = r.room_no) " +
                            "WHERE r.room_no = ?";
                        PreparedStatement updateOccupancyPstmt = DatabaseManager.getConnection().prepareStatement(updateOccupancySql);
                        updateOccupancyPstmt.setString(1, roomNo);
                        updateOccupancyPstmt.executeUpdate();
                        
                        JOptionPane.showMessageDialog(this, "Student removed from room successfully!");
                    }
                }
                
                // Refresh the table
                showRoomManagementPanel();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error processing room vacancy: " + ex.getMessage());
            }
        });
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add to main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.NORTH);  // Changed from SOUTH to NORTH
            
        contentPanel.add(mainPanel);
    }
    
    private void showCafeteriaManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create tabbed pane for different sections
        JTabbedPane tabbedPane = new JTabbedPane();

        // ====== Plan Details Panel ======
        JPanel planDetailsPanel = new JPanel(new BorderLayout(10, 10));
        
        // Add "Edit Plans" button at the top
        JPanel planButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editPlansButton = new JButton("Edit Plan Details");
        planButtonPanel.add(editPlansButton);
        planDetailsPanel.add(planButtonPanel, BorderLayout.NORTH);

        // Add scrollable plan details
        JPanel plansPanel = new JPanel();
        plansPanel.setLayout(new BoxLayout(plansPanel, BoxLayout.Y_AXIS));
        plansPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add plan details (initially from hardcoded values)
        addPlanDetails(plansPanel);

        // Edit Plans Button Action
        editPlansButton.addActionListener(e -> {
            JPanel editPanel = new JPanel(new BorderLayout(10, 10));
            
            // Create tabbed pane for different plans
            JTabbedPane planTabs = new JTabbedPane();
            
            // Basic Plan Tab
            JPanel basicPanel = createPlanEditPanel("Basic", 3000);
            planTabs.addTab("Basic Plan", basicPanel);
            
            // Standard Plan Tab
            JPanel standardPanel = createPlanEditPanel("Standard", 4500);
            planTabs.addTab("Standard Plan", standardPanel);
            
            // Premium Plan Tab
            JPanel premiumPanel = createPlanEditPanel("Premium", 6000);
            planTabs.addTab("Premium Plan", premiumPanel);
            
            editPanel.add(planTabs, BorderLayout.CENTER);
            
            // Save Button
            JButton saveButton = new JButton("Save Changes");
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(saveButton);
            editPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            // Show dialog
            JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(mainPanel), "Edit Meal Plans", true);
            dialog.setContentPane(editPanel);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(mainPanel);
            
            // Save button action
            saveButton.addActionListener(saveEvent -> {
                // Save changes to database
                try {
                    for (int i = 0; i < planTabs.getTabCount(); i++) {
                        JPanel planPanel = (JPanel)planTabs.getComponentAt(i);
                        String planType = planTabs.getTitleAt(i).replace(" Plan", "");
                        savePlanDetails(planPanel, planType);
                    }
                    
                    // Refresh plan details display
                    plansPanel.removeAll();
                    addPlanDetails(plansPanel);
                    plansPanel.revalidate();
                    plansPanel.repaint();
                    
                    dialog.dispose();
                    JOptionPane.showMessageDialog(mainPanel, "Plan details updated successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Error saving plan details: " + ex.getMessage());
                }
            });
            
            dialog.setVisible(true);
        });

        JScrollPane plansScrollPane = new JScrollPane(plansPanel);
        plansScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        planDetailsPanel.add(plansScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("Meal Plans", planDetailsPanel);

        // ====== Assignment Panel ======
        JPanel assignmentPanel = new JPanel(new BorderLayout(10, 10));
        
        // Create table model for assigned meal plans
        String[] columns = {"Student Roll No", "Name", "Plan Type", "Price", "Start Date", "End Date", "Payment Status", "Payment Method", "Payment Details"};
        DefaultTableModel mealPlanModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable mealPlanTable = new JTable(mealPlanModel);
        mealPlanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(mealPlanTable);

        // Control panel for buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton assignButton = new JButton("Assign Meal Plan");
        JButton updateButton = new JButton("Update Plan");
        JButton menuButton = new JButton("Set Daily Menu");
        JButton viewFeedbackButton = new JButton("View Feedback");
        JButton refreshButton = new JButton("Refresh");

        controlPanel.add(assignButton);
        controlPanel.add(updateButton);
        controlPanel.add(menuButton);
        controlPanel.add(viewFeedbackButton);
        controlPanel.add(refreshButton);

        // Assign Meal Plan Action
        assignButton.addActionListener(e -> {
            try {
                // Get list of students without meal plans
                String sql = "SELECT roll_no, name FROM students WHERE roll_no NOT IN (SELECT student_roll_no FROM meal_plans)";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                List<String> students = new ArrayList<>();
                Map<String, String> studentNames = new HashMap<>();
                while (rs.next()) {
                    String rollNo = rs.getString("roll_no");
                    students.add(rollNo + " - " + rs.getString("name"));
                    studentNames.put(rollNo, rs.getString("name"));
                }

                if (students.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "All students already have meal plans assigned!");
                    return;
                }

                // Create assignment panel
                JPanel assignPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                assignPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JComboBox<String> studentCombo = new JComboBox<>(students.toArray(new String[0]));
                JComboBox<String> planCombo = new JComboBox<>(new String[]{"Basic", "Standard", "Premium"});
                JTextField startDateField = new JTextField();
                
                // Create months combo box
                String[] monthOptions = new String[12];
                for (int i = 0; i < 12; i++) {
                    monthOptions[i] = (i + 1) + " month" + (i > 0 ? "s" : "");
                }
                JComboBox<String> monthsCombo = new JComboBox<>(monthOptions);
                
                // End date label (read-only)
                JLabel endDateLabel = new JLabel("End date will be calculated");
                endDateLabel.setForeground(Color.GRAY);
                
                JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Card", "UPI"});
                JPanel upiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField upiIdField = new JTextField(15);
                upiPanel.add(new JLabel("UPI ID:"));
                upiPanel.add(upiIdField);
                upiPanel.setVisible(false);

                JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField cardNumberField = new JTextField(16);
                JTextField expiryField = new JTextField(5);
                JTextField cvvField = new JTextField(3);
                cardPanel.add(new JLabel("Card Number:"));
                cardPanel.add(cardNumberField);
                cardPanel.add(new JLabel("Expiry (MM/YY):"));
                cardPanel.add(expiryField);
                cardPanel.add(new JLabel("CVV:"));
                cardPanel.add(cvvField);
                cardPanel.setVisible(false);

                paymentMethodCombo.addActionListener(e2 -> {
                    String method = (String) paymentMethodCombo.getSelectedItem();
                    upiPanel.setVisible("UPI".equals(method));
                    cardPanel.setVisible("Card".equals(method));
                });

                assignPanel.add(upiPanel);
                assignPanel.add(cardPanel);

                // Add amount label that updates when plan type or months change
                JLabel totalAmountLabel = new JLabel("Enter start date to see total amount");
                totalAmountLabel.setForeground(Color.GRAY);

                // Update end date and amount when start date changes or months selected
                DocumentListener startDateListener = new DocumentListener() {
                    private void updateDates() {
                        try {
                            if (startDateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                                LocalDate startDate = LocalDate.parse(startDateField.getText());
                                int selectedMonths = monthsCombo.getSelectedIndex() + 1;
                                LocalDate endDate = startDate.plusMonths(selectedMonths);
                                
                                // Calculate total amount
                                double monthlyPrice = getPlanPrice((String)planCombo.getSelectedItem());
                                double totalPrice = monthlyPrice * selectedMonths;
                                
                                endDateLabel.setText(endDate.toString());
                                endDateLabel.setForeground(Color.BLACK);
                                totalAmountLabel.setText(String.format("Amount to Pay: ₹%.2f for %d month%s", 
                                    totalPrice, selectedMonths, selectedMonths > 1 ? "s" : ""));
                                totalAmountLabel.setForeground(Color.BLACK);
                            } else {
                                endDateLabel.setText("Enter valid start date (YYYY-MM-DD)");
                                endDateLabel.setForeground(Color.RED);
                                totalAmountLabel.setText("Enter valid start date");
                                totalAmountLabel.setForeground(Color.RED);
                            }
                        } catch (Exception ex) {
                            endDateLabel.setText("Enter valid start date (YYYY-MM-DD)");
                            endDateLabel.setForeground(Color.RED);
                        }
                    }

                    public void insertUpdate(DocumentEvent e) { updateDates(); }
                    public void removeUpdate(DocumentEvent e) { updateDates(); }
                    public void changedUpdate(DocumentEvent e) { updateDates(); }
                };

                startDateField.getDocument().addDocumentListener(startDateListener);

                // Update amount when plan type or months change
                ActionListener updateAmountListener = evt -> {
                    try {
                        if (startDateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                            LocalDate startDate = LocalDate.parse(startDateField.getText());
                            int selectedMonths = monthsCombo.getSelectedIndex() + 1;
                            LocalDate endDate = startDate.plusMonths(selectedMonths);
                            
                            double monthlyPrice = getPlanPrice((String)planCombo.getSelectedItem());
                            double totalPrice = monthlyPrice * selectedMonths;
                            
                            endDateLabel.setText(endDate.toString());
                            endDateLabel.setForeground(Color.BLACK);
                            totalAmountLabel.setText(String.format("Amount to Pay: ₹%.2f for %d month%s", 
                                totalPrice, selectedMonths, selectedMonths > 1 ? "s" : ""));
                            totalAmountLabel.setForeground(Color.BLACK);
                        }
                    } catch (Exception ex) {
                        // Ignore if start date isn't valid yet
                    }
                };

                planCombo.addActionListener(updateAmountListener);
                monthsCombo.addActionListener(updateAmountListener);

                assignPanel.add(new JLabel("Student:"));
                assignPanel.add(studentCombo);
                assignPanel.add(new JLabel("Plan Type:"));
                assignPanel.add(planCombo);
                assignPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
                assignPanel.add(startDateField);
                assignPanel.add(new JLabel("Duration:"));
                assignPanel.add(monthsCombo);
                assignPanel.add(new JLabel("End Date:"));
                assignPanel.add(endDateLabel);
                assignPanel.add(new JLabel("Payment Method:"));
                assignPanel.add(paymentMethodCombo);
                assignPanel.add(totalAmountLabel);
                assignPanel.add(new JLabel()); // Empty label for grid alignment

                int result = JOptionPane.showConfirmDialog(mainPanel, assignPanel,
                    "Assign Meal Plan", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    // Validate start date
                    if (!startDateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                        JOptionPane.showMessageDialog(mainPanel, "Please enter valid start date in YYYY-MM-DD format!");
                        return;
                    }

                    LocalDate startDate = LocalDate.parse(startDateField.getText());
                    LocalDate today = LocalDate.now();

                    // Validate date logic
                    if (startDate.isBefore(today)) {
                        JOptionPane.showMessageDialog(mainPanel, "Start date cannot be in the past!");
                        return;
                    }

                    int selectedMonths = monthsCombo.getSelectedIndex() + 1;
                    LocalDate endDate = startDate.plusMonths(selectedMonths);
                    double monthlyPrice = getPlanPrice((String)planCombo.getSelectedItem());
                    double totalPrice = monthlyPrice * selectedMonths;

                    String selectedStudent = studentCombo.getSelectedItem().toString().split(" - ")[0];
                    String planType = planCombo.getSelectedItem().toString();

                    // Confirm payment with total amount
                    int confirmPayment = JOptionPane.showConfirmDialog(mainPanel,
                        String.format("Confirm full payment of ₹%.2f for %d month%s received via %s?",
                            totalPrice, selectedMonths, selectedMonths > 1 ? "s" : "", paymentMethodCombo.getSelectedItem()),
                        "Confirm Payment",
                        JOptionPane.YES_NO_OPTION);

                    if (confirmPayment == JOptionPane.YES_OPTION) {
                        // Validate payment details
                        String paymentMethod = paymentMethodCombo.getSelectedItem().toString();
                        String newPaymentDetails = "";
                        
                        if (!validatePaymentDetails(paymentMethod, 
                            upiIdField.getText(), 
                            cardNumberField.getText(), 
                            expiryField.getText(), 
                            cvvField.getText())) {
                            return;
                        }
                        
                        // Set payment details based on method
                        switch (paymentMethod) {
                            case "UPI":
                                newPaymentDetails = upiIdField.getText();
                                break;
                            case "Card":
                                newPaymentDetails = String.format("%s|%s|%s", 
                                    cardNumberField.getText(), 
                                    expiryField.getText(), 
                                    cvvField.getText());
                                break;
                            case "Cash":
                                newPaymentDetails = "Cash Payment";
                                break;
                        }

                        // Insert meal plan with Paid status
                        String insertSql = "INSERT INTO meal_plans (student_roll_no, plan_type, start_date, end_date, payment_status, payment_method, payment_details, amount_paid_for_plan) " + // Added amount_paid_for_plan
                                         "VALUES (?, ?, ?, ?, 'Paid', ?, ?, ?)"; // Added placeholder for amount_paid_for_plan
                        PreparedStatement insertStmt = DatabaseManager.getConnection().prepareStatement(insertSql);
                        insertStmt.setString(1, selectedStudent);
                        insertStmt.setString(2, planType);
                        insertStmt.setString(3, startDate.toString());
                        insertStmt.setString(4, endDate.toString());
                        insertStmt.setString(5, paymentMethod);
                        insertStmt.setString(6, newPaymentDetails);
                        insertStmt.setDouble(7, totalPrice); // Set the calculated total price here
                        insertStmt.executeUpdate();

                        refreshMealPlanTable(mealPlanModel);
                        JOptionPane.showMessageDialog(mainPanel, "Meal plan assigned successfully!");
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "Meal plan assignment cancelled - Payment not confirmed");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Error assigning meal plan: " + ex.getMessage());
            }
        });

        // Update Plan Action
        updateButton.addActionListener(e -> {
            int selectedRow = mealPlanTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select a meal plan to update");
                return;
            }

            String rollNo = (String) mealPlanTable.getValueAt(selectedRow, 0);
            try {
                String sql = "SELECT * FROM meal_plans WHERE student_roll_no = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                pstmt.setString(1, rollNo);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    JPanel updatePanel = new JPanel(new GridLayout(0, 2, 5, 5));
                    updatePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JComboBox<String> planCombo = new JComboBox<>(new String[]{"Basic", "Standard", "Premium"});
                    planCombo.setSelectedItem(rs.getString("plan_type"));
                    JTextField startDateField = new JTextField(rs.getDate("start_date").toString());
                    JTextField endDateField = new JTextField(rs.getDate("end_date").toString());
                    JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "Paid"});
                    statusCombo.setSelectedItem(rs.getString("payment_status"));
                    
                    // Add payment method and details fields
                    JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Card", "UPI"});
                    paymentMethodCombo.setSelectedItem(rs.getString("payment_method"));
                    
                    JPanel upiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JTextField upiIdField = new JTextField(15);
                    upiPanel.add(new JLabel("UPI ID:"));
                    upiPanel.add(upiIdField);
                    
                    JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JTextField cardNumberField = new JTextField(16);
                    JTextField expiryField = new JTextField(5);
                    JTextField cvvField = new JTextField(3);
                    cardPanel.add(new JLabel("Card Number:"));
                    cardPanel.add(cardNumberField);
                    cardPanel.add(new JLabel("Expiry (MM/YY):"));
                    cardPanel.add(expiryField);
                    cardPanel.add(new JLabel("CVV:"));
                    cardPanel.add(cvvField);
                    
                    // Parse existing payment details
                    String paymentDetails = rs.getString("payment_details");
                    if (paymentDetails != null && !paymentDetails.isEmpty()) {
                        String currentMethod = rs.getString("payment_method");
                        if ("UPI".equals(currentMethod)) {
                            upiIdField.setText(paymentDetails);
                        } else if ("Card".equals(currentMethod)) {
                            String[] parts = paymentDetails.split("\\|");
                            if (parts.length == 3) {
                                cardNumberField.setText(parts[0]);
                                expiryField.setText(parts[1]);
                                cvvField.setText(parts[2]);
                            }
                        }
                    }
                    
                    // Show/hide payment panels based on method
                    paymentMethodCombo.addActionListener(e2 -> {
                        String method = (String) paymentMethodCombo.getSelectedItem();
                        upiPanel.setVisible("UPI".equals(method));
                        cardPanel.setVisible("Card".equals(method));
                    });
                    
                    // Set initial visibility
                    upiPanel.setVisible("UPI".equals(paymentMethodCombo.getSelectedItem()));
                    cardPanel.setVisible("Card".equals(paymentMethodCombo.getSelectedItem()));

                    updatePanel.add(new JLabel("Plan Type:"));
                    updatePanel.add(planCombo);
                    updatePanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
                    updatePanel.add(startDateField);
                    updatePanel.add(new JLabel("End Date (YYYY-MM-DD):"));
                    updatePanel.add(endDateField);
                    updatePanel.add(new JLabel("Payment Status:"));
                    updatePanel.add(statusCombo);
                    updatePanel.add(new JLabel("Payment Method:"));
                    updatePanel.add(paymentMethodCombo);
                    updatePanel.add(upiPanel);
                    updatePanel.add(cardPanel);

                    int result = JOptionPane.showConfirmDialog(mainPanel, updatePanel,
                        "Update Meal Plan", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        // Validate payment details
                        String paymentMethod = paymentMethodCombo.getSelectedItem().toString();
                        String newPaymentDetails = "";
                        
                        if (!validatePaymentDetails(paymentMethod, 
                            upiIdField.getText(), 
                            cardNumberField.getText(), 
                            expiryField.getText(), 
                            cvvField.getText())) {
                            return;
                        }
                        
                        // Set payment details based on method
                        switch (paymentMethod) {
                            case "UPI":
                                newPaymentDetails = upiIdField.getText();
                                break;
                            case "Card":
                                newPaymentDetails = String.format("%s|%s|%s", 
                                    cardNumberField.getText(), 
                                    expiryField.getText(), 
                                    cvvField.getText());
                                break;
                            case "Cash":
                                newPaymentDetails = "Cash Payment";
                                break;
                        }

                        String updateSql = "UPDATE meal_plans SET plan_type = ?, start_date = ?, end_date = ?, " +
                                         "payment_status = ?, payment_method = ?, payment_details = ? WHERE student_roll_no = ?";
                        PreparedStatement updateStmt = DatabaseManager.getConnection().prepareStatement(updateSql);
                        updateStmt.setString(1, planCombo.getSelectedItem().toString());
                        updateStmt.setString(2, startDateField.getText());
                        updateStmt.setString(3, endDateField.getText());
                        updateStmt.setString(4, statusCombo.getSelectedItem().toString());
                        updateStmt.setString(5, paymentMethod);
                        updateStmt.setString(6, newPaymentDetails);
                        updateStmt.setString(7, rollNo);

                        updateStmt.executeUpdate();
                        refreshMealPlanTable(mealPlanModel);
                        JOptionPane.showMessageDialog(mainPanel, "Meal plan updated successfully!");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Error updating meal plan: " + ex.getMessage());
            }
        });

        // Set Daily Menu Action
        menuButton.addActionListener(e -> {
            JPanel menuPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField dateField = new JTextField();
            JTextArea breakfastArea = new JTextArea(4, 30);
            JTextArea lunchArea = new JTextArea(4, 30);
            JTextArea dinnerArea = new JTextArea(4, 30);

            breakfastArea.setLineWrap(true);
            lunchArea.setLineWrap(true);
            dinnerArea.setLineWrap(true);

            menuPanel.add(new JLabel("Date (YYYY-MM-DD):"));
            menuPanel.add(dateField);
            menuPanel.add(new JLabel("Breakfast Menu:"));
            menuPanel.add(new JScrollPane(breakfastArea));
            menuPanel.add(new JLabel("Lunch Menu:"));
            menuPanel.add(new JScrollPane(lunchArea));
            menuPanel.add(new JLabel("Dinner Menu:"));
            menuPanel.add(new JScrollPane(dinnerArea));

            int result = JOptionPane.showConfirmDialog(mainPanel, menuPanel,
                "Set Daily Menu", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    if (!dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                        JOptionPane.showMessageDialog(mainPanel, "Please enter a valid date!");
                        return;
                    }

                    String sql = "INSERT INTO daily_menu (menu_date, breakfast, lunch, dinner) VALUES (?, ?, ?, ?) " +
                               "ON DUPLICATE KEY UPDATE breakfast = VALUES(breakfast), lunch = VALUES(lunch), dinner = VALUES(dinner)";
                    PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                    pstmt.setString(1, dateField.getText());
                    pstmt.setString(2, breakfastArea.getText());
                    pstmt.setString(3, lunchArea.getText());
                    pstmt.setString(4, dinnerArea.getText());

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(mainPanel, "Daily menu updated successfully!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Error setting daily menu: " + ex.getMessage());
                }
            }
        });

        // View Feedback Action
        viewFeedbackButton.addActionListener(e -> {
            try {
                String sql = "SELECT f.*, s.name FROM meal_feedback f " +
                           "JOIN students s ON f.student_roll_no = s.roll_no " +
                           "ORDER BY f.feedback_date DESC";
                Statement stmt = DatabaseManager.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                DefaultTableModel feedbackModel = new DefaultTableModel(
                    new String[]{"Date", "Student", "Feedback"}, 0
                ) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                while (rs.next()) {
                    feedbackModel.addRow(new Object[]{
                        rs.getDate("feedback_date"),
                        rs.getString("name") + " (" + rs.getString("student_roll_no") + ")",
                        rs.getString("feedback_text")
                    });
                }

                JTable feedbackTable = new JTable(feedbackModel);
                feedbackTable.getColumnModel().getColumn(2).setPreferredWidth(300);
                JScrollPane scrollPane = new JScrollPane(feedbackTable);
                scrollPane.setPreferredSize(new Dimension(600, 400));

                JOptionPane.showMessageDialog(mainPanel, scrollPane, "Student Feedback", JOptionPane.PLAIN_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Error loading feedback: " + ex.getMessage());
            }
        });

        // Refresh button action
        refreshButton.addActionListener(e -> refreshMealPlanTable(mealPlanModel));

        assignmentPanel.add(controlPanel, BorderLayout.NORTH);
        assignmentPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("Manage Assignments", assignmentPanel);

        // Add tabbed pane to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Initial load of meal plans
        refreshMealPlanTable(mealPlanModel);

        contentPanel.removeAll();
        contentPanel.add(mainPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createPlanEditPanel(String planType, double basePrice) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Price field
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.add(new JLabel("Price (₹):"));
        JTextField priceField = new JTextField(String.valueOf(basePrice), 10);
        pricePanel.add(priceField);
        panel.add(pricePanel);

        // Features
        JPanel featuresPanel = new JPanel(new BorderLayout());
        featuresPanel.setBorder(BorderFactory.createTitledBorder("Features"));
        JTextArea featuresArea = new JTextArea(10, 40);
        featuresArea.setLineWrap(true);
        featuresArea.setWrapStyleWord(true);
        try {
            String sql = "SELECT features FROM meal_plan_details WHERE plan_type = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, planType);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                featuresArea.setText(rs.getString("features"));
            } else {
                featuresArea.setText(getDefaultFeatures(planType));
            }
        } catch (SQLException ex) {
            featuresArea.setText(getDefaultFeatures(planType));
        }
        featuresPanel.add(new JScrollPane(featuresArea), BorderLayout.CENTER);
        panel.add(featuresPanel);

        // Sample Menu
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Sample Menu"));
        JTextArea menuArea = new JTextArea(10, 40);
        menuArea.setLineWrap(true);
        menuArea.setWrapStyleWord(true);
        try {
            String sql = "SELECT sample_menu FROM meal_plan_details WHERE plan_type = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, planType);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                menuArea.setText(rs.getString("sample_menu"));
            } else {
                menuArea.setText(getDefaultMenu(planType));
            }
        } catch (SQLException ex) {
            menuArea.setText(getDefaultMenu(planType));
        }
        menuPanel.add(new JScrollPane(menuArea), BorderLayout.CENTER);
        panel.add(menuPanel);

        // Store references for robust access
        panel.putClientProperty("priceField", priceField);
        panel.putClientProperty("featuresArea", featuresArea);
        panel.putClientProperty("menuArea", menuArea);

        return panel;
    }

    private void savePlanDetails(JPanel planPanel, String planType) throws SQLException {
        // Get components by reference
        JTextField priceField = (JTextField) planPanel.getClientProperty("priceField");
        JTextArea featuresArea = (JTextArea) planPanel.getClientProperty("featuresArea");
        JTextArea menuArea = (JTextArea) planPanel.getClientProperty("menuArea");

        // Save to database
        String sql = "INSERT INTO meal_plan_details (plan_type, price, features, sample_menu) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE price = VALUES(price), features = VALUES(features), sample_menu = VALUES(sample_menu)";
        PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, planType);
        pstmt.setDouble(2, Double.parseDouble(priceField.getText().trim()));
        pstmt.setString(3, featuresArea.getText().trim());
        pstmt.setString(4, menuArea.getText().trim());
        pstmt.executeUpdate();
    }

    private String getDefaultFeatures(String planType) {
        switch (planType) {
            case "Basic":
                return "• 3 meals per day (Breakfast, Lunch, Dinner)\n" +
                       "• Standard vegetarian menu\n" +
                       "• Basic salad bar access\n" +
                       "• Regular tea/coffee\n" +
                       "• Fixed meal timings\n" +
                       "• Water dispenser access";
            case "Standard":
                return "• 3 meals per day + Evening snacks\n" +
                       "• Veg and Non-veg options (twice a week)\n" +
                       "• Extended salad bar access\n" +
                       "• Premium tea/coffee varieties\n" +
                       "• Flexible meal timings\n" +
                       "• Water dispenser access\n" +
                       "• Weekend special meals\n" +
                       "• Take-away option available";
            case "Premium":
                return "• 3 meals per day + Evening snacks + Night canteen access\n" +
                       "• Premium veg and non-veg options (daily)\n" +
                       "• Full salad bar access with premium items\n" +
                       "• All beverage varieties available\n" +
                       "• 24/7 meal timing flexibility\n" +
                       "• Water dispenser access\n" +
                       "• Daily special meals\n" +
                       "• Take-away option available\n" +
                       "• Monthly special dining events\n" +
                       "• Special diet options available\n" +
                       "• Personalized meal planning";
            default:
                return "";
        }
    }

    private String getDefaultMenu(String planType) {
        switch (planType) {
            case "Basic":
                return "Breakfast:\n" +
                       "- Poha/Upma/Idli/Dosa\n" +
                       "- Tea or Coffee\n" +
                       "- Bread and Butter\n\n" +
                       "Lunch:\n" +
                       "- Rice and Dal\n" +
                       "- 2 Rotis\n" +
                       "- 1 Vegetable Curry\n" +
                       "- Basic Salad\n\n" +
                       "Dinner:\n" +
                       "- Rice or Khichdi\n" +
                       "- 2 Rotis\n" +
                       "- 1 Vegetable Curry\n" +
                       "- Basic Salad";
            case "Standard":
                return "Breakfast:\n" +
                       "- All Basic Plan items PLUS:\n" +
                       "- Eggs to order\n" +
                       "- Cornflakes/Oats\n" +
                       "- Fresh Fruits\n" +
                       "- Multiple beverage options\n\n" +
                       "Lunch:\n" +
                       "- Rice and Dal\n" +
                       "- 3 Rotis\n" +
                       "- 2 Vegetable Curries\n" +
                       "- Non-veg (twice a week)\n" +
                       "- Extended Salad Bar\n" +
                       "- Dessert\n\n" +
                       "Evening Snacks:\n" +
                       "- Tea/Coffee\n" +
                       "- Biscuits/Samosa/Pakoda\n\n" +
                       "Dinner:\n" +
                       "- Rice Varieties\n" +
                       "- 3 Rotis\n" +
                       "- 2 Curries\n" +
                       "- Non-veg (twice a week)\n" +
                       "- Extended Salad Bar\n" +
                       "- Dessert (occasionally)";
            case "Premium":
                return "Breakfast:\n" +
                       "- All Standard Plan items PLUS:\n" +
                       "- Premium cereals variety\n" +
                       "- Multiple egg preparations\n" +
                       "- Fresh juice counter\n" +
                       "- Premium fruits\n" +
                       "- Specialty breads\n" +
                       "- Live cooking station\n\n" +
                       "Lunch:\n" +
                       "- Multiple Rice varieties\n" +
                       "- Unlimited Rotis\n" +
                       "- 3 Vegetable Curries\n" +
                       "- Daily Non-veg option\n" +
                       "- Premium Salad Bar\n" +
                       "- Live counter items\n" +
                       "- Dessert varieties\n\n" +
                       "Evening Snacks:\n" +
                       "- Premium Tea/Coffee\n" +
                       "- Multiple snack options\n" +
                       "- Fresh juice/shakes\n\n" +
                       "Dinner:\n" +
                       "- Multiple Rice varieties\n" +
                       "- Unlimited Rotis\n" +
                       "- 3 Curries\n" +
                       "- Daily Non-veg option\n" +
                       "- Premium Salad Bar\n" +
                       "- Live counter items\n" +
                       "- Dessert varieties\n\n" +
                       "Night Canteen Access:\n" +
                       "- Light meals\n" +
                       "- Beverages\n" +
                       "- Healthy snacks";
            default:
                return "";
        }
    }

    private void addPlanDetails(JPanel container) {
        String[] planTypes = {"Basic", "Standard", "Premium"};
        for (String planType : planTypes) {
            try {
                String sql = "SELECT price, features, sample_menu FROM meal_plan_details WHERE plan_type = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                pstmt.setString(1, planType);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    String features = rs.getString("features");
                    String menu = rs.getString("sample_menu");
                    addMealPlanPanel(container, planType, price, features, menu);
                } else {
                    // fallback to defaults if not found
                    addMealPlanPanel(container, planType, getPlanPrice(planType), getDefaultFeatures(planType), getDefaultMenu(planType));
                }
            } catch (SQLException ex) {
                // fallback to defaults if error
                addMealPlanPanel(container, planType, getPlanPrice(planType), getDefaultFeatures(planType), getDefaultMenu(planType));
            }
        }
    }

    private void addMealPlanPanel(JPanel container, String planType, double price, String features, String menu) {
        JPanel planPanel = new JPanel();
        planPanel.setLayout(new BoxLayout(planPanel, BoxLayout.Y_AXIS));
        planPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        planPanel.setBackground(new Color(245, 245, 255));

        // Add plan title with price
        JLabel titleLabel = new JLabel(planType + " Plan - ₹" + String.format("%.2f", price) + "/month");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        planPanel.add(titleLabel);
        planPanel.add(Box.createVerticalStrut(10));

        // Add features
        JLabel featuresLabel = new JLabel("<html><b>Features:</b><br>" + 
            features.replace("\n", "<br>") + "</html>");
        featuresLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        planPanel.add(featuresLabel);
        planPanel.add(Box.createVerticalStrut(10));

        // Add menu
        JLabel menuLabel = new JLabel("<html><b>Sample Menu:</b><br>" + 
            menu.replace("\n", "<br>") + "</html>");
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        planPanel.add(menuLabel);

        // Add some spacing between plans
        container.add(planPanel);
        container.add(Box.createVerticalStrut(20));
    }

    private void showStudentMealPlanPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Show available meal plans panel
        JPanel plansPanel = new JPanel();
        plansPanel.setLayout(new BoxLayout(plansPanel, BoxLayout.Y_AXIS));
        plansPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add all meal plans using the same method as admin panel
        addPlanDetails(plansPanel);

        // Add scrolling
        JScrollPane scrollPane = new JScrollPane(plansPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Add title
        JLabel titleLabel = new JLabel("Available Meal Plans", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        contentPanel.removeAll();
        contentPanel.add(mainPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void handlePayment(String studentRollNo) throws SQLException {
        // Get plan details
        String sql = "SELECT * FROM meal_plans WHERE student_roll_no = ?";
        PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, studentRollNo);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String planType = rs.getString("plan_type");
            double amount = getPlanPrice(planType);

            // Show payment dialog
            JPanel paymentPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            paymentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField cardNumberField = new JTextField();
            JTextField expiryField = new JTextField();
            JTextField cvvField = new JTextField();
            JLabel amountLabel = new JLabel("₹" + String.format("%.2f", amount));

            paymentPanel.add(new JLabel("Amount to Pay:"));
            paymentPanel.add(amountLabel);
            paymentPanel.add(new JLabel("Card Number:"));
            paymentPanel.add(cardNumberField);
            paymentPanel.add(new JLabel("Expiry (MM/YY):"));
            paymentPanel.add(expiryField);
            paymentPanel.add(new JLabel("CVV:"));
            paymentPanel.add(cvvField);

            int result = JOptionPane.showConfirmDialog(null, paymentPanel,
                "Payment Details", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                // Validate payment details
                if (!validatePaymentDetails(cardNumberField.getText(), 
                                         expiryField.getText(), 
                                         cvvField.getText())) {
                    JOptionPane.showMessageDialog(null, "Invalid payment details!");
                    return;
                }

                // Process payment and update status
                String updateSql = "UPDATE meal_plans SET payment_status = 'Paid' WHERE student_roll_no = ?";
                PreparedStatement updateStmt = DatabaseManager.getConnection().prepareStatement(updateSql);
                updateStmt.setString(1, studentRollNo);
                updateStmt.executeUpdate();

                // Record payment in payment_history table
                String paymentSql = "INSERT INTO payment_history (student_roll_no, amount, payment_date, payment_type) " +
                                  "VALUES (?, ?, CURRENT_DATE, 'Card')";
                PreparedStatement paymentStmt = DatabaseManager.getConnection().prepareStatement(paymentSql);
                paymentStmt.setString(1, studentRollNo);
                paymentStmt.setDouble(2, amount);
                paymentStmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Payment successful!");
                
                // Refresh the panel
                showStudentMealPlanPanel();
            }
        }
    }

    private boolean validatePaymentDetails(String cardNumber, String expiry, String cvv) {
        // Basic validation
        return cardNumber.matches("\\d{16}") && // 16 digits
               expiry.matches("(0[1-9]|1[0-2])/\\d{2}") && // MM/YY format
               cvv.matches("\\d{3}"); // 3 digits
    }

    private double getPlanPrice(String planType) {
        try {
            String sql = "SELECT price FROM meal_plan_details WHERE plan_type = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, planType);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException ex) {
            // Fallback to default prices if database query fails
        }
        
        // Default prices if database query fails
        switch (planType) {
            case "Basic": return 3000.0;
            case "Standard": return 4500.0;
            case "Premium": return 6000.0;
            default: return 0.0;
        }
    }

    // Update the refreshMealPlanTable method to include payment button in admin panel
    private void refreshMealPlanTable(DefaultTableModel model) {
        try {
            String sql = "SELECT m.*, s.name FROM meal_plans m " +
                        "JOIN students s ON m.student_roll_no = s.roll_no";
            Statement stmt = DatabaseManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            model.setRowCount(0);
            while (rs.next()) {
                String planType = rs.getString("plan_type");
                double price = getPlanPrice(planType);
                model.addRow(new Object[]{
                    rs.getString("student_roll_no"),
                    rs.getString("name"),
                    planType,
                    String.format("₹%.2f", rs.getDouble("amount_paid_for_plan")), // Use amount_paid_for_plan
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("payment_status"),
                    rs.getString("payment_method"),
                    rs.getString("payment_details")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error refreshing meal plans: " + ex.getMessage());
        }
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        JLabel valueComponent = new JLabel(value);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(labelComponent);
        panel.add(valueComponent);
    }

    private String getCurrentStudentRollNo() {
        try {
            // Simple query to get the first student
            String sql = "SELECT roll_no, name FROM students ORDER BY roll_no LIMIT 1";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("roll_no");
            }
        } catch (SQLException ex) {
            System.err.println("Error getting student roll number: " + ex.getMessage());
        }
        return null;
    }

    private void showRoomHistory() {
        JDialog historyDialog = new JDialog(this, "Room History", true);
        historyDialog.setLayout(new BorderLayout());
        
        // Create table model with correct columns (no Status column)
        String[] columns = {"Student ID", "Name", "Room No", "Room Type", "Sharing Type", "Block", "Floor", "Check In", "Check Out", "Reason"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable historyTable = new JTable(model);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Student ID
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Room No
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Room Type
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Sharing Type
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Block
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // Floor
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Check In
        historyTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Check Out
        historyTable.getColumnModel().getColumn(9).setPreferredWidth(150); // Reason
        
        try {
            // Updated query to properly join with students table and include student names
            String sql = "SELECT rh.*, s.name as student_name FROM room_history rh " +
                        "LEFT JOIN students s ON rh.student_id = s.roll_no " +
                        "ORDER BY rh.room_no, rh.student_id";
            PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("student_id"),
                    rs.getString("student_name"),  // Using the coalesced student name
                    rs.getString("room_no"),
                    rs.getString("room_type"),
                    rs.getString("sharing_type"),
                    rs.getString("block"),
                    rs.getString("floor"),
                    rs.getString("check_in"),
                    rs.getString("check_out"),
                    rs.getString("reason")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching room history: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> historyDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        historyDialog.setSize(1000, 500);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }

    // Helper method to calculate price difference between room types
    private double calculatePriceDifference(String fromType, String toType) {
        try {
            // Get sharing type for both rooms
            String sql = "SELECT sharing_type FROM rooms WHERE room_no = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            
            // Get source room sharing type
            pstmt.setString(1, fromType);
            ResultSet rs = pstmt.executeQuery();
            String fromSharingType = rs.next() ? rs.getString("sharing_type") : "4-sharing";
            
            // Get destination room sharing type
            pstmt.setString(1, toType);
            rs = pstmt.executeQuery();
            String toSharingType = rs.next() ? rs.getString("sharing_type") : "4-sharing";
            
            // Calculate price difference using room types and sharing types
            double fromPrice = DatabaseManager.calculateRoomFee(fromType, fromSharingType);
            double toPrice = DatabaseManager.calculateRoomFee(toType, toSharingType);
            
            return toPrice - fromPrice;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error calculating price difference: " + e.getMessage());
            return 0.0;
        }
    }

    private void showFeedbackPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create feedback input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add title
        JLabel titleLabel = new JLabel("Cafeteria Feedback");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridwidth = 2;
        inputPanel.add(titleLabel, gbc);

        // Add feedback text area
        gbc.gridy = 1;
        JLabel feedbackLabel = new JLabel("Your Feedback:");
        inputPanel.add(feedbackLabel, gbc);

        gbc.gridy = 2;
        JTextArea feedbackArea = new JTextArea(5, 30);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);
        inputPanel.add(feedbackScrollPane, gbc);

        // Add submit button
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitButton = new JButton("Submit Feedback");
        submitButton.addActionListener(e -> {
            String feedbackText = feedbackArea.getText().trim();
            
            if (feedbackText.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "Please enter your feedback before submitting.");
                return;
            }

            try {
                String sql = "INSERT INTO meal_feedback (student_roll_no, feedback_text, feedback_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
                pstmt.setString(1, currentUsername);
                pstmt.setString(2, feedbackText);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(mainPanel, "Thank you for your feedback!");
                feedbackArea.setText("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Error submitting feedback: " + ex.getMessage());
            }
        });
        inputPanel.add(submitButton, gbc);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        contentPanel.removeAll();
        contentPanel.add(mainPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private boolean validatePaymentDetails(String paymentMethod, String upiId, String cardNumber, String expiry, String cvv) {
        switch (paymentMethod) {
            case "UPI":
                if (upiId == null || upiId.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "Please enter UPI ID");
                    return false;
                }
                if (!upiId.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+$")) {
                    JOptionPane.showMessageDialog(mainPanel, "Invalid UPI ID format. Should be like name@bank");
                    return false;
                }
                break;
            case "Card":
                if (cardNumber == null || cardNumber.trim().isEmpty() || 
                    expiry == null || expiry.trim().isEmpty() || 
                    cvv == null || cvv.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "Please fill all card details");
                    return false;
                }
                if (!cardNumber.matches("^\\d{16}$")) {
                    JOptionPane.showMessageDialog(mainPanel, "Card number must be 16 digits");
                    return false;
                }
                if (!expiry.matches("^(0[1-9]|1[0-2])/(\\d{2})$")) {
                    JOptionPane.showMessageDialog(mainPanel, "Expiry must be in MM/YY format");
                    return false;
                }
                if (!cvv.matches("^\\d{3}$")) {
                    JOptionPane.showMessageDialog(mainPanel, "CVV must be 3 digits");
                    return false;
                }
                break;
            case "Cash":
                // No validation needed for cash
                break;
            default:
                JOptionPane.showMessageDialog(mainPanel, "Invalid payment method");
                return false;
        }
        return true;
    }

    // Add this method inside the DashboardFrame class
    private Map<String, Integer> getDashboardData() {
        Map<String, Integer> data = new HashMap<>();
        try {
            List<Map<String, Object>> students = DatabaseManager.getAllStudents();
            data.put("totalStudents", students.size());

            List<Map<String, Object>> rooms = DatabaseManager.getAllRooms();
            data.put("totalRooms", rooms.size());

            List<Map<String, Object>> wardens = DatabaseManager.getAllWardens();
            data.put("totalWardens", wardens.size());

            List<Map<String, Object>> pendingComplaints = DatabaseManager.getPendingComplaints();
            data.put("pendingComplaints", pendingComplaints.size());

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions, maybe return 0 for counts or show an error message
            data.put("totalStudents", 0);
            data.put("totalRooms", 0);
            data.put("totalWardens", 0);
            data.put("pendingComplaints", 0);
        }
        return data;
    }

    // Helper method to create a panel for a single dashboard data point
    private JPanel createDashboardDataPanel(String title, int value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title));
        panel.setBackground(color); // Set the background color

        JLabel valueLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 36f)); // Larger, bold font for the value
        valueLabel.setForeground(Color.WHITE); // Change text color to white for better contrast with colored background
        
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(200, 100)); // Give panels a preferred size

        return panel;
    }
}