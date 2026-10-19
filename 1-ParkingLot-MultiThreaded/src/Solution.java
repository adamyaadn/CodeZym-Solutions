import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Solution implements Q001ParkingLotInterface {
    private Helper01 helper;
    private String[][][] parking;

    // Concurrent queues for available spots indexed by [vehicleType][floor]
    // vehicleType: index 2 for 2-wheeler, index 4 for 4-wheeler
    private Queue<String>[][] availableSpots;

    // Free spots count tracking per floor and vehicle type: counts[vehicleType][floor]
    private AtomicInteger[][] freeSpotCounts;

    // Mapping from spotId -> current parked VehicleInfo (null if empty)
    private Map<String, VehicleInfo> spotToVehicleMap;

    // Key lookups mapping vehicleNumber, ticketId, and spotId -> VehicleInfo
    private Map<String, VehicleInfo> vehicleNumberToInfo;
    Map<String, VehicleInfo> ticketIdToInfo;
    private Map<String, VehicleInfo> spotIdToInfo;

    private static class VehicleInfo {
        String spotId;
        String vehicleNumber;
        String ticketId;
        int vehicleType;

        VehicleInfo(String spotId, String vehicleNumber, String ticketId, int vehicleType) {
            this.spotId = spotId;
            this.vehicleNumber = vehicleNumber;
            this.ticketId = ticketId;
            this.vehicleType = vehicleType;
        }
    }

    public Solution() {
    }

    @Override
    public void init(Helper01 helper, String[][][] parking) {
        this.helper = helper;
        this.parking = parking;
        int floors = parking.length;

        availableSpots = new ConcurrentLinkedQueue[5][floors];
        freeSpotCounts = new AtomicInteger[5][floors];

        for (int type : new int[]{2, 4}) {
            for (int f = 0; f < floors; f++) {
                availableSpots[type][f] = new ConcurrentLinkedQueue<>();
                freeSpotCounts[type][f] = new AtomicInteger(0);
            }
        }

        spotToVehicleMap = new ConcurrentHashMap<>();
        vehicleNumberToInfo = new ConcurrentHashMap<>();
        ticketIdToInfo = new ConcurrentHashMap<>();
        spotIdToInfo = new ConcurrentHashMap<>();

        // Traverse the parking layout and register active spots
        for (int i = 0; i < floors; i++) {
            for (int j = 0; j < parking[i].length; j++) {
                for (int k = 0; k < parking[i][j].length; k++) {
                    String spotType = parking[i][j][k];
                    if ("2-1".equals(spotType)) {
                        String spotId = helper.getSpotId(i, j, k);
                        availableSpots[2][i].add(spotId);
                        freeSpotCounts[2][i].incrementAndGet();
                    } else if ("4-1".equals(spotType)) {
                        String spotId = helper.getSpotId(i, j, k);
                        availableSpots[4][i].add(spotId);
                        freeSpotCounts[4][i].incrementAndGet();
                    }
                }
            }
        }

        helper.println("parking lot initialized");
    }

    @Override
    public ParkingResult park(int vehicleType, String vehicleNumber, String ticketId) {
        int floors = parking.length;
        String assignedSpotId = null;
        int assignedFloor = -1;

        // Find the first floor with an available spot for this vehicle type
        for (int f = 0; f < floors; f++) {
            Queue<String> queue = availableSpots[vehicleType][f];
            while ((assignedSpotId = queue.poll()) != null) {
                // Synchronize on the unique spotId string literal interned/checked via map putIfAbsent
                VehicleInfo dummy = new VehicleInfo(assignedSpotId, vehicleNumber, ticketId, vehicleType);
                if (spotToVehicleMap.putIfAbsent(assignedSpotId, dummy) == null) {
                    assignedFloor = f;
                    break;
                }
            }
            if (assignedSpotId != null) {
                break;
            }
        }

        if (assignedSpotId == null) {
            return new ParkingResult(404, "", vehicleNumber, ticketId);
        }

        // Decrement count for assigned floor
        freeSpotCounts[vehicleType][assignedFloor].decrementAndGet();

        // Update tracking info
        VehicleInfo info = spotToVehicleMap.get(assignedSpotId);
        if (vehicleNumber != null && !vehicleNumber.isBlank()) {
            vehicleNumberToInfo.put(vehicleNumber, info);
        }
        if (ticketId != null && !ticketId.isBlank()) {
            ticketIdToInfo.put(ticketId, info);
        }
        spotIdToInfo.put(assignedSpotId, info);

        return new ParkingResult(201, assignedSpotId, vehicleNumber, ticketId);
    }

    @Override
    public int removeVehicle(String spotId, String vehicleNumber, String ticketId) {
        VehicleInfo info = findVehicleInfo(spotId, vehicleNumber, ticketId);
        if (info == null) {
            return 404;
        }

        // Atomically attempt to remove vehicle from active parked spot map
        synchronized (info) {
            if (spotToVehicleMap.remove(info.spotId) == null) {
                // Vehicle was already removed by another concurrent thread
                return 404;
            }

            Integer[] loc = helper.getSpotLocation(info.spotId);
            int floor = loc[0];

            // Re-add spot back to available queue and increment free spots count
            availableSpots[info.vehicleType][floor].add(info.spotId);
            freeSpotCounts[info.vehicleType][floor].incrementAndGet();
        }

        return 201;
    }

    @Override
    public ParkingResult searchVehicle(String spotId, String vehicleNumber, String ticketId) {
        VehicleInfo info = findVehicleInfo(spotId, vehicleNumber, ticketId);
        if (info == null) {
            return new ParkingResult(404, spotId, vehicleNumber, ticketId);
        }

        return new ParkingResult(200, info.spotId, info.vehicleNumber, info.ticketId);
    }

    @Override
    public int getFreeSpotsCount(int floor, int vehicleType) {
        if (vehicleType != 2 && vehicleType != 4) {
            return 0;
        }
        return freeSpotCounts[vehicleType][floor].get();
    }

    private VehicleInfo findVehicleInfo(String spotId, String vehicleNumber, String ticketId) {
        if (spotId != null && !spotId.isBlank()) {
            return spotIdToInfo.get(spotId);
        }
        if (vehicleNumber != null && !vehicleNumber.isBlank()) {
            return vehicleNumberToInfo.get(vehicleNumber);
        }
        if (ticketId != null && !ticketId.isBlank()) {
            return ticketIdToInfo.get(ticketId);
        }
        return null;
    }
}

