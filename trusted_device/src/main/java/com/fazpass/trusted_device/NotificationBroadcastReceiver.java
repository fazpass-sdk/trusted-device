package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.BASE.BASE_URL;
import static com.fazpass.trusted_device.BASE.MERCHANT_TOKEN;
import static com.fazpass.trusted_device.BASE.USER_ID;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.fazpass.trusted_device.internet.Roaming;
import com.fazpass.trusted_device.internet.UseCase;
import com.fazpass.trusted_device.internet.request.ConfirmStatusRequest;
import com.fazpass.trusted_device.internet.request.LogFraudRequest;

import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Objects.equals(intent.getAction(), FazpassCd.ACTION_CONFIRM)
                || Objects.equals(intent.getAction(), FazpassCd.ACTION_DECLINE)) {

            String userId = Storage.readDataLocal(ctx, USER_ID);
            String packageName = ctx.getPackageName();

            String notificationId = intent.getStringExtra(Notification.NOTIFICATION_ID);
            String deviceName = intent.getStringExtra(Notification.NOTIFICATION_DEVICE);
            String notificationToken = intent.getStringExtra(Notification.NOTIFICATION_TOKEN);
            String userResponse = Objects.equals(intent.getAction(), FazpassCd.ACTION_CONFIRM)
                    ? "yes" : "no";

            String successMessage;
            int reason;
            if (userResponse.equals("yes")) {
                successMessage = "Login successfully accepted";
                reason = 1;
            }
            else {
                successMessage = "Login successfully rejected";
                reason = 3;
            }
            String errorMessage = "Failed to respond. Please try again.";

            ConfirmStatusRequest body = new ConfirmStatusRequest(
                    userId, notificationId, deviceName,
                    packageName, notificationToken, userResponse
            );
            LogFraudRequest fraudBody = new LogFraudRequest(
                    userId, deviceName, packageName, reason
            );
            UseCase u = Roaming.start(Storage.readDataLocal(ctx,BASE_URL));
            u.confirmStatus("Bearer "+Storage.readDataLocal(ctx,MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.newThread())
                    .doOnError(err->{
                        fraudBody.setReason(3);
                        u.logFraud("Bearer "+Storage.readDataLocal(ctx,MERCHANT_TOKEN), fraudBody).subscribe();
                    })
                    .switchMap(resp->u.logFraud("Bearer "+Storage.readDataLocal(ctx,MERCHANT_TOKEN), fraudBody))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resp-> Toast.makeText(ctx, successMessage, Toast.LENGTH_SHORT).show(),
                            err-> Toast.makeText(ctx, errorMessage, Toast.LENGTH_SHORT).show()
                    );

            notificationManager.cancel(Notification.NOTIFICATION_REQ_ID);
        }
    }
}
