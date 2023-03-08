# Fazpass-Android (Trusted Device)

This is the Official Android wrapper/library for Fazpass Trusted Device, that is compatible with Gradle.
Visit https://fazpass.com for more information about the product and see documentation at http://docs.fazpass.com for more technical details.
- [1. Installation](#1-Installation)
- [2. OTP Feature](#2-OTP Feature)
- [3. Header Enrichment Feature](#3-Header Enrichment Feature)
- [4. Trusted Device Feature](#4-Trusted Device Feature)

## 1. Installation
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
 com.github.fazpass-sdk:trusted-device:1.0.1
```
### Minimum OS
API 24 / Android 7.0 / Nougat 

### Target SDK
We use newest version of androidx library so make sure you use this configuration
```gradle
 android {
    compileSdk 33

    defaultConfig {
        minSdk 24
        targetSdk 33
        .....
 
```
### Initialize
You only need call this method once in the first page of your app
```java
Fazpass.initialize(Context, MERCHANT_KEY, TD_MODE.STAGING)
```
MERCHANT_KEY is unique string when you registered as a fazpass client.
(You can check your email after registered to get it) <br>
TD_MODE is type of mode, you can choose between staging or production.


## 2. OTP Feature
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
### Permission
If you want get auto read OTP you can enable this permission
```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.READ_CALL_LOG"/>
```
 
| Permission           |                                              Detail                                              |
|----------------------|:------------------------------------------------------------------------------------------------:|
| Receive SMS          |                        Will be used to auto read sms when OTP come by SMS                        |
| Read Call Log        |                    Will be used to auto read otp when OTP come by missed call                    |

## 3. Header Enrichment Feature
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
### Permission
```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
```
| Permission           |                                              Detail                                              |
|----------------------|:------------------------------------------------------------------------------------------------:|
| Read Phone State     | Will be used to detect root phone, emulator, or cloning, also will be used for Header Enrichment |
| Change Network State |                 Will be used to autoswitch connection to mobile on HE validation                 |


## 4.Trusted Device
First of all you need to call this method in the main class of your application
```java
Fazpass.initialize(this, MERCHANT_KEY,MODE.STAGING);
```
you can get merchant key from email when you registered as our client.
#### General Flow (Check & Enroll)
![alt text](https://firebasestorage.googleapis.com/v0/b/anvarisy-tech.appspot.com/o/TD%20General%20Flow.drawio.png?alt=media&token=d3b59ce0-c326-496c-867b-b0561b595677) <br>
as you can see, trusted device need to always call check method that will return trusted status from that device.<br>
After status already trusted you can directly call method enroll. In our system enroll will update the newest information from that device & user.<br>
NB: never call enroll method if result of check is untrusted

##### Implementation
```java
   Fazpass.check(Context, "EMAIL", "PHONE" new TrustedDeviceListener<FazpassTd>() {
        @Override
        public void onSuccess(FazpassTd o) {
            if(o.td_status.equals(TRUSTED_DEVICE.TRUSTED)){
                 o.enrollDeviceByPin(MainActivity.this, user, pin, new TrustedDeviceListener<EnrollStatus>() {
                    @Override
                    public void onSuccess() {
                        // Trusted device success
                    }

                    @Override
                    public void onFailure(Throwable err) {
                        //TODO handle error
                    }
                });
            }
        }

        @Override
        public void onFailure(Throwable err) {
            
        }
   });
```
#### Validate  User & Device
It will calculating how confidence that user and that device with your app.
```java
   Fazpass.check(this, "EMAIL", "PHONE", new TrustedDeviceListener<FazpassTd>() {
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
NB: If you use transaction inside your app, we recommended to use this method before transaction begin.
#### Cross Device Validation
![alt text](https://firebasestorage.googleapis.com/v0/b/anvarisy-tech.appspot.com/o/TD%20Cross%20Device%20Flow.drawio.png?alt=media&token=6cc7f01a-d33a-4ec3-b6fb-674ba818ff7f) <br>
You can use this method when your client already registered with one phone number or email in one device, and trying to login with that email or phone in another device
```java
   Fazpass.check(this, "EMAIL", "PHONE", new TrustedDeviceListener<FazpassTd>() {
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
And then you have to declare this method in your main activity launcher
```java
// if you use default confirmation activity
FazpassCd.initialize(activity, isRequirePin, null);

// if you use custom confirmation activity
FazpassCd.initialize(activity, isRequirePin, YourCustomConfirmationActivity.class);
```
It will show you confirmation activity that asks user to input PIN (if pin is required), before they can click button YES. <br><br>
![alt text](https://firebasestorage.googleapis.com/v0/b/anvarisy-tech.appspot.com/o/default.png?alt=media&token=d1081877-77fa-42b3-b1a5-f5ed8a75f6ee) <br>
This is your default view of your confirmation activity. <br><br>
![alt text](https://firebasestorage.googleapis.com/v0/b/anvarisy-tech.appspot.com/o/login.png?alt=media&token=bf17a8d9-2922-48d3-88e3-065631becd7d) <br>
Or you can also create your own custom activity that will be used for your application.<br>
To get device information of login requester, call this method in your custom activity
```java
String device = getIntent().getStringExtra("device");
```
To accept incoming login confirmation
```java
// if pin is not required
FazpassCd.onConfirm(activity);
// if pin is required
FazpassCd.onConfirm(activity, pin, callback);
```
To decline incoming login confirmation
```java
FazpassCd.onDecline(activity);
```

#### Remove Device
It will remove that device and user.
```java
Fazpass.removeDevice(Context, "PIN", new TrustedDeviceListener<RemoveStatus>{
    @Override
    public void onSuccess() {
        
    }
    
    @Override
    public void onError(Throwable err) {

    }
    
});
```


### Permission
As default this SDK used these permissions
```xml
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC"/>


```
To give best result of confidence rate, make sure you enable this permission

| Permission           |                                              Detail                                              |
|----------------------|:------------------------------------------------------------------------------------------------:|
| Coarse Location      |                  Will be used to read user location for detecting fraud access                   |
| Read Phone State     | Will be used to detect root phone, emulator, or cloning, also will be used for Header Enrichment |
| Read Phone Number    |       Will be used to read SIM serial position. it will affect with confidence rate result       |
| Read Contacts        |          Will be used to read user contact. It will affect with confidence rate result           |
| Use Biometric        |                       Will be used to open biometric dialog in user phone                        |