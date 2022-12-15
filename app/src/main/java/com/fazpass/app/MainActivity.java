package com.fazpass.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.fazpass.trusted_device.CrossDeviceListener;
import com.fazpass.trusted_device.EnrollStatus;
import com.fazpass.trusted_device.Fazpass;
import com.fazpass.trusted_device.FazpassTd;
import com.fazpass.trusted_device.MODE;
import com.fazpass.trusted_device.Notification;
import com.fazpass.trusted_device.Otp;
import com.fazpass.trusted_device.OtpResponse;
import com.fazpass.trusted_device.TrustedDeviceListener;
import com.fazpass.trusted_device.User;

public class MainActivity extends AppCompatActivity {
    private static final String MERCHANT_KEY =
            //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozfQ.1ye0zSaJgWPB5_SUU7oSDHKAs4tKjz_5RVtDnvc-HoE";
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozNn0.mfny8amysdJQYlCrUlYeA-u4EG1Dw9_nwotOl-0XuQ8";
    private static final String MISCALL_KEY = "9defc750-83d8-4167-93e4-4fdab80a3eaf";

    //private final User user = new User("panda@me.com", "085811755000", "","","");
    private final User user = new User("panda@me.com", "082213681285", "","","");
    private final String pin = "123456";
    private final long cdTimeout = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fazpass.initialize(this, MERCHANT_KEY,MODE.STAGING);
        Fazpass.requestPermission(this);
        Fazpass.launchedFromNotification(this, getIntent().getExtras(), true, dialogBuilder());

        Button enroll = findViewById(R.id.btnEnroll);
        enroll.setOnClickListener(this::onEnroll);

        Button validate = findViewById(R.id.btnValidate);
        validate.setOnClickListener(this::onValidateUser);

        Button crossDevice = findViewById(R.id.btnCrossDevice);
        crossDevice.setOnClickListener(this::onValidateCrossDevice);

        Button remove = findViewById(R.id.btnRemove);
        remove.setOnClickListener(this::onRemove);

        Button miscall = findViewById(R.id.btnMiscall);
        miscall.setOnClickListener(this::onMiscall);
    }

    private void onEnroll(View v) {
        Fazpass.check(this, user.getEmail(), user.getPhone(), pin, new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {
                o.enrollDeviceByPin(MainActivity.this, user, pin, new TrustedDeviceListener<EnrollStatus>() {
                    @Override
                    public void onSuccess(EnrollStatus o) {
                        Log.e("ENROLL", "Status:" + o.getStatus());
                        Log.e("ENROLL", "Message:" + o.getMessage());
                    }

                    @Override
                    public void onFailure(Throwable err) {
                        Log.e("ERR", err.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Throwable err) {
                Log.e("ERR", err.getMessage());
            }
        });
    }

    private void onValidateUser(View v) {
        startActivity(new Intent(this, ConfidenceActivity.class));
    }

    private void onValidateCrossDevice(View v) {
        Fazpass.check(this, user.getEmail(), user.getPhone(), pin, new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {
                o.validateCrossDevice(MainActivity.this, cdTimeout, new CrossDeviceListener() {
                    @Override
                    public void onResponse(String device, boolean status) {
                        Log.e("CROSS DEVICE", "Status:" + status);
                        Log.e("CROSS DEVICE", "Device:" + device);
                    }

                    @Override
                    public void onExpired() {
                        Log.e("CROSS DEVICE", "Expired");
                    }
                });
            }

            @Override
            public void onFailure(Throwable err) {
                Log.e("ERR", err.getMessage());
            }
        });
    }

    private void onRemove(View v) {
        Fazpass.removeDevice(this);
    }

    private void onMiscall(View view) {
        Fazpass.requestOtpByPhone(getApplicationContext(), user.getPhone(), MISCALL_KEY, new Otp.Request() {
            @Override
            public void onComplete(OtpResponse response) {
                Log.e("COMPLETE", response.getMessage());
            }

            @Override
            public void onIncomingMessage(String otp) {
                Log.e("OTP", otp);
            }

            @Override
            public void onError(Throwable err) {
                Log.e("ERROR", err.getMessage());
            }
        });
    }

    private Notification.DialogBuilder dialogBuilder() {
        View view = getLayoutInflater().inflate(R.layout.dialog_notification, null);
        return Fazpass.notificationDialogBuilder(this)
                .setContentView(view)
                .setPositiveButtonId(R.id.notification_yes)
                .setNegativeButtonId(R.id.notification_no)
                .setInputId(R.id.notification_input_pin);
    }
}