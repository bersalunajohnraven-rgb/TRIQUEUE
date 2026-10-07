import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EarningsPanel extends JPanel {
    private JComboBox<String> cbDriver = new JComboBox<>();
    private JTextField tfAmount = new JTextField(8);
    private JTextField tfDate = new JTextField(LocalDate.now().toString(), 10);
    private DefaultTableModel model = GuiUtil.readOnlyModel("ID", "Driver", "Trips", "Fares", "Boundary", "Net Earnings");
    private JLabel lblTotal = new JLabel(" ");

    public EarningsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pay = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pay.setBorder(BorderFactory.createTitledBorder("Boundary Payment (today)"));
        JButton bPay = GuiUtil.button("Record Boundary");
        pay.add(new JLabel("Driver:")); pay.add(cbDriver);
        pay.add(new JLabel("Amount:")); pay.add(tfAmount);
        pay.add(bPay);

        JPanel rep = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rep.setBorder(BorderFactory.createTitledBorder("Daily Earnings Report"));
        JButton bGen = GuiUtil.button("Generate Report");
        rep.add(new JLabel("Date (yyyy-MM-dd):")); rep.add(tfDate);
        rep.add(bGen);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 6));
        top.add(pay); top.add(rep);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(GuiUtil.makeTable(model)), BorderLayout.CENTER);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        lblTotal.setForeground(GuiUtil.NAVY);
        add(lblTotal, BorderLayout.SOUTH);

        bPay.addActionListener(e -> recordBoundary());
        bGen.addActionListener(e -> generate());
        refresh();
    }

    public void refresh() {
        Object sel = cbDriver.getSelectedItem();
        cbDriver.removeAllItems();
        for (Driver d : AppData.drivers) cbDriver.addItem(AppData.label(d));
        if (sel != null) cbDriver.setSelectedItem(sel);
        generate();
    }

    private void recordBoundary() {
        Driver d = AppData.fromLabel((String) cbDriver.getSelectedItem());
        if (d == null) { GuiUtil.error(this, "Choose a driver."); return; }
        double amount;
        try {
            amount = Double.parseDouble(tfAmount.getText().trim());
        } catch (NumberFormatException ex) {
            GuiUtil.error(this, "Amount must be a number.");
            return;
        }
        if (amount < 0) { GuiUtil.error(this, "Boundary amount cannot be negative."); return; }
        d.payBoundary(amount);
        String id = FileManager.nextId(FileManager.BOUNDARY, "B");
        FileManager.appendFile(FileManager.BOUNDARY,
                id + "|" + d.getId() + "|" + LocalDate.now() + "|" + String.format("%.2f", amount));
        tfAmount.setText("");
        tfDate.setText(LocalDate.now().toString());
        generate();
        GuiUtil.info(this, "Boundary of " + String.format("%.2f", amount) + " recorded for " + d.getName() + ".");
    }

    private void generate() {
        String date = tfDate.getText().trim();
        if (date.isEmpty()) date = LocalDate.now().toString();
        model.setRowCount(0);
        double fares = 0, boundary = 0;
        for (Object[] r : AppData.dispatcher.buildReportRows(AppData.drivers, date)) {
            fares += (Double) r[3];
            boundary += (Double) r[4];
            model.addRow(new Object[]{r[0], r[1], r[2],
                    String.format("%.2f", r[3]), String.format("%.2f", r[4]), String.format("%.2f", r[5])});
        }
        lblTotal.setText(String.format("TOTAL  -  Fares: %.2f   Boundary: %.2f   Net: %.2f",
                fares, boundary, fares - boundary));
    }
}
