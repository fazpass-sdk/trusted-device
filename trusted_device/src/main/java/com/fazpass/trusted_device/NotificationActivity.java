package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.BASE.USER_PIN;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class NotificationActivity extends FragmentActivity {

    public static Intent buildIntent(Context context, String notificationId, String notificationToken, String device, int requestId) {
        Intent intent = new Intent("com.fazpass.trusted_device.ACTION_PLAY");
        Context newProContext = context;
        try{
            newProContext = context.createPackageContext("com.fazpass.trusted_device", 0);
        } catch (PackageManager.NameNotFoundException ignored) {}

        intent.setClass(newProContext, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Notification.NOTIFICATION_ID, notificationId);
        intent.putExtra(Notification.NOTIFICATION_REQ_ID, requestId);
        intent.putExtra(Notification.NOTIFICATION_DEVICE, device);
        intent.putExtra(Notification.NOTIFICATION_TOKEN, notificationToken);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        String cryptPin = Storage.readDataLocal(this, USER_PIN);

        Intent intent = getIntent();
        String notificationId = intent.getStringExtra(Notification.NOTIFICATION_ID);
        int requestId = intent.getIntExtra(Notification.NOTIFICATION_REQ_ID, 0);
        String device = intent.getStringExtra(Notification.NOTIFICATION_DEVICE);
        String notificationToken = intent.getStringExtra(Notification.NOTIFICATION_TOKEN);
        //TODO Please use image from user.
        ImageView logoImg = findViewById(R.id.notification_logo);
        TextView appNameTxt = findViewById(R.id.notification_app_name);
        TextView messageTxt = findViewById(R.id.notification_message);
        Button yesBtn = findViewById(R.id.notification_yes);
        Button noBtn = findViewById(R.id.notification_no);
        EditText inputPin = findViewById(R.id.notification_input_pin);

        if (Notification.IS_REQUIRE_PIN) {
            inputPin.setVisibility(View.VISIBLE);
        }

        ApplicationInfo applicationInfo = getApplicationInfo();

        logoImg.setImageDrawable(getLogoDrawable(applicationInfo));
        appNameTxt.setText(getAppLabel(applicationInfo));
        messageTxt.setText(getString(R.string.notification_message, device.split(",")[1]));
        yesBtn.setOnClickListener(view -> {
            if (Notification.IS_REQUIRE_PIN) {
                String inputtedPin = inputPin.getText().toString();

                if (inputtedPin.equals("")) {
                    Toast.makeText(this, "PIN should be filled.", Toast.LENGTH_SHORT).show();
                    return;
                }

                BCrypt.Result result = BCrypt.verifyer().verify(inputtedPin.toCharArray(), cryptPin);
                if (result.verified) {
                    onConfirmation("com.fazpass.trusted_device.CONFIRM_STATUS", notificationId, requestId, device, notificationToken);
                } else {
                    Toast.makeText(this, "PIN doesn't match.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                onConfirmation("com.fazpass.trusted_device.CONFIRM_STATUS", notificationId, requestId, device, notificationToken);
            }
        });
        noBtn.setOnClickListener(view ->
                onConfirmation("com.fazpass.trusted_device.DECLINE_STATUS", notificationId, requestId, device, notificationToken));
    }

    private void onConfirmation(String action, String notificationId, int requestId, String device, String notificationToken) {
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction(action);
        intent.putExtra(Notification.NOTIFICATION_ID, notificationId);
        intent.putExtra(Notification.NOTIFICATION_REQ_ID, requestId);
        intent.putExtra(Notification.NOTIFICATION_DEVICE, device);
        intent.putExtra(Notification.NOTIFICATION_TOKEN, notificationToken);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        finish();
    }

    private Drawable getLogoDrawable(ApplicationInfo applicationInfo) {
        int logoId = applicationInfo.logo;
        int iconId = applicationInfo.icon;
        try {
            if (logoId != 0) {
                return AppCompatResources.getDrawable(this, logoId);
            } else if (iconId != 0) {
                return AppCompatResources.getDrawable(this, iconId);
            }
        } catch (Exception ignored) {}

        return null;
    }

    private String getAppLabel(ApplicationInfo applicationInfo) {
        int stringId = applicationInfo.labelRes;
        String appName;
        appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
        if (appName == null) {
            appName = (String) getPackageManager().getApplicationLabel(applicationInfo);
        }

        return appName;
    }
}