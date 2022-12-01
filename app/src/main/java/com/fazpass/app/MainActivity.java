package com.fazpass.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.fazpass.trusted_device.Fazpass;
import com.fazpass.trusted_device.FazpassTd;
import com.fazpass.trusted_device.MODE;
import com.fazpass.trusted_device.TrustedDeviceListener;

public class MainActivity extends AppCompatActivity {
    private static final String MERCHANT_TOKEN= "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjo0fQ.WEV3bCizw9U_hxRC6DxHOzZthuJXRE8ziI3b6bHUpEI";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fazpass.initialize(this,MERCHANT_TOKEN, MODE.STAGING);
        Fazpass.check(this, "koala@gmail.com", "085195310101", "123456", new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {

            }

            @Override
            public void onFailure(Throwable err) {
                Log.e("Err", err.getMessage());
            }
        });
    }
}