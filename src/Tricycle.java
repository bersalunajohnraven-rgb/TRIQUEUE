public class Tricycle extends Vehicle {
    public Tricycle(String plateNumber) {
        super(plateNumber, "Tricycle");
    }

    // Tricycle: fare is charged PER PASSENGER
    @Override
    public double computeFare(String route, String passengerType, int passengerCount) {
        double perPassenger = FileManager.getRoutePrice(route, true);
        return perPassenger * passengerCount;
    }
}
