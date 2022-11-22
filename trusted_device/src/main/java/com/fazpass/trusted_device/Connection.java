package com.fazpass.trusted_device;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

class Connection {
    private boolean useVpn;
    private final Context context;
    public boolean isUseVpn() {
        return useVpn;
    }

    public Connection(Context context){
        this.context = context;
        initialize();
    }
    private void initialize(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);
        this.useVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    }
}
