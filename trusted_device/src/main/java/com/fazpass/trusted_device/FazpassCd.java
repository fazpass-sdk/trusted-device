package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.Notification.CD_NOTIFICATION_CHANNEL;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Objects;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FazpassCd {
    protected static final String ACTION_CONFIRM = "com.fazpass.trusted_device.CONFIRM_STATUS";
    protected static final String ACTION_DECLINE = "com.fazpass.trusted_device.DECLINE_STATUS";

    protected static Intent cdActivity;
    protected static Notification notification;
    protected static boolean useDefaultActivity;
    private static BroadcastReceiver notificationReceiver;

    /**
     * Initialize Cross Device.
     * @param activity Reference to the first activity to boot when aplication is started.
     * @param requirePin whether this cross device activity should ask for pin or not.
     * @param useDefaultActivity  if true, automatically handle cross device activity.
     *                            otherwise cross device activity must be handled manually.
     * @return boolean will be true if app is launched from cross device notification.
     */
    public static boolean initialize(Activity activity, boolean requirePin, boolean useDefaultActivity) {
        FazpassCd.useDefaultActivity = useDefaultActivity;
        Notification.IS_REQUIRE_PIN = requirePin;

        Bundle notificationExtras = activity.getIntent().getExtras();
        if (notificationExtras != null) {
            notification = new Notification(notificationExtras);

            if (useDefaultActivity) {
                FazpassCd.cdActivity = NotificationActivity.buildIntent(activity);

                if (Objects.equals(notification.getApp(), activity.getPackageName())) {
                    if (Objects.equals(notification.getStatus(), "request")) {
                        FazpassCd.cdActivity.putExtras(notificationExtras);
                        FazpassCd.cdActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(FazpassCd.cdActivity);
                        //activity.finish();
                    }
                }
            }
        }

        return notification != null;
    }

    /**
     * Start notification listener. Used to handle incoming cross device notification while application is running.
     * @param activity Reference to the activity.
     * @param listener Called when there is an incoming cross device notification. String parameter will return their device information.
     */
    public static void startNotificationListener(Activity activity, Function<String, Void> listener) {
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notification = new Notification(intent.getExtras());
                listener.apply(notification.getDevice());
            }
        };
        LocalBroadcastManager.getInstance(activity)
                .registerReceiver(notificationReceiver, new IntentFilter(CD_NOTIFICATION_CHANNEL));
    }

    /**
     * Stop notification listener. Stop handling incoming cross device notification.
     * @param activity Reference to the activity that has started notification listener.
     */
    public static void stopNotificationListener(Activity activity) {
        LocalBroadcastManager.getInstance(activity)
                .unregisterReceiver(notificationReceiver);
    }

    /**
     * Accept incoming login confirmation from cross device notification.
     * @param activity Reference to your custom cross device activity.
     * @throws RuntimeException If {@code requirePin} in {@link #initialize(Activity, boolean, boolean) initialize} is set to true.
     */
    public static void onConfirm(Activity activity) {
        if (Notification.IS_REQUIRE_PIN) throw new RuntimeException("Pin is required according to initialize method.");
        sendBroadcast(activity, ACTION_CONFIRM);
    }

    /**
     * Accept incoming login confirmation from cross device notification with pin.
     * @param activity Reference to your custom cross device activity.
     * @param pin User inputted pin to validate.
     * @param pinValidationCallback Boolean argument will be true if pin is match and pin validation is successful. Otherwise it will be false.
     * @throws RuntimeException If {@code requirePin} in {@link #initialize(Activity, boolean, boolean) initialize} is set to false.
     */
    public static void onConfirmRequirePin(Activity activity, String pin, Function<Boolean, Void> pinValidationCallback) {
        if (!Notification.IS_REQUIRE_PIN) throw new RuntimeException("Pin is not required according to initialize method.");
        if (pin == null || pin.equals("")) {
            Toast.makeText(activity, "PIN is required", Toast.LENGTH_SHORT).show();
            return;
        }
        TrustedDevice.validatePin(activity, pin)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.getStatus()) {
                                sendBroadcast(activity, ACTION_CONFIRM);
                            }
                            pinValidationCallback.apply(response.getStatus());
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            pinValidationCallback.apply(false);
                        }
                );
    }

    /**
     * Decline incoming login confirmation from cross device notification.
     * @param activity Reference to your custom cross device activity.
     */
    public static void onDecline(Activity activity) {
        sendBroadcast(activity, ACTION_DECLINE);
    }

    private static void sendBroadcast(Activity activity, String action) {
        Intent intent = new Intent(activity, NotificationBroadcastReceiver.class);
        intent.setAction(action);
        assignIntentExtras(intent, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                activity, Notification.NOTIFICATION_REQ_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private static void assignIntentExtras(Intent intent, Notification notification) {
        intent.putExtra(Notification.NOTIFICATION_ID, notification.getNotificationId());
        intent.putExtra(Notification.NOTIFICATION_DEVICE, notification.getDevice());
        intent.putExtra(Notification.NOTIFICATION_TOKEN, notification.getNotificationToken());
    }
}
