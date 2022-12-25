package com.fazpass.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fazpass.trusted_device.Fazpass;
import com.fazpass.trusted_device.FazpassTd;
import com.fazpass.trusted_device.TrustedDeviceListener;
import com.fazpass.trusted_device.User;
import com.fazpass.trusted_device.ValidateStatus;

public class ConfidenceActivity extends AppCompatActivity {

    private final User user = MainActivity.user;
    private final String pin = MainActivity.pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confidence);

        TextView confidenceTxt = findViewById(R.id.confidence_text);
        confidenceTxt.setText("Loading...");
        Fazpass.check(this, user.getEmail(), user.getPhone(), new TrustedDeviceListener<FazpassTd>() {
            @Override
            public void onSuccess(FazpassTd o) {
                o.validateUser(ConfidenceActivity.this, pin, new TrustedDeviceListener<ValidateStatus>() {
                    @Override
                    public void onSuccess(ValidateStatus o) {
                        String rate = String.valueOf(o.getConfidenceRate().getSummary()*100);
                        confidenceTxt.setText(rate);
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
}