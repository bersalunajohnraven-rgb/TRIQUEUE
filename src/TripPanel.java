import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TripPanel extends JPanel {
    private JComboBox<String> cbDriver = new JComboBox<>();
    private JComboBox<String> cbRoute = new JComboBox<>();
    private JComboBox<String> cbType = new JComboBox<>();
    private JSpinner spCount = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private JLabel lblBase = new JLabel("0.00"), lblDiscount = new JLabel("0.00"), lblFinal = new JLabel("0.00");
    private DefaultTableModel model = GuiUtil.readOnlyModel("Trip ID", "Driver", "Route", "Passenger",
            "Count", "Base", "Discount", "Fare", "Time");
    private boolean refreshing = false;

    public TripPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Log a Trip"));
        GuiUtil.addRow(form, 0, "Driver:", cbDriver);
        GuiUtil.addRow(form, 1, "Route:", cbRoute);
        GuiUtil.addRow(form, 2, "Passenger type:", cbType);
        GuiUtil.addRow(form, 3, "Passenger count:", spCount);
        lblFinal.setFont(lblFinal.getFont().deriveFont(Font.BOLD, 18f));
        lblFinal.setForeground(GuiUtil.NAVY);
        GuiUtil.addRow(form, 4, "Base fare:", lblBase);
        GuiUtil.addRow(form, 5, "Discount:", lblDiscount);
        GuiUtil.addRow(form, 6, "FARE TO COLLECT:", lblFinal);
        JButton bLog = GuiUtil.button("Log Trip");
        JPanel inner = new JPanel(new BorderLayout());
        inner.add(form, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.add(bLog);
        inner.add(bp, BorderLayout.SOUTH);
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.add(inner, BorderLayout.NORTH);
        formWrap.setPreferredSize(new Dimension(340, 0));
        add(formWrap, BorderLayout.WEST);

        JScrollPane sp = new JScrollPane(GuiUtil.makeTable(model));
        sp.setBorder(BorderFactory.createTitledBorder("Trip Records"));
        add(sp, BorderLayout.CENTER);

        java.awt.event.ActionListener preview = e -> { if (!refreshing) updatePreview(); };
        cbDriver.addActionListener(preview);
        cbRoute.addActionListener(preview);
        cbType.addActionListener(preview);
        spCount.addChangeListener(e -> { if (!refreshing) updatePreview(); });
        bLog.addActionListener(e -> logTrip());
        refresh();
    }

    public void refresh() {
        refreshing = true;
        Object d = cbDriver.getSelectedItem(), r = cbRoute.getSelectedItem(), t = cbType.getSelectedItem();
        cbDriver.removeAllItems();
        for (Driver dr : AppData.drivers) cbDriver.addItem(AppData.label(dr));
        cbRoute.removeAllItems();
        for (String[] row : FileManager.getRoutes()) cbRoute.addItem(row[0]);
        cbType.removeAllItems();
        for (String[] row : FileManager.getDiscounts()) cbType.addItem(row[0]);
        if (d != null) cbDriver.setSelectedItem(d);
        if (r != null) cbRoute.setSelectedItem(r);
        if (t != null) cbType.setSelectedItem(t);
        refreshing = false;

        model.setRowCount(0);
        for (String line : FileManager.readFile(FileManager.TRIPS)) {
            String[] p = line.split("\\|");
            if (p.length >= 9) model.addRow(new Object[]{p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8]});
        }
        updatePreview();
    }

    private Trip buildTrip(String id, String time) {
        Driver d = AppData.fromLabel((String) cbDriver.getSelectedItem());
        String route = (String) cbRoute.getSelectedItem();
        String type = (String) cbType.getSelectedItem();
        if (d == null || route == null || type == null) return null;
        return new Trip(id, d, route, type, (Integer) spCount.getValue(), time);
    }

    private void updatePreview() {
        Trip t = buildTrip("PREVIEW", "");
        if (t == null) {
            lblBase.setText("0.00"); lblDiscount.setText("0.00"); lblFinal.setText("0.00");
            return;
        }
        t.calculateFare();
        lblBase.setText(String.format("%.2f", t.getFareDetails().getBaseFare()));
        lblDiscount.setText(String.format("%.2f", t.getFareDetails().getDiscountApplied()));
        lblFinal.setText(String.format("%.2f", t.getFareDetails().getFinalFare()));
    }

    private void logTrip() {
        String id = FileManager.nextId(FileManager.TRIPS, "T");
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Trip t = buildTrip(id, time);
        if (t == null) {
            GuiUtil.error(this, "Choose a driver, route and passenger type first.");
            return;
        }
        double fare = t.calculateFare();
        FileManager.appendFile(FileManager.TRIPS, t.toFileString());
        refresh();
        GuiUtil.info(this, String.format("Trip %s saved.%nFare to collect: %.2f", id, fare));
    }
}
