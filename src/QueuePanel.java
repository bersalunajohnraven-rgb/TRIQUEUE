import javax.swing.*;
import java.awt.*;

public class QueuePanel extends JPanel {
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> list = new JList<>(listModel);
    private JComboBox<String> cbDriver = new JComboBox<>();
    private JLabel lblNow = new JLabel("Now loading: -", SwingConstants.CENTER);

    public QueuePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblNow.setFont(lblNow.getFont().deriveFont(Font.BOLD, 18f));
        lblNow.setOpaque(true);
        lblNow.setBackground(GuiUtil.LIGHT);
        lblNow.setForeground(GuiUtil.NAVY);
        lblNow.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(lblNow, BorderLayout.NORTH);

        list.setFont(list.getFont().deriveFont(14f));
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createTitledBorder("Waiting Queue (first come, first served)"));
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bAdd = GuiUtil.button("Add to Queue");
        JButton bNext = GuiUtil.button("Call Next Driver");
        JButton bSkip = GuiUtil.button("Skip Absent Driver");
        bottom.add(new JLabel("Driver:"));
        bottom.add(cbDriver);
        bottom.add(bAdd); bottom.add(bNext); bottom.add(bSkip);
        add(bottom, BorderLayout.SOUTH);

        bAdd.addActionListener(e -> {
            Driver d = AppData.fromLabel((String) cbDriver.getSelectedItem());
            if (d == null) { GuiUtil.error(this, "Register a driver first."); return; }
            if (AppData.queue.contains(d)) { GuiUtil.error(this, d.getName() + " is already in the queue."); return; }
            AppData.dispatcher.addDriverToQueue(AppData.queue, d);
            refresh();
        });
        bNext.addActionListener(e -> {
            Driver d = AppData.queue.getNextDriver();
            if (d == null) { GuiUtil.error(this, "No drivers in the queue."); return; }
            lblNow.setText("Now loading: " + d.getName() + " (" + d.getVehicle().getPlateNumber() + ")");
            refresh();
        });
        bSkip.addActionListener(e -> {
            Driver d = AppData.queue.skipDriver();
            if (d == null) { GuiUtil.error(this, "No drivers in the queue."); return; }
            GuiUtil.info(this, d.getName() + " was skipped and moved to the back.");
            refresh();
        });
        refresh();
    }

    public void refresh() {
        Object selected = cbDriver.getSelectedItem();
        cbDriver.removeAllItems();
        for (Driver d : AppData.drivers) cbDriver.addItem(AppData.label(d));
        if (selected != null) cbDriver.setSelectedItem(selected);

        listModel.clear();
        for (String row : AppData.queue.getDisplayRows()) listModel.addElement(row);
    }
}
