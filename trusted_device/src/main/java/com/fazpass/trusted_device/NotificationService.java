package com.fazpass.trusted_device;

import static android.media.RingtoneManager.getDefaultUri;
import static com.fazpass.trusted_device.BASE.PRIVATE_KEY;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        try {
            Notification notification = new Notification(message.getData());

            if(Objects.equals(notification.getApp(), this.getPackageName())) {
                if(Objects.equals(notification.getStatus(), "request")) {
                    String key = Storage.readDataLocal(this, PRIVATE_KEY);
                    try {
                        Crypto.decrypt(Objects.requireNonNull(message.getData().get("meta")), key);
                    } catch (Exception e){
                        Log.e("ERROR", e.getMessage());
                    }

                    showNotification(notification);
                } else {
                    boolean isConfirmation = Objects.equals(message.getData().get("is_confirmation"), "yes");

                    Intent intent = new Intent(Notification.CROSS_DEVICE_CHANNEL);
                    intent.putExtra(Notification.NOTIFICATION_DEVICE, notification.getDevice());
                    intent.putExtra(Notification.NOTIFICATION_STATUS, isConfirmation);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            }
        } catch (Exception e) {
            Log.e("ERROR",e.getMessage());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
       /* TrustedDevice.updateFcm(this, token).subscribe(s->{
            Log.i("Fazpass", s.getMessage());
        }, err->{
            Log.e("Fazpass", err.getMessage());
        });*/
    }

    public void showNotification(Notification notification) {
        String channelId = "notification";
        int requestId = Notification.NOTIFICATION_REQ_ID;
        int logo = getApplicationInfo().icon;

        Intent contentIntent = FazpassCd.cdActivity;
        if (contentIntent == null) return;
        contentIntent.replaceExtras(notification.toExstras());
        //contentIntent.putExtras(notification.toExstras());
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, requestId, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent confirmPendingIntent = actionPendingIntent(
                FazpassCd.ACTION_CONFIRM,
                notification.getNotificationId(),
                requestId,
                notification.getDevice(),
                notification.getNotificationToken()
        );
        PendingIntent declinePendingIntent = actionPendingIntent(
                FazpassCd.ACTION_DECLINE,
                notification.getNotificationId(),
                requestId,
                notification.getDevice(),
                notification.getNotificationToken()
        );

        Uri defaultSoundUri = getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(logo) //(R.mipmap.ic_launcher)
                .setContentTitle("Confirmation")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentPendingIntent)
                .setContentText("You are trying to login from device "+notification.getDevice().split(",")[1])
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .addAction(R.drawable.check, "YES",
                        Notification.IS_REQUIRE_PIN ? contentPendingIntent : confirmPendingIntent)
                .addAction(R.drawable.cancel, "NO", declinePendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Confirmation",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(1);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(requestId, notificationBuilder.build());
    }

    private PendingIntent actionPendingIntent(String action, String notificationId, int requestId, String device, String notificationToken) {
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction(action);
        intent.putExtra(Notification.NOTIFICATION_ID, notificationId);
        intent.putExtra(Notification.NOTIFICATION_DEVICE, device);
        intent.putExtra(Notification.NOTIFICATION_TOKEN, notificationToken);
        return PendingIntent.getBroadcast(
                this, requestId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
