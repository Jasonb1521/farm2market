package com.farm2market.admin.ui;

import com.farm2market.admin.service.AdminClientService;

import javax.swing.*;
import java.awt.*;

public class SummaryPanel extends JPanel {

    private final AdminClientService clientService;

    private JLabel lblTotalUsers;
    private JLabel lblCustomers;
    private JLabel lblFarmers;
    private JLabel lblAdmins;
    private JLabel lblActiveListings;
    private JLabel lblOrdersToday;
    private JLabel lblOrdersWeek;
    private JLabel lblOrdersMonth;
    private JLabel lblRevenueToday;
    private JLabel lblRevenueWeek;
    private JLabel lblRevenueMonth;
    private JLabel lblPendingReports;

    public SummaryPanel(AdminClientService clientService) {
        this.clientService = clientService;
        initComponents();
        refresh();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Platform Summary");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(3, 4, 15, 15));

        lblTotalUsers = createStatCard(cardsPanel, "Total Users", "0");
        lblCustomers = createStatCard(cardsPanel, "Customers", "0");
        lblFarmers = createStatCard(cardsPanel, "Farmers", "0");
        lblAdmins = createStatCard(cardsPanel, "Admins", "0");

        lblActiveListings = createStatCard(cardsPanel, "Active Listings", "0");
        lblOrdersToday = createStatCard(cardsPanel, "Orders Today", "0");
        lblOrdersWeek = createStatCard(cardsPanel, "Orders This Week", "0");
        lblOrdersMonth = createStatCard(cardsPanel, "Orders This Month", "0");

        lblRevenueToday = createStatCard(cardsPanel, "Revenue Today", "₹0");
        lblRevenueWeek = createStatCard(cardsPanel, "Revenue This Week", "₹0");
        lblRevenueMonth = createStatCard(cardsPanel, "Revenue This Month", "₹0");
        lblPendingReports = createStatCard(cardsPanel, "Pending Reports", "0");

        add(cardsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel createStatCard(JPanel parent, String title, String value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLbl.setForeground(Color.GRAY);
        card.add(titleLbl, BorderLayout.NORTH);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        card.add(valueLbl, BorderLayout.CENTER);

        parent.add(card);
        return valueLbl;
    }

    public void refresh() {
        clientService.getPlatformSummary(result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.success()) {
                    var data = result.data();
                    lblTotalUsers.setText(String.valueOf(data.totalUsers()));
                    lblCustomers.setText(String.valueOf(data.customers()));
                    lblFarmers.setText(String.valueOf(data.farmers()));
                    lblAdmins.setText(String.valueOf(data.admins()));
                    lblActiveListings.setText(String.valueOf(data.activeListings()));
                    lblOrdersToday.setText(String.valueOf(data.ordersToday()));
                    lblOrdersWeek.setText(String.valueOf(data.ordersWeek()));
                    lblOrdersMonth.setText(String.valueOf(data.ordersMonth()));
                    lblRevenueToday.setText(String.format("₹%.0f", data.revenueToday()));
                    lblRevenueWeek.setText(String.format("₹%.0f", data.revenueWeek()));
                    lblRevenueMonth.setText(String.format("₹%.0f", data.revenueMonth()));
                    lblPendingReports.setText(String.valueOf(data.pendingReports()));
                }
            });
        });
    }
}
