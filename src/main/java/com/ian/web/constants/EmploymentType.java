package com.ian.web.constants;

public enum EmploymentType {
    PLANTILLA("Plantilla"),
    NON_PLANTILLA("Non-Plantilla");

    private final String displayEmployeeType;

    EmploymentType(String displayEmployeeType){
        this.displayEmployeeType = displayEmployeeType;
    }


}
