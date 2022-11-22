package com.fazpass.trusted_device;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

class Sim {
    private String serialNumber;
    private String phoneNumber;
    private Context context;
    private List<Sim> sims;
    public Sim(Context context) {
     this.context = context;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
          readSimData().subscribe(s->{
             this.sims = s;
          });
        }else{
            readSimBelow10().subscribe(s->{
               this.sims = s;
            });
        }
    }

    public List<Sim> getSims(){
        return this.sims;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private Sim(String serial, String phone){
        this.serialNumber = serial;
        this.phoneNumber = phone;
    }

    Observable<List<Sim>> readSimData(){
        return Observable.create(subscriber->{
            TelecomManager tm2;
            Iterator<PhoneAccountHandle> phoneAccounts;
            PhoneAccountHandle phoneAccountHandle;
            List<Sim> sims = new ArrayList<>();
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            boolean isPermit = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) &&
                    PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS);
            if(isPermit){
                List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                tm2 = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();

                for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                    CharSequence carrierName = subscriptionInfo.getCarrierName();
                    String countryIso = subscriptionInfo.getCountryIso();
                    int dataRoaming = subscriptionInfo.getDataRoaming();  // 1 is enabled ; 0 is disabled
                    CharSequence displayName = subscriptionInfo.getDisplayName();
                    String serialNumber =subscriptionInfo.getIccId();
                    int mcc = subscriptionInfo.getMcc();
                    int mnc = subscriptionInfo.getMnc();
                    boolean networkRoaming = subscriptionManager.isNetworkRoaming(slotIndex);
                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                    String phoneNumber = subscriptionInfo.getNumber();
                    phoneAccountHandle = phoneAccounts.next();
                    String simSerial = "";
                    try {
                        simSerial = phoneAccountHandle.getId();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Sim sim = new Sim(simSerial,phoneNumber);
                    sims.add(sim);
                }
            }else{
                Sim sim = new Sim("","");
                sims.add(sim);
            }
            subscriber.onNext(sims);
            subscriber.onComplete();
        });
    }

    Observable<List<Sim>> readSimBelow10(){
        return Observable.create(subscriber->{
            List<Sim> sims = new ArrayList<>();
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            boolean isPermit = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) &&
                    PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS);
            if(isPermit){
                List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
                for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                    CharSequence carrierName = subscriptionInfo.getCarrierName();
                    String countryIso = subscriptionInfo.getCountryIso();
                    int dataRoaming = subscriptionInfo.getDataRoaming();  // 1 is enabled ; 0 is disabled
                    CharSequence displayName = subscriptionInfo.getDisplayName();
                    String serialNumber = subscriptionInfo.getIccId();
                    boolean networkRoaming = subscriptionManager.isNetworkRoaming(slotIndex);
                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                    String phoneNumber = subscriptionInfo.getNumber();
                    Sim sim = new Sim(serialNumber, phoneNumber);
                    sims.add(sim);
                }
            }else{
                Sim sim = new Sim("","");
                sims.add(sim);
            }
            subscriber.onNext(sims);
            subscriber.onComplete();
        });
    }

}
