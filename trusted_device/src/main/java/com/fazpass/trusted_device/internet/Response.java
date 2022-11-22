package com.fazpass.trusted_device.internet;

import com.google.gson.annotations.SerializedName;

public class Response<T> {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private T data;

    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }
}
