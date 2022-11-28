package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.BASE.BASE_URL;
import static com.fazpass.trusted_device.BASE.MERCHANT_TOKEN;
import static com.fazpass.trusted_device.BASE.USER_ID;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fazpass.trusted_device.internet.Roaming;
import com.fazpass.trusted_device.internet.UseCase;
import com.fazpass.trusted_device.internet.request.ConfirmStatusRequest;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Objects.equals(intent.getAction(), "com.fazpass.trusted_device.CONFIRM_STATUS")
                || Objects.equals(intent.getAction(), "com.fazpass.trusted_device.DECLINE_STATUS")) {

            String userId = Storage.readDataLocal(ctx, USER_ID);
            String packageName = ctx.getPackageName();

            String notificationId = intent.getStringExtra(Notification.NOTIFICATION_ID);
            int requestId = intent.getIntExtra(Notification.NOTIFICATION_REQ_ID, 0);
            String deviceName = intent.getStringExtra(Notification.NOTIFICATION_DEVICE);
            String notificationToken = intent.getStringExtra(Notification.NOTIFICATION_TOKEN);
            String userResponse = Objects.equals(intent.getAction(), "com.fazpass.trusted_device.CONFIRM_STATUS")
                    ? "yes" : "no";

            ConfirmStatusRequest body = new ConfirmStatusRequest(
                    userId, notificationId, deviceName,
                    packageName, notificationToken, userResponse
            );
            UseCase u = Roaming.start(Storage.readDataLocal(ctx,BASE_URL));
            u.confirmStatus("Bearer "+Storage.readDataLocal(ctx,MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resp-> Toast.makeText(ctx, resp.getMessage(), Toast.LENGTH_SHORT).show(),
                            err-> Toast.makeText(ctx, "Failed to confirm login.", Toast.LENGTH_SHORT).show()
                    );

            notificationManager.cancel(requestId);
        }
    }
}
