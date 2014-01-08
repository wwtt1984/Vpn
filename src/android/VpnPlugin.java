package com.vpn.vpn;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import android.widget.Toast;
import com.mycompany.webInspect.webInspect;

import java.util.Timer;
import java.util.TimerTask;

public class VpnPlugin extends CordovaPlugin {
    public static String ACTION = "Vpn";
    public static String VpnFirst = "VpnFirst";
    private static CallbackContext CallbackContext;
    Timer timer;
    Timer timeload; ////监听是否第一次

    public boolean execute(String action, JSONArray data,
            CallbackContext callbackContext) throws JSONException {
        if (ACTION.equals(action)) {
            timer = new Timer();
            timer.schedule(new RemindTask(),0,500);
            CallbackContext = callbackContext;
            return true;
        }
        else if(VpnFirst.equals(action))
        {
            timeload = new Timer();
            timeload.schedule(new RemindTaskLoad(),0,500);
            CallbackContext = callbackContext;
            return true;
        }
        return false;
    }

    public static void VpnLogin(String message) {
        CallbackContext.success(message);
    }

    class RemindTask extends TimerTask{

        int numWarningBeeps = 60; ////////////循环30秒，如果还是不成功，则返回失败
        public void run(){
             if(numWarningBeeps > 0)
             {
                 if(webInspect.VpnOk == "true" && webInspect.VpnSuccess == "true")
                 {
                     VpnLogin(webInspect.VpnOk);
                     timer.cancel();
                 }
                 else if(webInspect.VpnOk == "true" &&  webInspect.VpnSuccess == "false")
                 {
                     VpnLogin("initfalse");
                     timer.cancel();
                 }
                 numWarningBeeps--;
             }
             else
             {
                 VpnLogin("false");
                 timer.cancel();
             }
        }
    }

     class RemindTaskLoad extends TimerTask{   //////////////////////检查是否第一次

        int numWarningBeeps = 60; ////////////循环30秒，如果还是不成功，则返回失败
        public void run(){
             if(numWarningBeeps > 0)
             {

             }
             else
             {

             }
        }
     }

}
