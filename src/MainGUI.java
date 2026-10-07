import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {
    public MainGUI() {
        super("TrikeQueue - Tricycle & Habal-Habal Terminal Queue and Fare Management System");
        AppData.load();

        DriverPanel drivers = new DriverPanel();
        QueuePanel queue = new QueuePanel();
        TripPanel trips = new TripPanel();
        FarePanel fares = new FarePanel();
        EarningsPanel earnings = new EarningsPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Drivers & Vehicles", drivers);
        tabs.addTab("Terminal Queue", queue);
        tabs.addTab("Trip Logging", trips);
        tabs.addTab("Fares & Routes", fares);
        tabs.addTab("Boundary & Earnings", earnings);

        // Refresh the visible tab so combo boxes and tables are always up to date
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c == drivers) drivers.refresh();
            else if (c == queue) queue.refresh();
            else if (c == trips) trips.refresh();
            else if (c == fares) fares.refresh();
            else if (c == earnings) earnings.refresh();
        });

        JLabel header = new JLabel("  TrikeQueue", SwingConstants.LEFT);
        header.setOpaque(true);
        header.setBackground(GuiUtil.NAVY);
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        header.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
