package com.fazpass.trusted_device;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.TimeZone;

class GeoLocation extends Service implements LocationListener {
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String timezone;
    private final Context context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    protected LocationManager locationManager;


    public GeoLocation(Context context) {
        this.context = context;
        readTimezone();
        readLocation();
    }

    private void readTimezone(){
        TimeZone timeZone = TimeZone.getDefault();
        this.timezone = timeZone.getID();
    }

    void readLocation() {
        boolean isPermit = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)&&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                latitude = 0.0;
                longitude = 0.0;
            } else {
                this.canGetLocation = true;
/*
                if (isNetworkEnabled) {
                    if(isPermit) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
*/

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        //check the network permission
                        assert locationManager != null;
                        if(isPermit){
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }else{
                            latitude = 0.0;
                            longitude = 0.0;
                        }

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    double getLatitude() {
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    double getLongitude() {
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    boolean isMockLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(PackageManager.PERMISSION_GRANTED == ContextCompat
                .checkSelfPermission(context, Manifest.permission.READ_CONTACTS)){
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return location != null && location.isFromMockProvider();
        }else{
            return true;
        }
    }

    boolean canGetLocation() {
        return this.canGetLocation;
    }

    String getTimezone() {
        return timezone;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
