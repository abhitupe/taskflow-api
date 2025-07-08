package com.taskflow.api.model.enums;

public enum TaskStatus {

    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("In Review"),
    TESTING("Testing"),
    DONE("Done"),
    CANCELLED("Cancelled");

    private final String displayName;

    TaskStatus(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }

    /**
     * Business Logic: Valid status transitions
     * This is how to implement workflow rules.
     */

    public boolean canTransitionTo(TaskStatus newStatus){
        return switch (this) {
            case TODO -> newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case IN_PROGRESS -> newStatus == IN_REVIEW || newStatus == TODO || newStatus == CANCELLED;
            case IN_REVIEW -> newStatus == TESTING || newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case TESTING -> newStatus == DONE || newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case DONE, CANCELLED -> false; // Terminal states
        };
    }

}
