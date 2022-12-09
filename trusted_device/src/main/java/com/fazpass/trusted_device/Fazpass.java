package com.fazpass.trusted_device;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fazpass.trusted_device.internet.Roaming;
import com.fazpass.trusted_device.internet.UseCase;
import com.fazpass.trusted_device.internet.request.CheckUserRequest;
import com.fazpass.trusted_device.internet.request.HEAuthRequest;
import com.fazpass.trusted_device.internet.request.OTPVerificationRequest;
import com.fazpass.trusted_device.internet.request.OTPWithEmailRequest;
import com.fazpass.trusted_device.internet.request.OTPWithPhoneRequest;
import com.fazpass.trusted_device.internet.request.RemoveDeviceRequest;
import com.fazpass.trusted_device.internet.response.CheckUserResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.sentry.Sentry;
import io.sentry.android.core.SentryAndroid;

public abstract class Fazpass extends TrustedDevice{

    public static void initialize(Context context, String merchantToken, MODE mode){
        if (merchantToken == null || merchantToken.equals("")){
            throw new NullPointerException("merchant id cannot be null or empty");
        }
        Device d = new Device(context);
        if(EmulatorDetector.isEmulator() || d.isRooted()){
            throw new SecurityException("Device rooted or is an emulator lib stage");
        }
        Storage.storeDataLocal(context, MERCHANT_TOKEN, merchantToken);
        SentryAndroid.init(context, options -> {
            options.setDsn("https://1f85de8be5544aaab7847e377b4c6227@o1173329.ingest.sentry.io/6720667");
            options.setTracesSampleRate(1.0);
        });
        switch (mode){
            case DEBUG:
                Storage.storeDataLocal(context, BASE_URL, DEBUG);
                break;
            case STAGING:
                Storage.storeDataLocal(context, BASE_URL, STAGING);
                break;
            case PRODUCTION:
                Storage.storeDataLocal(context, BASE_URL, PRODUCTION);
                break;
        }
    }

    /**
     * @param context -
     * @param intent -
     */
    public static void launchedFromNotification(Context context, @Nullable Intent intent) {
        if (intent != null) {
            Notification.IS_REQUIRE_PIN = false;
            launchedFromNotification(context, intent.getExtras());
        } else {
            Log.e("intent", "NULL");
        }
    }

    public static void launchedFromNotificationRequirePin(Context context, @Nullable Intent intent) {
        if (intent != null) {
            Notification.IS_REQUIRE_PIN = true;
            launchedFromNotification(context, intent.getExtras());
        } else {
            Log.e("intent", "NULL");
        }
    }

    /**
     * Removing device without listener
     * @author Anvarisy
     */
    public static void removeDevice(Context ctx) {
        String userId = Storage.readDataLocal(ctx,USER_ID);
        RemoveDeviceRequest body = collectDataRemove(ctx, userId);
        Helper.sentryMessage("REMOVE_DEVICE", body);
        remove(ctx, body).subscribe(resp->{
            Storage.removeDataLocal(ctx);
        }, Sentry::captureException);
    }

    /**
     * It will check status of trusted device status & cross device status and returning new object of FazpassTd
     * @param email-
     * @param phone-
     * @param pin-
     * @param listener-
     */



    public static void check(Context ctx,@NonNull String email, @NonNull String phone,@NonNull String pin, TrustedDeviceListener<FazpassTd> listener) {
        initializeChecking(ctx);
        checking(ctx, email,phone,pin).subscribe(listener::onSuccess, listener::onFailure);
    }

    public static Observable<FazpassTd> check(Context ctx, String email, String phone, String pin){
        if (email.equals("") && phone.equals("")) {
            throw new NullPointerException("email or phone cannot be empty");
        }
        String packageName = ctx.getPackageName();
        GeoLocation location = new GeoLocation(ctx);
        CheckUserRequest.Location locationDetail = new CheckUserRequest.Location(location.getLatitude(), location.getLongitude());
        CheckUserRequest body = new CheckUserRequest(email, phone, packageName, Device.name, location.getTimezone(), locationDetail);
        Helper.sentryMessage("CHECK", body);
        return Observable.create(subscriber->{
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.startService("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp->{
                        String key = Storage.readDataLocal(ctx, PRIVATE_KEY);
                        /*
                        If status return false that mean user not found in our data
                        */
                        if (!resp.getStatus()) {
                            subscriber.onNext(new FazpassTd(TRUSTED_DEVICE.UNTRUSTED, CROSS_DEVICE.UNAVAILABLE));
                            subscriber.onComplete();
                        } else {
                             /*
                              It will checking status of cross device for this user
                             */
                            Storage.storeDataLocal(ctx, USER_ID, resp.getData().getUser().getId());
                            CROSS_DEVICE crossStatus = CROSS_DEVICE.UNAVAILABLE;
                            try {
                                List<CheckUserResponse.App> devices = resp.getData().getApps().getOthers().stream()
                                        .filter(app -> app.getApp().equals(ctx.getPackageName())).collect(Collectors.toList());
                                if (devices.size() >= 1) {
                                    crossStatus = CROSS_DEVICE.AVAILABLE;
                                }
                            } catch (Exception ignored) {
                            }
                            /*
                            It will checking status of trusted device for this user
                             */
                            if (!resp.getData().getApps().getCurrent().getMeta().equals("")) {
                                // If key in local was null, will automatically remove key in server
                                if (key.equals("")) {
                                    removeDevice(ctx, new User(email, phone), pin, resp.getData().getUser().getId(), crossStatus, resp.getData()).subscribe(f -> {
                                        subscriber.onNext(f);
                                        subscriber.onComplete();
                                    }, subscriber::onError);
                                } else {
                                    subscriber.onNext(new FazpassTd(ctx, new User(email, phone), pin, crossStatus, resp.getData()));
                                    subscriber.onComplete();
                                }
                            } else {
                                subscriber.onNext(new FazpassTd(ctx, new User(email, phone), pin, TRUSTED_DEVICE.UNTRUSTED, crossStatus, resp.getData()));
                                subscriber.onComplete();
                            }
                        }
                    },err->{
                        subscriber.onError(err);
                        Sentry.captureException(err);
                    });
        });
    }

    public static void requestOtpByPhone(Context ctx, String phone, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithPhoneRequest body = new OTPWithPhoneRequest(phone, gateway, new ArrayList<>());
        requestOtpWithPhone(ctx, body).subscribe(resp->{
            listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId()));

            startSMSListener(ctx, listener, resp.getData().getOtpLength());
            startMiscallListener(ctx, listener, resp.getData().getOtpLength());
        }, listener::onError);
    }

    public static void requestOtpByEmail(Context ctx, String email, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithEmailRequest body = new OTPWithEmailRequest(email, gateway, new ArrayList<>());
        requestOtpWithEmail(ctx, body).subscribe(
                resp->listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId())),
                listener::onError);
    }

    public static void generateOtpByPhone(Context ctx, String phone, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithPhoneRequest body = new OTPWithPhoneRequest(phone, gateway, new ArrayList<>());
        generateOtpWithPhone(ctx, body).subscribe(resp->{
            listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId()));

            startSMSListener(ctx, listener, resp.getData().getOtpLength());
            startMiscallListener(ctx, listener, resp.getData().getOtpLength());
        }, listener::onError);
    }

    public static void generateOtpByEmail(Context ctx, String email, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithEmailRequest body = new OTPWithEmailRequest(email, gateway, new ArrayList<>());
        generateOtpWithEmail(ctx, body).subscribe(
                resp->listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId())),
                listener::onError);
    }

    public static void validateOtp(Context ctx, String otpId, String otp,  Otp.Validate listener){
        initializeChecking(ctx);
        OTPVerificationRequest body = new OTPVerificationRequest(otpId, otp);
        verifyOtp(ctx, body).subscribe(resp->listener.onComplete(resp.getStatus()), listener::onError);
    }

    public static void heValidation(Context ctx, String phone, String gateway, HeaderEnrichment.Request listener){
        initializeChecking(ctx);
        if (!isTransportCellular(ctx)) {
            listener.onError(new Throwable("Internet not connected via cellular"));
            return;
        }
        try {
            if (!isCarrierMatch(ctx, phone)) {
                listener.onError(new Throwable("Phone number doesn't match it's carrier. ("+phone+")"));
                return;
            }
        } catch (Throwable e) {
            listener.onError(e);
            return;
        }
        HEAuthRequest body = new HEAuthRequest(gateway, phone);
        getAuthPage(ctx, body).subscribe(response -> launchAuthPage(response.getData().getAuthPage())
                .subscribe(response1 -> listener.onComplete(response1.getStatus()), listener::onError), listener::onError);
    }

    public static void requestPermission(Activity activity) {
        ArrayList<String> requiredPermissions = new ArrayList<>(Arrays.asList(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_CONTACTS
        ));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requiredPermissions.add(Manifest.permission.READ_PHONE_NUMBERS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requiredPermissions.add(Manifest.permission.USE_BIOMETRIC);
        }

        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.size() != 0)
            ActivityCompat.requestPermissions(activity, deniedPermissions.toArray(new String[0]), 1);
    }
}