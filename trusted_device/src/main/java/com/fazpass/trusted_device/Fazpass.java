package com.fazpass.trusted_device;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fazpass.trusted_device.internet.request.CheckUserRequest;
import com.fazpass.trusted_device.internet.request.HEAuthRequest;
import com.fazpass.trusted_device.internet.request.OTPVerificationRequest;
import com.fazpass.trusted_device.internet.request.OTPWithEmailRequest;
import com.fazpass.trusted_device.internet.request.OTPWithPhoneRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
        switch (mode) {
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
        initializeMiscallListener(context);
    }

    /**
     * Removing device without listener
     * @author Anvarisy
     */
/*    public static void removeDevice(Context ctx, String pin) {
        Observable
                .create(emitter -> {
                    String userId = Storage.readDataLocal(ctx,USER_ID);
                    RemoveDeviceRequest body = collectDataRemove(ctx, userId);
                    //Helper.sentryMessage("REMOVE_DEVICE", body);
                    emitter.onNext(body);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .switchMap(data -> remove(ctx, (RemoveDeviceRequest) data))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp-> Storage.removeDataLocal(ctx), Sentry::captureException);
        return null;
    }*/

    public static void removeDevice(Context context, String pin, TrustedDeviceListener<Boolean> listener){
       validatePin(context, pin)
               .subscribeOn(Schedulers.newThread())
               .switchMap(response->{
                   if (response.getStatus()) {
                       return removeDevice(context, Storage.readDataLocal(context, USER_ID));
                   }
                   throw new Exception(response.getMessage());
               })
               .map(response->{
                   if (response.getStatus()) {
                       return true;
                   }
                   throw new Exception(response.getMessage());
               })
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(listener::onSuccess, listener::onFailure);
    }

    /**
     * It will check status of trusted device status & cross device status and returning new object of FazpassTd
     * @param email-
     * @param phone-
     * @param listener-
     */

    public static void check(Context ctx,@NonNull String email, @NonNull String phone, TrustedDeviceListener<FazpassTd> listener) {
        initializeChecking(ctx);
        Observable.create(emitter -> {
            if (email.equals("") && phone.equals("")) {
                throw new NullPointerException("email or phone cannot be empty");
            }
            String packageName = ctx.getPackageName();
            GeoLocation location = new GeoLocation(ctx);
            CheckUserRequest.Location locationDetail = new CheckUserRequest.Location(location.getLatitude(), location.getLongitude());
            CheckUserRequest body = new CheckUserRequest(email, phone, packageName, Device.name, location.getTimezone(), locationDetail);
            //Helper.sentryMessage("CHECK", body);
            emitter.onNext(body);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .switchMap(data -> checking(ctx, (CheckUserRequest) data))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onSuccess, listener::onFailure);
    }

    public static void requestOtpByPhone(Context ctx, String phone, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithPhoneRequest body = new OTPWithPhoneRequest(phone, gateway, new ArrayList<>());
        requestOtpWithPhone(ctx, body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        resp->{
                            listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId()));

                            startSMSListener(ctx, listener, resp.getData().getOtpLength());
                            startMiscallListener(ctx, listener, resp.getData().getOtpLength());
                        },
                        listener::onError);
    }

    public static void requestOtpByEmail(Context ctx, String email, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithEmailRequest body = new OTPWithEmailRequest(email, gateway, new ArrayList<>());
        requestOtpWithEmail(ctx, body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        resp->listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId())),
                        listener::onError);
    }

    public static void generateOtpByPhone(Context ctx, String phone, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithPhoneRequest body = new OTPWithPhoneRequest(phone, gateway, new ArrayList<>());
        generateOtpWithPhone(ctx, body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        resp->{
                            listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId()));

                            startSMSListener(ctx, listener, resp.getData().getOtpLength());
                            startMiscallListener(ctx, listener, resp.getData().getOtpLength());
                        },
                        listener::onError);
    }

    public static void generateOtpByEmail(Context ctx, String email, String gateway, Otp.Request listener){
        initializeChecking(ctx);
        OTPWithEmailRequest body = new OTPWithEmailRequest(email, gateway, new ArrayList<>());
        generateOtpWithEmail(ctx, body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        resp->listener.onComplete(new OtpResponse(resp.getStatus(), resp.getMessage(), resp.getData().getId())),
                        listener::onError);
    }

    public static void validateOtp(Context ctx, String otpId, String otp,  Otp.Validate listener){
        initializeChecking(ctx);
        OTPVerificationRequest body = new OTPVerificationRequest(otpId, otp);
        verifyOtp(ctx, body)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp->listener.onComplete(resp.getStatus()), listener::onError);
    }

    public static void heValidation(Context ctx, String phone, String gateway, HeaderEnrichment.Request listener){
        initializeChecking(ctx);
        HEAuthRequest body = new HEAuthRequest(gateway, phone);
        Observable
                .create(emitter -> {
                    if (!isTransportCellular(ctx)) {
                        throw new Throwable("Internet not connected via cellular");
                    }
                    if (!isCarrierMatch(ctx, phone)) {
                        throw new Throwable("Phone number doesn't match it's carrier. ("+phone+")");
                    }
                    emitter.onNext(0);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .switchMap(data -> getAuthPage(ctx, body))
                .switchMap(data -> launchAuthPage(data.getData().getAuthPage()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> listener.onComplete(resp.getStatus()), listener::onError);
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