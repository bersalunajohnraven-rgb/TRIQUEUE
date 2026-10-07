import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TerminalQueue {
    private static class Entry {
        Driver driver;
        String time;
        Entry(Driver d, String t) { driver = d; time = t; }
    }

    private LinkedList<Entry> queue = new LinkedList<>();

    public boolean addDriver(Driver d) {
        for (Entry e : queue) {
            if (e.driver.getId().equals(d.getId())) {
                System.out.println(d.getName() + " is already in the queue.");
                return false;
            }
        }
        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        queue.addLast(new Entry(d, now));
        save();
        return true;
    }

    // First come, first served
    public Driver getNextDriver() {
        if (queue.isEmpty()) return null;
        Driver d = queue.removeFirst().driver;
        save();
        return d;
    }

    // Absent driver goes to the back of the line
    public Driver skipDriver() {
        if (queue.isEmpty()) return null;
        Entry e = queue.removeFirst();
        queue.addLast(e);
        save();
        return e.driver;
    }

    public boolean isEmpty() { return queue.isEmpty(); }

    public boolean contains(Driver d) {
        for (Entry e : queue) if (e.driver.getId().equals(d.getId())) return true;
        return false;
    }

    // Used by the GUI to show the queue in order
    public List<String> getDisplayRows() {
        List<String> rows = new ArrayList<>();
        int pos = 1;
        for (Entry e : queue) {
            rows.add(pos++ + ".  " + e.driver.getName() + "  (" + e.driver.getVehicle().getPlateNumber()
                    + ", " + e.driver.getVehicle().getVehicleType() + ")  -  joined " + e.time);
        }
        return rows;
    }

    public void display() {
        if (queue.isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }
        int pos = 1;
        for (Entry e : queue) {
            System.out.println(pos++ + ". " + e.driver.getName() + " (" + e.driver.getVehicle().getPlateNumber()
                    + ") - joined " + e.time);
        }
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (Entry e : queue) {
            lines.add(e.driver.getId() + "|" + e.time + "|WAITING");
        }
        FileManager.writeFile(FileManager.QUEUE, lines);
    }

    public void load(List<Driver> drivers) {
        queue.clear();
        for (String line : FileManager.readFile(FileManager.QUEUE)) {
            String[] p = line.split("\\|");
            if (p.length < 2) continue;
            for (Driver d : drivers) {
                if (d.getId().equals(p[0])) {
                    queue.addLast(new Entry(d, p[1]));
                    break;
                }
            }
        }
    }
}
