package com.farm2market.admin.ui;

import com.farm2market.admin.service.AdminClientService;
import com.farm2market.admin.util.TableModelFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UsersPanel extends JPanel {

    private final AdminClientService clientService;

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbRole;
    private JComboBox<String> cmbStatus;
    private JTextArea txtDetails;
    private JButton btnSuspend;
    private JButton btnActivate;

    private String selectedUserId;
    private String selectedUserStatus;

    public UsersPanel(AdminClientService clientService) {
        this.clientService = clientService;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(15);
        txtSearch.addActionListener(e -> refresh());
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Role:"));
        cmbRole = new JComboBox<>(new String[]{"All", "CUSTOMER", "FARMER", "ADMIN"});
        cmbRole.addActionListener(e -> refresh());
        filterPanel.add(cmbRole);

        filterPanel.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"All", "ACTIVE", "SUSPENDED"});
        cmbStatus.addActionListener(e -> refresh());
        filterPanel.add(cmbStatus);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());
        filterPanel.add(btnRefresh);

        add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Email", "Role", "Status", "Joined"};
        tableModel = TableModelFactory.createReadOnlyModel(columns);
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onUserSelected();
        });

        JScrollPane tableScroll = new JScrollPane(usersTable);
        tableScroll.setPreferredSize(new Dimension(700, 300));

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("User Details"));

        txtDetails = new JTextArea(10, 50);
        txtDetails.setEditable(false);
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        detailsPanel.add(new JScrollPane(txtDetails), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnViewOrders = new JButton("View Orders");
        btnViewOrders.addActionListener(e -> viewUserOrders());
        actionPanel.add(btnViewOrders);

        JButton btnViewReports = new JButton("View Reports");
        btnViewReports.addActionListener(e -> viewUserReports());
        actionPanel.add(btnViewReports);

        btnSuspend = new JButton("Suspend User");
        btnSuspend.setEnabled(false);
        btnSuspend.setForeground(Color.RED);
        btnSuspend.addActionListener(e -> suspendUser());
        actionPanel.add(btnSuspend);

        btnActivate = new JButton("Activate User");
        btnActivate.setEnabled(false);
        btnActivate.addActionListener(e -> activateUser());
        actionPanel.add(btnActivate);

        detailsPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailsPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void onUserSelected() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            selectedUserId = null;
            selectedUserStatus = null;
            txtDetails.setText("");
            btnSuspend.setEnabled(false);
            btnActivate.setEnabled(false);
            return;
        }

        selectedUserId = (String) tableModel.getValueAt(row, 0);
        selectedUserStatus = (String) tableModel.getValueAt(row, 4);
        String role = (String) tableModel.getValueAt(row, 3);

        boolean isAdmin = "ADMIN".equals(role);
        btnSuspend.setEnabled(!isAdmin && "ACTIVE".equals(selectedUserStatus));
        btnActivate.setEnabled(!isAdmin && "SUSPENDED".equals(selectedUserStatus));

        loadUserDetails(selectedUserId);
    }

    private void loadUserDetails(String userId) {
        clientService.getUserDetails(userId, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    var user = result.data();
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format(
                        "User ID: %s\n" +
                        "Name: %s\n" +
                        "Email: %s\n" +
                        "Mobile: %s\n" +
                        "Role: %s\n" +
                        "Status: %s\n" +
                        "Joined: %s\n",
                        user.id(), user.name(), user.email(),
                        user.mobile(), user.role(), user.status(),
                        user.createdAt()
                    ));

                    if ("FARMER".equals(user.role())) {
                        sb.append(String.format(
                            "\nFarm Name: %s\n" +
                            "Location: %s\n" +
                            "Active Listings: %d\n" +
                            "Total Orders: %d\n" +
                            "Rating: %.1f (%d reviews)\n",
                            user.farmName(), user.location(),
                            user.activeListings(), user.totalOrders(),
                            user.rating(), user.reviewCount()
                        ));
                    } else if ("CUSTOMER".equals(user.role())) {
                        sb.append(String.format(
                            "\nTotal Orders: %d\n" +
                            "Total Spent: ₹%.2f\n",
                            user.totalOrders(), user.totalSpent()
                        ));
                    }

                    txtDetails.setText(sb.toString());
                }
            });
        });
    }

    private void viewUserOrders() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first");
            return;
        }

        clientService.getUserOrders(selectedUserId, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    var orders = result.data();
                    if (orders.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No orders found for this user");
                    } else {
                        StringBuilder sb = new StringBuilder("Orders:\n\n");
                        for (var order : orders) {
                            sb.append(String.format("%s - %s - ₹%.2f - %s\n",
                                order.id().substring(0, 8), order.date(),
                                order.total(), order.status()));
                        }
                        JOptionPane.showMessageDialog(this, sb.toString(),
                            "User Orders", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        });
    }

    private void viewUserReports() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first");
            return;
        }

        clientService.getUserReports(selectedUserId, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    var reports = result.data();
                    if (reports.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No reports found for this user");
                    } else {
                        StringBuilder sb = new StringBuilder("Reports:\n\n");
                        for (var report : reports) {
                            sb.append(String.format("%s - %s - %s - %s\n",
                                report.id().substring(0, 8), report.type(),
                                report.status(), report.date()));
                        }
                        JOptionPane.showMessageDialog(this, sb.toString(),
                            "User Reports", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        });
    }

    private void suspendUser() {
        if (selectedUserId == null) return;

        String reason = JOptionPane.showInputDialog(this, "Enter suspension reason:");
        if (reason == null || reason.trim().isEmpty()) return;

        clientService.suspendUser(selectedUserId, reason, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    JOptionPane.showMessageDialog(this, "User suspended");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed: " + result.error(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }

    private void activateUser() {
        if (selectedUserId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Activate this user?", "Confirm Activation",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            clientService.activateUser(selectedUserId, result -> {
                SwingUtilities.invokeLater(() -> {
                    if (result.success()) {
                        JOptionPane.showMessageDialog(this, "User activated");
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed: " + result.error(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        }
    }

    public void refresh() {
        String search = txtSearch.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();

        clientService.getUsers(
            search.isEmpty() ? null : search,
            "All".equals(role) ? null : role,
            "All".equals(status) ? null : status,
            result -> {
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    if (result.success()) {
                        for (var user : result.data()) {
                            tableModel.addRow(new Object[]{
                                user.id(),
                                user.name(),
                                user.email(),
                                user.role(),
                                user.status(),
                                user.createdAt()
                            });
                        }
                    }
                });
            });
    }
}
