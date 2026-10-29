package com.farm2market.admin;

import com.farm2market.admin.ui.AdminLoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class AdminMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }

            new AdminLoginFrame().setVisible(true);
        });
    }
}
