import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class EarningsPanel extends JPanel {
    private final JComboBox<String> cbDriver = new JComboBox<>();
    private final JTextField tfAmount = new JTextField(8);
    private final JTextField tfDate = new JTextField(LocalDate.now().toString(), 10);
    private final DefaultTableModel model = GuiUtil.readOnlyModel("ID", "Driver", "Trips", "Fares", "Boundary", "Net Earnings");
    private final JLabel lblTotal = new JLabel(" ");
    private String loadedReportDate;

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
        JButton bExport = GuiUtil.button("Export CSV");
        rep.add(new JLabel("Date (yyyy-MM-dd):")); rep.add(tfDate);
        rep.add(bGen); rep.add(bExport);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 6));
        top.add(pay); top.add(rep);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(GuiUtil.makeTable(model)), BorderLayout.CENTER);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        lblTotal.setForeground(GuiUtil.NAVY);
        add(lblTotal, BorderLayout.SOUTH);

        bPay.addActionListener(e -> recordBoundary());
        bGen.addActionListener(e -> generate());
        bExport.addActionListener(e -> exportCsv());
        reloadData();
    }

    public final void reloadData() {
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
        if (!Double.isFinite(amount) || amount < 0) {
            GuiUtil.error(this, "Boundary amount must be a finite, non-negative number.");
            return;
        }
        if (!d.payBoundary(amount)) return;
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
        try {
            date = LocalDate.parse(date).toString();
        } catch (DateTimeParseException ex) {
            loadedReportDate = null;
            GuiUtil.error(this, "Enter a valid date in yyyy-MM-dd format.");
            return;
        }
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
        loadedReportDate = date;
    }

    private void exportCsv() {
        String date = tfDate.getText().trim();
        if (date.isEmpty()) date = LocalDate.now().toString();
        try {
            date = LocalDate.parse(date).toString();
        } catch (DateTimeParseException ex) {
            GuiUtil.error(this, "Enter a valid date in yyyy-MM-dd format before exporting.");
            return;
        }
        if (!date.equals(loadedReportDate)) generate();
        if (!date.equals(loadedReportDate)) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Daily Earnings Report");
        chooser.setSelectedFile(new java.io.File("Daily-Earnings-" + date + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path output = chooser.getSelectedFile().toPath();
        if (Files.exists(output) && JOptionPane.showConfirmDialog(this,
                "Replace the selected file?", "Confirm Export", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        List<String> lines = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        for (int col = 0; col < model.getColumnCount(); col++) {
            if (col > 0) header.append(',');
            header.append(csvValue(model.getColumnName(col)));
        }
        lines.add(header.toString());

        double totalFares = 0, totalBoundary = 0, totalNet = 0;
        int totalTrips = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            StringBuilder csvRow = new StringBuilder();
            for (int col = 0; col < model.getColumnCount(); col++) {
                if (col > 0) csvRow.append(',');
                csvRow.append(csvValue(model.getValueAt(row, col)));
            }
            lines.add(csvRow.toString());
            totalTrips += Integer.parseInt(model.getValueAt(row, 2).toString());
            totalFares += Double.parseDouble(model.getValueAt(row, 3).toString());
            totalBoundary += Double.parseDouble(model.getValueAt(row, 4).toString());
            totalNet += Double.parseDouble(model.getValueAt(row, 5).toString());
        }
        lines.add("TOTAL,," + totalTrips + "," + String.format("%.2f", totalFares) + ","
                + String.format("%.2f", totalBoundary) + "," + String.format("%.2f", totalNet));
        try {
            Files.write(output, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            GuiUtil.info(this, "Report exported to:\n" + output.toAbsolutePath().normalize());
        } catch (IOException ex) {
            GuiUtil.error(this, "Could not export report: " + ex.getMessage());
        }
    }

    private String csvValue(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r"))
            return "\"" + text.replace("\"", "\"\"") + "\"";
        return text;
    }
}
