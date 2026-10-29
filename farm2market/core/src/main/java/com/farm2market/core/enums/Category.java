package com.farm2market.core.enums;

public enum Category {
    VEGETABLES("Vegetables", "Fresh vegetables"),
    FRUITS("Fruits", "Fresh fruits"),
    GRAINS("Grains", "Rice, wheat, millets, and other grains"),
    DAIRY("Dairy", "Milk, curd, butter, and dairy products"),
    PULSES("Pulses", "Lentils, beans, and legumes"),
    SPICES("Spices", "Fresh and dried spices"),
    HERBS("Herbs", "Fresh herbs and greens"),
    EGGS("Eggs", "Farm fresh eggs"),
    HONEY("Honey", "Natural honey products"),
    OTHER("Other", "Other farm products");

    private final String displayName;
    private final String description;

    Category(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
