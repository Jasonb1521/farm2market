package com.farm2market.core.util;

import com.farm2market.core.enums.Unit;
import java.text.DecimalFormat;

public final class PriceFormatter {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("#,##0.##");

    private PriceFormatter() {}

    public static String formatPrice(double price) {
        return AppConstants.CURRENCY_SYMBOL + PRICE_FORMAT.format(price);
    }

    public static String formatPricePerUnit(double price, Unit unit) {
        return formatPrice(price) + "/" + unit.getSymbol();
    }

    public static String formatQuantity(double quantity) {
        return QUANTITY_FORMAT.format(quantity);
    }

    public static String formatQuantityWithUnit(double quantity, Unit unit) {
        return formatQuantity(quantity) + " " + unit.getSymbol();
    }

    public static String formatLineItem(String name, double quantity, Unit unit, double price) {
        return String.format("%s × %s = %s",
                name,
                formatQuantityWithUnit(quantity, unit),
                formatPrice(price * quantity));
    }

    public static String formatOrderSummary(double subtotal, double deliveryFee, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Subtotal: ").append(formatPrice(subtotal)).append("\n");
        if (deliveryFee > 0) {
            sb.append("Delivery: ").append(formatPrice(deliveryFee)).append("\n");
        }
        sb.append("Total: ").append(formatPrice(total));
        return sb.toString();
    }

    public static double roundPrice(double price) {
        return Math.round(price * 100.0) / 100.0;
    }
}
