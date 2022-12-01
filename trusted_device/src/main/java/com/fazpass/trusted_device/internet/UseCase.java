package com.fazpass.trusted_device.internet;

import com.fazpass.trusted_device.OtpResponse;
import com.fazpass.trusted_device.internet.request.CheckUserRequest;
import com.fazpass.trusted_device.internet.request.ConfirmStatusRequest;
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

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface UseCase {

    @POST("v1/trusted-device/check")
    Observable<Response<CheckUserResponse>> startService(@Header("Authorization")String token, @Body CheckUserRequest body);

    @POST("v1/trusted-device/enroll")
    Observable<Response<EnrollDeviceResponse>> enrollDevice(@Header("Authorization")String token, @Body EnrollDeviceRequest body);

    @POST("v1/trusted-device/verify")
    Observable<Response<ValidateDeviceResponse>> validateDevice(@Header("Authorization")String token, @Body ValidateDeviceRequest body);

    @POST("v1/trusted-device/remove")
    Observable<Response<RemoveDeviceResponse>> removeDevice(@Header("Authorization")String token, @Body RemoveDeviceRequest body);

    @POST("v1/trusted-device/send/notification")
    Observable<Response<NotificationResponse>> sendNotification(@Header("Authorization")String token, @Body NotificationRequest body);

    @PUT("v1/trusted-device/update/expired")
    Observable<Response> updateNotification(@Header("Authorization")String token, @Body UpdateNotificationRequest body);

    @POST("v1/trusted-device/confirmation/status")
    Observable<Response> confirmStatus(@Header("Authorization")String token, @Body ConfirmStatusRequest body);

    @PUT("v1/trusted-device/update/last-active")
    Observable<Response> updateLastActive(@Header("Authorization")String token, @Body LastActiveRequest body);

    @PUT("v1/trusted-device/update/notification-token")
    Observable<Response> updateFcm(@Header("Authorization")String token, @Body UpdateFcmRequest body);

    @POST("v1/otp/request")
    Observable<Response<OTPResponse>> requestOTPWithPhone(@Header("Authorization")String token, @Body OTPWithPhoneRequest body);

    @POST("v1/otp/request")
    Observable<Response<OtpResponse>> requestOTPWithEmail(@Header("Authorization")String token, @Body OTPWithEmailRequest body);

    @POST("v1/otp/generate")
    Observable<Response<OTPResponse>> generateOTPWithPhone(@Header("Authorization")String token, @Body OTPWithPhoneRequest body);

    @POST("v1/otp/generate")
    Observable<Response<OTPResponse>> generateOTPWithEmail(@Header("Authorization")String token, @Body OTPWithEmailRequest body);

    @POST("v1/otp/verify")
    Observable<Response> verifyOTP(@Header("Authorization")String token, @Body OTPVerificationRequest body);

    @POST("v1/he/request/auth-page")
    Observable<Response<HEAuthResponse>> HEAuth(@Header("Authorization") String token, @Body HEAuthRequest body);

    @GET("{path}")
    Observable<Response> HERedirectAuth(@Path("path") String path, @QueryMap Map<String, String> queries);
}
