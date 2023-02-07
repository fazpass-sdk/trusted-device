package com.fazpass.trusted_device;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.Executor;

public class FingerprintActivity extends FragmentActivity {
    public static final String FINGERPRINT_BROADCAST_CHANNEL = "fazpass_fingerprint_channel";
    public static final String ON_AUTH_SUCCEED = "onAuthenticationSucceeded";
    public static final String ON_AUTH_ERROR = "onAuthenticationError";
    public static final String ON_AUTH_FAILED = "onAuthenticationFailed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finish();
                sendBroadcast(ON_AUTH_ERROR);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                finish();
                sendBroadcast(ON_AUTH_SUCCEED);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                sendBroadcast(ON_AUTH_FAILED);
            }
        });
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Required")
                .setSubtitle("")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(FINGERPRINT_BROADCAST_CHANNEL);
        intent.putExtra("status", action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}