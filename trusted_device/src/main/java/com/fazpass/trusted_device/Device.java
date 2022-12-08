package com.fazpass.trusted_device;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.scottyab.rootbeer.RootBeer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class Device {
    private final Context context;
    static String name;
    static String notificationToken;

    public Device(Context context) {
        this.context = context;
        initialize();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    notificationToken = task.getResult();
                });
    }

    private void initialize(){
        Device.name = readMeta();
    }

    private String readMeta() {
        return ""+ Build.BRAND+","+Build.MODEL+","+Build.VERSION.SDK_INT+","+Build.TIME;
    }

    boolean isRooted() {
        RootBeer rootBeer = new RootBeer(context);
        return rootBeer.isRooted();
    }

}
