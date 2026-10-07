public class Driver extends Person {
    private String licenseNumber;
    private double totalEarningsToday;
    private Vehicle vehicle;

    public Driver(String id, String name, String contactNumber,
                  String licenseNumber, String plateNumber, String vehicleType) {
        super(id, name, contactNumber);
        this.licenseNumber = licenseNumber;
        this.totalEarningsToday = 0;
        // Polymorphism: the reference is a Vehicle, the object is Tricycle or HabalHabal
        if (vehicleType.equalsIgnoreCase("HabalHabal")) {
            this.vehicle = new HabalHabal(plateNumber);
        } else {
            this.vehicle = new Tricycle(plateNumber);
        }
    }

    public String getLicenseNumber() { return licenseNumber; }
    public Vehicle getVehicle() { return vehicle; }
    public double getNetEarnings() { return totalEarningsToday; }

    public void addFare(double fare) {
        if (fare < 0) {
            System.out.println("Fare cannot be negative.");
            return;
        }
        totalEarningsToday += fare;
    }

    // Boundary is subtracted from today's earnings. Negative amounts are rejected.
    public boolean payBoundary(double amount) {
        if (amount < 0) {
            System.out.println("Boundary amount cannot be negative.");
            return false;
        }
        totalEarningsToday -= amount;
        return true;
    }

    public void resetEarnings() { totalEarningsToday = 0; }

    public void joinQueue(TerminalQueue queue) {
        queue.addDriver(this);
    }

    public String toFileString() {
        return getId() + "|" + getName() + "|" + getContactNumber() + "|" + licenseNumber
                + "|" + vehicle.getPlateNumber() + "|" + vehicle.getVehicleType();
    }

    @Override
    public String toString() {
        return String.format("%-6s %-20s %-12s %-14s %-8s %s", getId(), getName(),
                getContactNumber(), licenseNumber, vehicle.getPlateNumber(), vehicle.getVehicleType());
    }
}
