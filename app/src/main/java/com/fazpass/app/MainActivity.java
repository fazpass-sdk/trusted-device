package com.fazpass.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fazpass.trusted_device.CrossDeviceListener;
import com.fazpass.trusted_device.EnrollStatus;
import com.fazpass.trusted_device.Fazpass;
import com.fazpass.trusted_device.FazpassCd;
import com.fazpass.trusted_device.FazpassTd;
import com.fazpass.trusted_device.HeaderEnrichment;
import com.fazpass.trusted_device.MODE;
import com.fazpass.trusted_device.Otp;
import com.fazpass.trusted_device.OtpResponse;
import com.fazpass.trusted_device.TrustedDeviceListener;
import com.fazpass.trusted_device.User;

public class MainActivity extends AppCompatActivity {
    private static final String MERCHANT_KEY =
            //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozfQ.1ye0zSaJgWPB5_SUU7oSDHKAs4tKjz_5RVtDnvc-HoE";
            //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozNn0.mfny8amysdJQYlCrUlYeA-u4EG1Dw9_nwotOl-0XuQ8"; // staging grade 3
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjo0fQ.WEV3bCizw9U_hxRC6DxHOzZthuJXRE8ziI3b6bHUpEI"; // staging grade 5
    private static final String MISCALL_KEY =
            //"9defc750-83d8-4167-93e4-4fdab80a3eaf"; // staging grade 3
            "595ea55e-95d2-4ec4-969e-910de41585a0"; // staging grade 5
    private static final String HE_KEY = "6cb0b024-9721-4243-9010-fd9e386157ec";

    //private final User user = new User("panda@me.com", "085811755000", "","","");
    public static final User user = new User("", "PHONE_NUMBER", "","","");
    public static final String pin = "5555";
    private final long cdTimeout = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fazpass.initialize(this, MERCHANT_KEY, MODE.STAGING);
        Fazpass.requestPermission(this);
        FazpassCd.initialize(this,true);

        Button enroll = findViewById(R.id.btnEnroll);
        enroll.setOnClickListener(this::onEnroll);

        Button enrollFinger = findViewById(R.id.btnEnrollFinger);
        enrollFinger.setOnClickListener(this::onEnrollFinger);

        Button validate = findViewById(R.id.btnValidate);
        validate.setOnClickListener(this::onValidateUser);

        Button he = findViewById(R.id.btnHE);
        he.setOnClickListener(this::onHE);

        Button crossDevice = findViewById(R.id.btnCrossDevice);
        crossDevice.setOnClickListener(this::onValidateCrossDevice);

        Button remove = findViewById(R.id.btnRemove);
        remove.setOnClickListener(this::onRemove);

        Button miscall = findViewById(R.id.btnMiscall);
        miscall.setOnClickListener(this::onMiscall);
    }

    private void onEnroll(View v) {
        Fazpass.check(this, user.getEmail(), user.getPhone(), new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {
                o.enrollDeviceByPin(MainActivity.this, user, pin, new TrustedDeviceListener<EnrollStatus>() {
                    @Override
                    public void onSuccess(EnrollStatus o) {
                        Log.e("Enroll", "status: "+o.getStatus());
                        Log.e("Enroll", "message: "+o.getMessage());
                    }

                    @Override
                    public void onFailure(Throwable err) {
                        err.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable err) {
                err.printStackTrace();
            }
        });
    }

    private void onEnrollFinger(View v) {
        Fazpass.check(this, user.getEmail(), user.getPhone(), new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {
                o.enrollDeviceByFinger(MainActivity.this, user, pin, new TrustedDeviceListener<EnrollStatus>() {
                    @Override
                    public void onSuccess(EnrollStatus o) {
                        Log.e("Enroll", "status: "+o.getStatus());
                        Log.e("Enroll", "message: "+o.getMessage());
                    }

                    @Override
                    public void onFailure(Throwable err) {
                        err.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable err) {
                err.printStackTrace();
            }
        });
    }

    private void onValidateUser(View v) {
        startActivity(new Intent(this, ConfidenceActivity.class));
    }

    private void onHE(View view) {
        Fazpass.heValidation(this, user.getPhone(), HE_KEY, new HeaderEnrichment.Request() {
            @Override
            public void onComplete(boolean status) {
                Log.e("HE", "status: "+ status);
            }

            @Override
            public void onError(Throwable err) {
                err.printStackTrace();
            }
        });
    }

    private void onValidateCrossDevice(View v) {
        Fazpass.check(this, user.getEmail(), user.getPhone(), new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {
                o.validateCrossDevice(MainActivity.this, cdTimeout, new CrossDeviceListener() {
                    @Override
                    public void onResponse(String device, boolean status) {
                        Log.e("cd", "status: "+ status);
                        Log.e("cd", "device: "+ device);
                    }

                    @Override
                    public void onExpired() {
                        Log.e("cd", "expired");
                    }
                });
            }

            @Override
            public void onFailure(Throwable err) {
                err.printStackTrace();
            }
        });
    }

    private void onRemove(View v) {
        Fazpass.removeDevice(this, pin, new TrustedDeviceListener<Boolean>() {
            @Override
            public void onSuccess(Boolean o) {
                Log.e("remove device", o.toString());
            }

            @Override
            public void onFailure(Throwable err) {
                err.printStackTrace();
            }
        });
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
                Toast.makeText(MainActivity.this, otp, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable err) {
                Log.e("ERROR", err.getMessage());
            }
        });
    }
}