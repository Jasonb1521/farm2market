package com.farm2market.admin.ui;

import com.farm2market.admin.service.AdminClientService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminLoginFrame extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private AdminClientService clientService;

    public AdminLoginFrame() {
        initComponents();
        setupLayout();
        setupListeners();
        clientService = new AdminClientService();
    }

    private void initComponents() {
        setTitle("Farm2Market Admin Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        txtEmail = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Login");
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Admin Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("Email:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel(""));
        formPanel.add(btnLogin);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(formPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.add(lblStatus);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void setupListeners() {
        btnLogin.addActionListener(e -> performLogin());

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clientService.disconnect();
            }
        });
    }

    private void performLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter email and password");
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setText("Connecting...");

        new SwingWorker<Boolean, Void>() {
            private String errorMessage;
            private String token;
            private String userName;

            @Override
            protected Boolean doInBackground() {
                try {
                    if (!clientService.connect()) {
                        errorMessage = "Failed to connect to server";
                        return false;
                    }

                    var result = clientService.login(email, password);
                    if (result.success()) {
                        token = result.token();
                        userName = result.userName();
                        return true;
                    } else {
                        errorMessage = result.error();
                        return false;
                    }
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    if (get()) {
                        openDashboard(userName, token);
                    } else {
                        lblStatus.setText(errorMessage != null ? errorMessage : "Login failed");
                    }
                } catch (Exception e) {
                    lblStatus.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void openDashboard(String userName, String token) {
        AdminDashboardFrame dashboard = new AdminDashboardFrame(clientService, userName);
        dashboard.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new AdminLoginFrame().setVisible(true);
        });
    }
}
