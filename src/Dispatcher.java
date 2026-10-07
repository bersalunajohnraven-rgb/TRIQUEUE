import java.util.ArrayList;
import java.util.List;

public class Dispatcher extends Person {
    private String terminalName;

    public Dispatcher(String id, String name, String contactNumber, String terminalName) {
        super(id, name, contactNumber);
        this.terminalName = terminalName;
    }

    public String getTerminalName() { return terminalName; }

    public void addDriverToQueue(TerminalQueue queue, Driver driver) {
        if (queue.addDriver(driver)) {
            System.out.println(driver.getName() + " added to the queue.");
        }
    }

    public void callNextDriver(TerminalQueue queue) {
        Driver d = queue.getNextDriver();
        if (d == null) System.out.println("No drivers in the queue.");
        else System.out.println("NOW LOADING: " + d.getName() + " (" + d.getVehicle().getPlateNumber() + ")");
    }

    public void skipNextDriver(TerminalQueue queue) {
        Driver d = queue.skipDriver();
        if (d == null) System.out.println("No drivers in the queue.");
        else System.out.println(d.getName() + " was skipped and moved to the back.");
    }

    // Returns one row per driver: {id, name, trips, fares, boundary, net}
    public List<Object[]> buildReportRows(List<Driver> drivers, String date) {
        List<Object[]> rows = new ArrayList<>();
        for (Driver d : drivers) {
            int trips = 0;
            double fares = 0, boundary = 0;

            for (String line : FileManager.readFile(FileManager.TRIPS)) {
                String[] p = line.split("\\|");
                if (p.length >= 9 && p[1].equals(d.getId()) && p[8].startsWith(date)) {
                    trips++;
                    fares += Double.parseDouble(p[7]);
                }
            }
            for (String line : FileManager.readFile(FileManager.BOUNDARY)) {
                String[] p = line.split("\\|");
                if (p.length >= 4 && p[1].equals(d.getId()) && p[2].equals(date)) {
                    boundary += Double.parseDouble(p[3]);
                }
            }

            d.resetEarnings();
            d.addFare(fares);
            d.payBoundary(boundary);
            rows.add(new Object[]{d.getId(), d.getName(), trips, fares, boundary, d.getNetEarnings()});
        }
        return rows;
    }

    public void generateDailyReport(List<Driver> drivers, String date) {
        System.out.println("\n===== DAILY EARNINGS REPORT: " + date + " (" + terminalName + ") =====");
        System.out.printf("%-6s %-20s %6s %10s %10s %10s%n", "ID", "Driver", "Trips", "Fares", "Boundary", "Net");
        double totalFares = 0, totalBoundary = 0;
        for (Object[] r : buildReportRows(drivers, date)) {
            totalFares += (Double) r[3];
            totalBoundary += (Double) r[4];
            System.out.printf("%-6s %-20s %6d %10.2f %10.2f %10.2f%n", r[0], r[1], r[2], r[3], r[4], r[5]);
        }
        System.out.printf("%-27s %6s %10.2f %10.2f %10.2f%n", "TOTAL", "", totalFares, totalBoundary,
                totalFares - totalBoundary);
    }
}
