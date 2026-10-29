package com.farm2market.admin.util;

import javax.swing.table.DefaultTableModel;

public class TableModelFactory {

    public static DefaultTableModel createReadOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static DefaultTableModel createEditableModel(String[] columns, int[] editableColumns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                for (int col : editableColumns) {
                    if (col == column) return true;
                }
                return false;
            }
        };
    }
}
