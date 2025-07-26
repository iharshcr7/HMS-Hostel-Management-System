package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import database.DatabaseManager;
import java.util.Map;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class MainSystemPanel extends JPanel {
    private JPanel contentPanel;
    private JPanel menuPanel;
    private CardLayout cardLayout;
    private Color primaryColor = new Color(0, 199, 0);  // Green color from reference
    private int screenWidth;
    private int screenHeight;
    private Image backgroundImage;
    
    public MainSystemPanel() {
        // Get screen dimensions
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        screenWidth = gd.getDisplayMode().getWidth();
        screenHeight = gd.getDisplayMode().getHeight();

        setLayout(null);
        setBounds(0, 0, screenWidth, screenHeight);
        
        // Load background image
        try {
            backgroundImage = ImageIO.read(new File("Hostel.jpg"));
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
            // Try loading from different paths
            try {
                backgroundImage = ImageIO.read(new File("src/resources/Hostel.jpg"));
            } catch (IOException e2) {
                System.err.println("Error loading background image from alternate path: " + e2.getMessage());
            }
        }
        
        // Create header panel (1/6 of screen height)
        JPanel headerPanel = createHeader();
        headerPanel.setBounds(0, 0, screenWidth, screenHeight/6);
        add(headerPanel);
        
        // Calculate content area height (2/3 of screen)
        int contentHeight = screenHeight - (2 * screenHeight/6);

        // Create menu panel (1/4 of screen width)
        menuPanel = createMenuPanel();
        menuPanel.setBounds(0, screenHeight/6, screenWidth/4, contentHeight);
        add(menuPanel);
        
        // Create content panel with card layout
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBounds(screenWidth/4, screenHeight/6, 
                             screenWidth - screenWidth/4, contentHeight);
        
        // Add content panels
        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createRoomManagementPanel(), "Room Management");
        contentPanel.add(createStudentManagementPanel(), "Student Management");
        contentPanel.add(createWardenManagementPanel(), "Warden Management");
        contentPanel.add(createComplaintManagementPanel(), "Complaint Management");
        contentPanel.add(createHostelRulesPanel(), "Hostel Rules");
        
        add(contentPanel);
        
        // Create footer panel (1/6 of screen height)
        JPanel footerPanel = createFooter();
        footerPanel.setBounds(0, screenHeight - screenHeight/6, 
                            screenWidth, screenHeight/6);
        add(footerPanel);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(null);
        header.setBackground(primaryColor);

        JLabel titleLabel = new JLabel("Welcome To DHHP Hostel");
        titleLabel.setFont(titleLabel.getFont().deriveFont(45.0f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(screenWidth/2 - 300, screenHeight/12 - 40, 600, 80);
        header.add(titleLabel);

        return header;
    }
    
    private JPanel createMenuPanel() {
        JPanel menu = new JPanel(null);
        menu.setBackground(new Color(255, 165, 0));  // Orange background like reference
        
        String[] menuItems = {
            "Dashboard",
            "Room Management",
            "Student Management",
            "Warden Management",
            "Complaint Management",
            "Hostel Rules"
        };

        // Calculate button dimensions based on content area height
        int contentHeight = screenHeight - (2 * screenHeight/6);
        int buttonHeight = contentHeight / menuItems.length;
        int buttonWidth = screenWidth/4;  // Full width of menu panel

        for (int i = 0; i < menuItems.length; i++) {
            JButton button = new JButton(menuItems[i]);
            button.setFont(button.getFont().deriveFont(24f));
            button.setBounds(0, i * buttonHeight, buttonWidth, buttonHeight);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBackground(new Color(240, 240, 255));  // Light blue background
            button.setForeground(new Color(51, 51, 51));
            
            // Add hover effect
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(200, 200, 255));
                }
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(240, 240, 255));
                }
            });
            
            final String item = menuItems[i];
            button.addActionListener(e -> cardLayout.show(contentPanel, item));
            
            menu.add(button);
        }

        return menu;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    // Calculate dimensions to fill width while maintaining aspect ratio
                    double aspectRatio = (double) backgroundImage.getHeight(null) / backgroundImage.getWidth(null);
                    int targetWidth = getWidth();
                    int targetHeight = (int) (targetWidth * aspectRatio);
                    
                    // If height is too large, adjust based on panel height
                    if (targetHeight > getHeight()) {
                        targetHeight = getHeight();
                        targetWidth = (int) (targetHeight / aspectRatio);
                    }
                    
                    // Center the image
                    int x = (getWidth() - targetWidth) / 2;
                    int y = (getHeight() - targetHeight) / 2;
                    
                    // Use Graphics2D for better quality scaling
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        panel.setOpaque(false);
        return panel;
    }
    
    private JPanel createRoomManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JButton("Add Room"));
        toolbar.add(new JButton("Edit Room"));
        toolbar.add(new JButton("Delete Room"));
        
        // Create table
        String[] columns = {"Room No", "Type", "Capacity", "Occupied", "Block", "Floor"};
        Object[][] data = getRoomData();
        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Object[][] getRoomData() {
        List<Map<String, Object>> rooms = DatabaseManager.getAllRooms();
        Object[][] data = new Object[rooms.size()][6];
        for (int i = 0; i < rooms.size(); i++) {
            Map<String, Object> room = rooms.get(i);
            data[i][0] = room.get("roomNo");
            data[i][1] = room.get("type");
            data[i][2] = room.get("capacity");
            data[i][3] = room.get("currentOccupancy");
            data[i][4] = room.get("block");
            data[i][5] = room.get("floor");
        }
        return data;
    }
    
    private JPanel createStudentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create toolbar with better spacing
        JPanel toolbar = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.add(new JButton("Add Student"));
        buttonPanel.add(new JButton("Edit Student"));
        buttonPanel.add(new JButton("Delete Student"));
        
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(new JTextField(20));
        
        toolbar.add(buttonPanel, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);
        
        // Create table with all columns
        String[] columns = {
            "Roll No", "Name", "College", "Department", "Semester", 
            "Age", "Contact", "Room", "Room Type", "Sharing Type",
            "Block", "Floor", "Amount Paid", "Amount Due"
        };
        
        Object[][] data = getStudentData();
        JTable table = new JTable(data, columns);
        
        // Configure table for better visibility
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        
        // Set specific column widths proportionally
        int totalWidth = panel.getWidth();
        int[] columnWidths = {
            80,  // Roll No
            120, // Name
            100, // College
            100, // Department
            70,  // Semester
            50,  // Age
            100, // Contact
            60,  // Room
            80,  // Room Type
            80,  // Sharing Type
            60,  // Block
            50,  // Floor
            90,  // Amount Paid
            90   // Amount Due
        };
        
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
        }
        
        // Create scroll pane that fills the available space
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Create a wrapper panel to ensure the table uses full width
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(tableWrapper, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Object[][] getStudentData() {
        List<Map<String, Object>> students = DatabaseManager.getAllStudents();
        Object[][] data = new Object[students.size()][14];  // Updated to 14 columns
        for (int i = 0; i < students.size(); i++) {
            Map<String, Object> student = students.get(i);
            data[i][0] = student.get("rollNo");
            data[i][1] = student.get("name");
            data[i][2] = student.get("college");
            data[i][3] = student.get("department");
            data[i][4] = student.get("semester");
            data[i][5] = student.get("age");
            data[i][6] = student.get("mobile");
            data[i][7] = student.get("roomNo");
            data[i][8] = student.get("roomType");
            data[i][9] = student.get("sharingType");
            data[i][10] = student.get("blockName");
            data[i][11] = student.get("floorNo");
            data[i][12] = String.format("₹%.0f", student.get("amountPaid"));
            data[i][13] = String.format("₹%.0f", student.get("amountDue"));
        }
        return data;
    }
    
    private JPanel createWardenManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JButton("Add Warden"));
        toolbar.add(new JButton("Edit Warden"));
        toolbar.add(new JButton("Delete Warden"));
        
        // Create table
        String[] columns = {"ID", "Name", "Block", "Contact", "Joining Date"};
        Object[][] data = getWardenData();
        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Object[][] getWardenData() {
        List<Map<String, Object>> wardens = DatabaseManager.getAllWardens();
        Object[][] data = new Object[wardens.size()][5];
        for (int i = 0; i < wardens.size(); i++) {
            Map<String, Object> warden = wardens.get(i);
            data[i][0] = warden.get("id");
            data[i][1] = warden.get("name");
            data[i][2] = warden.get("block");
            data[i][3] = warden.get("mobile");
            data[i][4] = warden.get("joiningDate");
        }
        return data;
    }
    
    private JPanel createComplaintManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JButton("New Complaint"));
        toolbar.add(new JButton("Resolve Complaint"));
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Filter:"));
        JComboBox<String> filterBox = new JComboBox<>(new String[]{"All", "Pending", "Resolved"});
        filterPanel.add(filterBox);
        toolbar.add(filterPanel);
        
        // Create table
        String[] columns = {"ID", "Student", "Description", "Status", "Filed Date"};
        Object[][] data = getComplaintData();
        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Object[][] getComplaintData() {
        List<Map<String, Object>> complaints = DatabaseManager.getAllComplaints();
        Object[][] data = new Object[complaints.size()][5];
        for (int i = 0; i < complaints.size(); i++) {
            Map<String, Object> complaint = complaints.get(i);
            data[i][0] = complaint.get("id");
            data[i][1] = complaint.get("studentName");
            data[i][2] = complaint.get("text");
            data[i][3] = complaint.get("status");
            data[i][4] = complaint.get("filedOn");
        }
        return data;
    }
    
    private JPanel createHostelRulesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextArea rulesArea = new JTextArea();
        rulesArea.setEditable(false);
        rulesArea.setFont(new Font("Arial", Font.PLAIN, 14));
        rulesArea.setText(
            "Hostel Rules and Regulations:\n\n" +
            "1. Entry/Exit Timing:\n" +
            "   - Entry closes at 10:00 PM\n" +
            "   - No entry/exit allowed between 10:00 PM to 6:00 AM\n\n" +
            "2. Room Maintenance:\n" +
            "   - Keep rooms clean and tidy\n" +
            "   - No modifications to room structure\n" +
            "   - Report damages immediately\n\n" +
            "3. Visitors:\n" +
            "   - Visitors allowed only in common areas\n" +
            "   - Visiting hours: 9:00 AM to 6:00 PM\n\n" +
            "4. Common Areas:\n" +
            "   - Maintain cleanliness\n" +
            "   - Follow noise regulations\n" +
            "   - Respect other residents\n\n" +
            "5. Safety:\n" +
            "   - No prohibited items\n" +
            "   - Follow fire safety guidelines\n" +
            "   - Report suspicious activities"
        );
        
        JScrollPane scrollPane = new JScrollPane(rulesArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(new Color(255, 192, 203));  // Pink color

        JLabel footerLabel = new JLabel("<html><p>Developers: Pranav Mistry, Henisha Kandoi, Harsh Chauhan, Dhanvin Patel" +
                                      "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Address: Bhayli,Vadodara,Gujarat-390016" +
                                      "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Mobile: +91 9876543210</p></html>");
        footerLabel.setFont(footerLabel.getFont().deriveFont(22f));
        footerLabel.setVerticalAlignment(JLabel.BOTTOM);
        footer.add(footerLabel);

        return footer;
    }
} 