package com.vpn.vpn;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import android.widget.Toast;

public class VpnPlugin extends CordovaPlugin {
    public static String ACTION = "Vpn";
    public static String user = "";

    public boolean execute(String action, JSONArray data,
            CallbackContext callbackContext) throws JSONException {
        if (ACTION.equals(action)) {
            user = data.getString(0);
            VpnLogin(callbackContext);
        }
        return false;
    }

    public synchronized void VpnLogin(CallbackContext callbackContext) {
       Toast.makeText(this.cordova.getActivity(), user,3000).show();
    }
}
