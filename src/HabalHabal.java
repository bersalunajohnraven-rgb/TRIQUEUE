public class HabalHabal extends Vehicle {
    public HabalHabal(String plateNumber) {
        super(plateNumber, "HabalHabal");
    }

    // HabalHabal: fare is charged PER TRIP (passenger count is ignored)
    @Override
    public double computeFare(String route, String passengerType, int passengerCount) {
        return FileManager.getRoutePrice(route, false);
    }
}
