package com.taskflow.api.model.enums;

/**
 * Task priority levels
 *
 * Using ordinal values for easy sorting (LOW=0, HIGH=3)
 */

public enum Priority {

    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");

    private final String displayName;

    Priority(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return this.ordinal();
    }

}
