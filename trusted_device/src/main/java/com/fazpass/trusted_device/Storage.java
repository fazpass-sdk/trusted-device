package com.fazpass.trusted_device;

import static com.fazpass.trusted_device.BASE.FAZPASS;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;


class Storage {
    private static void saveData(Context context, String key, String value){
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void storeDataLocal(Context context, String key, int value){
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void storeDataLocal(Context context, String key, Bitmap logo){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        logo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, encodedImage);
        editor.apply();
    }

    static void removeDataLocal(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
    }

    static String readDataLocal(Context context, String key){
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        return sharedPref.getString(key,"");
    }

    static int readIntDataLocal(Context context, String key){
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        return sharedPref.getInt(key,0);
    }

    static Bitmap readBitmapLocal(Context context, String key){
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        byte[] b = Base64.decode(sharedPref.getString(key,""), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    private static void removeDataLocal(Context context, String key){
        SharedPreferences sharedPref = context.getSharedPreferences(FAZPASS,Context.MODE_PRIVATE);
        sharedPref.edit().remove(key).apply();

    }

    static void storeDataLocal(Context context, String key, String newValue){
        if(isDataExists(context,key)){
            removeDataLocal(context,key);
        }
        saveData(context,key,newValue);
    }

    private static boolean isDataExists(Context context, String key){
        return !readDataLocal(context, key).equals("");
    }
     static String readDataPublic(@NonNull Context context, String packageName, String password, String key){
        try{
            Context packageContext = context.createPackageContext(packageName,0);
            SharedPreferences pref = packageContext.getSharedPreferences(
                    password, Context.MODE_PRIVATE);
            return pref.getString(key,"");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }
}
