package com.abc.expensevoucher.entity;

public enum ExpenseCategory {
    TRAVEL("Travel"),
    FOOD_MEALS("Food & Meals"),
    ACCOMMODATION("Accommodation"),
    OFFICE_SUPPLIES("Office Supplies"),
    SOFTWARE_SUBSCRIPTIONS("Software & Subscriptions"),
    CLIENT_ENTERTAINMENT("Client Entertainment"),
    FUEL_TRANSPORT("Fuel & Transport"),
    TRAINING("Training"),
    COMMUNICATION("Communication"),
    MISCELLANEOUS("Miscellaneous");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
