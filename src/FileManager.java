import java.io.*;
import java.util.*;

public class FileManager {
    public static final String DIR = "data/";
    public static final String DRIVERS = DIR + "drivers.txt";
    public static final String QUEUE = DIR + "queue.txt";
    public static final String TRIPS = DIR + "trips.txt";
    public static final String FARES = DIR + "fares.txt";
    public static final String BOUNDARY = DIR + "boundary.txt";

    private static void ensureFile(String path) throws IOException {
        File f = new File(path);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();
        if (!f.exists()) f.createNewFile();
    }

    public static List<String> readFile(String path) {
        List<String> lines = new ArrayList<>();
        try {
            ensureFile(path);
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + path + ": " + e.getMessage());
        }
        return lines;
    }

    public static void writeFile(String path, List<String> lines) {
        try {
            ensureFile(path);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, false))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing " + path + ": " + e.getMessage());
        }
    }

    public static void appendFile(String path, String line) {
        try {
            ensureFile(path);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error appending to " + path + ": " + e.getMessage());
        }
    }

    // ---------- Drivers ----------
    public static List<Driver> loadDrivers() {
        List<Driver> drivers = new ArrayList<>();
        for (String line : readFile(DRIVERS)) {
            String[] p = line.split("\\|");
            if (p.length < 6) continue; // skip bad lines instead of crashing
            drivers.add(new Driver(p[0], p[1], p[2], p[3], p[4], p[5]));
        }
        return drivers;
    }

    public static void saveDrivers(List<Driver> drivers) {
        List<String> lines = new ArrayList<>();
        for (Driver d : drivers) lines.add(d.toFileString());
        writeFile(DRIVERS, lines);
    }

    // ---------- Fares ----------
    // fares.txt lines: ROUTE|name|tricyclePerPassenger|habalHabalPerTrip
    //                  DISCOUNT|passengerType|percent
    public static double getRoutePrice(String route, boolean tricycle) {
        for (String line : readFile(FARES)) {
            String[] p = line.split("\\|");
            if (p.length >= 4 && p[0].equals("ROUTE") && p[1].equalsIgnoreCase(route)) {
                try {
                    return Double.parseDouble(tricycle ? p[2] : p[3]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static boolean routeExists(String route) {
        for (String line : readFile(FARES)) {
            String[] p = line.split("\\|");
            if (p.length >= 4 && p[0].equals("ROUTE") && p[1].equalsIgnoreCase(route)) return true;
        }
        return false;
    }

    public static double getDiscountPercent(String passengerType) {
        for (String line : readFile(FARES)) {
            String[] p = line.split("\\|");
            if (p.length >= 3 && p[0].equals("DISCOUNT") && p[1].equalsIgnoreCase(passengerType)) {
                try {
                    return Double.parseDouble(p[2]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    // Adds a new ROUTE line or replaces the existing one with the same name
    public static void saveRoute(String route, double tricycleFare, double habalFare) {
        List<String> lines = readFile(FARES);
        String newLine = "ROUTE|" + route + "|" + String.format("%.2f", tricycleFare)
                + "|" + String.format("%.2f", habalFare);
        boolean replaced = false;
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split("\\|");
            if (p.length >= 2 && p[0].equals("ROUTE") && p[1].equalsIgnoreCase(route)) {
                lines.set(i, newLine);
                replaced = true;
            }
        }
        if (!replaced) lines.add(newLine);
        writeFile(FARES, lines);
    }

    public static void saveDiscount(String type, double percent) {
        List<String> lines = readFile(FARES);
        String newLine = "DISCOUNT|" + type + "|" + (int) percent;
        boolean replaced = false;
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split("\\|");
            if (p.length >= 2 && p[0].equals("DISCOUNT") && p[1].equalsIgnoreCase(type)) {
                lines.set(i, newLine);
                replaced = true;
            }
        }
        if (!replaced) lines.add(newLine);
        writeFile(FARES, lines);
    }

    public static List<String[]> getRoutes() {
        List<String[]> out = new ArrayList<>();
        for (String line : readFile(FARES)) {
            String[] p = line.split("\\|");
            if (p.length >= 4 && p[0].equals("ROUTE")) out.add(new String[]{p[1], p[2], p[3]});
        }
        return out;
    }

    public static List<String[]> getDiscounts() {
        List<String[]> out = new ArrayList<>();
        for (String line : readFile(FARES)) {
            String[] p = line.split("\\|");
            if (p.length >= 3 && p[0].equals("DISCOUNT")) out.add(new String[]{p[1], p[2]});
        }
        return out;
    }

    public static void printFares() {
        System.out.println("\n--- ROUTES (Tricycle per passenger / HabalHabal per trip) ---");
        for (String line : readFile(FARES)) {
            String[] p = line.split("\\|");
            if (p[0].equals("ROUTE") && p.length >= 4)
                System.out.printf("%-28s %8s %8s%n", p[1], p[2], p[3]);
        }
        System.out.println("--- DISCOUNTS ---");
        for (String line : readFile(FARES)) {
            String[] p = line.split("\\|");
            if (p[0].equals("DISCOUNT") && p.length >= 3)
                System.out.printf("%-28s %7s%%%n", p[1], p[2]);
        }
    }

    // ---------- ID generators ----------
    public static String nextId(String path, String prefix) {
        int count = readFile(path).size() + 1;
        return String.format("%s%03d", prefix, count);
    }
}
