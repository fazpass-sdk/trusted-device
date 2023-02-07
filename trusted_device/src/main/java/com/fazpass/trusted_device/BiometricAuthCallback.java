package com.fazpass.trusted_device;

public interface BiometricAuthCallback {
    void onSucceed();
    void onFailed();
    void onError();
}
