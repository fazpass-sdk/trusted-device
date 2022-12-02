package com.fazpass.trusted_device;

abstract class BASE {
    public TRUSTED_DEVICE td_status;
    public CROSS_DEVICE cd_status;
    protected CROSS_APP ca_status;
    static final String DEBUG = "http://localhost:8080/";
    static final String STAGING = "https://channa.fazpas.com/";
    static final String BASE_URL = "base_url";
    static final String MERCHANT_TOKEN = "merchant_token";
    static final String PRODUCTION = "https://api.fazpass.com/";
    static final String PACKAGE_NAME = "package_name";
    static final String PRIVATE_KEY = "private_key";
    static final String PUBLIC_KEY = "public_key";
    static final String META = "meta";
    static final String USER_EMAIL = "user_email";
    static final String USER_PHONE = "user_phone";
    static final String USER_PIN = "user_pin";
    static final String USER_ID = "user_id";
    static final String DEVICE = "device";
    static final String FAZPASS = "com.fazpass.sdk";
    static final String TAG = "fazpass";
    static final String LOCAL_MISSING = "LOCAL DATA IS MISSING";
    static final String PIN_NOT_MATCH = "COMPARING PIN IS FAILED";

}
