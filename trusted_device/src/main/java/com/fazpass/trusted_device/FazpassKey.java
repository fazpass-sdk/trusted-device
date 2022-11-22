package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.BASE.DEVICE;
import static com.fazpass.trusted_device.BASE.META;
import static com.fazpass.trusted_device.BASE.PACKAGE_NAME;
import static com.fazpass.trusted_device.BASE.PRIVATE_KEY;
import static com.fazpass.trusted_device.BASE.PUBLIC_KEY;
import static com.fazpass.trusted_device.BASE.USER_EMAIL;
import static com.fazpass.trusted_device.BASE.USER_PHONE;
import static com.fazpass.trusted_device.BASE.USER_PIN;
import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;

import at.favre.lib.crypto.bcrypt.BCrypt;


class FazpassKey {
    private Context context;
    private String pubKey;
    private static String meta;
    private String pin;
    private User user;
    private Device device;

    public FazpassKey(){

    }

    public FazpassKey(Context context, User user, Device device, String pin) {
        this.context = context;
        this.pin = BCrypt.withDefaults().hashToString(12, pin.toCharArray());;
        this.user = user;
        this.device = device;
        initialize();
    }

    private void initialize(){
        try{
            pubKey = UUID.randomUUID().toString();
            String keyStoreAlias = generateKeyAlias(pubKey);
            String password = BCrypt.withDefaults().hashToString(12, pubKey.toCharArray());
            Storage.storeDataLocal(context, USER_PIN, pin);
            Storage.storeDataLocal(context, PRIVATE_KEY, password);
            meta = Crypto.encrypt(keyStoreAlias, password);
            Storage.storeDataLocal(context, META, password);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String generateKeyAlias(String uuid){
        JSONObject json = new JSONObject();
        try{
            json.put(PACKAGE_NAME, context.getPackageName());
            json.put(PUBLIC_KEY, uuid);
            json.put(USER_PHONE, user.getPhone());
            json.put(USER_EMAIL, user.getEmail());
            json.put(USER_PIN, pin);
            json.put(DEVICE, device.getDevice());
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return json.toString();
        }

    }

    public String getPubKey() {
        return pubKey;
    }

    public String getMeta() {
        return meta;
    }

    public String getPin() {
        return pin;
    }

    public static void setMeta(String meta) {
        FazpassKey.meta = meta;
    }
}
