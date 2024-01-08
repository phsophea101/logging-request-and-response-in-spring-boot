package com.sample.spring.conts;

public enum BizErrorCode implements ErrorCodeType {
    /**
     * Error General exception.
     */
    E0000("E0000", "General error occurred."),
    E0001("E0001", "Accept language header not support for [%s]."),
    E0002("E0002", "Record not found."),
    E0003("E0003", "Valid field validation."),
    E0004("E0004", "Rest client error occurred."),
    ;

    final String value;
    final String description;

    BizErrorCode(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
