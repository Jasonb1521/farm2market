package com.farm2market.admin.ui;

import com.farm2market.admin.service.AdminClientService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminDashboardFrame extends JFrame {

    private final AdminClientService clientService;
    private final String userName;
    private JTabbedPane tabbedPane;
    private SummaryPanel summaryPanel;
    private ReportsPanel reportsPanel;
    private ListingsPanel listingsPanel;
    private UsersPanel usersPanel;

    public AdminDashboardFrame(AdminClientService clientService, String userName) {
        this.clientService = clientService;
        this.userName = userName;
        initComponents();
        setupMenuBar();
        setupListeners();
    }

    private void initComponents() {
        setTitle("Farm2Market Admin - " + userName);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Farm2Market Admin Console");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel userLabel = new JLabel("Logged in as: " + userName);
        headerPanel.add(userLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();

        summaryPanel = new SummaryPanel(clientService);
        reportsPanel = new ReportsPanel(clientService);
        listingsPanel = new ListingsPanel(clientService);
        usersPanel = new UsersPanel(clientService);

        tabbedPane.addTab("Summary", new ImageIcon(), summaryPanel, "Platform overview");
        tabbedPane.addTab("Reports", new ImageIcon(), reportsPanel, "Manage reports");
        tabbedPane.addTab("Listings", new ImageIcon(), listingsPanel, "Manage product listings");
        tabbedPane.addTab("Users", new ImageIcon(), usersPanel, "Manage users");

        add(tabbedPane, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(new JLabel("Connected to server"));
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exportOrdersItem = new JMenuItem("Export Orders CSV");
        exportOrdersItem.addActionListener(e -> exportOrders());
        fileMenu.add(exportOrdersItem);

        JMenuItem exportSalesItem = new JMenuItem("Export Sales CSV");
        exportSalesItem.addActionListener(e -> exportSales());
        fileMenu.add(exportSalesItem);

        fileMenu.addSeparator();

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setMnemonic(KeyEvent.VK_L);
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.addActionListener(e -> confirmExit());
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.addActionListener(e -> refreshCurrentPanel());
        viewMenu.add(refreshItem);

        menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void setupListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            switch (index) {
                case 0 -> summaryPanel.refresh();
                case 1 -> reportsPanel.refresh();
                case 2 -> listingsPanel.refresh();
                case 3 -> usersPanel.refresh();
            }
        });
    }

    private void refreshCurrentPanel() {
        int index = tabbedPane.getSelectedIndex();
        switch (index) {
            case 0 -> summaryPanel.refresh();
            case 1 -> reportsPanel.refresh();
            case 2 -> listingsPanel.refresh();
            case 3 -> usersPanel.refresh();
        }
    }

    private void exportOrders() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("orders.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            clientService.exportOrdersCsv(path, result -> {
                SwingUtilities.invokeLater(() -> {
                    if (result.success()) {
                        JOptionPane.showMessageDialog(this, "Orders exported to " + path);
                    } else {
                        JOptionPane.showMessageDialog(this, "Export failed: " + result.error(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        }
    }

    private void exportSales() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("sales.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            clientService.exportSalesCsv(path, result -> {
                SwingUtilities.invokeLater(() -> {
                    if (result.success()) {
                        JOptionPane.showMessageDialog(this, "Sales exported to " + path);
                    } else {
                        JOptionPane.showMessageDialog(this, "Export failed: " + result.error(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        }
    }

    private void logout() {
        clientService.logout();
        clientService.disconnect();
        AdminLoginFrame loginFrame = new AdminLoginFrame();
        loginFrame.setVisible(true);
        dispose();
    }

    private void confirmExit() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit?", "Confirm Exit",
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            clientService.disconnect();
            System.exit(0);
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "Farm2Market Admin Console\nVersion 1.0\n\nManage your marketplace",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
