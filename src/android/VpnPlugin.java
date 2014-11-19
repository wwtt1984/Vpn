package com.vpn.vpn;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import android.widget.Toast;
import com.mycompany.webInspect.webInspect;

import com.sangfor.ssl.IVpnDelegate;
import com.sangfor.ssl.SFException;
import com.sangfor.ssl.SangforAuth;
import com.sangfor.ssl.common.VpnCommon;
import com.sangfor.ssl.easyapp.SangforNbAuth;

import android.location.LocationManager;
import android.view.View;
import android.content.Context;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.net.SocketException;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Formatter;

import android.content.Intent;
import android.provider.Settings;
import android.content.ActivityNotFoundException;

import android.view.inputmethod.InputMethodManager;
import android.app.ActivityManager;

public class VpnPlugin extends CordovaPlugin implements IVpnDelegate{

    private static CallbackContext CallbackContext;
    private String User = "";
    private String Pwd = "";
    private WifiManager wifiManager;
    private DhcpInfo dhcpInfo;
    private WifiInfo wifiInfo;

    public boolean execute(String action, JSONArray data,
            CallbackContext callbackContext) throws JSONException {
        if(action.equals("Vpn"))
        {
            User = data.getString(0);
            Pwd = data.getString(1);
            int authStatus =  SangforNbAuth.getInstance().vpnQueryStatus();
            switch (authStatus) {
                case IVpnDelegate.VPN_STATUS_UNSTART:
                this.VpnInit();///初始化VPN
                break;
                case IVpnDelegate.VPN_STATUS_INIT_OK:
                VpnReset();///重新登录VPN
                break;
                case IVpnDelegate.VPN_STATUS_AUTH_OK:////VPN连接正常
                callbackContext.success("true");
                break;
                default:
                break;
            }
            CallbackContext = callbackContext;
            return true;
        }
        else if(action.equals("VpnOnWifi"))
        {
            callbackContext.success(getGateWay(this.cordova.getActivity()));
            return true;
        }
        else if(action.equals("VpnCheckOnLine"))
        {
            int authStatus =  SangforNbAuth.getInstance().vpnQueryStatus();
            String result = "";
            switch (authStatus) {

                case IVpnDelegate.VPN_STATUS_AUTH_OK:////VPN连接正常
                result = "true";
                break;
                default:
                break;
            }
            callbackContext.success(result);
            return true;
        }
        else if(action.equals("VpnOFF"))
        {
            int authStatus =  SangforNbAuth.getInstance().vpnQueryStatus();
            if(authStatus == IVpnDelegate.VPN_STATUS_AUTH_OK)
            {
                VpnLogout();
            }
            return true;
        }
        else if(action.equals("VpnReset")) ////////////////重连
        {
           VpnReset();
           return true;
        }
        else if(action.equals("VpnGPSON")) ////////////////判断 GPS 
        {
           String result = isOPen(this.cordova.getActivity());
           callbackContext.success(result);
           return true;
        }
        else if(action.equals("VpnGPSSet")) ////////////////到 GPS 设置界面
        {
           SetGPSOPen();
           return true;
        }
        else if(action.equals("VpnInputON")) ///////////////键盘是否打开
        {
           String result = GetInputON();
           callbackContext.success(result);
           return true;
        }

        return false;
    }

    public void VpnLogin(String message) {
        CallbackContext.success(message);
    }

    private void VpnReset() {     /////重连VPN，VPN默认初始化成功
       this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
            }
       });
    }

    private void VpnLogout() {   ///////VPN退出
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                SangforNbAuth.getInstance().vpnLogout();
            }
        });
    }

    private void VpnInit() {   //////////////VPN初始化并登陆验证

       final Context cnn = this.cordova.getActivity();
       this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                 try
                 {
                     SangforAuth sfAuth = SangforAuth.getInstance();
                     sfAuth.init(cnn, webInspect.ivg);
                     sfAuth.setLoginParam(AUTH_CONNECT_TIME_OUT, String.valueOf(5));
                 }
                 catch (SFException e) {
                     e.printStackTrace();
                 }
                 // 开始初始化VPN
                 if (initSslVpn() == false) {
                    Toast.makeText(cnn, "VPN初始化失败",3000).show();
                 }
                 doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
            }
       });

    }

   private boolean initSslVpn() {
   		SangforAuth sfAuth = SangforAuth.getInstance();
   		InetAddress mAddr = null;
        try {
            mAddr = InetAddress.getByName("115.236.68.195");
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
   		if (mAddr == null || mAddr.getHostAddress() == null) {
   			return false;
   		}
   		long host = VpnCommon.ipToLong(mAddr.getHostAddress());
   		int port = 443;
   		if (sfAuth.vpnInit(host, port) == false) {
   			return false;
   		}
   		return true;
   	}

   public void doVpnLogin(int authType) {

        boolean ret = false;
        SangforNbAuth sForward = SangforNbAuth.getInstance();
        switch (authType) {
        case IVpnDelegate.AUTH_TYPE_CERTIFICATE:
            sForward.setLoginParam(IVpnDelegate.CERT_PASSWORD, "123456");
            sForward.setLoginParam(IVpnDelegate.CERT_P12_FILE_NAME, "/sdcard/csh/csh.p12");
            ret = sForward.vpnLogin(IVpnDelegate.AUTH_TYPE_CERTIFICATE);
            break;
        case IVpnDelegate.AUTH_TYPE_PASSWORD:
            sForward.setLoginParam(IVpnDelegate.PASSWORD_AUTH_USERNAME, User);
            sForward.setLoginParam(IVpnDelegate.PASSWORD_AUTH_PASSWORD, Pwd);
            ret = sForward.vpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
            break;
        default:
            //Log.w(TAG, "default authType " + authType);
            break;
        }
        if (ret == true) {
          //Toast.makeText(this.cordova.getActivity(), "111",3000).show();
          //Log.i(TAG, "success to call login method");
        } else {
          //Log.i(TAG, "fail to call login method");
        }
   }

    @Override
    public void vpnCallback(int vpnResult, int authType) {}

    @Override
    public void vpnRndCodeCallback(byte[] data) {}

    @Override
    public void reloginCallback(int status, int result) {}

    private String getGateWay(Context context){

        String rtxvalue = "";
        if(isWifi(context))
        {
            try
            {
                wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                dhcpInfo = wifiManager.getDhcpInfo();
                //return "dh_ip:"+FormatIP(dhcpInfo.ipAddress)+"$"+"dh_gateway"+FormatIP(dhcpInfo.gateway);
                rtxvalue = FormatIP(dhcpInfo.gateway);
            }
            catch(Exception ex)
            {
                rtxvalue = "";
            }
        }

        return rtxvalue;
    }

    private  boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    private String FormatIP(int IpAddress) {
        return Formatter.formatIpAddress(IpAddress);
    }

    private String getIp(Context context){
         wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
         dhcpInfo = wifiManager.getDhcpInfo();
         wifiInfo = wifiManager.getConnectionInfo();
         //wifiInfo返回当前的Wi-Fi连接的动态信息
         int ip = wifiInfo.getIpAddress();
         return "wifi_ip:"+FormatIP(ip);
    }

    private String isOPen(final Context context) {
        LocationManager locationManager
                                 = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return "true";
        }
        return "false";
    }

     private void SetGPSOPen() {

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
           this.cordova.getActivity().startActivity(intent);

        } catch(ActivityNotFoundException ex)
        {
           intent.setAction(Settings.ACTION_SETTINGS);
           try {
                 this.cordova.getActivity().startActivity(intent);
           }
           catch (Exception e) {
           }
        }


     }


      private String GetInputON() {

        String result = "false";
        InputMethodManager imm = (InputMethodManager)this.cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();//isOpen若返回true，则表示输入法打开
        if(isOpen)
        {
            //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            result = "true";
        }
        return result;
     }
}
