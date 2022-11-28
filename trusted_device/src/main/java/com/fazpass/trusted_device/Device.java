package com.fazpass.trusted_device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.scottyab.rootbeer.RootBeer;

import java.util.concurrent.atomic.AtomicReference;


class Device {
    private final Context context;
    private String device;
    static String notificationToken;

    public Device(Context context) {
        this.context = context;
        initialize(context);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    // Get new Instance ID token
                    notificationToken = task.getResult().getToken();
                });
    }

    private void initialize(Context context){
        this.device = readMeta()+"-"+generateAppId(context);
    }

    @SuppressLint("HardwareIds")
    private String generateAppId(Context context){
        return Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String readMeta(){
        return ""+ Build.BRAND+","+Build.MODEL+","+Build.VERSION.SDK_INT;
    }

    boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    boolean isRooted() {
        RootBeer rootBeer = new RootBeer(context);
        return rootBeer.isRooted();
    }

    String getDevice() {
        return device;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

}
