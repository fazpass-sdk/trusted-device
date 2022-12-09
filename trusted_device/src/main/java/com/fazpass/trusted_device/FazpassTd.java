package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.Notification.CROSS_DEVICE_CHANNEL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fazpass.trusted_device.internet.Roaming;
import com.fazpass.trusted_device.internet.UseCase;
import com.fazpass.trusted_device.internet.request.EnrollDeviceRequest;
import com.fazpass.trusted_device.internet.request.RemoveDeviceRequest;
import com.fazpass.trusted_device.internet.request.ValidateDeviceRequest;
import com.fazpass.trusted_device.internet.response.CheckUserResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.sentry.Sentry;

public class FazpassTd extends Fazpass{
    private static CheckUserResponse cUser;
    private FazpassTd(){
        throw new RuntimeException("Stub!");
    }
    private BroadcastReceiver messageReceiver;

    protected FazpassTd(TRUSTED_DEVICE td, CROSS_DEVICE cd){
        td_status = td;
        cd_status = cd;
    }

    protected FazpassTd(Context ctx, User user, String pin, TRUSTED_DEVICE td, CROSS_DEVICE cd, CheckUserResponse resp){
        if(resp.getApps().isCrossApp()){
            autoEnroll(ctx, user, pin);
        }else{
            td_status = td;
        }
        cd_status = cd;
        cUser = resp;
    }

    protected void autoEnroll(Context ctx, User user, String pin){
        enrollDeviceByPin(ctx, user, pin);
        td_status = TRUSTED_DEVICE.TRUSTED;
    }

    protected FazpassTd(Context context, User user, String pin, CROSS_DEVICE cd, CheckUserResponse resp){
        cUser = resp;
        FazpassKey.setMeta(resp.getApps().getCurrent().getMeta());
        cd_status = cd;
        String password = Storage.readDataLocal(context, PRIVATE_KEY);
        try{
            String hashedInformation = resp.getApps().getCurrent().getMeta();
            String jsonString = Crypto.decrypt(hashedInformation, password);
            JSONObject json = new JSONObject(jsonString);
            if(json.getString(PACKAGE_NAME).equals(context.getPackageName())&& json.getString(DEVICE).equals(Device.name)){
                td_status = TRUSTED_DEVICE.TRUSTED;
                User.setIsUseFinger(resp.getApps().getCurrent().isUse_fingerprint());
                updateLastActive(context, Storage.readDataLocal(context, USER_ID));
            }else{
                if(resp.getApps().isCrossApp()){
                   autoEnroll(context, user, pin);
                }else {
                    td_status = TRUSTED_DEVICE.UNTRUSTED;
                }

            }
        } catch (Exception e) {
            if(resp.getApps().isCrossApp()){
                autoEnroll(context, user, pin);
            }else {
                td_status = TRUSTED_DEVICE.UNTRUSTED;
            }
        }
    }

    /**
     * enroll new device or new user with PIN as a authentication
     * @param user-
     * @param pin-
     * @param enroll- It will check status of that enroll status
     * @author Anvarisy
     */
    public void enrollDeviceByPin(Context ctx, User user, @NonNull String pin, TrustedDeviceListener<EnrollStatus> enroll) {
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        EnrollDeviceRequest body = collectDataEnroll(ctx, user,pin, false);
        Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
        enroll(ctx, body).subscribe(resp-> enroll.onSuccess(new EnrollStatus(resp.getStatus(),resp.getMessage())),
                err->{
                    enroll.onFailure(err);
                    Sentry.captureException(err);
                });
    }

    /**
     *
     * @param ctx -
     * @param pin -
     * @param user -
     * @return
     */
    public Observable<Boolean> enrollDeviceByPin(Context ctx, @NonNull String pin, User user){
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        return Observable.create(subscriber->{
            EnrollDeviceRequest body = collectDataEnroll(ctx, user,pin, false);
            Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.enrollDevice("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        if(resp.getStatus()){
                            subscriber.onNext(true);
                        }else{
                            subscriber.onNext(false);
                        }
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    /**
     * enroll new device or new user with PIN as a authentication
     * @param user-
     * @param pin-
     * @author Anvarisy
     */
    public void enrollDeviceByPin(Context ctx, User user, @NonNull String pin) {
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        EnrollDeviceRequest body = collectDataEnroll(ctx, user,pin, false);
        Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
        enroll(ctx, body).subscribe(resp-> {},
                Sentry::captureException);
    }

    /**
     *
     * @param ctx
     * @param pin
     * @param user
     * @return
     */
    public Observable<Boolean> enrollDeviceByFinger(Context ctx, String pin, User user){
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        return Observable.create(subscriber->{
            openBiometric(ctx, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Exception e = Error.biometricError();
                    Sentry.captureException(e);
                    subscriber.onError(e);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, true);
                    Helper.sentryMessage("ENROLL_DEVICE_BY_FINGER", body);
                    enroll(ctx, body).subscribe(resp-> {}, Sentry::captureException);
                    subscriber.onNext(true);
                    subscriber.onComplete();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Exception e = Error.biometricFailed();
                    Sentry.captureException(e);
                    subscriber.onError(e);
                }
            });
        });
    }

    /**
     * enroll new device or new user with PIN & Finger as a authentication
     * @param user-
     * @param pin-
     * @author Anvarisy
     */
    public void enrollDeviceByFinger(Context ctx, User user, String pin) {
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        openBiometric(ctx, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Exception e = Error.biometricError();
                Sentry.captureException(e);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, true);
                Helper.sentryMessage("ENROLL_DEVICE_BY_FINGER", body);
                enroll(ctx, body).subscribe(resp-> {}, Sentry::captureException);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Exception e = Error.biometricFailed();
                Sentry.captureException(e);
            }
        });
    }

    public Observable<Double> validateUser(Context ctx, String pin){
        return Observable.create(subscriber->{
            if(User.isUseFinger()){
                openBiometric(ctx, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Exception e = Error.biometricError();
                        subscriber.onError(e);
                        Sentry.captureException(e);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        ValidateDeviceRequest body = collectDataValidate(ctx);
                        if (body.getKey().equals("")) {
                            subscriber.onError(new NullPointerException("Key is missing"));
                            return;
                        }
                        Helper.sentryMessage("VALIDATE_BY_FINGER", body);
                        validate(ctx, body).subscribe(resp -> {
                            ValidateStatus.Confidence cfd = new ValidateStatus.Confidence(
                                    resp.getData().getMeta(),
                                    resp.getData().getKey(),
                                    resp.getData().getSim(),
                                    resp.getData().getContact(),
                                    resp.getData().getContact()
                            );
                            ValidateStatus status = new ValidateStatus(resp.getStatus(), cfd);
                            subscriber.onNext(status.getConfidenceRate().getConfidence());
                            subscriber.onComplete();
                        }, err -> {
                            Log.e(TAG, err.getMessage());
                            subscriber.onError(err);
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Exception e = Error.biometricFailed();
                        subscriber.onError(e);
                        Sentry.captureException(e);
                    }
                });
            }else{
                String meta = new FazpassKey().getMeta();
                String key = Storage.readDataLocal(ctx, PRIVATE_KEY);
                if (meta.equals("") || key.equals("")) {
                   subscriber.onError(new NullPointerException("Key is missing"));
                    return;
                }
                String rawData = Crypto.decrypt(meta, key);
                try {
                    JSONObject json = new JSONObject(rawData);
                    String cryptPin = json.getString(USER_PIN);
                    BCrypt.Result result = BCrypt.verifyer().verify(pin.toCharArray(), cryptPin);
                    if (result.verified) {
                        ValidateDeviceRequest body = collectDataValidate(ctx);
                        if (body.getKey().equals("")) {
                            subscriber.onError(new NullPointerException("Key is missing"));
                            return;
                        }
                        Helper.sentryMessage("VALIDATE_BY_PIN", body);
                        validate(ctx, body).subscribe(resp -> {
                            ValidateStatus.Confidence cfd = new ValidateStatus.Confidence(
                                    resp.getData().getMeta(),
                                    resp.getData().getKey(),
                                    resp.getData().getSim(),
                                    resp.getData().getContact(),
                                    resp.getData().getLocation()
                            );
                            ValidateStatus status = new ValidateStatus(resp.getStatus(), cfd);
                            subscriber.onNext(status.getConfidenceRate().getConfidence());
                            subscriber.onComplete();
                        }, err -> {
                            Log.e(TAG, err.getMessage());
                            subscriber.onError(err);
                        });
                    } else {
                        subscriber.onError(new Exception("Pin not match"));
                    }
                } catch (JSONException e) {
                    Sentry.captureException(e);
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * Validating user and will return confidence rate for that user
     * @param pin-
     * @param listener- It will listen status of validating user
     * @author Anvarisy
     */
    public void validateUser(Context ctx, String pin, TrustedDeviceListener<ValidateStatus> listener) {
        if(User.isUseFinger()){
            validateUserByFinger(ctx, listener);
        }else{
            validateUserByPin(ctx, pin, listener);
        }
    }

    /**
     * Removing device with listener
     * @param listener - It will listen status of removing status
     * @author Anvarisy
     */
    public void removeDevice(Context ctx, TrustedDeviceListener<RemoveStatus> listener) {
        String userId = Storage.readDataLocal(ctx,USER_ID);
        RemoveDeviceRequest body = collectDataRemove(ctx, userId);
        Helper.sentryMessage("REMOVE_DEVICE", body);
        remove(ctx, body).subscribe(resp->{
            listener.onSuccess(new RemoveStatus(resp.getStatus(),resp.getMessage()));
            Storage.removeDataLocal(ctx);
        }, err->{
            listener.onFailure(err);
            Sentry.captureException(err);
        });
    }

    /**
     * Validating other device for authentication
     * @param timeOut-
     * @param enroll-
     * @author Anvarisy
     */
    public void validateCrossDevice(Context ctx, long timeOut, CrossDeviceListener enroll) {
        if(cd_status.equals(CROSS_DEVICE.UNAVAILABLE)){
            throw new NullPointerException("Other device not detected");
        }
        String notificationId = UUID.randomUUID().toString();
        String userId = Storage.readDataLocal(ctx, USER_ID);
        Handler handler = new Handler();
        Runnable r = () -> {
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(messageReceiver);
            enroll.onExpired();
            Log.e("Notification","Expired");
            notificationExpired(ctx, userId,notificationId);
        };
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String device = intent.getStringExtra(Notification.NOTIFICATION_DEVICE);
                boolean status = intent.getBooleanExtra(Notification.NOTIFICATION_STATUS,false);
                enroll.onResponse(device,status);
                LocalBroadcastManager.getInstance(ctx).unregisterReceiver(messageReceiver);
                handler.removeCallbacks(r);
            }
        };
        LocalBroadcastManager.getInstance(ctx)
                .registerReceiver(messageReceiver, new IntentFilter(CROSS_DEVICE_CHANNEL));
        handler.postDelayed(r, timeOut*1000);
        sendNotification(ctx, cUser.getApps().getOthers(), timeOut,userId,notificationId);
    }


}
