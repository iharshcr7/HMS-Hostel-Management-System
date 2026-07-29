# Hostel Management System

A robust Java-based application built with Swing for the user interface and JDBC for database connectivity, the Hostel Management System is designed to streamline hostel operations. With an intuitive GUI and efficient database transactions, the system allows administrators to manage hostel rooms, student records, fee payments, and more with ease.

---

## Features

- **User-Friendly Interface**: Built using Java Swing, providing a modern and responsive desktop UI.
- **Database Connectivity**: Seamless integration with a SQL database using JDBC.
- **Student Records Management**: Add, update, delete, and search for student details.
- **Room Allocation**: Manage room assignments and occupancy status.
- **Fee Management**: Track and update fee payments for students.
- **Reporting**: Generate basic reports for occupancy, payment statuses, and other management needs.
- **Extensible Architecture**: Modular design for easy future enhancements and maintenance.

---

## Prerequisites

Before you begin, ensure you have met the following requirements:

- **Java Development Kit (JDK)**: Version 8 or later.  
  [Download JDK](https://www.oracle.com/java/technologies/javase-downloads.html)
- **JDBC Driver**: Depending on your database (e.g., MySQL, PostgreSQL). For MySQL, download the [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/).
- **SQL Database**: A running instance of your preferred SQL database (e.g., MySQL, PostgreSQL). Create a database named `hostel_management` (or update your configuration accordingly).

---

## Configuration

1. **Database Setup**:
   - Create a new database named `hostel_management` using your SQL client or command line.
   - Execute the provided SQL script (found in the `/sql` folder of the project) to create necessary tables:
     ```sql
     CREATE TABLE students (
       id INT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(100),
       room_number VARCHAR(10),
       course VARCHAR(50),
       fee_status VARCHAR(20)
     );
     -- Add any additional tables as required for your system.
     ```
   
2. **Configure Database Connection**:
   - Open the configuration file (e.g., `DBConnection.java` or a dedicated properties file like `config.properties`).
   - Update the database URL, username, and password:
     ```java
     private static final String URL = "jdbc:mysql://localhost:3306/hostel_management";
     private static final String USER = "your_username";
     private static final String PASS = "your_password";
     ```
   - Ensure that your JDBC driver is added to the project classpath. If using an IDE, add the connector JAR to your project's libraries.

---

## How to Run

### Using an IDE (Eclipse, IntelliJ IDEA, or NetBeans)

1. **Import the Project**:
   - Open your IDE and select *File > Import* or *Open Project*.
   - Import the project as a Java project.

2. **Add JDBC Driver**:
   - Right-click on the project, navigate to *Build Path > Configure Build Path*.
   - Add the JDBC driver JAR file (e.g., `mysql-connector-java-x.x.xx.jar`) to the project's libraries.

3. **Configure Run Configuration**:
   - Ensure your main class is set as the entry point (e.g., `com.example.hostelmanagement.Main`).

4. **Run the Application**:
   - Click on *Run* (or press `F5`/`Shift+F10` depending on your IDE) to start the application.
   - The Hostel Management System GUI should launch, ready for use.

### Using the Command Line

1. **Compile the Project**:
   - Open a terminal or command prompt.
   - Navigate to the project’s root directory.
   - Compile the Java files (ensure the JDBC driver JAR is in your classpath):
     ```bash
     javac -cp ".;path/to/mysql-connector-java-x.x.xx.jar" -d bin src/com/example/hostelmanagement/*.java
     ```
     *(On Linux/Mac, replace the semicolon `;` with a colon `:` in the classpath)*

2. **Run the Application**:
   - Execute the program from the `bin` directory:
     ```bash
     java -cp ".;../path/to/mysql-connector-java-x.x.xx.jar" com.example.hostelmanagement.Main
     ```

---

StayFlow Hub — Hostel Management System
StayFlow Hub is a desktop-based Hostel Management System built using Java Swing for a rich graphical user interface and MySQL for robust database management. This application simplifies hostel operations by providing specialized dashboards for both Administrators and Hostel Residents (Students). It automates key tasks such as room allocation, warden tracking, student registration, cafeteria/meal plan subscriptions, and student complaint processing.

Screenshots
Here is a visual walkthrough of the StayFlow Hub application:

1. Database Setup	2. Role Selection
Database Setup	Role Selection
3. Admin Dashboard	4. Room Management
Admin Dashboard	Room Management
5. Student Dashboard	
Student Dashboard	
Features
Administrator Panel
Live Dashboard Metrics: Instantly view metrics like Total Students, Total Rooms, Total Wardens, and Pending Complaints.
Room Management: Add, update, delete, and view rooms (Standard or Luxury, sharing capacities, block names, and floor levels).
Warden Tracking: Manage warden rosters (Warden ID, name, age, contact, assigned hostel, and block).
Student Directory (CRUD): Easily enroll new students, edit profiles, check registration details, and record fee transactions.
Complaint Resolution: View complaints submitted by students, update processing status (Pending, In-Progress, Resolved), and log resolution timestamps.
Cafeteria & Meals: Monitor student meal plans, view user feedback, and modify daily dining menus.
Student (Resident) Panel
Availability Viewer: Check real-time room occupancies (Standard vs. Luxury, Sharing capacities, and Block details).
Profile Management: View personal student details, payment history, amounts paid, outstanding dues, and update credentials.
Complaint Box: Lodge complaints directly to administrators and track resolution progress.
Meal Plan Center:
View features & menus for different plans (Basic, Standard, Premium).
Check the daily breakfast, lunch, and dinner menus.
Submit dining feedback directly to the administration.
Technology Stack
GUI Framework: Java Swing (AWT, javax.swing)
Language: Java SE (JDK 8 or higher)
Database: MySQL (relational database storage)
Driver: JDBC (MySQL Connector/J)
Build Tool: Ant (standard NetBeans structure)
Database Schema
The database (hostel_management) automatically initializes itself on setup. The key entities include:

admins: Login credentials for host administrators.
students: Host details, fees paid/due, passwords, and room keys.
rooms: Room numbers, type (Standard/Luxury), sharing capacity, and floor.
wardens: Warden details and block assignments.
complaints: Student complaints, status updates, and logging timestamps.
meal_plan_details: Information on basic, standard, and premium plans.
meal_plans: Links student subscriptions to active meal plans.
daily_menu: Today's dining menu details.
meal_feedback: Feedback comments written by students.
payment_history: Records of payment transactions.
room_history: Tracking log of room allocations/vacations.
Setup & Installation Instructions
Follow these steps to run the project on your local machine:

Prerequisites
Java Development Kit (JDK): Version 8 or higher installed and added to your system environment variables.
MySQL Server: Installed and running (usually on default port 3306).
IDE (Optional but recommended): NetBeans IDE, Eclipse, or IntelliJ IDEA.
Step 1: Clone the Repository
git clone https://github.com/YOUR_USERNAME/Hostel-Management-Swing-StayFlow_HUB.git
cd Hostel-Management-Swing-StayFlow_HUB
Step 2: Database Initialization (Automatic)
The application is designed to create the database and seed it automatically:

Open the project in NetBeans or run the application (see Step 3).
Upon first launch, the Database Setup window will appear.
Enter your MySQL Username (defaults to root) and MySQL Password.
Click Initialize Database.
The application will automatically create the database hostel_management and insert all required tables and sample data.
Step 3: Compile and Run
Using NetBeans IDE:
Open NetBeans.
Go to File -> Open Project and select the Hostel-Management-CLI directory.
Right-click the project name in the left panel and select Clean and Build.
Right-click the project and select Run (or press F6).
Using Command Line:
Navigate to the Hostel-Management-CLI directory and compile/run the project:

# Clean and build with ant (if Ant is installed)
ant clean compile jar

# Execute the project jar
java -jar dist/Hostel-Management-CLI.jar
Developers
Pranav Mistry (NUV, CSE)
Henisha Kandoi (NUV, CSE)
Harsh Chauhan (NUV, CSE)
Dhanvin Patel (NUV, CSE)
License
This project is open-source and available under the MIT License.
## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---


