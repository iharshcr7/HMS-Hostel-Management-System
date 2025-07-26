package management;

import database.DatabaseManager;
import java.util.List;
import java.util.Map;

public class HostelManager {
    public static void allocateRoom(String rollNo, String roomNo) {
        try {
            // Check if room exists and is available
            List<Map<String, Object>> rooms = DatabaseManager.getAllRooms();
            boolean roomFound = false;
            boolean roomAvailable = false;
            
            for (Map<String, Object> room : rooms) {
                if (room.get("roomNo").equals(roomNo)) {
                    roomFound = true;
                    if ((Integer)room.get("currentOccupancy") < (Integer)room.get("capacity")) {
                        roomAvailable = true;
                    }
                    break;
                }
            }

            if (!roomFound) {
                System.out.println("Room not found!");
            return;
        }

            if (!roomAvailable) {
                System.out.println("Room is already full!");
                return;
            }

            // Update student's room
            List<Map<String, Object>> students = DatabaseManager.searchStudents(rollNo);
            if (students.isEmpty()) {
                System.out.println("Student not found!");
                return;
            }

            // Update room allocation in database
            DatabaseManager.updateStudentRoom(rollNo, roomNo, "Initial room assignment");
            System.out.println("Room " + roomNo + " assigned to student " + rollNo + " successfully.");
        } catch (Exception e) {
            System.err.println("Error assigning room: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void deallocateRoom(String rollNo) {
        try {
            List<Map<String, Object>> students = DatabaseManager.searchStudents(rollNo);
            if (students.isEmpty()) {
                System.out.println("Student not found!");
                return;
            }

            // Update student's room to null in database
            DatabaseManager.updateStudentRoom(rollNo, null, "Room vacated");
            System.out.println("Room vacated for student " + rollNo + " successfully.");
        } catch (Exception e) {
            System.err.println("Error vacating room: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
