package com.fazpass.trusted_device.internet.response;

import com.google.gson.annotations.SerializedName;

public class OTPResponse {

   @SerializedName("id")
   private String id;

   @SerializedName("otp")
   private String otp;

   @SerializedName("otp_length")
   private int otpLength;

   @SerializedName("channel")
   private String channel;

   @SerializedName("provider")
   private String provider;

   @SerializedName("purpose")
   private String purpose;

   public OTPResponse(String id, String otp, int otpLength, String channel, String provider, String purpose) {
      this.id = id;
      this.otp = otp;
      this.otpLength = otpLength;
      this.channel = channel;
      this.provider = provider;
      this.purpose = purpose;
   }

   public String getId() {
      return id;
   }

   public String getOtp() {
      return otp;
   }

   public int getOtpLength() {
      return otpLength;
   }

   public String getChannel() {
      return channel;
   }

   public String getProvider() {
      return provider;
   }

   public String getPurpose() {
      return purpose;
   }
}
