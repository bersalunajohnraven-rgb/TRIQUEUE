public abstract class Vehicle {
    private String plateNumber;
    private String vehicleType;

    public Vehicle(String plateNumber, String vehicleType) {
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
    }

    public String getPlateNumber() { return plateNumber; }
    public String getVehicleType() { return vehicleType; }

    // Each vehicle type computes the BASE fare (before discount) its own way.
    public abstract double computeFare(String route, String passengerType, int passengerCount);
}
