package com.fazpass.trusted_device;

import androidx.annotation.NonNull;

public class User {


    private String email;
    private String phone;
    private String name;
    private String idCard;
    private String address;
    private static boolean useFinger;

    /**
     *
     * @param email - Email of user just for recording
     * @param phone - Phone of user just for recording
     * @param name - Name of user just for recording
     * @param idCard - ID card of user just for recording
     * @param address - address of user just for recording
     */
    public User(@NonNull String email, @NonNull String phone, @NonNull String name, @NonNull String idCard, @NonNull String address) {
        if(email.equals("") && phone.equals("")){
            throw new NullPointerException("email or phone is required");
        }
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.idCard = idCard;
        this.address = address;
    }

    /**
     * Will be use for check user that possibility use cross app feature
     */
    protected User(){

    }

    /**
     *
     * @param email - Email of user just for recording
     * @param phone - Phone of user just for recording
     */
    protected User(String email, String phone) {
        this.email = email;
        this.phone = phone;
        this.name = "";
        this.idCard = "";
        this.address = "";
    }

    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getIdCard() {
        return idCard;
    }

    public String getAddress() {
        return address;
    }

    public static boolean isUseFinger() {
        return useFinger;
    }

    public static void setIsUseFinger(boolean isUseFinger) {
        User.useFinger = isUseFinger;
    }
}
