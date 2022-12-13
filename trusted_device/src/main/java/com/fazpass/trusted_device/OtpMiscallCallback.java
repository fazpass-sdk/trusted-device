package com.fazpass.trusted_device;

import android.os.Build;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

/**
 * Min SDK = 31
 */
@RequiresApi(api = Build.VERSION_CODES.S)
class OtpMiscallCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
    private final Function<Void, TelephonyManager> onCalledCallback;
    private int lastState = 0;

    public OtpMiscallCallback(Function<Void, TelephonyManager> onCalledCallback) {
        this.onCalledCallback = onCalledCallback;
    }

    @Override
    public void onCallStateChanged(int state) {
        if (state==TelephonyManager.CALL_STATE_IDLE && lastState!=TelephonyManager.CALL_STATE_IDLE) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    TelephonyManager manager = onCalledCallback.apply(null);
                    // unregister listener
                    manager.unregisterTelephonyCallback(OtpMiscallCallback.this);
                }
            }, 1000);
        }
        lastState = state;
    }
}