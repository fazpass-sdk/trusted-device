package com.fazpass.trusted_device;

public class Error {

    public static NullPointerException localDataMissing(){
        return new NullPointerException(BASE.LOCAL_MISSING);
    }

    public static SecurityException pinNotMatch(){
        return new SecurityException(BASE.PIN_NOT_MATCH);
    }

    public static Exception biometricError(){
        return new Exception("Biometric Error");
    }

    public static Exception biometricFailed(){
        return new Exception("Biometric Failed");
    }
}
