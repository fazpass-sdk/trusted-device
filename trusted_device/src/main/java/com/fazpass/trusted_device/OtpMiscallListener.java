package com.fazpass.trusted_device;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;


/**
 * Max SDK = 30
 */
class OtpMiscallListener extends PhoneStateListener {
   private final Function<Void, TelephonyManager> onCalledCallback;
   private int lastState = 0;

   public OtpMiscallListener(Function<Void, TelephonyManager> onCalledCallback) {
      this.onCalledCallback = onCalledCallback;
   }

   @Override
   public void onCallStateChanged(int state, String phoneNumber) {
      super.onCallStateChanged(state, phoneNumber);
      if (state==TelephonyManager.CALL_STATE_IDLE && lastState!=TelephonyManager.CALL_STATE_IDLE) {
         new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
               TelephonyManager manager = onCalledCallback.apply(null);
               // unregister listener
               manager.listen(OtpMiscallListener.this, PhoneStateListener.LISTEN_NONE);
            }
         }, 1000);
      }
      lastState = state;
   }
}