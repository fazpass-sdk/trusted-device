package com.fazpass.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.fazpass.trusted_device.Fazpass;
import com.fazpass.trusted_device.FazpassTd;
import com.fazpass.trusted_device.MODE;
import com.fazpass.trusted_device.TrustedDeviceListener;
import com.fazpass.trusted_device.User;
import com.fazpass.trusted_device.ValidateStatus;

public class MainActivity extends AppCompatActivity {
    private static final String MERCHANT_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozfQ.1ye0zSaJgWPB5_SUU7oSDHKAs4tKjz_5RVtDnvc-HoE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fazpass.initialize(this, MERCHANT_KEY,MODE.STAGING);
        Button enroll = findViewById(R.id.btnEnroll);
        enroll.setOnClickListener(v->{
            Fazpass.check(this, "panda@me.com", "085811755000", "123456", new TrustedDeviceListener<FazpassTd>() {
                @Override
                public void onSuccess(FazpassTd o) {

                }

                @Override
                public void onFailure(Throwable err) {
                    Log.e("ERR", err.getMessage());
                }
            });
        });

        Button validate = findViewById(R.id.btnValidate);
        validate.setOnClickListener(v->{
            Fazpass.check(this, "panda@me.com", "085811755000", "123456", new TrustedDeviceListener<FazpassTd>() {
                @Override
                public void onSuccess(FazpassTd o) {

                    o.validateUser(MainActivity.this, "123456", new TrustedDeviceListener<ValidateStatus>() {
                        @Override
                        public void onSuccess(ValidateStatus o) {
                            Log.e("APP", String.valueOf(o.getConfidenceRate().getConfidence()));
                        }

                        @Override
                        public void onFailure(Throwable err) {

                        }
                    });
                }

                @Override
                public void onFailure(Throwable err) {
                    Log.e("ERR", err.getMessage());
                }
            });
        });

        Button c = findViewById(R.id.btnRemove);
        c.setOnClickListener(v->{
            Fazpass.removeDevice(this);
        });
    }
}