package com.farm2market.core.enums;

public enum Unit {
    KG("kg", "Kilogram", 1.0),
    G("g", "Gram", 0.001),
    LITRE("L", "Litre", 1.0),
    ML("ml", "Millilitre", 0.001),
    BUNCH("bunch", "Bunch", 1.0),
    PIECE("piece", "Piece", 1.0),
    DOZEN("dozen", "Dozen", 12.0);

    private final String symbol;
    private final String displayName;
    private final double baseMultiplier;

    Unit(String symbol, String displayName, double baseMultiplier) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.baseMultiplier = baseMultiplier;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseMultiplier() {
        return baseMultiplier;
    }

    public String format(double quantity) {
        if (quantity == (int) quantity) {
            return String.format("%d %s", (int) quantity, symbol);
        }
        return String.format("%.2f %s", quantity, symbol);
    }

    public String formatPrice(double price) {
        return String.format("₹%.2f/%s", price, symbol);
    }
}
