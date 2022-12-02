package com.fazpass.trusted_device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.fazpass.trusted_device.internet.Response;
import com.fazpass.trusted_device.internet.Roaming;
import com.fazpass.trusted_device.internet.UseCase;
import com.fazpass.trusted_device.internet.request.CheckUserRequest;
import com.fazpass.trusted_device.internet.request.EnrollDeviceRequest;
import com.fazpass.trusted_device.internet.request.HEAuthRequest;
import com.fazpass.trusted_device.internet.request.LastActiveRequest;
import com.fazpass.trusted_device.internet.request.NotificationRequest;
import com.fazpass.trusted_device.internet.request.OTPVerificationRequest;
import com.fazpass.trusted_device.internet.request.OTPWithEmailRequest;
import com.fazpass.trusted_device.internet.request.OTPWithPhoneRequest;
import com.fazpass.trusted_device.internet.request.RemoveDeviceRequest;
import com.fazpass.trusted_device.internet.request.UpdateFcmRequest;
import com.fazpass.trusted_device.internet.request.UpdateNotificationRequest;
import com.fazpass.trusted_device.internet.request.ValidateDeviceRequest;
import com.fazpass.trusted_device.internet.response.CheckUserResponse;
import com.fazpass.trusted_device.internet.response.EnrollDeviceResponse;
import com.fazpass.trusted_device.internet.response.HEAuthResponse;
import com.fazpass.trusted_device.internet.response.NotificationResponse;
import com.fazpass.trusted_device.internet.response.OTPResponse;
import com.fazpass.trusted_device.internet.response.RemoveDeviceResponse;
import com.fazpass.trusted_device.internet.response.ValidateDeviceResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.sentry.Sentry;
import kotlin.text.Regex;

abstract class TrustedDevice extends BASE {
    private static BroadcastReceiver smsReceiver;
    private static BroadcastReceiver phoneReceiver;

//    public abstract void check(Context ctx, String email, String phone, String pin, TrustedDeviceListener<FazpassTd> enroll);

    protected void openBiometric(Context ctx, BiometricPrompt.AuthenticationCallback listener) {
        Executor executor = ContextCompat.getMainExecutor(ctx);
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) ctx, executor, listener);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Required")
                .setSubtitle("")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    protected static void initializeChecking(Context ctx) {
        if (Storage.readDataLocal(ctx, MERCHANT_TOKEN).equals("") || Storage.readDataLocal(ctx, BASE_URL).equals("")) {
            throw new NullPointerException("Call Fazpass initialize first !");
        }
    }

    @NonNull
    protected EnrollDeviceRequest collectDataEnroll(Context ctx, User user, String pin, boolean isUseFinger) {
        Sim sim = new Sim(ctx);
        Connection c = new Connection(ctx);
        FazpassKey key = new FazpassKey(ctx, user, pin);
        GeoLocation geo = new GeoLocation(ctx);
        Contact contact = new Contact(ctx);
/*        List<EnrollDeviceRequest.Contact> contactBody = new ArrayList<>();
        if(contact.getContacts().size()>0){
            for(Contact ct: contact.getContacts()){
                if(ct.getPhoneNumber().size()>0){
                    EnrollDeviceRequest.Contact ctBody = new EnrollDeviceRequest.Contact(ct.getName(),ct.getPhoneNumber().get(0));
                    contactBody.add(ctBody);
                }else {
                    EnrollDeviceRequest.Contact ctBody = new EnrollDeviceRequest.Contact(ct.getName(),"");
                    contactBody.add(ctBody);
                }
            }
        }*/
        EnrollDeviceRequest.Location locationBody = new EnrollDeviceRequest.Location(geo.getLatitude(), geo.getLongitude());
        List<EnrollDeviceRequest.Sim> simBody = new ArrayList<>();
        for (Sim s : sim.getSims()) {
            EnrollDeviceRequest.Sim smBody = new EnrollDeviceRequest.Sim(s.getSerialNumber(), s.getPhoneNumber());
            simBody.add(smBody);
        }
        return new EnrollDeviceRequest(
                user.getName(), user.getEmail(), user.getPhone(), user.getIdCard(), user.getAddress(),
                Device.name, ctx.getPackageName(), true, isUseFinger, true, c.isUseVpn(),
                Device.notificationToken, key.getMeta(), key.getPubKey(), geo.getTimezone(), contact.getContacts().size(), locationBody, simBody);
    }

    protected ValidateDeviceRequest collectDataValidate(Context ctx) {
        String userId = Storage.readDataLocal(ctx, USER_ID);
        String meta =Storage.readDataLocal(ctx, META);
        String key = Storage.readDataLocal(ctx, PUBLIC_KEY);
        if (userId.equals("") || meta.equals("") || key.equals("")) {
            return null;
        }
        Sim sim = new Sim(ctx);
        Device device = new Device(ctx);
        GeoLocation geo = new GeoLocation(ctx);
        Contact contact = new Contact(ctx);

        List<ValidateDeviceRequest.Contact> contactBody = new ArrayList<>();
        if (contact.getContacts().size() > 0) {
            for (Contact ct : contact.getContacts()) {
                if (ct.getPhoneNumber().size() > 0) {
                    ValidateDeviceRequest.Contact ctBody = new ValidateDeviceRequest.Contact(ct.getName(), ct.getPhoneNumber().get(0));
                    contactBody.add(ctBody);
                } else {
                    ValidateDeviceRequest.Contact ctBody = new ValidateDeviceRequest.Contact(ct.getName(), "");
                    contactBody.add(ctBody);
                }
            }
        }
        ValidateDeviceRequest.Location locationBody = new ValidateDeviceRequest.Location(geo.getLatitude(), geo.getLongitude());
        List<ValidateDeviceRequest.Sim> simBody = new ArrayList<>();
        for (Sim s : sim.getSims()) {
            ValidateDeviceRequest.Sim smBody = new ValidateDeviceRequest.Sim(s.getSerialNumber(), s.getPhoneNumber());
            simBody.add(smBody);
        }
        return new ValidateDeviceRequest(userId, Device.name, ctx.getPackageName(),
                meta, key, geo.getTimezone(), contactBody, locationBody, simBody);
    }

    protected static RemoveDeviceRequest collectDataRemove(Context ctx, String userId) {
        Device device = new Device(ctx);
        GeoLocation geo = new GeoLocation(ctx);
        RemoveDeviceRequest.Location l = new RemoveDeviceRequest.Location(geo.getLatitude(), geo.getLongitude());
        return new RemoveDeviceRequest(userId, ctx.getPackageName(), Device.name, l, geo.getTimezone());
    }

    protected LastActiveRequest collectDataLastActive(Context ctx, String userId) {
        Device device = new Device(ctx);
        GeoLocation geo = new GeoLocation(ctx);
        LastActiveRequest.Location l = new LastActiveRequest.Location(geo.getLatitude(), geo.getLongitude());
        return new LastActiveRequest(userId, ctx.getPackageName(),
                Device.name, geo.getTimezone(), l);
    }

    protected Observable<Response> updateLastActive(Context ctx, String userId) {
        LastActiveRequest body = collectDataLastActive(ctx, userId);
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.updateLastActive("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected Observable<Response<EnrollDeviceResponse>> enroll(Context ctx, EnrollDeviceRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.enrollDevice("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected Observable<Response<NotificationResponse>> requestNotification(Context ctx, NotificationRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.sendNotification("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected Observable<Response<ValidateDeviceResponse>> validate(Context ctx, ValidateDeviceRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.validateDevice("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static Observable<Response<RemoveDeviceResponse>> remove(Context ctx, RemoveDeviceRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.removeDevice("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected Observable<Response> updateNotification(Context ctx, UpdateNotificationRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.updateNotification("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static Observable<FazpassTd> checking(Context ctx, String email, String phone, String pin) {
        if (email.equals("") && phone.equals("")) {
            throw new NullPointerException("email or phone cannot be empty");
        }
        String packageName = ctx.getPackageName();
        GeoLocation location = new GeoLocation(ctx);
        CheckUserRequest.Location locationDetail = new CheckUserRequest.Location(location.getLatitude(), location.getLongitude());
        CheckUserRequest body = new CheckUserRequest(email, phone, packageName, Device.name, location.getTimezone(), locationDetail);
        Helper.sentryMessage("CHECK", body);
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.startService("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resp -> {
                                String key = Storage.readDataLocal(ctx, PRIVATE_KEY);
                                /*
                                 If status return false that mean user not found in our data
                                 */
                                if (!resp.getStatus()) {
                                    subscriber.onNext(new FazpassTd(ctx, new User(), pin, TRUSTED_DEVICE.UNTRUSTED, CROSS_DEVICE.UNAVAILABLE));
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
                                    if (!resp.getData().getApps().getCurrent().getKey().equals("")) {
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

                            }, err -> {
                                subscriber.onError(err);
                                Sentry.captureException(err);
                            }
                    );
        });
    }

    /**
     * Usage to remove device from server cause key on local is missing
     * @param userId - user ID from response
     * @param finalCrossStatus - cross device status
     * @param resp - response from check user
     * @return FazpassTd
     * @see FazpassTd
     */
    protected static Observable<FazpassTd> removeDevice(Context ctx, User user, String pin, String userId, CROSS_DEVICE finalCrossStatus, CheckUserResponse resp) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            Helper.sentryMessage("FORCE_REMOVE_DEVICE", collectDataRemove(ctx, userId));
            u.removeDevice("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), collectDataRemove(ctx, userId))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        subscriber.onNext(new FazpassTd(ctx, user, pin, TRUSTED_DEVICE.UNTRUSTED, finalCrossStatus, resp));
                        subscriber.onComplete();
                    }, err -> {

                    });
        });
    }

    protected void sendNotification(Context ctx, List<CheckUserResponse.App> d, long timeOut, String userId, String notificationId) {
        List<CheckUserResponse.App> devices = d.stream()
                .filter(app -> app.getApp().equals(ctx.getPackageName())).collect(Collectors.toList());
        List<NotificationRequest.Device> receiver = new ArrayList<>();
        for (CheckUserResponse.App device : devices) {
            NotificationRequest.Device r = new NotificationRequest.Device(device.getApp(), Device.name);
            receiver.add(r);
        }
        String notificationToken = Device.notificationToken;
        String thisDevice = Device.name;
        NotificationRequest body = new NotificationRequest(timeOut, notificationToken, notificationId, userId, receiver, thisDevice);
        Helper.sentryMessage("SEND_NOTIFICATION", body);
        requestNotification(ctx, body).subscribe(s -> {

        }, err -> {
            Log.e("SEND NOTIFICATION", err.getMessage());
        });
    }

    protected void validateUserByFinger(Context ctx, TrustedDeviceListener<ValidateStatus> listener) {
        openBiometric(ctx, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Exception e = Error.biometricError();
                listener.onFailure(e);
                Sentry.captureException(e);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                ValidateDeviceRequest body = collectDataValidate(ctx);
                if (body.getKey().equals("")) {
                    listener.onFailure(Error.localDataMissing());
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
                    listener.onSuccess(status);
                }, err -> {
                    Log.e(TAG, err.getMessage());
                    listener.onFailure(err);
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Exception e = Error.biometricFailed();
                listener.onFailure(e);
                Sentry.captureException(e);
            }
        });
    }

    protected void validateUserByPin(Context ctx, String pin, TrustedDeviceListener<ValidateStatus> listener) {
        if (pin.equals("")) {
            throw new NullPointerException("PIN cannot be null or empty");
        }
        String meta = new FazpassKey().getMeta();
        String key = Storage.readDataLocal(ctx, PRIVATE_KEY);
        if (meta.equals("") || key.equals("")) {
            listener.onFailure(Error.localDataMissing());
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
                    listener.onFailure(Error.localDataMissing());
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
                    listener.onSuccess(status);
                }, err -> {
                    Log.e(TAG, err.getMessage());
                    listener.onFailure(err);
                });
            } else {
                listener.onFailure(Error.pinNotMatch());
            }
        } catch (JSONException e) {
            Sentry.captureException(e);
            listener.onFailure(e);
        }
    }

    protected void notificationExpired(Context ctx, String userId, String notificationId) {
        UpdateNotificationRequest body = new UpdateNotificationRequest(notificationId, userId);
        Helper.sentryMessage("SEND_NOTIFICATION", body);
        updateNotification(ctx, body).subscribe(s -> {

        }, err -> {
            Log.e("SEND NOTIFICATION", err.getMessage());
        });
    }

    protected static void launchedFromNotification(Context context, @Nullable Bundle extras) {
        if (extras != null) {
            String app = extras.getString("app");
            String status = extras.getString("status");
            String device = extras.getString("device");
            String notificationToken = extras.getString("notification_token");
            String notificationId = extras.getString("uuid_notif");

            if (Objects.equals(app, context.getPackageName())) {
                if (Objects.equals(status, "request")) {
                    Intent dialogIntent = NotificationActivity
                            .buildIntent(context, notificationId, notificationToken, device, -1);
                    context.startActivity(dialogIntent);
                }
            } else {
                Log.e("intent extras", "NULL");
            }
        }
    }

    static Observable<Response> updateFcm(Context ctx, String token) {
        return Observable.create(subscriber -> {
            String meta = Storage.readDataLocal(ctx, META);
            String userId = Storage.readDataLocal(ctx, USER_ID);
            String packageName = ctx.getPackageName();
            String device = Device.name;
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            UpdateFcmRequest body = new UpdateFcmRequest(userId, packageName, device, token, meta);
            u.updateFcm("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        subscriber.onNext(s);
                        subscriber.onComplete();
                    }, subscriber::onError);
            ;
        });
    }

    protected static Observable<Response<OTPResponse>> requestOtpWithPhone(Context ctx, OTPWithPhoneRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.requestOTPWithPhone("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static Observable<Response<OTPResponse>> requestOtpWithEmail(Context ctx, OTPWithEmailRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.requestOTPWithEmail("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static Observable<Response<OTPResponse>> generateOtpWithPhone(Context ctx, OTPWithPhoneRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.generateOTPWithPhone("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static Observable<Response<OTPResponse>> generateOtpWithEmail(Context ctx, OTPWithEmailRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.generateOTPWithEmail("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static Observable<Response> verifyOtp(Context ctx, OTPVerificationRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.verifyOTP("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static void startSMSListener(Context context, Otp.Request listener, int otpLength) {
        if (anyDeclinedPermission(context, new String[]{Manifest.permission.RECEIVE_SMS})) {
            String err = "RECEIVE_SMS permission is not granted. Hence, autofill otp will be disabled.";
            Log.e("AUTOFILL SMS", err);
            return;
        }

        String action = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (Objects.equals(intent.getAction(), action)) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        final SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                        for (SmsMessage m : messages) {
                            String message = m.getMessageBody();
                            Regex re = new Regex("[^A-Za-z0-9]");
                            message = re.replace(message, " ");
                            Regex re2 = new Regex("(?m)^[ \t]*\r?\n");
                            String[] messageArr = re2.replace(message, " ").split(" ");
                            for (String item : messageArr) {
                                if (item.isEmpty()) continue;
                                try {
                                    String trimmed = item.replace(" ", "");
                                    Double.parseDouble(trimmed);
                                    if (trimmed.length() == otpLength) {
                                        listener.onIncomingMessage(trimmed);
                                        if (phoneReceiver != null)
                                            context.unregisterReceiver(phoneReceiver);
                                        context.unregisterReceiver(this);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(action);
        context.registerReceiver(smsReceiver, filter);
    }

    protected static void startMiscallListener(Context context, Otp.Request listener, int otpLength) {
        if (anyDeclinedPermission(context, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG})) {
            String err = "READ_PHONE_STATE and READ_CALL_LOG permission are not granted. Hence, autofill otp will be disabled.";
            Log.e("AUTOFILL MISCALL", err);
            return;
        }

        String action = "android.intent.action.PHONE_STATE";
        phoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(action)) {
                    String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                    if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        listener.onIncomingMessage(number.substring(number.length() - otpLength));
                        if (smsReceiver != null) context.unregisterReceiver(smsReceiver);
                        context.unregisterReceiver(this);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(action);
        context.registerReceiver(phoneReceiver, filter);
    }

    protected static Observable<Response<HEAuthResponse>> getAuthPage(Context ctx, HEAuthRequest body) {
        return Observable.create(subscriber -> {
            UseCase u = Roaming.start(Storage.readDataLocal(ctx, BASE_URL));
            u.HEAuth("Bearer " + Storage.readDataLocal(ctx, MERCHANT_TOKEN), body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static Observable<Response> launchAuthPage(String url) {
        return Observable.create(subscriber -> {
            Uri uri = Uri.parse(url);
            UseCase u = Roaming.start(uri.getScheme() + "://" + uri.getHost() + "/");
            Map<String, String> queries = new ArrayMap<>();
            uri.getQueryParameterNames().forEach(name -> queries.put(name, uri.getQueryParameter(name)));
            u.HERedirectAuth(uri.getPath().replace("/", ""), queries)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    protected static boolean isTransportCellular(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network currentNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);

        List<Integer> bannedNetworkTransports = new ArrayList<>();
        bannedNetworkTransports.add(NetworkCapabilities.TRANSPORT_WIFI);
        bannedNetworkTransports.add(NetworkCapabilities.TRANSPORT_BLUETOOTH);
        bannedNetworkTransports.add(NetworkCapabilities.TRANSPORT_ETHERNET);
        bannedNetworkTransports.add(NetworkCapabilities.TRANSPORT_VPN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bannedNetworkTransports.add(NetworkCapabilities.TRANSPORT_USB);
        }

        return bannedNetworkTransports.stream().noneMatch(caps::hasTransport);
    }

    @SuppressLint("MissingPermission")
    protected static boolean isCarrierMatch(Context context, String phone) throws Throwable {
        if (anyDeclinedPermission(context, new String[]{Manifest.permission.READ_PHONE_STATE})) {
            String err = "READ_PHONE_STATE permission is not granted. Hence, Header Enrichment request failed.";
            Log.e("PERMISSION NOT GRANTED", err);
            throw new Throwable(err);
        }

        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        SubscriptionInfo info = subscriptionManager.getActiveSubscriptionInfo(SubscriptionManager.getDefaultDataSubscriptionId());
        String carrierName = info.getCarrierName().toString();
        Log.e("CARRIER NAME", carrierName);

        boolean is0First = phone.startsWith("0");
        if (is0First && phone.length() < 3) return false;
        if (!is0First && phone.length() < 4) return false;

        String finalPhone;
        if (is0First) finalPhone = phone;
        else finalPhone = phone.replaceFirst("62", "0");
        return DataCarrierUtility.check(finalPhone.substring(0,4), carrierName);
    }

    private static boolean anyDeclinedPermission(Context context, String[] permissions) {
        boolean declined = false;
        for (String permission : permissions) {
            int grant = context.checkSelfPermission(permission);
            if (grant != PackageManager.PERMISSION_GRANTED) {
                declined = true;
                break;
            }
        }
        return declined;
    }
}
