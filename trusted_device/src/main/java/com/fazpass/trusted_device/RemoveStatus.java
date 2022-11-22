package com.fazpass.trusted_device;

public class RemoveStatus {
    private final boolean status;

    private final String message;

    public RemoveStatus(boolean status, String message) {
        this.status = status;
        this.message = message;
    }


    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
