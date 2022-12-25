package com.fazpass.trusted_device;

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

public class NotificationActivity extends FragmentActivity {

    public static Intent buildIntent(Context context) {
        Intent intent = new Intent("com.fazpass.trusted_device.ACTION_PLAY");
        Context newProContext = context;
        try{
            newProContext = context.createPackageContext("com.fazpass.trusted_device", 0);
        } catch (PackageManager.NameNotFoundException ignored) {}
        intent.setClass(newProContext, NotificationActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        String device = new Notification(getIntent().getExtras()).getDevice();
        ImageView logoImg = findViewById(R.id.notification_logo);
        TextView appNameTxt = findViewById(R.id.notification_app_name);
        TextView messageTxt = findViewById(R.id.notification_message);
        Button yesBtn = findViewById(R.id.notification_yes);
        Button noBtn = findViewById(R.id.notification_no);
        EditText inputPin = findViewById(R.id.notification_input_pin);

        ApplicationInfo applicationInfo = getApplicationInfo();

        logoImg.setImageDrawable(getLogoDrawable(applicationInfo));
        appNameTxt.setText(getAppLabel(applicationInfo));
        messageTxt.setText(getString(R.string.notification_message, device.split(",")[1]));
        if (Notification.IS_REQUIRE_PIN) {
            inputPin.setVisibility(View.VISIBLE);
            yesBtn.setOnClickListener(view -> FazpassCd.onConfirmRequirePin(
                    this,
                    inputPin.getText().toString(),
                    isMatch -> {
                        if (isMatch) finish();
                        else Toast.makeText(this, "PIN doesn't match", Toast.LENGTH_SHORT).show();
                        return null;
                    }
            ));
        }
        else {
            yesBtn.setOnClickListener(view -> FazpassCd.onConfirm(this));
        }
        noBtn.setOnClickListener(view -> {
            FazpassCd.onDecline(this);
            finish();
        });
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