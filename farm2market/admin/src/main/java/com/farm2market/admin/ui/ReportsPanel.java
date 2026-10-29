package com.farm2market.admin.ui;

import com.farm2market.admin.service.AdminClientService;
import com.farm2market.admin.util.TableModelFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReportsPanel extends JPanel {

    private final AdminClientService clientService;

    private JTable reportsTable;
    private DefaultTableModel tableModel;
    private JTextArea txtDetails;
    private JTextField txtAdminNote;
    private JButton btnResolve;
    private JButton btnDismiss;
    private JComboBox<String> cmbFilter;

    private String selectedReportId;

    public ReportsPanel(AdminClientService clientService) {
        this.clientService = clientService;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Filter:"));
        cmbFilter = new JComboBox<>(new String[]{"All", "Open", "Reviewed", "Resolved", "Dismissed"});
        cmbFilter.addActionListener(e -> refresh());
        topPanel.add(cmbFilter);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Type", "Target", "Reporter", "Status", "Created"};
        tableModel = TableModelFactory.createReadOnlyModel(columns);
        reportsTable = new JTable(tableModel);
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsTable.getSelectionModel().addListSelectionListener(this::onReportSelected);
        reportsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        reportsTable.getColumnModel().getColumn(1).setPreferredWidth(80);

        JScrollPane tableScroll = new JScrollPane(reportsTable);
        tableScroll.setPreferredSize(new Dimension(600, 300));

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Report Details"));

        txtDetails = new JTextArea(8, 40);
        txtDetails.setEditable(false);
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        detailsPanel.add(new JScrollPane(txtDetails), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new BorderLayout(10, 5));

        JPanel notePanel = new JPanel(new BorderLayout(5, 0));
        notePanel.add(new JLabel("Admin Note:"), BorderLayout.WEST);
        txtAdminNote = new JTextField(30);
        notePanel.add(txtAdminNote, BorderLayout.CENTER);
        actionPanel.add(notePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnResolve = new JButton("Resolve");
        btnResolve.setEnabled(false);
        btnResolve.addActionListener(e -> resolveReport("RESOLVED"));
        buttonPanel.add(btnResolve);

        btnDismiss = new JButton("Dismiss");
        btnDismiss.setEnabled(false);
        btnDismiss.addActionListener(e -> resolveReport("DISMISSED"));
        buttonPanel.add(btnDismiss);

        actionPanel.add(buttonPanel, BorderLayout.EAST);
        detailsPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailsPanel);
        splitPane.setResizeWeight(0.6);
        add(splitPane, BorderLayout.CENTER);
    }

    private void onReportSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        int row = reportsTable.getSelectedRow();
        if (row < 0) {
            selectedReportId = null;
            txtDetails.setText("");
            btnResolve.setEnabled(false);
            btnDismiss.setEnabled(false);
            return;
        }

        selectedReportId = (String) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 4);
        boolean canAction = "OPEN".equals(status) || "REVIEWED".equals(status);
        btnResolve.setEnabled(canAction);
        btnDismiss.setEnabled(canAction);

        loadReportDetails(selectedReportId);
    }

    private void loadReportDetails(String reportId) {
        clientService.getReportDetails(reportId, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    var report = result.data();
                    txtDetails.setText(String.format(
                        "Report ID: %s\n" +
                        "Type: %s\n" +
                        "Target ID: %s\n" +
                        "Reporter: %s\n" +
                        "Status: %s\n" +
                        "Created: %s\n\n" +
                        "Reason:\n%s",
                        report.id(), report.type(), report.targetId(),
                        report.reporterName(), report.status(),
                        report.createdAt(), report.reason()
                    ));
                }
            });
        });
    }

    private void resolveReport(String status) {
        if (selectedReportId == null) return;

        String note = txtAdminNote.getText().trim();
        clientService.resolveReport(selectedReportId, status, note, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    JOptionPane.showMessageDialog(this, "Report " + status.toLowerCase());
                    txtAdminNote.setText("");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed: " + result.error(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }

    public void refresh() {
        String filter = (String) cmbFilter.getSelectedItem();
        String status = "All".equals(filter) ? null : filter.toUpperCase();

        clientService.getReports(status, result -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                if (result.success()) {
                    for (var report : result.data()) {
                        tableModel.addRow(new Object[]{
                            truncate(report.id(), 8),
                            report.type(),
                            truncate(report.targetId(), 8),
                            report.reporterName(),
                            report.status(),
                            report.createdAt()
                        });
                    }
                }
            });
        });
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
