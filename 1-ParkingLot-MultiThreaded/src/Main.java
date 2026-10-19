
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   RUNNING EXAMPLE 1 (1 Floor, 4x4 Grid)");
        System.out.println("==========================================");
        runExample1();

        System.out.println("\n==========================================");
        System.out.println("   RUNNING EXAMPLE 2 (2 Floors, 2x4 Grid)");
        System.out.println("==========================================");
        runExample2();
    }

    private static void runExample1() {
        Helper01 helper = new Helper01();

        // Input layout from Example 1
        String[][][] parking = {
                {
                        {"4-1", "4-1", "2-1", "2-0"},
                        {"2-1", "4-1", "2-1", "2-1"},
                        {"4-0", "2-1", "4-0", "2-1"},
                        {"4-1", "4-1", "4-1", "2-1"}
                }
        };

        Solution solution = new Solution();
        solution.init(helper, parking);

        // Check initial free spots for 4-wheelers on floor 0 (Expected: 6)
        int free4Wheelers = solution.getFreeSpotsCount(0, 4);
        System.out.println("Initial free 4-wheeler spots on floor 0: " + free4Wheelers);

        // Park a 4-wheeler vehicle
        System.out.println("\n[Action] Parking vehicle 'bh234' with ticket 'tkt4534'...");
        ParkingResult parkRes = solution.park(4, "bh234", "tkt4534");
        System.out.println("Park Result: " + parkRes);

        // Check free spots after parking (Expected: 5)
        System.out.println("Free 4-wheeler spots after parking: " + solution.getFreeSpotsCount(0, 4));

        // Search vehicle using vehicleNumber
        System.out.println("\n[Action] Searching vehicle by vehicleNumber 'bh234'...");
        ParkingResult searchRes1 = solution.searchVehicle("", "bh234", "");
        System.out.println("Search Result: " + searchRes1);

        // Search vehicle using ticketId
        System.out.println("\n[Action] Searching vehicle by ticketId 'tkt4534'...");
        ParkingResult searchRes2 = solution.searchVehicle("", "", "tkt4534");
        System.out.println("Search Result: " + searchRes2);

        // Remove vehicle using assigned spotId
        String assignedSpotId = parkRes.getSpotId();
        System.out.println("\n[Action] Removing vehicle using spotId '" + assignedSpotId + "'...");
        int removeStatus = solution.removeVehicle(assignedSpotId, "", "");
        System.out.println("Remove Status Code: " + removeStatus + " (201 = Success)");

        // Check free spots after unparking (Expected: 6)
        System.out.println("Free 4-wheeler spots after unparking: " + solution.getFreeSpotsCount(0, 4));

        // Search vehicle after unparking (Should still find last known location)
        System.out.println("\n[Action] Searching vehicle 'bh234' after removal...");
        ParkingResult searchRes3 = solution.searchVehicle("", "bh234", "");
        System.out.println("Search Result post-removal: " + searchRes3);
    }

    private static void runExample2() {
        Helper01 helper = new Helper01();

        // Input layout from Example 2 (2 Floors)
        String[][][] parking = {
                {
                        {"2-1", "4-1", "2-1", "4-1"},
                        {"2-1", "2-1", "4-1", "4-1"}
                },
                {
                        {"2-1", "2-1", "4-1", "2-1"},
                        {"4-1", "4-1", "2-1", "2-1"}
                }
        };

        Solution solution = new Solution();
        solution.init(helper, parking);

        // Check free spots count on Floor 0
        System.out.println("Floor 0 - Free 2-Wheelers: " + solution.getFreeSpotsCount(0, 2) + " (Expected: 4)");
        System.out.println("Floor 0 - Free 4-Wheelers: " + solution.getFreeSpotsCount(0, 4) + " (Expected: 4)");

        // Check free spots count on Floor 1
        System.out.println("Floor 1 - Free 2-Wheelers: " + solution.getFreeSpotsCount(1, 2) + " (Expected: 5)");
        System.out.println("Floor 1 - Free 4-Wheelers: " + solution.getFreeSpotsCount(1, 4) + " (Expected: 3)");

        // Park multiple vehicles on different floors
        System.out.println("\n[Action] Parking 2-wheeler 'KA-01-1234'...");
        ParkingResult p1 = solution.park(2, "KA-01-1234", "TKT-001");
        System.out.println("Park 1 Result: " + p1);

        System.out.println("[Action] Parking 4-wheeler 'MH-12-5678'...");
        ParkingResult p2 = solution.park(4, "MH-12-5678", "TKT-002");
        System.out.println("Park 2 Result: " + p2);

        System.out.println("\nFloor 0 - Free 2-Wheelers after park: " + solution.getFreeSpotsCount(0, 2));
        System.out.println("Floor 0 - Free 4-Wheelers after park: " + solution.getFreeSpotsCount(0, 4));
    }
}

