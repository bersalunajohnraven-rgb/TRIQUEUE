import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DriverPanel extends JPanel {
    private DefaultTableModel model = GuiUtil.readOnlyModel("ID", "Name", "Contact", "License", "Plate", "Vehicle");
    private JTable table = GuiUtil.makeTable(model);
    private JTextField tfSearch = new JTextField();
    private JTextField tfName = new JTextField(15), tfContact = new JTextField(15);
    private JTextField tfLicense = new JTextField(15), tfPlate = new JTextField(15);
    private JComboBox<String> cbType = new JComboBox<>(new String[]{"Tricycle", "HabalHabal"});

    public DriverPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(new JLabel("Search (name, ID or plate):"), BorderLayout.WEST);
        top.add(tfSearch, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Driver & Vehicle Details"));
        GuiUtil.addRow(form, 0, "Name:", tfName);
        GuiUtil.addRow(form, 1, "Contact (digits):", tfContact);
        GuiUtil.addRow(form, 2, "License No.:", tfLicense);
        GuiUtil.addRow(form, 3, "Plate No.:", tfPlate);
        GuiUtil.addRow(form, 4, "Vehicle Type:", cbType);

        JButton bAdd = GuiUtil.button("Add");
        JButton bUpdate = GuiUtil.button("Update Contact");
        JButton bDelete = GuiUtil.button("Delete");
        JButton bClear = GuiUtil.button("Clear");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(bAdd); buttons.add(bUpdate); buttons.add(bDelete); buttons.add(bClear);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        bAdd.addActionListener(e -> addDriver());
        bUpdate.addActionListener(e -> updateContact());
        bDelete.addActionListener(e -> deleteDriver());
        bClear.addActionListener(e -> clearForm());
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });
        refresh();
    }

    public void refresh() {
        String key = tfSearch.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (Driver d : AppData.drivers) {
            if (key.isEmpty() || d.getName().toLowerCase().contains(key)
                    || d.getId().toLowerCase().contains(key)
                    || d.getVehicle().getPlateNumber().toLowerCase().contains(key)) {
                model.addRow(new Object[]{d.getId(), d.getName(), d.getContactNumber(), d.getLicenseNumber(),
                        d.getVehicle().getPlateNumber(), d.getVehicle().getVehicleType()});
            }
        }
    }

    private Driver selectedDriver() {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        return AppData.findDriver((String) model.getValueAt(r, 0));
    }

    private void fillFormFromSelection() {
        Driver d = selectedDriver();
        if (d == null) return;
        tfName.setText(d.getName());
        tfContact.setText(d.getContactNumber());
        tfLicense.setText(d.getLicenseNumber());
        tfPlate.setText(d.getVehicle().getPlateNumber());
        cbType.setSelectedItem(d.getVehicle().getVehicleType());
    }

    private void clearForm() {
        tfName.setText(""); tfContact.setText(""); tfLicense.setText(""); tfPlate.setText("");
        cbType.setSelectedIndex(0);
        table.clearSelection();
    }

    private void addDriver() {
        String name = tfName.getText().trim(), contact = tfContact.getText().trim();
        String license = tfLicense.getText().trim(), plate = tfPlate.getText().trim();
        if (name.isEmpty() || license.isEmpty() || plate.isEmpty()) {
            GuiUtil.error(this, "Name, license and plate cannot be empty.");
            return;
        }
        if (!contact.matches("\\d{7,11}")) {
            GuiUtil.error(this, "Contact number must be 7-11 digits.");
            return;
        }
        String id = FileManager.nextId(FileManager.DRIVERS, "D");
        AppData.drivers.add(new Driver(id, name, contact, license, plate, (String) cbType.getSelectedItem()));
        FileManager.saveDrivers(AppData.drivers);
        clearForm();
        refresh();
        GuiUtil.info(this, "Registered " + name + " with ID " + id);
    }

    private void updateContact() {
        Driver d = selectedDriver();
        if (d == null) { GuiUtil.error(this, "Select a driver in the table first."); return; }
        String contact = tfContact.getText().trim();
        if (!contact.matches("\\d{7,11}")) {
            GuiUtil.error(this, "Contact number must be 7-11 digits.");
            return;
        }
        d.setContactNumber(contact);
        FileManager.saveDrivers(AppData.drivers);
        refresh();
        GuiUtil.info(this, "Contact number updated.");
    }

    private void deleteDriver() {
        Driver d = selectedDriver();
        if (d == null) { GuiUtil.error(this, "Select a driver in the table first."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Delete " + d.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        AppData.drivers.remove(d);
        FileManager.saveDrivers(AppData.drivers);
        AppData.queue.load(AppData.drivers);
        AppData.queue.save();
        clearForm();
        refresh();
    }
}
