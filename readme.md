# Fazpass-Android (Trusted Device)

This is the Official Android wrapper/library for Fazpass Trusted Device, that is compatible with Gradle.
Visit https://fazpass.com for more information about the product and see documentation at http://docs.fazpass.com for more technical details.

## Installation
Gradle
```groovy
allprojects {
	repositories {
	...
	maven {
           url "https://jitpack.io"
            // You can obtain by contacting us
           }
	}
}
```

```
 implementation 'com.github.fazpass:fazpass-android-trusted-device-sdk:TAG'
```
## Minimum OS
API 24 / Android 7.0 / Nougat 

## Target SDK
We use newest version of androidx library so make sure you use this configuration
```gradle
 android {
    compileSdk 33

    defaultConfig {
        minSdk 24
        targetSdk 33
        .....
 
```
## Permission
As default this SDK used these permissions
```xml
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.USE_BIOMETRIC"/>
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.READ_CALL_LOG"/>
```
So make sure you request all of these permission as a requirement.
#### 
| Permission                    | Detail            |
| -------------                 |:-------------:    |
| Coarse Location               | Will be used to read user location for detecting fraud access  |
| Read Phone State              | Will be used to detect root phone, emulator, or cloning, also will be used for Header Enrichment        |
| Read Phone Number             | Will be used to read SIM serial position. it will affect with confidence rate result |
| Read Contacts                 | Will be used to read user contact. It will affect with confidence rate result  |
| Use Biometric                 | Will be used to open biometric dialog in user phone            |
| Receive SMS                   | Will be used to auto read sms when OTP come by SMS           |
| Read Call Log                 | Will be used to auto read otp when OTP come by missed call           |


## Usage

### Initialize
You only need call this method once in the first page of your app
```java
Fazpass.initialize(Context, MERCHANT_KEY, TD_MODE.STAGING)
```
MERCHANT_KEY is unique string when you registered as a fazpass client.
(You can check your email after registered to get it) <br>
TD_MODE is type of mode, you can choose between staging or production.

### OTP
We have request OTP by email and by phone. Same as like the API we also have generate Otp function if you want to use it.
#### Requesting Method
```java
    Fazpass.requestOtpByPhone(this, "PHONE_NUMBER", "GATEWAY_KEY", new Otp.Request() {
        @Override
        public void onComplete(OtpResponse response) {
                // Response object will be came here,
        }

        @Override
        public void onIncomingMessage(String otp) {
                // If you use SMS/Missed Call gateway your OTP will come here. as long the permissions already allowed.
        }

        @Override
        public void onError(Throwable err) {
                // It should be always null.
        }
    });
```
#### Validating OTP
```java
    Fazpass.validateOtp(this, "OTP_ID", "OTP", new Otp.Validate() {
        @Override
        public void onComplete(boolean status) {
        // Result of validating OTP (true/false)
        }

        @Override
        public void onError(Throwable err) {
        // It should be always null
        }
    });
```
### Header Enrichment
Header enrichment will make you more confidence, because it will match between data roaming and sim that registered in that phone.<br>
The result, your app can't validate phone number that is not inside that phone.
```java
   Fazpass.heValidation(this, "PHONE_NUMBER", "GATEWAY_KEY", new HeaderEnrichment.Request() {
        @Override
        public void onComplete(boolean status) {
            
        }
    
        @Override
        public void onError(Throwable err) {
    
        }
   });
```
### Trusted Device
#### Check User & Device
Trusted device required to call method check before call other method.
```java
   Fazpass.check(this, "EMAIL", "PHONE", "PIN", new TrustedDeviceListener<FazpassTd>() {
        @Override
        public void onSuccess(FazpassTd o) {
            if(o.td_status.equals(TRUSTED_DEVICE.TRUSTED)){
                // You can pass
            }
        }

        @Override
        public void onFailure(Throwable err) {

        }
   });
```
#### Enroll New Device
It will register new device and new user, You can also call this method if user forgot PIN or reinstall your app.
```java
   Fazpass.check(this, "EMAIL", "PHONE", "PIN", new TrustedDeviceListener<FazpassTd>() {
        @Override
        public void onSuccess(FazpassTd o) {
            if(o.td_status.equals(TRUSTED_DEVICE.UNTRUSTED)){
                User user = new User("EMAIL","PHONE","NAME","ID_CARD", "ADDRESS");
                o.enrollDeviceByPin(MainActivity.this, user,"PIN");
            }
        }

        @Override
        public void onFailure(Throwable err) {

        }
   });
```
We also have method enroll device by finger, if you prefer to use finger authentication.
#### Validate  User & Device
It will calculating how confidence that user and that device with your app.
```java
   Fazpass.check(this, "EMAIL", "PHONE", "PIN", new TrustedDeviceListener<FazpassTd>() {
        @Override
        public void onSuccess(FazpassTd o) {
            o.validateUser(MainActivity.this, "PIN", new TrustedDeviceListener<ValidateStatus>() {
                @Override
                public void onSuccess(ValidateStatus o) {
                    if(o.getConfidenceRate()>=80){
                        // This user confidence enough to do something
                    }
                }
                
                @Override
                public void onFailure(Throwable err) {
                
                }
            });
        }

        @Override
        public void onFailure(Throwable err) {

        }
   });
```
If you use transaction inside your app, we recommended to use this method before transaction begin.
#### Cross Device Validation
You can use this method when your client already registered with one phone or email, and trying to login with that email or phone in another device
```java
   Fazpass.check(this, "EMAIL", "PHONE", "PIN", new TrustedDeviceListener<FazpassTd>() {
        @Override
        public void onSuccess(FazpassTd o) {
            if(o.cd_status.equals(CROSS_DEVICE.AVAILABLE)){
                o.validateCrossDevice(MainActivity.this, COUNTDOWN, new CrossDeviceListener() {
                    @Override
                    public void onResponse(String device, boolean status) {
                        // status will show the other device allow or not
                        // device will show what device that already allow, if this user already register more than one device
                    }
        
                    @Override
                    public void onExpired() {
                        
                    }
                });
            }
        }

        @Override
        public void onFailure(Throwable err) {

        }
   });
```
If COUNTDOWN has finish and no response from all device, that request will become expired.<br>
NB: For android 10 and higher make sure you add this line in your main activity launcher
```java
Fazpass.launchedFromNotificationRequirePin(this, getIntent());
```
It will show you dialog confirmation that required user to input PIN, before they can click button YES.

#### Remove Device
It will remove that device and user, so when that user try to login again, they need to enroll again.
```java
Fazpass.removeDevice(this);
```