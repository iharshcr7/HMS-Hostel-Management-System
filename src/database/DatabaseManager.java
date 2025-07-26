package database;

import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class DatabaseManager {
    private static Connection connection = null;
    
    // Room pricing constants
    private static final double LUXURY_BASE_PRICE = 60000.0;    // Base price for luxury rooms
    private static final double STANDARD_BASE_PRICE = 40000.0;  // Base price for standard rooms
    private static final double FIXED_SHARING_MULTIPLIER = 0.6;  // Fixed at 4-sharing rate (60%)
    
    private static boolean sampleDataInitialized = false;
    
    public static void initializeDatabase(String host, String port, String username, String password, String databaseName) {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to the MySQL server without specifying a database
            String url = "jdbc:mysql://" + host + ":" + port + "/";
            try (Connection initialConnection = DriverManager.getConnection(url, username, password);
                 Statement stmt = initialConnection.createStatement()) {

                // Check if the database exists
                ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + databaseName + "'");
                boolean databaseExists = rs.next();

                // Create the database if it doesn't exist
                if (!databaseExists) {
                    System.out.println("Database '" + databaseName + "' not found. Creating database...");
                    stmt.executeUpdate("CREATE DATABASE " + databaseName);
                    System.out.println("Database '" + databaseName + "' created successfully.");
                }
            }

            // Now connect to the specific database
            connectToDatabase(host, port, username, password, databaseName);

            // Initialize database tables by dropping and recreating them with correct schemas
            try (Statement stmt = connection.createStatement()) {
                // Temporarily disable foreign key checks to allow dropping tables
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

                // Drop tables in reverse order of dependencies
                dropTableIfExists("meal_feedback");
                dropTableIfExists("daily_menu");
                dropTableIfExists("meal_plans");
                dropTableIfExists("meal_plan_details");
                dropTableIfExists("payment_history");
                dropTableIfExists("room_history");
                dropTableIfExists("complaints");
                dropTableIfExists("students");
                dropTableIfExists("rooms");
                dropTableIfExists("wardens");
                dropTableIfExists("admins");

                // Create tables with complete and correct schemas
                stmt.executeUpdate(
                    "CREATE TABLE admins (" +
                    "username VARCHAR(50) PRIMARY KEY," +
                    "password VARCHAR(50) NOT NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE wardens (" +
                    "warden_id VARCHAR(20) PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "age INT," +
                    "mobile VARCHAR(15)," +
                    "assigned_hostel VARCHAR(50)," +
                    "block_name VARCHAR(20)," +
                    "joining_date DATE" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE rooms (" +
                    "room_no VARCHAR(10) PRIMARY KEY," +
                    "room_type ENUM('Standard', 'Luxury') NOT NULL," +
                    "sharing_type ENUM('1 Sharing', '2 Sharing', '4 Sharing') NOT NULL," +
                    "capacity INT NOT NULL," +
                    "current_occupancy INT DEFAULT 0," +
                    "block_name VARCHAR(20)," +
                    "floor_no INT" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE students (" +
                    "roll_no VARCHAR(20) PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "college VARCHAR(100)," +
                    "department VARCHAR(50)," +
                    "semester VARCHAR(20)," +
                    "age INT," +
                    "mobile VARCHAR(15)," +
                    "room_no VARCHAR(10)," +
                    "room_type ENUM('Standard', 'Luxury')," +
                    "sharing_type ENUM('1 Sharing', '2 Sharing', '4 Sharing')," +
                    "block_name VARCHAR(20)," +
                    "floor_no INT," +
                    "amount_paid DECIMAL(10,2) DEFAULT 0.00," +
                    "amount_due DECIMAL(10,2) DEFAULT 0.00," +
                    "payment_method VARCHAR(20)," +
                    "password VARCHAR(50) NOT NULL," +
                    "FOREIGN KEY (room_no) REFERENCES rooms(room_no) ON DELETE SET NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE payment_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "student_id VARCHAR(20)," +
                    "amount DECIMAL(10,2) NOT NULL," +
                    "type ENUM('PAYMENT', 'REFUND', 'CHARGE') NOT NULL," +
                    "reason VARCHAR(100)," +
                    "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (student_id) REFERENCES students(roll_no) ON DELETE SET NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE complaints (" +
                    "complaint_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "student_roll_no VARCHAR(20)," +
                    "complaint_text TEXT," +
                    "status VARCHAR(20) DEFAULT 'Pending'," +
                    "filing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "resolution_date TIMESTAMP NULL," +
                    "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE SET NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE meal_plan_details (" +
                    "plan_type ENUM('Basic', 'Standard', 'Premium') PRIMARY KEY," +
                    "price DECIMAL(10,2) NOT NULL," +
                    "features TEXT NOT NULL," +
                    "sample_menu TEXT NOT NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE meal_plans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "student_roll_no VARCHAR(20)," +
                    "plan_type ENUM('Basic', 'Standard', 'Premium')," +
                    "start_date DATE NOT NULL," +
                    "end_date DATE NOT NULL," +
                    "payment_status ENUM('Pending', 'Paid') DEFAULT 'Pending'," +
                    "payment_method VARCHAR(20)," +
                    "payment_details VARCHAR(100)," +
                    "amount_paid_for_plan DECIMAL(10,2) DEFAULT 0.00," + // Added column
                    "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE," +
                    "FOREIGN KEY (plan_type) REFERENCES meal_plan_details(plan_type)," +
                    "UNIQUE KEY unique_student (student_roll_no)" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE daily_menu (" +
                    "menu_date DATE PRIMARY KEY," +
                    "breakfast TEXT," +
                    "lunch TEXT," +
                    "dinner TEXT" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE meal_feedback (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "student_roll_no VARCHAR(20)," +
                    "feedback_text TEXT NOT NULL," +
                    "feedback_date DATE NOT NULL," +
                    "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_daily_feedback (student_roll_no, feedback_date)" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE room_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "student_id VARCHAR(20)," +
                    "room_no VARCHAR(10)," +
                    "room_type VARCHAR(20)," +
                    "sharing_type VARCHAR(20)," +
                    "block VARCHAR(20)," +
                    "floor INT," +
                    "check_in TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "check_out TIMESTAMP NULL," +
                    "reason VARCHAR(100)," +
                    "FOREIGN KEY (student_id) REFERENCES students(roll_no) ON DELETE SET NULL" +
                    ")"
                );

                // Re-enable foreign key checks
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            }

            // Initialize sample data
            importSampleData();

            System.out.println("✓ Database initialized successfully!");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            // Consider adding more robust error handling or logging here
        }
    }

    public static void connectToDatabase(String host, String port, String username, String password, String database) throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to the database
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, username, password);
            
            System.out.println("✓ Connected to database successfully!");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static void importSampleData() throws SQLException {
        if (sampleDataInitialized) {
            return;  // Silently return if already initialized
        }
        initializeSampleData();
        sampleDataInitialized = true;
    }
    
    public static void initializeSampleData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Add sample wardens
            stmt.execute("INSERT IGNORE INTO wardens (warden_id, name, age, mobile, assigned_hostel, block_name, joining_date) VALUES " +
                       "('W001', 'Rajesh Kumar', 35, '9898989898', 'DHHP Hostel', 'A Block', '2023-01-01'), " +
                       "('W002', 'Priya Sharma', 32, '9797979797', 'DHHP Hostel', 'B Block', '2023-02-01'), " +
                       "('W003', 'Amit Patel', 40, '9696969696', 'DHHP Hostel', 'B Block', '2023-03-01')");

            // Add rooms with different sharing types and appropriate capacities
            stmt.execute("INSERT IGNORE INTO rooms (room_no, room_type, sharing_type, capacity, current_occupancy, block_name, floor_no) VALUES " +
                       "('101', 'Standard', '4 Sharing', 4, 0, 'A Block', 1), " +  // 4-sharing standard
                       "('102', 'Standard', '2 Sharing', 2, 0, 'A Block', 1), " +  // 2-sharing standard
                       "('103', 'Standard', '1 Sharing', 1, 0, 'A Block', 1), " +  // 1-sharing standard
                       "('201', 'Luxury', '4 Sharing', 4, 0, 'B Block', 2), " +    // 4-sharing luxury
                       "('202', 'Luxury', '2 Sharing', 2, 0, 'B Block', 2), " +    // 2-sharing luxury
                       "('203', 'Luxury', '1 Sharing', 1, 0, 'B Block', 2)");      // 1-sharing luxury
            
            // Add sample students with different sharing types and correct pricing
            stmt.execute("INSERT IGNORE INTO students (roll_no, name, college, department, semester, age, mobile, room_no, room_type, sharing_type, block_name, floor_no, amount_paid, amount_due, payment_method, password) VALUES " +
                       // Standard Room Students (Total cost 40000)
                       "('23000877', 'Pranav Mistry', 'NUV', 'CSE', '4th', 21, '9876543210', '101', 'Standard', '4 Sharing', 'A Block', 1, 8000, 2000, 'UPI', 'P123'), " +      // 10000 per person (40000/4)
                       "('23000872', 'Henisha Kandoi', 'NUV', 'CSE', '4th', 20, '9876543211', '102', 'Standard', '2 Sharing', 'A Block', 1, 15000, 5000, 'Card', 'HK123'), " +    // 20000 per person (40000/2)
                       "('23000929', 'Harsh Chauhan', 'NUV', 'CSE', '4th', 21, '9876543212', '103', 'Standard', '1 Sharing', 'A Block', 1, 35000, 5000, 'Cash', 'H123'), " +      // 40000 per person (40000/1)
                       // Luxury Room Students (Total cost 60000)
                       "('23000900', 'Asmetha Thoppe', 'NUV', 'CSE', '4th', 21, '9876543100', '201', 'Luxury', '4 Sharing', 'B Block', 2, 12000, 3000, 'UPI', 'A123'), " +       // 15000 per person (60000/4)
                       "('23000892', 'Om Patel', 'NUV', 'CSE', '4th', 21, '9876543000', '202', 'Luxury', '2 Sharing', 'B Block', 2, 25000, 5000, 'Card', 'O123'), " +           // 30000 per person (60000/2)
                       "('23000894', 'Dhanvin Patel', 'NUV', 'CSE', '4th', 20, '9876543213', '203', 'Luxury', '1 Sharing', 'B Block', 2, 45000, 15000, 'Cash', 'D123')");        // 60000 per person (60000/1)

            // Add sample complaints
            stmt.execute("INSERT IGNORE INTO complaints (student_roll_no, complaint_text, status, filing_date) VALUES " +
                       "('23000877', 'AC not working properly', 'Pending', NOW()), " +
                       "('23000872', 'Water leakage in bathroom', 'Resolved', NOW()), " +
                       "('23000929', 'Need new mattress', 'In Progress', NOW())");

            // Add sample meal plan details
            stmt.execute("INSERT IGNORE INTO meal_plan_details (plan_type, price, features, sample_menu) VALUES " +
                "('Basic', 3000.00, '• 3 meals per day (Breakfast, Lunch, Dinner)\n• Standard vegetarian menu\n• Basic salad bar access\n• Regular tea/coffee\n• Fixed meal timings\n• Water dispenser access', 'Breakfast:\n- Poha/Upma/Idli/Dosa\n- Tea or Coffee\n- Bread and Butter\n\nLunch:\n- Rice and Dal\n- 2 Rotis\n- 1 Vegetable Curry\n- Basic Salad\n\nDinner:\n- Rice or Khichdi\n- 2 Rotis\n- 1 Vegetable Curry\n- Basic Salad'), " +
                "('Standard', 4500.00, '• 3 meals per day + Evening snacks\n• Veg and Non-veg options (twice a week)\n• Extended salad bar access\n• Premium tea/coffee varieties\n• Flexible meal timings\n• Water dispenser access\n• Weekend special meals\n• Take-away option available', 'Breakfast:\n- All Basic Plan items PLUS:\n- Eggs to order\n- Cornflakes/Oats\n- Fresh Fruits\n- Multiple beverage options\n\nLunch:\n- Rice and Dal\n- 3 Rotis\n- 2 Vegetable Curries\n- Non-veg (twice a week)\n- Extended Salad Bar\n- Dessert\n\nEvening Snacks:\n- Tea/Coffee\n- Biscuits/Samosa/Pakoda\n\nDinner:\n- Rice Varieties\n- 3 Rotis\n- 2 Curries\n- Non-veg (twice a week)\n- Extended Salad Bar\n- Dessert (occasionally)'), " +
                "('Premium', 6000.00, '• 3 meals per day + Evening snacks + Night canteen access\n• Premium veg and non-veg options (daily)\n• Full salad bar access with premium items\n• All beverage varieties available\n• 24/7 meal timing flexibility\n• Water dispenser access\n• Daily special meals\n• Take-away option available\n• Monthly special dining events\n• Special diet options available\n• Personalized meal planning', 'Breakfast:\n- All Standard Plan items PLUS:\n- Premium cereals variety\n- Multiple egg preparations\n- Fresh juice counter\n- Premium fruits\n- Specialty breads\n- Live cooking station\n\nLunch:\n- Multiple Rice varieties\n- Unlimited Rotis\n- 3 Vegetable Curries\n- Daily Non-veg option\n- Premium Salad Bar\n- Live counter items\n- Dessert varieties\n\nEvening Snacks:\n- Premium Tea/Coffee\n- Multiple snack options\n- Fresh juice/shakes\n\nDinner:\n- Multiple Rice varieties\n- Unlimited Rotis\n- 3 Curries\n- Daily Non-veg option\n- Premium Salad Bar\n- Live counter items\n- Dessert varieties\n\nNight Canteen Access:\n- Light meals\n- Beverages\n- Healthy snacks')");

            // Add sample meal plans for students
            stmt.execute("INSERT IGNORE INTO meal_plans (student_roll_no, plan_type, start_date, end_date, payment_status, payment_method, payment_details, amount_paid_for_plan) VALUES " +
                       "('23000877', 'Premium', '2024-01-01', '2024-06-30', 'Paid', 'Cash', 'N/A', 36000.00), " +      // Premium for 6 months
                       "('23000872', 'Standard', '2024-01-01', '2024-06-30', 'Paid', 'Card', '**** **** **** 1234', 27000.00), " +      // Standard for 6 months
                       "('23000929', 'Basic', '2024-01-01', '2024-06-30', 'Paid', 'UPI', 'harsh.c@examplebank', 18000.00), " +   // Basic for 6 months
                       "('23000900', 'Premium', '2024-01-01', '2024-06-30', 'Paid', 'UPI', 'asmetha.t@otherupi', 36000.00)");    // Premium for 6 months

            // Add sample daily menu
            stmt.execute("INSERT IGNORE INTO daily_menu (menu_date, breakfast, lunch, dinner) VALUES " +
                       "(CURDATE(), 'Tea/Coffee, Bread, Butter, Jam, Eggs, Poha', 'Rice, Dal Makhani, Roti, Mix Veg, Salad, Papad', 'Rice, Dal Tadka, Roti, Paneer Butter Masala, Salad, Gulab Jamun'), " +
                       "(DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'Tea/Coffee, Bread, Butter, Jam, Eggs, Upma', 'Rice, Dal Fry, Roti, Aloo Gobi, Salad, Papad', 'Rice, Dal, Roti, Veg Kofta, Salad, Kheer')");

            // Add sample meal feedback
            stmt.execute("INSERT IGNORE INTO meal_feedback (student_roll_no, feedback_text, feedback_date) VALUES " +
                       "('23000877', 'Food quality is excellent, especially the breakfast variety', CURDATE()), " +
                       "('23000872', 'Good food but need more variety in dinner', CURDATE()), " +
                       "('23000929', 'Please add more fruits in breakfast', CURDATE())");
            
            System.out.println("✓ Sample data initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
            throw e;
        }
    }
    
    private static void createOtherTables(Statement stmt) throws SQLException {
        // Create admins table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS admins (" +
            "username VARCHAR(50) PRIMARY KEY," +
            "password VARCHAR(50) NOT NULL" +
            ")"
        );
        
        // Create wardens table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS wardens (" +
            "warden_id VARCHAR(20) PRIMARY KEY," +
            "name VARCHAR(100) NOT NULL," +
            "age INT," +
            "mobile VARCHAR(15)," +
            "assigned_hostel VARCHAR(50)," +
            "block_name VARCHAR(20)," +
            "joining_date DATE" +
            ")"
        );

        // Create complaints table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS complaints (" +
            "complaint_id INT AUTO_INCREMENT PRIMARY KEY," +
            "student_roll_no VARCHAR(20)," +
            "complaint_text TEXT," +
            "status VARCHAR(20)," +
            "filing_date DATETIME," +
            "resolution_date DATETIME," +
            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no)" +
            ")"
        );

        // Create meal_plan_details table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS meal_plan_details (" +
            "plan_type ENUM('Basic', 'Standard', 'Premium') PRIMARY KEY," +
            "price DECIMAL(10,2) NOT NULL," +
            "features TEXT NOT NULL," +
            "sample_menu TEXT NOT NULL" +
            ")"
        );

        // Create meal_plans table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS meal_plans (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "student_roll_no VARCHAR(20)," +
            "plan_type ENUM('Basic', 'Standard', 'Premium') NOT NULL," +
            "start_date DATE NOT NULL," +
            "end_date DATE NOT NULL," +
            "payment_status ENUM('Pending', 'Paid') DEFAULT 'Pending'," +
            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE," +
            "UNIQUE KEY unique_student (student_roll_no)" +
            ")"
        );

        // Create daily_menu table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS daily_menu (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "menu_date DATE NOT NULL," +
            "breakfast TEXT," +
            "lunch TEXT," +
            "dinner TEXT," +
            "UNIQUE KEY unique_date (menu_date)" +
            ")"
        );

        // Create meal_feedback table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS meal_feedback (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "student_roll_no VARCHAR(20)," +
            "feedback_text TEXT NOT NULL," +
            "feedback_date DATE NOT NULL," +
            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE" +
            ")"
        );
    }
    
    private static void createTables(Statement stmt) throws SQLException {
        // Admin table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS admins (" +
            "username VARCHAR(50) PRIMARY KEY," +
            "password VARCHAR(50) NOT NULL" +
            ")"
        );
        
        // Student table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS students (" +
            "roll_no VARCHAR(20) PRIMARY KEY," +
            "name VARCHAR(100) NOT NULL," +
            "college VARCHAR(100)," +
            "department VARCHAR(50)," +
            "semester VARCHAR(20)," +
            "age INT," +
            "mobile VARCHAR(15)," +
            "room_no VARCHAR(10)," +
            "room_type VARCHAR(20)," +
            "sharing_type VARCHAR(20)," +
            "block_name VARCHAR(20)," +
            "floor_no INT," +
            "amount_paid DECIMAL(10,2)," +
            "amount_due DECIMAL(10,2)," +
            "payment_method VARCHAR(20)," + // Added payment_method column
            "password VARCHAR(50) NOT NULL" +
            ")"
        );

        // Wardens table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS wardens (" +
            "warden_id VARCHAR(20) PRIMARY KEY," +
            "name VARCHAR(100) NOT NULL," +
            "age INT," +
            "mobile VARCHAR(15)," +
            "assigned_hostel VARCHAR(50)," +
            "block_name VARCHAR(20)," +
            "joining_date DATE" +
            ")"
        );

        // Complaints table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS complaints (" +
            "complaint_id INT AUTO_INCREMENT PRIMARY KEY," +
            "student_roll_no VARCHAR(20)," +
            "complaint_text TEXT," +
            "status VARCHAR(20)," +
            "filing_date DATETIME," +
            "resolution_date DATETIME," +
            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no)" +
            ")"
        );

        // Drop existing rooms table if exists to update schema
        stmt.executeUpdate("DROP TABLE IF EXISTS rooms");

        // Rooms table with sharing_type column
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS rooms (" +
            "room_no VARCHAR(10) PRIMARY KEY," +
            "room_type VARCHAR(20) NOT NULL," +
            "capacity INT NOT NULL," +
            "current_occupancy INT DEFAULT 0," +
            "floor_no INT," +
            "block_name VARCHAR(20)," +
            "sharing_type VARCHAR(20) NOT NULL" +
            ")"
        );

        // Meal Plans table
                stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS meal_plans (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "student_roll_no VARCHAR(20)," +
            "plan_type ENUM('Basic', 'Standard', 'Premium') NOT NULL," +
            "start_date DATE NOT NULL," +
            "end_date DATE NOT NULL," +
            "payment_status ENUM('Pending', 'Paid') DEFAULT 'Pending'," +
            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE," +
            "UNIQUE KEY unique_student (student_roll_no)" +
            ")"
        );

        // Daily Menu table
                stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS daily_menu (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "menu_date DATE NOT NULL," +
            "breakfast TEXT," +
            "lunch TEXT," +
            "dinner TEXT," +
            "UNIQUE KEY unique_date (menu_date)" +
            ")"
        );

        // Meal Feedback table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS meal_feedback (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "student_roll_no VARCHAR(20)," +
            "feedback_text TEXT NOT NULL," +
            "feedback_date DATE NOT NULL," +
            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE" +
            ")"
        );

        // Meal Plan Details table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS meal_plan_details (" +
            "plan_type ENUM('Basic', 'Standard', 'Premium') PRIMARY KEY," +
            "price DECIMAL(10,2) NOT NULL," +
            "features TEXT NOT NULL," +
            "sample_menu TEXT NOT NULL" +
            ")"
        );

        // Create room_history table if it doesn't exist
        createRoomHistoryTable();
    }
    
    private static void insertInitialData(Statement stmt) throws SQLException {
        // Insert default admin
        stmt.executeUpdate(
            "INSERT IGNORE INTO admins (username, password) VALUES " +
            "('admin', 'admin123')"
        );
        
        System.out.println("✓ Admin credentials initialized successfully!");
    }
    
    public static Connection getConnection() {
        return connection;
    }
    
    // CRUD Operations for Students
    public static void displayAllStudents() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            
            System.out.println("\n=== Student Records ===");
            while (rs.next()) {
                System.out.println("\nRoll No: " + rs.getString("roll_no"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("College: " + rs.getString("college"));
                System.out.println("Department: " + rs.getString("department"));
                System.out.println("Semester: " + rs.getString("semester"));
                System.out.println("Room: " + rs.getString("room_no") + " (" + rs.getString("room_type") + ", " + rs.getString("sharing_type") + ")");
                System.out.println("Payment Status: Paid=" + rs.getDouble("amount_paid") + ", Due=" + rs.getDouble("amount_due"));
                System.out.println("------------------------");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying students: " + e.getMessage());
        }
    }
    
    public static void updateStudent(String rollNo) {
        Scanner scanner = new Scanner(System.in);
        try {
            // First check if student exists
            String checkSql = "SELECT * FROM students WHERE roll_no = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, rollNo);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("❌ Student not found!");
                return;
            }

            // Store current student details
            String currentName = rs.getString("name");
            String currentMobile = rs.getString("mobile");
            String currentRoom = rs.getString("room_no");
            double currentAmountPaid = rs.getDouble("amount_paid");
            double currentAmountDue = rs.getDouble("amount_due");

            System.out.println("\n=== Update Student ===");
            System.out.println("1. Update Name");
            System.out.println("2. Update Mobile");
            System.out.println("3. Update Room");
            System.out.println("4. Update Payment");
            System.out.print("Enter choice: ");
            
            int choice = Integer.parseInt(scanner.nextLine());
            String sql = "";
            PreparedStatement stmt;
            
            switch (choice) {
                case 1:
                    System.out.print("Enter new name: ");
                    String newName = scanner.nextLine();
                    if (newName.equals(currentName)) {
                        System.out.println("❌ New name is same as current name!");
                        return;
                    }
                    sql = "UPDATE students SET name = ? WHERE roll_no = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, newName);
                    stmt.setString(2, rollNo);
                    break;
                    
                case 2:
                    System.out.print("Enter new mobile: ");
                    String newMobile = scanner.nextLine();
                    if (newMobile.equals(currentMobile)) {
                        System.out.println("❌ New mobile is same as current mobile!");
                        return;
                    }
                    sql = "UPDATE students SET mobile = ? WHERE roll_no = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, newMobile);
                    stmt.setString(2, rollNo);
                    break;
                    
                case 3:
                    System.out.println("\n=== Available Rooms ===");
                    displayAvailableRooms();
                    System.out.print("Enter new room number: ");
                    String newRoom = scanner.nextLine();
                    if (newRoom.equals(currentRoom)) {
                        System.out.println("❌ New room is same as current room!");
                        return;
                    }
                    // First check if the new room exists and has space
                    String checkRoomSql = "SELECT capacity, current_occupancy, room_type, block_name, floor_no FROM rooms WHERE room_no = ?";
                    PreparedStatement checkRoomStmt = connection.prepareStatement(checkRoomSql);
                    checkRoomStmt.setString(1, newRoom);
                    ResultSet roomRs = checkRoomStmt.executeQuery();
                    
                    if (!roomRs.next()) {
                        System.out.println("❌ Room not found!");
                        return;
                    }
                    
                    if (roomRs.getInt("current_occupancy") >= roomRs.getInt("capacity")) {
                        System.out.println("❌ Room is already full!");
                        return;
                    }
                    
                    // Get current sharing type from student's record
                    String currentSharingType = rs.getString("sharing_type");
                    
                    // Update student's room with all related information
                    sql = "UPDATE students SET room_no = ?, room_type = ?, block_name = ?, floor_no = ? WHERE roll_no = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, newRoom);
                    stmt.setString(2, roomRs.getString("room_type"));
                    stmt.setString(3, roomRs.getString("block_name"));
                    stmt.setInt(4, roomRs.getInt("floor_no"));
                    stmt.setString(5, rollNo);
                    int roomUpdateResult = stmt.executeUpdate();
                    
                    if (roomUpdateResult > 0) {
                        // Update room occupancy
                        updateRoomOccupancy();
                        System.out.println("✓ Room updated successfully!");
                        return;
                    } else {
                        System.out.println("❌ Failed to update room!");
                        return;
                    }
                    
                case 4:
                    System.out.print("Enter new amount paid: ");
                    double newAmountPaid = Double.parseDouble(scanner.nextLine());
                    if (newAmountPaid == currentAmountPaid) {
                        System.out.println("❌ New amount is same as current amount!");
                        return;
                    }
                    // Calculate new amount due based on total fee
                    double totalFee = calculateFee(rs.getString("room_type"), rs.getString("sharing_type"));
                    double newAmountDue = totalFee - newAmountPaid;
                    
                    sql = "UPDATE students SET amount_paid = ?, amount_due = ? WHERE roll_no = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setDouble(1, newAmountPaid);
                    stmt.setDouble(2, newAmountDue);
                    stmt.setString(3, rollNo);
                    int paymentUpdateResult = stmt.executeUpdate();
                    
                    if (paymentUpdateResult > 0) {
                        System.out.println("✓ Payment status updated successfully!");
                        System.out.printf("New Payment Status: Paid=%.2f, Due=%.2f\n", newAmountPaid, newAmountDue);
                        return;
                    } else {
                        System.out.println("❌ Failed to update payment status!");
                        return;
                    }
                    
                default:
                    System.out.println("❌ Invalid choice!");
                    return;
            }
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Student details updated successfully!");
            } else {
                System.out.println("❌ Failed to update student details!");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error updating student: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid payment amount format!");
        }
    }
    
    public static boolean validateStudentLogin(String rollNo, String password) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM students WHERE roll_no = ? AND password = ?"
            );
            pstmt.setString(1, rollNo);
            pstmt.setString(2, password);
            
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("❌ Error validating student login: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean validateAdminLogin(String username, String password) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM admins WHERE username = ? AND password = ?"
            );
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("❌ Error validating admin login: " + e.getMessage());
            return false;
        }
    }

    // Warden Management Methods
    public static void displayAllWardens() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM wardens");
            
            System.out.println("\n=== Warden Records ===");
            System.out.println("Warden ID | Name         | Age | Mobile       | Hostel      | Block    | Joining Date");
            System.out.println("----------------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-9s | %-11s | %-3d | %-11s | %-11s | %-8s | %s\n",
                    rs.getString("warden_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("mobile"),
                    rs.getString("assigned_hostel"),
                    rs.getString("block_name"),
                    rs.getDate("joining_date"));
            }
            System.out.println("----------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("❌ Error displaying wardens: " + e.getMessage());
        }
    }

    public static void addNewWarden() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Add New Warden ===");
            System.out.print("Enter Warden ID (e.g., W003): ");
            String wardenId = scanner.nextLine();
            
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter Age: ");
            int age = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter Mobile: ");
            String mobile = scanner.nextLine();
            
            System.out.print("Enter Assigned Hostel: ");
            String hostel = scanner.nextLine();

            System.out.print("Enter Block Name: ");
            String blockName = scanner.nextLine();

            String sql = "INSERT INTO wardens VALUES (?, ?, ?, ?, ?, ?, CURDATE())";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, wardenId);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, mobile);
            pstmt.setString(5, hostel);
            pstmt.setString(6, blockName);
            
            pstmt.executeUpdate();
            System.out.println("✓ Warden added successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error adding warden: " + e.getMessage());
        }
    }

    public static void updateWarden(String wardenId) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Update Warden ===");
            System.out.println("1. Update Name");
            System.out.println("2. Update Mobile");
            System.out.println("3. Update Assigned Hostel");
            System.out.println("4. Update Block Name");
            System.out.print("Enter choice: ");
            
            int choice = Integer.parseInt(scanner.nextLine());
            String sql = "";
            String newValue = "";
            
            switch (choice) {
                case 1:
                    System.out.print("Enter new name: ");
                    newValue = scanner.nextLine();
                    sql = "UPDATE wardens SET name = ? WHERE warden_id = ?";
                    break;
                case 2:
                    System.out.print("Enter new mobile: ");
                    newValue = scanner.nextLine();
                    sql = "UPDATE wardens SET mobile = ? WHERE warden_id = ?";
                    break;
                case 3:
                    System.out.print("Enter new hostel: ");
                    newValue = scanner.nextLine();
                    sql = "UPDATE wardens SET assigned_hostel = ? WHERE warden_id = ?";
                    break;
                case 4:
                    System.out.print("Enter new block name: ");
                    newValue = scanner.nextLine();
                    sql = "UPDATE wardens SET block_name = ? WHERE warden_id = ?";
                    break;
                default:
                    System.out.println("❌ Invalid choice!");
                    return;
            }
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, newValue);
            pstmt.setString(2, wardenId);
            
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✓ Warden updated successfully!");
            } else {
                System.out.println("❌ Warden not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating warden: " + e.getMessage());
        }
    }

    public static void deleteWarden(String wardenId) {
        try {
            String sql = "DELETE FROM wardens WHERE warden_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, wardenId);
            
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✓ Warden deleted successfully!");
            } else {
                System.out.println("❌ Warden not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error deleting warden: " + e.getMessage());
        }
    }

    public static void displayPendingComplaints() {
        try {
            String sql = "SELECT c.*, s.name FROM complaints c JOIN students s ON c.student_roll_no = s.roll_no WHERE c.status = 'Pending'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\n=== Pending Complaints ===");
            while (rs.next()) {
                System.out.println("\nComplaint ID: " + rs.getInt("complaint_id"));
                System.out.println("Student: " + rs.getString("name") + " (" + rs.getString("student_roll_no") + ")");
                System.out.println("Complaint: " + rs.getString("complaint_text"));
                System.out.println("Filed On: " + rs.getTimestamp("filing_date"));
                System.out.println("------------------------");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying pending complaints: " + e.getMessage());
        }
    }

    public static void addNewComplaint() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Add New Complaint ===");
            System.out.print("Enter Student Roll Number: ");
            String rollNo = scanner.nextLine();
            
            System.out.println("Enter Complaint Text: ");
            String complaintText = scanner.nextLine();
            
            String sql = "INSERT INTO complaints (student_roll_no, complaint_text, status, filing_date) VALUES (?, ?, 'Pending', NOW())";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, complaintText);
            
            pstmt.executeUpdate();
            System.out.println("✓ Complaint added successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error adding complaint: " + e.getMessage());
        }
    }

    public static void deleteComplaint(int complaintId) {
        try {
            String sql = "DELETE FROM complaints WHERE complaint_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, complaintId);
            
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✓ Complaint deleted successfully!");
            } else {
                System.out.println("❌ Complaint not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error deleting complaint: " + e.getMessage());
        }
    }

    public static void searchStudentByRoll(String rollNo) {
        try {
            String sql = "SELECT * FROM students WHERE roll_no = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                displayStudentRecord(rs);
            } else {
                System.out.println("❌ Student not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching student: " + e.getMessage());
        }
    }

    public static void searchStudentByName(String name) {
        try {
            String sql = "SELECT * FROM students WHERE name LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            boolean found = false;
            while (rs.next()) {
                displayStudentRecord(rs);
                found = true;
            }
            
            if (!found) {
                System.out.println("❌ No students found with that name!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching students: " + e.getMessage());
        }
    }

    public static void displayStudentDetails(String rollNo) {
        try {
            String sql = "SELECT * FROM students WHERE roll_no = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                displayStudentRecord(rs);
            } else {
                System.out.println("❌ Student details not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying student details: " + e.getMessage());
        }
    }

    public static void displayRoomDetails(String rollNo) {
        try {
            String sql = "SELECT room_no, room_type, sharing_type FROM students WHERE roll_no = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n=== Room Details ===");
                System.out.println("Room Number: " + rs.getString("room_no"));
                System.out.println("Room Type: " + rs.getString("room_type"));
                System.out.println("Sharing Type: " + rs.getString("sharing_type"));
            } else {
                System.out.println("❌ Room details not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying room details: " + e.getMessage());
        }
    }

    public static void displayPaymentStatus(String rollNo) {
        try {
            String sql = "SELECT amount_paid, amount_due FROM students WHERE roll_no = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n=== Payment Status ===");
                System.out.println("Amount Paid: Rs." + rs.getDouble("amount_paid"));
                System.out.println("Amount Due: Rs." + rs.getDouble("amount_due"));
            } else {
                System.out.println("❌ Payment details not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying payment status: " + e.getMessage());
        }
    }

    private static void displayStudentRecord(ResultSet rs) throws SQLException {
        System.out.println("\n=== Student Details ===");
        System.out.println("Roll Number: " + rs.getString("roll_no"));
        System.out.println("Name: " + rs.getString("name"));
        System.out.println("College: " + rs.getString("college"));
        System.out.println("Department: " + rs.getString("department"));
        System.out.println("Semester: " + rs.getString("semester"));
        System.out.println("Age: " + rs.getInt("age"));
        System.out.println("Mobile: " + rs.getString("mobile"));
        if (rs.getString("room_no") != null) {
            System.out.println("Room: " + rs.getString("room_no") + " (" + rs.getString("room_type") + ", " + rs.getString("sharing_type") + ")");
            System.out.println("Block: " + rs.getString("block_name"));
            System.out.println("Floor: " + rs.getInt("floor_no"));
        }
        System.out.println("------------------------");
    }

    public static void searchWardenById(String wardenId) {
        try {
            String sql = "SELECT * FROM wardens WHERE warden_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, wardenId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n=== Warden Details ===");
                System.out.println("Warden ID: " + rs.getString("warden_id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Age: " + rs.getInt("age"));
                System.out.println("Mobile: " + rs.getString("mobile"));
                System.out.println("Assigned Hostel: " + rs.getString("assigned_hostel"));
                System.out.println("Joining Date: " + rs.getDate("joining_date"));
            } else {
                System.out.println("❌ Warden not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching warden: " + e.getMessage());
        }
    }

    public static void searchWardenByName(String name) {
        try {
            String sql = "SELECT * FROM wardens WHERE name LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            boolean found = false;
            while (rs.next()) {
                System.out.println("\n=== Warden Details ===");
                System.out.println("Warden ID: " + rs.getString("warden_id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Age: " + rs.getInt("age"));
                System.out.println("Mobile: " + rs.getString("mobile"));
                System.out.println("Assigned Hostel: " + rs.getString("assigned_hostel"));
                System.out.println("Joining Date: " + rs.getDate("joining_date"));
                System.out.println("------------------------");
                found = true;
            }
            
            if (!found) {
                System.out.println("❌ No wardens found with that name!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching wardens: " + e.getMessage());
        }
    }

    public static void searchComplaintsByStudent(String rollNo) {
        try {
            String sql = "SELECT c.*, s.name FROM complaints c " +
                        "JOIN students s ON c.student_roll_no = s.roll_no " +
                        "WHERE c.student_roll_no = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            boolean found = false;
            while (rs.next()) {
                System.out.println("\n=== Complaint Details ===");
                System.out.println("Complaint ID: " + rs.getInt("complaint_id"));
                System.out.println("Student: " + rs.getString("name") + " (" + rs.getString("student_roll_no") + ")");
                System.out.println("Complaint: " + rs.getString("complaint_text"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Filed On: " + rs.getTimestamp("filing_date"));
                if (rs.getTimestamp("resolution_date") != null) {
                    System.out.println("Resolved On: " + rs.getTimestamp("resolution_date"));
                }
                System.out.println("------------------------");
                found = true;
            }
            
            if (!found) {
                System.out.println("❌ No complaints found for this student!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching complaints: " + e.getMessage());
        }
    }

    public static void searchComplaintsByStatus(String status) {
        try {
            String sql = "SELECT c.*, s.name FROM complaints c " +
                        "JOIN students s ON c.student_roll_no = s.roll_no " +
                        "WHERE c.status = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            boolean found = false;
            while (rs.next()) {
                System.out.println("\n=== Complaint Details ===");
                System.out.println("Complaint ID: " + rs.getInt("complaint_id"));
                System.out.println("Student: " + rs.getString("name") + " (" + rs.getString("student_roll_no") + ")");
                System.out.println("Complaint: " + rs.getString("complaint_text"));
                System.out.println("Filed On: " + rs.getTimestamp("filing_date"));
                if (rs.getTimestamp("resolution_date") != null) {
                    System.out.println("Resolved On: " + rs.getTimestamp("resolution_date"));
                }
                System.out.println("------------------------");
                found = true;
            }
            
            if (!found) {
                System.out.println("❌ No complaints found with status: " + status);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching complaints: " + e.getMessage());
        }
    }

    public static void fileNewComplaint(String rollNo) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== File New Complaint ===");
            System.out.println("Enter your complaint:");
            String complaintText = scanner.nextLine();
            
            String sql = "INSERT INTO complaints (student_roll_no, complaint_text, status, filing_date) VALUES (?, ?, 'Pending', NOW())";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, complaintText);
            
            pstmt.executeUpdate();
            System.out.println("✓ Complaint filed successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error filing complaint: " + e.getMessage());
        }
    }

    public static void viewMyComplaints(String rollNo) {
        try {
            String sql = "SELECT * FROM complaints WHERE student_roll_no = ? ORDER BY filing_date DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n=== My Complaints ===");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("\nComplaint ID: " + rs.getInt("complaint_id"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Filed On: " + rs.getTimestamp("filing_date"));
                System.out.println("Complaint: " + rs.getString("complaint_text"));
                if (rs.getTimestamp("resolution_date") != null) {
                    System.out.println("Resolved On: " + rs.getTimestamp("resolution_date"));
                }
                System.out.println("------------------------");
            }
            
            if (!found) {
                System.out.println("You haven't filed any complaints yet.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error viewing complaints: " + e.getMessage());
        }
    }

    // Room Management Methods
    public static void displayAllRooms() {
        try {
            String sql = "SELECT * FROM rooms ORDER BY room_no";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\n=== Room Details ===");
            System.out.println("Room No | Type     | Capacity | Occupied | Floor | Block");
            System.out.println("--------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-7s | %-8s | %-8d | %-8d | %-5d | %s\n",
                    rs.getString("room_no"),
                    rs.getString("room_type"),
                    rs.getInt("capacity"),
                    rs.getInt("current_occupancy"),
                    rs.getInt("floor_no"),
                    rs.getString("block_name")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying rooms: " + e.getMessage());
        }
    }

    public static void addNewRoom() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Add New Room ===");
            System.out.print("Enter Room Number: ");
            String roomNo = scanner.nextLine();
            
            System.out.println("Room Type:");
            System.out.println("1. Standard");
            System.out.println("2. Luxury");
            System.out.print("Enter choice (1-2): ");
            String roomType = scanner.nextLine().equals("1") ? "Standard" : "Luxury";
            
            System.out.println("Room Capacity:");
            System.out.println("1. 4 Sharing (4 students)");
            System.out.println("2. 2 Sharing (2 students)");
            System.out.println("3. 1 Sharing (1 student)");
            System.out.println("4. No Sharing (1 student)");
            System.out.print("Enter choice (1-4): ");
            int choice = Integer.parseInt(scanner.nextLine());
            int capacity;
            switch(choice) {
                case 1: capacity = 4; break;
                case 2: capacity = 2; break;
                case 3:
                case 4: capacity = 1; break;
                default: throw new IllegalArgumentException("Invalid capacity choice");
            }
            
            System.out.print("Enter Floor Number: ");
            int floorNo = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter Block Name: ");
            String blockName = scanner.nextLine();
            
            String sql = "INSERT INTO rooms (room_no, room_type, capacity, current_occupancy, floor_no, block_name) " +
                        "VALUES (?, ?, ?, 0, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, roomNo);
            pstmt.setString(2, roomType);
            pstmt.setInt(3, capacity);
            pstmt.setInt(4, floorNo);
            pstmt.setString(5, blockName);
            
            pstmt.executeUpdate();
            System.out.println("✓ Room added successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error adding room: " + e.getMessage());
        }
    }

    public static void updateRoom() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Update Room ===");
            System.out.print("Enter Room Number to update: ");
            String roomNo = scanner.nextLine();
            
            System.out.println("What would you like to update?");
            System.out.println("1. Room Type");
            System.out.println("2. Capacity");
            System.out.println("3. Block Name");
            System.out.print("Enter choice: ");
            
            int choice = Integer.parseInt(scanner.nextLine());
            String sql = "";
            
            switch (choice) {
                case 1:
                    System.out.println("New Room Type (1. Standard, 2. Luxury): ");
                    String roomType = scanner.nextLine().equals("1") ? "Standard" : "Luxury";
                    sql = "UPDATE rooms SET room_type = ? WHERE room_no = ?";
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, roomType);
                    pstmt.setString(2, roomNo);
                    pstmt.executeUpdate();
                    break;
                    
                case 2:
                    System.out.println("New Capacity (1-4): ");
                    int capacity = Integer.parseInt(scanner.nextLine());
                    sql = "UPDATE rooms SET capacity = ? WHERE room_no = ?";
                    pstmt = connection.prepareStatement(sql);
                    pstmt.setInt(1, capacity);
                    pstmt.setString(2, roomNo);
                    pstmt.executeUpdate();
                    break;
                    
                case 3:
                    System.out.print("New Block Name: ");
                    String blockName = scanner.nextLine();
                    sql = "UPDATE rooms SET block_name = ? WHERE room_no = ?";
                    pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, blockName);
                    pstmt.setString(2, roomNo);
                    pstmt.executeUpdate();
                    break;
            }
            System.out.println("✓ Room updated successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error updating room: " + e.getMessage());
        }
    }

    public static void deleteRoom() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Delete Room ===");
            System.out.print("Enter Room Number to delete: ");
            String roomNo = scanner.nextLine();
            
            // Check if room is occupied
            String checkSql = "SELECT current_occupancy FROM rooms WHERE room_no = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, roomNo);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt("current_occupancy") > 0) {
                System.out.println("❌ Cannot delete room: Room is currently occupied!");
                return;
            }
            
            String sql = "DELETE FROM rooms WHERE room_no = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, roomNo);
            
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✓ Room deleted successfully!");
            } else {
                System.out.println("❌ Room not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error deleting room: " + e.getMessage());
        }
    }

    private static String getSharingType(int sharingChoice) {
        switch (sharingChoice) {
            case 1: return "4 Sharing";
            case 2: return "2 Sharing";
            case 3: return "1 Sharing";
            case 4: return "No Sharing";
            default: throw new IllegalArgumentException("Invalid sharing choice");
        }
    }

    // Add this method to update room occupancy
    public static void updateRoomOccupancy() {
        try {
            // First display current student room allocations
            System.out.println("\n=== Current Room Allocations ===");
            String selectSql = "SELECT roll_no, name, room_no, room_type, sharing_type FROM students " +
                              "WHERE room_no IS NOT NULL";
            Statement selectStmt = connection.createStatement();
            ResultSet rs = selectStmt.executeQuery(selectSql);
            
            while (rs.next()) {
                System.out.printf("Room %s: %s (%s) - %s, %s\n",
                    rs.getString("room_no"),
                    rs.getString("name"),
                    rs.getString("roll_no"),
                    rs.getString("room_type"),
                    rs.getString("sharing_type"));
            }

            // Update room occupancies
            String updateSql = "UPDATE rooms r SET current_occupancy = (" +
                              "SELECT COUNT(*) FROM students s WHERE s.room_no = r.room_no)";
            Statement updateStmt = connection.createStatement();
            updateStmt.executeUpdate(updateSql);

            // Display updated room occupancies
            System.out.println("\n=== Updated Room Occupancies ===");
            String roomsSql = "SELECT room_no, room_type, capacity, current_occupancy FROM rooms " +
                             "WHERE current_occupancy > 0 ORDER BY room_no";
            rs = selectStmt.executeQuery(roomsSql);
            
            while (rs.next()) {
                System.out.printf("Room %s (%s): %d/%d occupied\n",
                    rs.getString("room_no"),
                    rs.getString("room_type"),
                    rs.getInt("current_occupancy"),
                    rs.getInt("capacity"));
            }
            
            System.out.println("\n✓ Room occupancies updated successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error updating room occupancies: " + e.getMessage());
        }
    }

    public static List<String> getAvailableRooms(String roomType, String sharingType) {
        List<String> availableRooms = new ArrayList<>();
        try {
            String sql = "SELECT room_no FROM rooms " +
                        "WHERE room_type = ? AND sharing_type = ? " +
                        "AND current_occupancy < capacity " +
                        "ORDER BY room_no";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, roomType);
            pstmt.setString(2, sharingType);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                availableRooms.add(rs.getString("room_no"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error getting available rooms: " + e.getMessage());
        }
        return availableRooms;
    }

    public static void registerStudent(String rollNo, String name, String college, 
        String department, String semester, int age, String mobile, String password) {
        try {
            // First get room preferences
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n=== Room Selection ===");
            
            // Get room type preference
            System.out.println("Select Room Type:");
            System.out.println("1. Standard");
            System.out.println("2. Luxury");
            System.out.print("Enter choice (1-2): ");
            String roomType = scanner.nextLine().equals("1") ? "Standard" : "Luxury";
            
            // Get sharing type preference
            System.out.println("\nSelect Sharing Type:");
            System.out.println("1. 4 Sharing");
            System.out.println("2. 2 Sharing");
            System.out.println("3. 1 Sharing");
            System.out.println("4. No Sharing");
            System.out.print("Enter choice (1-4): ");
            int sharingChoice = Integer.parseInt(scanner.nextLine());
            String sharingType = getSharingType(sharingChoice);
            
            // Get available rooms
            List<String> availableRooms = getAvailableRooms(roomType, sharingType);
            
            if (availableRooms.isEmpty()) {
                System.out.println("❌ No rooms available for selected preferences!");
                return;
            }
            
            // Display available rooms
            System.out.println("\nAvailable Rooms:");
            for (String roomNo : availableRooms) {
                System.out.println("Room " + roomNo);
            }
            
            // Get room selection
            System.out.print("\nEnter Room Number from available rooms: ");
            String selectedRoom = scanner.nextLine();
            
            if (!availableRooms.contains(selectedRoom)) {
                System.out.println("❌ Invalid room selection!");
                return;
            }
            
            // Get block and floor information from the selected room
            String roomInfoSql = "SELECT block_name, floor_no FROM rooms WHERE room_no = ?";
            PreparedStatement roomInfoStmt = connection.prepareStatement(roomInfoSql);
            roomInfoStmt.setString(1, selectedRoom);
            ResultSet roomInfo = roomInfoStmt.executeQuery();
            roomInfo.next();
            String blockName = roomInfo.getString("block_name");
            int floorNo = roomInfo.getInt("floor_no");
            
            // Calculate fees
            double totalFee = calculateFee(roomType, sharingType);
            System.out.printf("\nTotal Fee: Rs. %.2f\n", totalFee);
            System.out.print("Enter Initial Payment Amount: ");
            double amountPaid = Double.parseDouble(scanner.nextLine());
            double amountDue = totalFee - amountPaid;

            // Get payment method
            System.out.println("\nSelect Payment Method:");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. UPI");
            System.out.print("Enter choice (1-3): ");
            String paymentMethod;
            switch(scanner.nextLine()) {
                case "1": paymentMethod = "Cash"; break;
                case "2": paymentMethod = "Card"; break;
                case "3": paymentMethod = "UPI"; break;
                default: throw new IllegalArgumentException("Invalid payment method");
            }
            
            // Insert student record
            String sql = "INSERT INTO students (roll_no, name, college, department, " +
                        "semester, age, mobile, room_no, room_type, sharing_type, " +
                        "block_name, floor_no, amount_paid, amount_due, payment_method, password) VALUES " +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, name);
            pstmt.setString(3, college);
            pstmt.setString(4, department);
            pstmt.setString(5, semester);
            pstmt.setInt(6, age);
            pstmt.setString(7, mobile);
            pstmt.setString(8, selectedRoom);
            pstmt.setString(9, roomType);
            pstmt.setString(10, sharingType);
            pstmt.setString(11, blockName);
            pstmt.setInt(12, floorNo);
            pstmt.setDouble(13, amountPaid);
            pstmt.setDouble(14, amountDue);
            pstmt.setString(15, paymentMethod);
            pstmt.setString(16, password);
            
            pstmt.executeUpdate();
            
            // Update room occupancy
            updateRoomOccupancy();
            
            System.out.println("✓ Student registered successfully!");
            System.out.printf("Amount Due: Rs. %.2f\n", amountDue);
            
        } catch (SQLException e) {
            System.out.println("❌ Error registering student: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format!");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    public static void fixRoomInconsistencies() {
        try {
            // Fix room type for Room 102 (currently shows Luxury for Priya but was Standard in rooms table)
            String updateStudentSql = "UPDATE students SET room_type = " +
                                    "(SELECT room_type FROM rooms WHERE room_no = students.room_no) " +
                                    "WHERE room_no IS NOT NULL";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(updateStudentSql);
            
            System.out.println("✓ Room type inconsistencies fixed!");
        } catch (SQLException e) {
            System.out.println("❌ Error fixing room inconsistencies: " + e.getMessage());
        }
    }

    private static double calculateFee(String roomType, String sharingType) {
        return calculateRoomFee(roomType, sharingType);
    }

    /**
     * Calculates the per-person fee for a room based on its type and sharing preference.
     * @param roomType "Luxury" or "Standard"
     * @param sharingType "1 Sharing", "2 Sharing", or "4 Sharing"
     * @return The calculated fee per person
     */
    public static double calculateRoomFee(String roomType, String sharingType) {
        double baseCost = "Luxury".equals(roomType) ? LUXURY_BASE_PRICE : STANDARD_BASE_PRICE;
        int numOccupants;
        
        // Normalize sharing type format by removing hyphen and converting to standard format
        String normalizedSharingType = sharingType.replace("-", " ")  // Convert "4-sharing" to "4 sharing"
                                               .replace("Sharing", "Sharing")  // Ensure proper capitalization
                                               .trim();  // Remove any extra spaces
        
        switch (normalizedSharingType.toLowerCase()) {
            case "4 sharing":
            case "4-sharing":
            case "4sharing":
                numOccupants = 4;
                break;
            case "2 sharing":
            case "2-sharing":
            case "2sharing":
                numOccupants = 2;
                break;
            case "1 sharing":
            case "1-sharing":
            case "1sharing":
            case "no sharing":
                numOccupants = 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid sharing type: " + sharingType + 
                    ". Expected formats: '4 Sharing', '2 Sharing', '1 Sharing', or 'No Sharing'");
        }
        
        // Calculate per-person cost by dividing total room cost by number of occupants
        return baseCost / numOccupants;
    }

    public static void addNewStudent() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Add New Student ===");
            System.out.print("Enter Roll Number: ");
            String rollNo = scanner.nextLine();
            
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter College: ");
            String college = scanner.nextLine();
            
            System.out.print("Enter Department: ");
            String department = scanner.nextLine();
            
            System.out.print("Enter Semester: ");
            String semester = scanner.nextLine();
            
            System.out.print("Enter Age: ");
            int age = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter Mobile: ");
            String mobile = scanner.nextLine();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            registerStudent(rollNo, name, college, department, semester, age, mobile, password);
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format!");
        }
    }

    public static void deleteStudent(String rollNo) {
        try {
            String sql = "DELETE FROM students WHERE roll_no = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✓ Student deleted successfully!");
                // Update room occupancy after deleting student
                updateRoomOccupancy();
            } else {
                System.out.println("❌ Student not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error deleting student: " + e.getMessage());
        }
    }

    public static void displayAllComplaints() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT c.*, s.name FROM complaints c " +
                "JOIN students s ON c.student_roll_no = s.roll_no"
            );
            
            System.out.println("\n=== Complaint Records ===");
            while (rs.next()) {
                System.out.println("\nComplaint ID: " + rs.getInt("complaint_id"));
                System.out.println("Student: " + rs.getString("name") + " (" + rs.getString("student_roll_no") + ")");
                System.out.println("Complaint: " + rs.getString("complaint_text"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Filed On: " + rs.getTimestamp("filing_date"));
                if (rs.getTimestamp("resolution_date") != null) {
                    System.out.println("Resolved On: " + rs.getTimestamp("resolution_date"));
                }
                System.out.println("------------------------");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying complaints: " + e.getMessage());
        }
    }

    public static void updateComplaintStatus(int complaintId) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("\n=== Update Complaint Status ===");
            System.out.println("1. Pending");
            System.out.println("2. In Progress");
            System.out.println("3. Resolved");
            System.out.print("Enter new status (1-3): ");
            
            int choice = Integer.parseInt(scanner.nextLine());
            String status;
            
            switch (choice) {
                case 1:
                    status = "Pending";
                    break;
                case 2:
                    status = "In Progress";
                    break;
                case 3:
                    status = "Resolved";
                    break;
                default:
                    System.out.println("❌ Invalid choice!");
                    return;
            }

            String sql = "UPDATE complaints SET status = ?, resolution_date = ? WHERE complaint_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setTimestamp(2, status.equals("Resolved") ? new Timestamp(System.currentTimeMillis()) : null);
            pstmt.setInt(3, complaintId);
            
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✓ Complaint status updated successfully!");
            } else {
                System.out.println("❌ Complaint not found!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating complaint: " + e.getMessage());
        }
    }

    public static void displayAvailableRooms() {
        try {
            String sql = "SELECT r.*, COUNT(s.roll_no) as current_occupancy " +
                        "FROM rooms r " +
                        "LEFT JOIN students s ON r.room_no = s.room_no " +
                        "GROUP BY r.room_no " +
                        "ORDER BY r.room_no";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\nRoom No | Type     | Capacity | Occupied | Available | Block    | Floor");
            System.out.println("----------------------------------------------------------------");
            while (rs.next()) {
                int available = rs.getInt("capacity") - rs.getInt("current_occupancy");
                System.out.printf("%-8s | %-9s | %-8d | %-9d | %-9d | %-9s | %-5d\n",
                    rs.getString("room_no"),
                    rs.getString("room_type"),
                    rs.getInt("capacity"),
                    rs.getInt("current_occupancy"),
                    available,
                    rs.getString("block_name"),
                    rs.getInt("floor_no"));
            }
            System.out.println("----------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("❌ Error displaying available rooms: " + e.getMessage());
        }
    }

    // GUI-friendly methods for Complaint Management
    public static List<Map<String, Object>> getAllComplaints() {
        List<Map<String, Object>> complaints = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT c.*, s.name FROM complaints c " +
                "JOIN students s ON c.student_roll_no = s.roll_no " +
                "ORDER BY c.filing_date DESC"
            );
            
            while (rs.next()) {
                Map<String, Object> complaint = new HashMap<>();
                complaint.put("id", rs.getInt("complaint_id"));
                complaint.put("studentName", rs.getString("name"));
                complaint.put("rollNo", rs.getString("student_roll_no"));
                complaint.put("text", rs.getString("complaint_text"));
                complaint.put("status", rs.getString("status"));
                complaint.put("filedOn", rs.getTimestamp("filing_date"));
                complaint.put("resolvedOn", rs.getTimestamp("resolution_date"));
                complaints.add(complaint);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching complaints: " + e.getMessage());
        }
        return complaints;
    }

    public static List<Map<String, Object>> getPendingComplaints() {
        List<Map<String, Object>> complaints = new ArrayList<>();
        try {
            String sql = "SELECT c.*, s.name FROM complaints c " +
                        "JOIN students s ON c.student_roll_no = s.roll_no " +
                        "WHERE c.status = 'Pending' " +
                        "ORDER BY c.filing_date DESC";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> complaint = new HashMap<>();
                complaint.put("id", rs.getInt("complaint_id"));
                complaint.put("studentName", rs.getString("name"));
                complaint.put("rollNo", rs.getString("student_roll_no"));
                complaint.put("text", rs.getString("complaint_text"));
                complaint.put("filedOn", rs.getTimestamp("filing_date"));
                complaints.add(complaint);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching pending complaints: " + e.getMessage());
        }
        return complaints;
    }

    // GUI-friendly methods for Room Management
    public static List<Map<String, Object>> getAllRooms() {
        List<Map<String, Object>> rooms = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT r.*, COUNT(s.roll_no) as current_occupancy " +
                "FROM rooms r LEFT JOIN students s ON r.room_no = s.room_no " +
                "GROUP BY r.room_no ORDER BY r.room_no"
            );
            
            while (rs.next()) {
                Map<String, Object> room = new HashMap<>();
                room.put("roomNo", rs.getString("room_no"));
                room.put("type", rs.getString("room_type"));
                room.put("capacity", rs.getInt("capacity"));
                room.put("currentOccupancy", rs.getInt("current_occupancy"));
                room.put("block", rs.getString("block_name"));
                room.put("floor", rs.getInt("floor_no"));
                room.put("sharingType", rs.getString("sharing_type"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching rooms: " + e.getMessage());
        }
        return rooms;
    }

    // GUI-friendly methods for Warden Management
    public static List<Map<String, Object>> getAllWardens() {
        List<Map<String, Object>> wardens = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM wardens ORDER BY warden_id");
            
            while (rs.next()) {
                Map<String, Object> warden = new HashMap<>();
                warden.put("id", rs.getString("warden_id"));
                warden.put("name", rs.getString("name"));
                warden.put("age", rs.getInt("age"));
                warden.put("mobile", rs.getString("mobile"));
                warden.put("hostel", rs.getString("assigned_hostel"));
                warden.put("block", rs.getString("block_name"));
                warden.put("joiningDate", rs.getDate("joining_date"));
                wardens.add(warden);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching wardens: " + e.getMessage());
        }
        return wardens;
    }

    // GUI-friendly methods for Student Management
    public static List<Map<String, Object>> getAllStudents() {
        List<Map<String, Object>> students = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY roll_no");
            
            while (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("rollNo", rs.getString("roll_no"));
                student.put("name", rs.getString("name"));
                student.put("college", rs.getString("college"));
                student.put("department", rs.getString("department"));
                student.put("semester", rs.getString("semester"));
                student.put("age", rs.getInt("age"));
                student.put("mobile", rs.getString("mobile"));
                student.put("roomNo", rs.getString("room_no"));
                student.put("roomType", rs.getString("room_type"));
                student.put("sharingType", rs.getString("sharing_type"));
                student.put("blockName", rs.getString("block_name"));
                student.put("floorNo", rs.getInt("floor_no"));
                student.put("amountPaid", rs.getDouble("amount_paid"));
                student.put("amountDue", rs.getDouble("amount_due"));
                students.add(student);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching students: " + e.getMessage());
        }
        return students;
    }

    // GUI-friendly search methods
    public static List<Map<String, Object>> searchComplaints(String searchTerm) {
        List<Map<String, Object>> complaints = new ArrayList<>();
        try {
            String sql = "SELECT c.*, s.name FROM complaints c " +
                        "JOIN students s ON c.student_roll_no = s.roll_no " +
                        "WHERE c.student_roll_no = ? OR s.name LIKE ? OR c.status = ? " +
                        "ORDER BY c.filing_date DESC";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, "%" + searchTerm + "%");
            pstmt.setString(3, searchTerm);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> complaint = new HashMap<>();
                complaint.put("id", rs.getInt("complaint_id"));
                complaint.put("studentName", rs.getString("name"));
                complaint.put("rollNo", rs.getString("student_roll_no"));
                complaint.put("text", rs.getString("complaint_text"));
                complaint.put("status", rs.getString("status"));
                complaint.put("filedOn", rs.getTimestamp("filing_date"));
                complaint.put("resolvedOn", rs.getTimestamp("resolution_date"));
                complaints.add(complaint);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching complaints: " + e.getMessage());
        }
        return complaints;
    }

    public static List<Map<String, Object>> searchStudents(String searchTerm) {
        List<Map<String, Object>> students = new ArrayList<>();
        try {
            String sql = "SELECT * FROM students WHERE roll_no = ? OR name LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("rollNo", rs.getString("roll_no"));
                student.put("name", rs.getString("name"));
                student.put("college", rs.getString("college"));
                student.put("department", rs.getString("department"));
                student.put("semester", rs.getString("semester"));
                student.put("age", rs.getInt("age"));
                student.put("mobile", rs.getString("mobile"));
                student.put("roomNo", rs.getString("room_no"));
                student.put("roomType", rs.getString("room_type"));
                student.put("sharingType", rs.getString("sharing_type"));
                student.put("blockName", rs.getString("block_name"));
                student.put("floorNo", rs.getInt("floor_no"));
                student.put("amountPaid", rs.getDouble("amount_paid"));
                student.put("amountDue", rs.getDouble("amount_due"));
                students.add(student);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching students: " + e.getMessage());
        }
        return students;
    }

    public static List<Map<String, Object>> searchWardens(String searchTerm) {
        List<Map<String, Object>> wardens = new ArrayList<>();
        try {
            String sql = "SELECT * FROM wardens WHERE warden_id = ? OR name LIKE ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> warden = new HashMap<>();
                warden.put("id", rs.getString("warden_id"));
                warden.put("name", rs.getString("name"));
                warden.put("age", rs.getInt("age"));
                warden.put("mobile", rs.getString("mobile"));
                warden.put("hostel", rs.getString("assigned_hostel"));
                warden.put("block", rs.getString("block_name"));
                warden.put("joiningDate", rs.getDate("joining_date"));
                wardens.add(warden);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error searching wardens: " + e.getMessage());
        }
        return wardens;
    }

    public static void updateStudentRoom(String rollNo, String roomNo, String reason) {
        try {
            connection.setAutoCommit(false);  // Start transaction
            try {
                // Step 1: Get current student details with room info
                String currentStudentSql = 
                    "SELECT s.*, r.room_type as actual_room_type, r.sharing_type as actual_sharing_type, " +
                    "r.block_name as actual_block_name, r.floor_no as actual_floor_no " +
                    "FROM students s " +
                    "LEFT JOIN rooms r ON s.room_no = r.room_no " +
                    "WHERE s.roll_no = ? FOR UPDATE";
                
                Map<String, Object> currentDetails = new HashMap<>();
                try (PreparedStatement stmt = connection.prepareStatement(currentStudentSql)) {
                    stmt.setString(1, rollNo);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        currentDetails.put("oldRoomNo", rs.getString("room_no"));
                        currentDetails.put("oldRoomType", rs.getString("actual_room_type"));
                        currentDetails.put("oldSharingType", rs.getString("actual_sharing_type"));
                        currentDetails.put("oldBlockName", rs.getString("actual_block_name"));
                        currentDetails.put("oldFloorNo", rs.getInt("actual_floor_no"));
                        currentDetails.put("amountPaid", rs.getDouble("amount_paid"));
                        currentDetails.put("amountDue", rs.getDouble("amount_due"));
                    } else {
                        throw new SQLException("Student not found: " + rollNo);
                    }
                }

                // Step 2: Close current room history entry if exists
                String oldRoomNo = (String) currentDetails.get("oldRoomNo");
                if (oldRoomNo != null) {
                    // Update room history with check_out time
                    String closeHistorySql = 
                        "UPDATE room_history SET check_out = CURRENT_TIMESTAMP, reason = ? " +
                        "WHERE student_id = ? AND room_no = ? AND check_out IS NULL";
                    try (PreparedStatement stmt = connection.prepareStatement(closeHistorySql)) {
                        stmt.setString(1, reason);
                        stmt.setString(2, rollNo);
                        stmt.setString(3, oldRoomNo);
                        stmt.executeUpdate();
                    }

                    // Decrement old room occupancy
                    String updateOldRoomSql = 
                        "UPDATE rooms SET current_occupancy = current_occupancy - 1 " +
                        "WHERE room_no = ? AND current_occupancy > 0";
                    try (PreparedStatement stmt = connection.prepareStatement(updateOldRoomSql)) {
                        stmt.setString(1, oldRoomNo);
                        stmt.executeUpdate();
                    }
                }

                // Step 3: Handle room vacation
            if (roomNo == null) {
                    // Calculate refund based on actual room type
                    double refundAmount = 0.0;
                    if (currentDetails.get("oldRoomType") != null && currentDetails.get("oldSharingType") != null) {
                        double oldPrice = calculateRoomFee(
                            (String) currentDetails.get("oldRoomType"), 
                            (String) currentDetails.get("oldSharingType")
                        );
                        double currentPaid = (Double) currentDetails.get("amountPaid");
                        refundAmount = Math.max(0, currentPaid - oldPrice);
                    }

                    // Update student record for vacation
                    String vacateSql = 
                        "UPDATE students SET " +
                        "room_no = NULL, room_type = NULL, sharing_type = NULL, " +
                        "block_name = NULL, floor_no = NULL, " +
                        "amount_paid = amount_paid - ?, amount_due = 0 " +
                              "WHERE roll_no = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(vacateSql)) {
                        stmt.setDouble(1, refundAmount);
                        stmt.setString(2, rollNo);
                        stmt.executeUpdate();
                    }

                    // Record refund in payment history if applicable
                    if (refundAmount > 0) {
                        String refundSql = 
                            "INSERT INTO payment_history (student_id, amount, type, reason) " +
                            "VALUES (?, ?, 'REFUND', ?)";
                        try (PreparedStatement stmt = connection.prepareStatement(refundSql)) {
                            stmt.setString(1, rollNo);
                            stmt.setDouble(2, refundAmount);
                            stmt.setString(3, "Room vacation refund");
                            stmt.executeUpdate();
                        }
                    }

                    System.out.println("✓ Room vacated successfully!");
                    if (refundAmount > 0) {
                        System.out.printf("Refund amount: Rs. %.2f\n", refundAmount);
                    }
                } 
                // Step 4: Handle room transfer
                else {
                    // Get new room details
                    String newRoomSql = "SELECT * FROM rooms WHERE room_no = ? FOR UPDATE";
                    Map<String, Object> newRoomDetails = new HashMap<>();
                    try (PreparedStatement stmt = connection.prepareStatement(newRoomSql)) {
                        stmt.setString(1, roomNo);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            newRoomDetails.put("roomType", rs.getString("room_type"));
                            newRoomDetails.put("sharingType", rs.getString("sharing_type"));
                            newRoomDetails.put("blockName", rs.getString("block_name"));
                            newRoomDetails.put("floorNo", rs.getInt("floor_no"));
                            newRoomDetails.put("capacity", rs.getInt("capacity"));
                            newRoomDetails.put("currentOccupancy", rs.getInt("current_occupancy"));
                        } else {
                            throw new SQLException("Room not found: " + roomNo);
                        }
                    }

                    // Validate room capacity
                    int capacity = (Integer) newRoomDetails.get("capacity");
                    int occupancy = (Integer) newRoomDetails.get("currentOccupancy");
                    if (occupancy >= capacity) {
                        throw new SQLException("Room " + roomNo + " is at full capacity");
                    }

                    // Calculate price difference
                    double oldPrice = 0.0;
                    if (currentDetails.get("oldRoomType") != null) {
                        oldPrice = calculateRoomFee(
                            (String) currentDetails.get("oldRoomType"),
                            (String) currentDetails.get("oldSharingType")
                        );
                    }
                    double newPrice = calculateRoomFee(
                        (String) newRoomDetails.get("roomType"),
                        (String) newRoomDetails.get("sharingType")
                    );
                    double priceDiff = newPrice - oldPrice;

                    // Update student record with new room details
                    String transferSql = 
                        "UPDATE students SET " +
                        "room_no = ?, room_type = ?, sharing_type = ?, " +
                        "block_name = ?, floor_no = ?, " +
                        "amount_due = amount_due + ? " +
                        "WHERE roll_no = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(transferSql)) {
                        stmt.setString(1, roomNo);
                        stmt.setString(2, (String) newRoomDetails.get("roomType"));
                        stmt.setString(3, (String) newRoomDetails.get("sharingType"));
                        stmt.setString(4, (String) newRoomDetails.get("blockName"));
                        stmt.setInt(5, (Integer) newRoomDetails.get("floorNo"));
                        stmt.setDouble(6, Math.max(0, priceDiff));  // Only add positive difference to dues
                        stmt.setString(7, rollNo);
                        stmt.executeUpdate();
                    }

                    // Create new room history entry
                    String historySql = 
                        "INSERT INTO room_history " +
                        "(student_id, room_no, room_type, sharing_type, block, floor, reason) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(historySql)) {
                        stmt.setString(1, rollNo);
                        stmt.setString(2, roomNo);
                        stmt.setString(3, (String) newRoomDetails.get("roomType"));
                        stmt.setString(4, (String) newRoomDetails.get("sharingType"));
                        stmt.setString(5, (String) newRoomDetails.get("blockName"));
                        stmt.setInt(6, (Integer) newRoomDetails.get("floorNo"));
                        stmt.setString(7, reason);
                        stmt.executeUpdate();
                    }

                    // Update new room occupancy
                    String updateNewRoomSql = 
                        "UPDATE rooms SET current_occupancy = current_occupancy + 1 " +
                        "WHERE room_no = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(updateNewRoomSql)) {
                        stmt.setString(1, roomNo);
                        stmt.executeUpdate();
                    }

                    // Record payment adjustment in history if needed
                    if (priceDiff != 0) {
                        String paymentSql = 
                            "INSERT INTO payment_history (student_id, amount, type, reason) " +
                            "VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmt = connection.prepareStatement(paymentSql)) {
                            stmt.setString(1, rollNo);
                            stmt.setDouble(2, Math.abs(priceDiff));
                            stmt.setString(3, priceDiff > 0 ? "CHARGE" : "REFUND");
                            stmt.setString(4, priceDiff > 0 ? "Room upgrade charge" : "Room downgrade refund");
                            stmt.executeUpdate();
                        }
                    }

                    System.out.println("✓ Room transfer completed successfully!");
                    if (priceDiff > 0) {
                        System.out.printf("Additional payment due: Rs. %.2f\n", priceDiff);
                    } else if (priceDiff < 0) {
                        System.out.printf("Refund amount: Rs. %.2f\n", -priceDiff);
                    }
                }

                // Step 5: Final verification
                String verifySql = 
                    "SELECT s.*, r.current_occupancy " +
                    "FROM students s " +
                    "LEFT JOIN rooms r ON s.room_no = r.room_no " +
                    "WHERE s.roll_no = ?";
                try (PreparedStatement stmt = connection.prepareStatement(verifySql)) {
                    stmt.setString(1, rollNo);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        // Verify room details match
                        if (roomNo == null && rs.getString("room_no") != null) {
                            throw new SQLException("Room vacation failed - room still assigned");
                        } else if (roomNo != null && !roomNo.equals(rs.getString("room_no"))) {
                            throw new SQLException("Room transfer failed - wrong room assigned");
                        }
                    }
                }

                connection.commit();
        } catch (SQLException e) {
                connection.rollback();
            throw e;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error updating student room: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Warning: Could not reset auto-commit: " + e.getMessage());
            }
        }
    }

    public static void displayMealPlanDetails() {
        try {
            String sql = "SELECT * FROM meal_plan_details ORDER BY monthly_price";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\n=== Available Meal Plans ===");
            while (rs.next()) {
                System.out.println("\nPlan Type: " + rs.getString("plan_type"));
                System.out.println("Monthly Price: Rs." + rs.getDouble("monthly_price"));
                System.out.println("Description: " + rs.getString("description"));
                System.out.println("\nBreakfast Items:");
                System.out.println(rs.getString("breakfast_items"));
                System.out.println("\nLunch Items:");
                System.out.println(rs.getString("lunch_items"));
                System.out.println("\nDinner Items:");
                System.out.println(rs.getString("dinner_items"));
                System.out.println("\nSpecial Features: " + rs.getString("special_features"));
                System.out.println("----------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying meal plan details: " + e.getMessage());
        }
    }

    // Add method to get meal plan price
    public static double getMealPlanPrice(String planType) {
        try {
            String sql = "SELECT monthly_price FROM meal_plan_details WHERE plan_type = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, planType);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("monthly_price");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error getting meal plan price: " + e.getMessage());
        }
        return 0.0;
    }

    // Create room_history table if it doesn't exist
    private static void createRoomHistoryTable() throws SQLException {
        String sql = "CREATE TABLE room_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id VARCHAR(20), " +
            "room_no VARCHAR(10), " +
                    "room_type VARCHAR(20), " +
                    "sharing_type VARCHAR(20), " +
                    "block VARCHAR(20), " +
                    "floor INT, " +
                    "check_in TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "check_out TIMESTAMP NULL, " +
                    "reason VARCHAR(100), " +
                    "FOREIGN KEY (student_id) REFERENCES students(roll_no) ON DELETE SET NULL" +
                    ")";
        Statement stmt = connection.createStatement();
            stmt.execute(sql);
    }

    private static void createStudentsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students (" +
            "roll_no VARCHAR(20) PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "college VARCHAR(100), " +
            "department VARCHAR(50), " +
            "semester VARCHAR(20), " +
            "age INT, " +
            "mobile VARCHAR(15), " +
            "room_no VARCHAR(10), " +
            "password VARCHAR(50), " +
            "amount_paid DECIMAL(10,2) DEFAULT 0.00, " +
            "amount_due DECIMAL(10,2) DEFAULT 0.00, " +
            "payment_method VARCHAR(20), " +
            "FOREIGN KEY (room_no) REFERENCES rooms(room_no) ON DELETE SET NULL" +
            ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void createRoomsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS rooms (" +
            "room_no VARCHAR(10) PRIMARY KEY, " +
            "room_type VARCHAR(20) NOT NULL, " +
            "sharing_type VARCHAR(20) NOT NULL, " +
            "capacity INT NOT NULL, " +
            "current_occupancy INT DEFAULT 0, " +
            "block_name VARCHAR(20), " +
            "floor_no INT" +
            ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Initialize tables
    public static void initializeTables() throws SQLException {
        createStudentsTable();
        createRoomsTable();
        createRoomHistoryTable();
        // ... existing table creation calls ...
    }

    private static void dropTableIfExists(String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createWardenTable() throws SQLException {
        String sql = "CREATE TABLE wardens (" +
                    "warden_id VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "age INT, " +
                    "mobile VARCHAR(15), " +
                    "assigned_hostel VARCHAR(50), " +
                    "block_name VARCHAR(20), " +
                    "joining_date DATE" +
                    ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createRoomTable() throws SQLException {
        String sql = "CREATE TABLE rooms (" +
                    "room_no VARCHAR(10) PRIMARY KEY, " +
                    "room_type ENUM('Standard', 'Luxury') NOT NULL, " +
                    "sharing_type ENUM('1 Sharing', '2 Sharing', '4 Sharing') NOT NULL, " +
                    "capacity INT NOT NULL, " +
                    "current_occupancy INT DEFAULT 0, " +
                    "block_name VARCHAR(20), " +
                    "floor_no INT" +
                    ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createStudentTable() throws SQLException {
        String sql = "CREATE TABLE students (" +
                    "roll_no VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "college VARCHAR(100), " +
                    "department VARCHAR(50), " +
                    "semester VARCHAR(20), " +
                    "age INT, " +
                    "mobile VARCHAR(15), " +
                    "room_no VARCHAR(10), " +
                    "room_type ENUM('Standard', 'Luxury'), " +
                    "sharing_type ENUM('1 Sharing', '2 Sharing', '4 Sharing'), " +
                    "block_name VARCHAR(20), " +
                    "floor_no INT, " +
                    "amount_paid DECIMAL(10,2) DEFAULT 0.00, " +
                    "amount_due DECIMAL(10,2) DEFAULT 0.00, " +
                    "payment_method ENUM('Cash', 'Card', 'UPI'), " +
                    "password VARCHAR(50) NOT NULL, " +
                    "FOREIGN KEY (room_no) REFERENCES rooms(room_no) ON DELETE SET NULL" +
                    ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createComplaintTable() throws SQLException {
        String sql = "CREATE TABLE complaints (" +
                    "complaint_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_roll_no VARCHAR(20), " +
                    "complaint_text TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "filing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "resolution_date TIMESTAMP NULL, " +
                    "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE SET NULL" +
                    ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createMealPlanDetailsTable() throws SQLException {
        String sql = "CREATE TABLE meal_plan_details (" +
                    "plan_type ENUM('Basic', 'Standard', 'Premium') PRIMARY KEY, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "features TEXT NOT NULL, " +
                    "sample_menu TEXT NOT NULL" +
                    ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createMealPlanTable() throws SQLException {
        String sql = "CREATE TABLE meal_plans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_roll_no VARCHAR(20), " +
                    "plan_type ENUM('Basic', 'Standard', 'Premium'), " +
                    "start_date DATE NOT NULL, " +
                    "end_date DATE NOT NULL, " +
                    "payment_status ENUM('Pending', 'Paid') DEFAULT 'Pending', " +
                    "payment_method VARCHAR(20), " +
                    "payment_details VARCHAR(100), " +
                    "amount_paid_for_plan DECIMAL(10,2) DEFAULT 0.00," + // Added column
                    "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE, " +
                    "FOREIGN KEY (plan_type) REFERENCES meal_plan_details(plan_type), " +
                    "UNIQUE KEY unique_student (student_roll_no)" +
                    ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createDailyMenuTable() throws SQLException {
        String sql = "CREATE TABLE daily_menu (" +
                    "menu_date DATE PRIMARY KEY, " +
                    "breakfast TEXT, " +
                    "lunch TEXT, " +
                    "dinner TEXT" +
                    ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private static void createMealFeedbackTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS meal_feedback (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "student_roll_no VARCHAR(20)," +
            "feedback_text TEXT NOT NULL," +
            "feedback_date DATE NOT NULL," +
            "FOREIGN KEY (student_roll_no) REFERENCES students(roll_no) ON DELETE CASCADE," +
            "UNIQUE KEY unique_daily_feedback (student_roll_no, feedback_date)" +
            ")";
        connection.createStatement().executeUpdate(sql);
    }

    public static boolean submitFeedback(String rollNo, String feedbackText) {
        try {
            String sql = "REPLACE INTO meal_feedback (student_roll_no, feedback_text, feedback_date) VALUES (?, ?, CURRENT_DATE)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, feedbackText);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handlePaymentAdjustment(String rollNo, double priceDiff, Connection conn) throws SQLException {
        if (priceDiff == 0) return; // No adjustment needed
        
        String updateSql = "UPDATE students SET ";
        if (priceDiff > 0) {
            // Student needs to pay more
            updateSql += "amount_due = amount_due + ? WHERE roll_no = ?";
        } else {
            // Student should get a refund
            double refundAmount = -priceDiff;
            updateSql += "amount_paid = amount_paid - ?, amount_due = GREATEST(0, amount_due - ?) WHERE roll_no = ?";
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            if (priceDiff > 0) {
                pstmt.setDouble(1, priceDiff);
                pstmt.setString(2, rollNo);
            } else {
                double refundAmount = -priceDiff;
                pstmt.setDouble(1, refundAmount);
                pstmt.setDouble(2, refundAmount);
                pstmt.setString(3, rollNo);
            }
            pstmt.executeUpdate();
            
            // Record the transaction
            String transactionSql = "INSERT INTO payment_history (student_id, amount, type, reason) VALUES (?, ?, ?, ?)";
            try (PreparedStatement transPstmt = conn.prepareStatement(transactionSql)) {
                transPstmt.setString(1, rollNo);
                transPstmt.setDouble(2, Math.abs(priceDiff));
                transPstmt.setString(3, priceDiff > 0 ? "CHARGE" : "REFUND");
                transPstmt.setString(4, "Room transfer adjustment");
                transPstmt.executeUpdate();
            }
        }
    }

    // Create payment_history table
    private static void createPaymentHistoryTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS payment_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id VARCHAR(20), " +
                    "amount DECIMAL(10,2) NOT NULL, " +
                    "type ENUM('PAYMENT', 'REFUND', 'CHARGE') NOT NULL, " +
                    "reason VARCHAR(100), " +
                    "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (student_id) REFERENCES students(roll_no) ON DELETE SET NULL" +
                    ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static List<Map<String, Object>> getPaymentHistory(String rollNo) {
        List<Map<String, Object>> history = new ArrayList<>();
        try {
            String sql = "SELECT ph.*, s.name as student_name " +
                        "FROM payment_history ph " +
                        "JOIN students s ON ph.student_id = s.roll_no " +
                        "WHERE ph.student_id = ? " +
                        "ORDER BY ph.transaction_date DESC";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, rollNo);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("id", rs.getInt("id"));
                    transaction.put("studentName", rs.getString("student_name"));
                    transaction.put("amount", rs.getDouble("amount"));
                    transaction.put("type", rs.getString("type"));
                    transaction.put("reason", rs.getString("reason"));
                    transaction.put("date", rs.getTimestamp("transaction_date"));
                    history.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payment history: " + e.getMessage());
        }
        return history;
    }

    public static String getPaymentSummary(String rollNo) {
        try {
            String sql = "SELECT " +
                        "SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END) as total_paid, " +
                        "SUM(CASE WHEN type = 'CHARGE' THEN amount ELSE 0 END) as total_charges, " +
                        "SUM(CASE WHEN type = 'REFUND' THEN amount ELSE 0 END) as total_refunds " +
                        "FROM payment_history WHERE student_id = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, rollNo);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    double totalPaid = rs.getDouble("total_paid");
                    double totalCharges = rs.getDouble("total_charges");
                    double totalRefunds = rs.getDouble("total_refunds");
                    
                    return String.format("Payment Summary:\n" +
                                       "Total Paid: Rs. %.2f\n" +
                                       "Total Charges: Rs. %.2f\n" +
                                       "Total Refunds: Rs. %.2f\n" +
                                       "Net Balance: Rs. %.2f",
                                       totalPaid, totalCharges, totalRefunds,
                                       totalPaid - totalCharges + totalRefunds);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment summary: " + e.getMessage());
        }
        return "Payment summary not available";
    }

    public static void validateAndFixRoomAssignments() {
        try {
            connection.setAutoCommit(false);
            try {
                // Find inconsistencies between rooms and student records
                String checkSql = 
                    "SELECT s.roll_no, s.name, s.room_no, " +
                    "s.room_type as student_room_type, r.room_type as actual_room_type, " +
                    "s.sharing_type as student_sharing_type, r.sharing_type as actual_sharing_type, " +
                    "s.block_name as student_block, r.block_name as actual_block, " +
                    "s.floor_no as student_floor, r.floor_no as actual_floor, " +
                    "s.amount_paid, s.amount_due " +
                    "FROM students s " +
                    "JOIN rooms r ON s.room_no = r.room_no " +
                    "WHERE s.room_type != r.room_type " +
                    "OR s.sharing_type != r.sharing_type " +
                    "OR s.block_name != r.block_name " +
                    "OR s.floor_no != r.floor_no";

                List<Map<String, Object>> inconsistencies = new ArrayList<>();
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(checkSql);
                    while (rs.next()) {
                        Map<String, Object> record = new HashMap<>();
                        record.put("rollNo", rs.getString("roll_no"));
                        record.put("name", rs.getString("name"));
                        record.put("roomNo", rs.getString("room_no"));
                        record.put("studentRoomType", rs.getString("student_room_type"));
                        record.put("actualRoomType", rs.getString("actual_room_type"));
                        record.put("studentSharingType", rs.getString("student_sharing_type"));
                        record.put("actualSharingType", rs.getString("actual_sharing_type"));
                        record.put("amountPaid", rs.getDouble("amount_paid"));
                        record.put("amountDue", rs.getDouble("amount_due"));
                        inconsistencies.add(record);
                    }
                }

                // Fix inconsistencies
                for (Map<String, Object> record : inconsistencies) {
                    String rollNo = (String) record.get("rollNo");
                    String roomNo = (String) record.get("roomNo");
                    String actualRoomType = (String) record.get("actualRoomType");
                    String actualSharingType = (String) record.get("actualSharingType");
                    double currentPaid = (Double) record.get("amountPaid");
                    double currentDue = (Double) record.get("amountDue");

                    // Calculate correct fee
                    double correctFee = calculateRoomFee(actualRoomType, actualSharingType);
                    double totalPaid = currentPaid + currentDue;
                    double newDue = correctFee - currentPaid;

                    // Update student record with correct room information
                    String updateSql = 
                        "UPDATE students s " +
                        "SET room_type = (SELECT room_type FROM rooms r WHERE r.room_no = s.room_no), " +
                        "sharing_type = (SELECT sharing_type FROM rooms r WHERE r.room_no = s.room_no), " +
                        "block_name = (SELECT block_name FROM rooms r WHERE r.room_no = s.room_no), " +
                        "floor_no = (SELECT floor_no FROM rooms r WHERE r.room_no = s.room_no), " +
                        "amount_due = ? " +
                        "WHERE roll_no = ?";

                    try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
                        pstmt.setDouble(1, newDue);
                        pstmt.setString(2, rollNo);
                        pstmt.executeUpdate();
                    }

                    // Record the adjustment in payment history
                    if (Math.abs(newDue - currentDue) > 0.01) {  // Check if there's a significant difference
                        String reason = String.format("Fee adjustment due to room type correction from %s to %s", 
                            record.get("studentRoomType"), actualRoomType);
                        
                        String historySql = "INSERT INTO payment_history (student_id, amount, type, reason) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement historyStmt = connection.prepareStatement(historySql)) {
                            historyStmt.setString(1, rollNo);
                            historyStmt.setDouble(2, Math.abs(newDue - currentDue));
                            historyStmt.setString(3, newDue > currentDue ? "CHARGE" : "REFUND");
                            historyStmt.setString(4, reason);
                            historyStmt.executeUpdate();
                        }
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error validating and fixing room assignments: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Add this method to the room transfer logic
    private static void validateRoomTransfer(String rollNo, String roomNo) throws SQLException {
        // Validate student exists
        String validateStudentSql = "SELECT COUNT(*) FROM students WHERE roll_no = ?";
        try (PreparedStatement validateStmt = connection.prepareStatement(validateStudentSql)) {
            validateStmt.setString(1, rollNo);
            ResultSet validateRs = validateStmt.executeQuery();
            validateRs.next();
            if (validateRs.getInt(1) == 0) {
                throw new SQLException("Student not found: " + rollNo);
            }
        }

        // If roomNo is null, we're vacating - no need to validate room
        if (roomNo != null) {
            // Validate room exists and has capacity
            String validateRoomSql = "SELECT capacity, current_occupancy FROM rooms WHERE room_no = ? FOR UPDATE";
            try (PreparedStatement validateRoomStmt = connection.prepareStatement(validateRoomSql)) {
                validateRoomStmt.setString(1, roomNo);
                ResultSet validateRoomRs = validateRoomStmt.executeQuery();
                if (!validateRoomRs.next()) {
                    throw new SQLException("Room not found: " + roomNo);
                }

                int capacity = validateRoomRs.getInt("capacity");
                int currentOccupancy = validateRoomRs.getInt("current_occupancy");
                
                // Check if student is already in this room
                String currentRoomSql = "SELECT room_no FROM students WHERE roll_no = ?";
                try (PreparedStatement currentRoomStmt = connection.prepareStatement(currentRoomSql)) {
                    currentRoomStmt.setString(1, rollNo);
                    ResultSet currentRoomRs = currentRoomStmt.executeQuery();
                    if (currentRoomRs.next() && roomNo.equals(currentRoomRs.getString("room_no"))) {
                        throw new SQLException("Student is already assigned to room " + roomNo);
                    }
                }

                // Only count the room as full if the student isn't currently in it
                if (currentOccupancy >= capacity) {
                    throw new SQLException("Room " + roomNo + " is at full capacity");
                }
            }
        }
    }
} 