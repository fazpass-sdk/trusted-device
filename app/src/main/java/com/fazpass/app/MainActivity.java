package com.fazpass.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fazpass.trusted_device.CrossDeviceListener;
import com.fazpass.trusted_device.EnrollStatus;
import com.fazpass.trusted_device.Fazpass;
import com.fazpass.trusted_device.FazpassTd;
import com.fazpass.trusted_device.MODE;
import com.fazpass.trusted_device.TrustedDeviceListener;
import com.fazpass.trusted_device.User;
import com.fazpass.trusted_device.ValidateStatus;

public class MainActivity extends AppCompatActivity {
    private static final String MERCHANT_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozfQ.1ye0zSaJgWPB5_SUU7oSDHKAs4tKjz_5RVtDnvc-HoE";

    private final User user = new User("panda@me.com", "085811755000", "","","");
    private final String pin = "123456";
    private final long cdTimeout = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fazpass.initialize(this, MERCHANT_KEY,MODE.STAGING);

        Button enroll = findViewById(R.id.btnEnroll);
        enroll.setOnClickListener(this::onEnroll);

        Button validate = findViewById(R.id.btnValidate);
        validate.setOnClickListener(this::onValidateUser);

        Button crossDevice = findViewById(R.id.btnCrossDevice);
        crossDevice.setOnClickListener(this::onValidateCrossDevice);

        Button remove = findViewById(R.id.btnRemove);
        remove.setOnClickListener(this::onRemove);
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
        Fazpass.check(this, user.getEmail(), user.getPhone(), pin, new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {
                o.validateUser(MainActivity.this, pin, new TrustedDeviceListener<ValidateStatus>() {
                    @Override
                    public void onSuccess(ValidateStatus o) {
                        Log.e("VALIDATE", String.valueOf(o.getConfidenceRate().getConfidence()));
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
}