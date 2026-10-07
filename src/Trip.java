public class Trip {
    private String tripId;
    private Driver driver;
    private Vehicle vehicle;
    private String route;
    private String passengerType;
    private int passengerCount;
    private String timestamp;
    private FareDetails fareDetails; // composition: a Trip owns its FareDetails

    public Trip(String tripId, Driver driver, String route,
                String passengerType, int passengerCount, String timestamp) {
        this.tripId = tripId;
        this.driver = driver;
        this.vehicle = driver.getVehicle();
        this.route = route;
        this.passengerType = passengerType;
        this.passengerCount = passengerCount;
        this.timestamp = timestamp;
    }

    public double calculateFare() {
        double base = vehicle.computeFare(route, passengerType, passengerCount); // polymorphic call
        double percent = FileManager.getDiscountPercent(passengerType);
        double discount = base * percent / 100.0;
        fareDetails = new FareDetails(base, discount);
        return fareDetails.getFinalFare();
    }

    public FareDetails getFareDetails() { return fareDetails; }

    public String toFileString() {
        return tripId + "|" + driver.getId() + "|" + route + "|" + passengerType + "|" + passengerCount
                + "|" + String.format("%.2f", fareDetails.getBaseFare())
                + "|" + String.format("%.2f", fareDetails.getDiscountApplied())
                + "|" + String.format("%.2f", fareDetails.getFinalFare())
                + "|" + timestamp;
    }
}
