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
    private static final String MERCHANT_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjo0fQ.WEV3bCizw9U_hxRC6DxHOzZthuJXRE8ziI3b6bHUpEI";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fazpass.initialize(this, MERCHANT_KEY,MODE.STAGING);
        Button a = findViewById(R.id.btnEnroll);
        a.setOnClickListener(v->{
            Fazpass.check(this, "koala@panda.com", "086811754000", "123456", new TrustedDeviceListener<FazpassTd>() {
                @Override
                public void onSuccess(FazpassTd o) {
                    o.enrollDeviceByPin(MainActivity.this,new User("koala@panda.com","085811754000","","",""),"123456");
                }

                @Override
                public void onFailure(Throwable err) {
                    Log.e("ERR", err.getMessage());
                }
            });
        });

        Button b = findViewById(R.id.btnValidate);
        b.setOnClickListener(v->{
            Fazpass.check(this, "koala@panda.com", "086811754000", "123456", new TrustedDeviceListener<FazpassTd>() {
                @Override
                public void onSuccess(FazpassTd o) {
                    o.validateUser(MainActivity.this, "123456", new TrustedDeviceListener<ValidateStatus>() {
                        @Override
                        public void onSuccess(ValidateStatus o) {
                            o.getConfidenceRate().getConfidence();
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