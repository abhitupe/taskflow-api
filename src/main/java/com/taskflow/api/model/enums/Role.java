package com.taskflow.api.model.enums;

public enum Role {

    ADMIN("System Administrator"),
    PROJECT_MANAGER("Project Manager"),
    DEVELOPER("Developer"),
    TESTER("Tester");

    private final String displayName;

    Role(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }


}
