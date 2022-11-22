package com.fazpass.trusted_device;

public abstract class Otp {
    public interface Request {
        void onComplete(OtpResponse response);
        // Will be handled only for sms & missed call
        void onIncomingMessage(String otp);
        void onError(Throwable err);

    }
    public interface Validate {
        void onComplete(boolean status);
        void onError(Throwable err);
    }
}
