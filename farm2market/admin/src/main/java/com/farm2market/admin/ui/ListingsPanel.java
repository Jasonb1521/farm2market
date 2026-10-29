package com.farm2market.admin.ui;

import com.farm2market.admin.service.AdminClientService;
import com.farm2market.admin.util.TableModelFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ListingsPanel extends JPanel {

    private final AdminClientService clientService;

    private JTable listingsTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbCategory;
    private JComboBox<String> cmbStatus;
    private JTextArea txtDetails;
    private JButton btnHide;
    private JButton btnUnhide;
    private JButton btnDelete;

    private String selectedProductId;
    private String selectedStatus;

    public ListingsPanel(AdminClientService clientService) {
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

        filterPanel.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"All", "VEGETABLES", "FRUITS", "GRAINS",
            "DAIRY", "PULSES", "SPICES", "HERBS", "EGGS", "HONEY", "OTHER"});
        cmbCategory.addActionListener(e -> refresh());
        filterPanel.add(cmbCategory);

        filterPanel.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"All", "ACTIVE", "INACTIVE", "HIDDEN"});
        cmbStatus.addActionListener(e -> refresh());
        filterPanel.add(cmbStatus);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());
        filterPanel.add(btnRefresh);

        add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Farmer", "Category", "Price", "Stock", "Status"};
        tableModel = TableModelFactory.createReadOnlyModel(columns);
        listingsTable = new JTable(tableModel);
        listingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onListingSelected();
        });

        JScrollPane tableScroll = new JScrollPane(listingsTable);
        tableScroll.setPreferredSize(new Dimension(700, 300));

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Listing Details"));

        txtDetails = new JTextArea(10, 50);
        txtDetails.setEditable(false);
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        detailsPanel.add(new JScrollPane(txtDetails), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnHide = new JButton("Hide Listing");
        btnHide.setEnabled(false);
        btnHide.addActionListener(e -> hideListing());
        actionPanel.add(btnHide);

        btnUnhide = new JButton("Unhide Listing");
        btnUnhide.setEnabled(false);
        btnUnhide.addActionListener(e -> unhideListing());
        actionPanel.add(btnUnhide);

        btnDelete = new JButton("Delete Listing");
        btnDelete.setEnabled(false);
        btnDelete.setForeground(Color.RED);
        btnDelete.addActionListener(e -> deleteListing());
        actionPanel.add(btnDelete);

        detailsPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailsPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void onListingSelected() {
        int row = listingsTable.getSelectedRow();
        if (row < 0) {
            selectedProductId = null;
            selectedStatus = null;
            txtDetails.setText("");
            btnHide.setEnabled(false);
            btnUnhide.setEnabled(false);
            btnDelete.setEnabled(false);
            return;
        }

        selectedProductId = (String) tableModel.getValueAt(row, 0);
        selectedStatus = (String) tableModel.getValueAt(row, 6);

        btnHide.setEnabled(!"HIDDEN".equals(selectedStatus));
        btnUnhide.setEnabled("HIDDEN".equals(selectedStatus));
        btnDelete.setEnabled(true);

        loadListingDetails(selectedProductId);
    }

    private void loadListingDetails(String productId) {
        clientService.getProductDetails(productId, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    var product = result.data();
                    txtDetails.setText(String.format(
                        "Product ID: %s\n" +
                        "Name: %s\n" +
                        "Category: %s\n" +
                        "Price: ₹%.2f per %s\n" +
                        "Stock: %.1f %s\n" +
                        "Status: %s\n" +
                        "Farmer: %s\n" +
                        "Organic: %s\n" +
                        "Created: %s\n\n" +
                        "Description:\n%s",
                        product.id(), product.name(), product.category(),
                        product.price(), product.unit(),
                        product.stock(), product.unit(),
                        product.status(), product.farmerName(),
                        product.organic() ? "Yes" : "No",
                        product.createdAt(), product.description()
                    ));
                }
            });
        });
    }

    private void hideListing() {
        if (selectedProductId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Hide this listing from customers?", "Confirm Hide",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            clientService.hideListing(selectedProductId, result -> {
                SwingUtilities.invokeLater(() -> {
                    if (result.success()) {
                        JOptionPane.showMessageDialog(this, "Listing hidden");
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed: " + result.error(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        }
    }

    private void unhideListing() {
        if (selectedProductId == null) return;

        clientService.unhideListing(selectedProductId, result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    JOptionPane.showMessageDialog(this, "Listing unhidden");
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed: " + result.error(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }

    private void deleteListing() {
        if (selectedProductId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Permanently delete this listing? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            clientService.deleteListing(selectedProductId, result -> {
                SwingUtilities.invokeLater(() -> {
                    if (result.success()) {
                        JOptionPane.showMessageDialog(this, "Listing deleted");
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
        String category = (String) cmbCategory.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();

        clientService.getListings(
            search.isEmpty() ? null : search,
            "All".equals(category) ? null : category,
            "All".equals(status) ? null : status,
            result -> {
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    if (result.success()) {
                        for (var listing : result.data()) {
                            tableModel.addRow(new Object[]{
                                listing.id(),
                                listing.name(),
                                listing.farmerName(),
                                listing.category(),
                                String.format("₹%.2f/%s", listing.price(), listing.unit()),
                                String.format("%.1f", listing.stock()),
                                listing.status()
                            });
                        }
                    }
                });
            });
    }
}
