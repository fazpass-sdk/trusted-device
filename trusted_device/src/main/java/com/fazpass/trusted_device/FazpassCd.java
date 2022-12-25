package com.fazpass.trusted_device;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FazpassCd {
    protected static final String ACTION_CONFIRM = "com.fazpass.trusted_device.CONFIRM_STATUS";
    protected static final String ACTION_DECLINE = "com.fazpass.trusted_device.DECLINE_STATUS";

    protected static Intent cdActivity;

    /**
     * Initialize Cross Device.
     * @param activity Reference to the first activity to boot when aplication is started.
     * @param requirePin whether this cross device activity should ask for pin or not.
     * @param crossDeviceActivity custom activity to ask user confirmation on an incoming cross device login.
     *                            This activity class is NOT to be called manually.
     *                            If null, default dialog activity for cross device will be used.
     *                            <p>Usage : {@code CustomCrossDeviceActivity.class}</p>
     */
    public static void initialize(Activity activity, boolean requirePin, @Nullable Class<?> crossDeviceActivity) {
        Notification.IS_REQUIRE_PIN = requirePin;
        if (crossDeviceActivity != null) {
            FazpassCd.cdActivity = new Intent(activity, crossDeviceActivity);
        } else {
            FazpassCd.cdActivity = NotificationActivity.buildIntent(activity);
        }

        Bundle notificationExtras = activity.getIntent().getExtras();
        if (notificationExtras != null) {
            Notification notification = new Notification(notificationExtras);

            if (Objects.equals(notification.getApp(), activity.getPackageName())) {
                if (Objects.equals(notification.getStatus(), "request")) {
                    FazpassCd.cdActivity.putExtras(notificationExtras);
                    FazpassCd.cdActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(FazpassCd.cdActivity);
                    activity.finish();
                }
            }
        }
    }

    /**
     * Accept incoming login confirmation from cross device notification.
     * @param activity Reference to your custom cross device activity.
     * @throws RuntimeException If {@code requirePin} in {@link #initialize(Activity, boolean, Class) initialize} is set to true.
     * @throws UnsupportedOperationException If activity argument is not referring to
     * cross device activity initialized in {@link #initialize(Activity, boolean, Class) initialize}.
     * @throws UnsupportedOperationException If cross device activity is started manually.
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
     * @throws RuntimeException If {@code requirePin} in {@link #initialize(Activity, boolean, Class) initialize} is set to false.
     * @throws UnsupportedOperationException If activity argument is not referring to
     * cross device activity initialized in {@link #initialize(Activity, boolean, Class) initialize}.
     * @throws UnsupportedOperationException If cross device activity is started manually.
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
     * @throws UnsupportedOperationException If activity argument is not referring to
     * cross device activity initialized in {@link #initialize(Activity, boolean, Class) initialize}.
     * @throws UnsupportedOperationException If cross device activity is started manually.
     */
    public static void onDecline(Activity activity) {
        sendBroadcast(activity, ACTION_DECLINE);
    }

    private static void sendBroadcast(Activity activity, String action) throws UnsupportedOperationException {
        Notification notification = null;
        try {
            notification = new Notification(activity.getIntent().getExtras());
        } catch (Exception ignored) {}
        if (notification == null) throw new UnsupportedOperationException();
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
