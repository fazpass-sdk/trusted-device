package com.fazpass.app;

import static com.fazpass.app.MainActivity.HE_KEY;
import static com.fazpass.app.MainActivity.MERCHANT_KEY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fazpass.trusted_device.Fazpass;
import com.fazpass.trusted_device.HeaderEnrichment;
import com.fazpass.trusted_device.MODE;

public class HeActivity extends AppCompatActivity {
    EditText phone;
    TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_he);
        Fazpass.initialize(this, MERCHANT_KEY, MODE.STAGING);
        Button b = findViewById(R.id.button);
        phone = findViewById(R.id.editTextPhone);
        result = findViewById(R.id.textView);

        b.setOnClickListener(v->{
           forceConnectionToMobile(HeActivity.this);
        });
    }

    void heValidation(){
        Fazpass.heValidation(HeActivity.this, phone.getText().toString(), HE_KEY, new HeaderEnrichment.Request() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(boolean status) {
                result.setText("Status HE "+status);
            }

            @Override
            public void onError(Throwable err) {
                result.setText("Status HE "+err.getMessage());
            }
        });
    }

    void forceConnectionToMobile(Context context) {
        Log.e("Check","Koala");
        ConnectivityManager connection_manager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        try{
            NetworkRequest req = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connection_manager.requestNetwork(req, new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    Toast.makeText(context,"Available sim channel",Toast.LENGTH_LONG).show();
                    heValidation();
                    connection_manager.bindProcessToNetwork(network);
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Toast.makeText(context,"UnAvailable",Toast.LENGTH_LONG).show();
                    Log.e("MobileNetwork", "no network");
                }
            });
        }catch (Exception e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }
}