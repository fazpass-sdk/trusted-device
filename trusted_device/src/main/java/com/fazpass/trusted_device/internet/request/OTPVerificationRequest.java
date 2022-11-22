package com.fazpass.trusted_device.internet.request;

import com.google.gson.annotations.SerializedName;

public class OTPVerificationRequest {

   @SerializedName("otp_id")
   private String otpId;

   @SerializedName("otp")
   private String otp;

   public OTPVerificationRequest(String otpId, String otp) {
      this.otpId = otpId;
      this.otp = otp;
   }

   public String getOtpId() {
      return otpId;
   }

   public String getOtp() {
      return otp;
   }
}
