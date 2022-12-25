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

import com.fazpass.trusted_device.internet.request.EnrollDeviceRequest;
import com.fazpass.trusted_device.internet.request.RemoveDeviceRequest;
import com.fazpass.trusted_device.internet.response.CheckUserResponse;

import org.json.JSONObject;

import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.sentry.Sentry;

public class FazpassTd extends Fazpass{
    private static CheckUserResponse cUser;
    private BroadcastReceiver messageReceiver;
    private FazpassTd(){
        throw new RuntimeException("Stub!");
    }

    protected FazpassTd(TRUSTED_DEVICE td, CROSS_DEVICE cd){
        td_status = td;
        cd_status = cd;
    }

    protected FazpassTd(Context context, CROSS_DEVICE cd, CheckUserResponse resp){
        cUser = resp;
        if(!resp.getApps().getCurrent().getMeta().equals(""))
            FazpassKey.setMeta(resp.getApps().getCurrent().getMeta());
        this.cd_status = cd;
        String password = Storage.readDataLocal(context, PRIVATE_KEY);
        try{
            String hashedInformation = resp.getApps().getCurrent().getMeta();
            String jsonString = Crypto.decrypt(hashedInformation, password);
            JSONObject json = new JSONObject(jsonString);
            if(json.getString(PACKAGE_NAME).equals(context.getPackageName())&& json.getString(DEVICE).equals(Device.name)){
                td_status = TRUSTED_DEVICE.TRUSTED;
                User.setIsUseFinger(resp.getApps().getCurrent().isUse_fingerprint());
                updateLastActive(context, Storage.readDataLocal(context, USER_ID))
                        .subscribeOn(Schedulers.newThread())
                        .subscribe();
            }else{
                if(resp.getApps().isCrossApp())
                    td_status = TRUSTED_DEVICE.TRUSTED;
                else
                    td_status = TRUSTED_DEVICE.UNTRUSTED;
            }
        } catch (Exception e) {
            if(resp.getApps().isCrossApp())
                td_status = TRUSTED_DEVICE.TRUSTED;
            else
                td_status = TRUSTED_DEVICE.UNTRUSTED;

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
        if(cUser.getApps().getCurrent().getMeta().equals("")){
            Observable.<EnrollDeviceRequest>create(subscriber->{
                EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, false);
                //Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
                subscriber.onNext(body);
                subscriber.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .switchMap(body -> enroll(ctx, body))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp->enroll.onSuccess(new EnrollStatus(resp.getStatus(), resp.getMessage())),
                            err->{
                                enroll.onFailure(err);
                                Sentry.captureException(err);
                            });
        }else{
            validatePin(ctx, pin)
                    .subscribeOn(Schedulers.newThread())
                    .switchMap(response->{
                        if (response.getStatus()) {
                            return removeDevice(ctx, Storage.readDataLocal(ctx, USER_ID));
                        }
                        throw new Exception(response.getMessage());
                    })
                    .map(response->{
                        if (response.getStatus()) {
                            EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, false);
                            //Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
                            return body;
                        }
                        throw new Exception(response.getMessage());
                    })
                    .switchMap(body -> enroll(ctx, body))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp->enroll.onSuccess(new EnrollStatus(resp.getStatus(), resp.getMessage())),
                            err->{
                                enroll.onFailure(err);
                                Sentry.captureException(err);
                            });
        }


       /* Observable
                .create(subscriber -> {
                    EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, false);
                    //Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
                    subscriber.onNext(body);
                    subscriber.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .switchMap(body -> enroll(ctx, (EnrollDeviceRequest) body))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        resp -> enroll.onSuccess(new EnrollStatus(resp.getStatus(), resp.getMessage())),
                        err -> {
                            enroll.onFailure(err);
                            Sentry.captureException(err);
                        });*/
    }

    /**
     * enroll new device or new user with PIN as a authentication
     * @param user-
     * @param pin-
     * @author Anvarisy
     */
    public void enrollDeviceByPin(Context ctx, User user, @NonNull String pin) {
        if (pin.equals("")) {
            throw new NullPointerException("PIN cannot be null or empty");
        }
        if (cUser.getApps().getCurrent().getMeta().equals("")) {
            Observable.<EnrollDeviceRequest>create(subscriber -> {
                        EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, false);
                        //Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
                        subscriber.onNext(body);
                        subscriber.onComplete();
                    }).subscribeOn(Schedulers.newThread())
                    .switchMap(body -> enroll(ctx, body))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {}, Sentry::captureException);
        } else {
            validatePin(ctx, pin)
                    .subscribeOn(Schedulers.newThread())
                    .switchMap(response -> {
                        if (response.getStatus()) {
                            return removeDevice(ctx, Storage.readDataLocal(ctx, USER_ID));
                        }
                        throw new Exception(response.getMessage());
                    })
                    .map(response->{
                        if (response.getStatus()) {
                            EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, false);
                            //Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
                            return body;
                        }
                        throw new Exception(response.getMessage());
                    })
                    .switchMap(body -> enroll(ctx, body))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {}, Sentry::captureException);
        }
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
                Observable
                        .<EnrollDeviceRequest>create(subscriber -> {
                            EnrollDeviceRequest body = collectDataEnroll(ctx, user, pin, true);
                            //Helper.sentryMessage("ENROLL_DEVICE_BY_FINGER", body);
                            subscriber.onNext(body);
                            subscriber.onComplete();
                        }).subscribeOn(Schedulers.newThread())
                        .switchMap(body -> enroll(ctx, body))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp-> {}, Sentry::captureException);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Exception e = Error.biometricFailed();
                Sentry.captureException(e);
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
