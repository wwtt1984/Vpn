package com.vpn.vpn;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import android.widget.Toast;
import com.mycompany.webInspect.webInspect;

import java.io.IOException;
import java.io.InputStream;

import com.sangfor.vpn.IVpnDelegate;
import com.sangfor.vpn.SFException;
import com.sangfor.vpn.auth.SangforNbAuth;
import com.sangfor.vpn.common.VpnCommon;
import android.app.Activity;
import android.view.View;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.net.SocketException;

import android.content.Context;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Formatter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

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
            //Toast.makeText(this.cordova.getActivity(), ""+authStatus,3000).show();
            switch (authStatus) {
                case IVpnDelegate.VPN_STATUS_UNSTART:
                this.VpnInit();///初始化VPN
                break;
                case IVpnDelegate.VPN_STATUS_INIT_OK:
                VpnReset();///重新登录VPN
                break;
                case IVpnDelegate.VPN_STATUS_OK:////VPN连接正常
                callbackContext.success("true");
                break;
                case IVpnDelegate.VPN_STATUS_RELOGIN:////VPN连接正常
                //Toast.makeText(this.cordova.getActivity(), "vpn状态抓取代码编号1",3000).show();
                break;
                case IVpnDelegate.VPN_STATUS_INITING:////VPN连接正常
                //Toast.makeText(this.cordova.getActivity(), "vpn状态抓取代码编号2",3000).show();
                break;
                case IVpnDelegate.VPN_STATUS_LOGINING:////VPN连接正常
                //Toast.makeText(this.cordova.getActivity(), "vpn状态抓取代码编号3",3000).show();
                break;
                case IVpnDelegate.VPN_STATUS_EXITING:////VPN连接正常
                //Toast.makeText(this.cordova.getActivity(), "vpn状态抓取代码编号4",3000).show();
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

                case IVpnDelegate.VPN_STATUS_OK:////VPN连接正常
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
            if(authStatus == IVpnDelegate.VPN_STATUS_OK)
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
                     SangforNbAuth.getInstance().init(cnn,  webInspect.ivg);
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

    /**
     * 开始初始化VPN，该初始化为异步接口，后续动作通过回调函数通知结果
     *
     * @return 成功返回true，失败返回false，一般情况下返回true
     */
    private boolean initSslVpn() {
        SangforNbAuth sfAuth = SangforNbAuth.getInstance();
        InetAddress iAddr = null;
        try {
            iAddr = InetAddress.getByName("115.236.68.195");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (iAddr == null || iAddr.getHostAddress() == null) {
            //Log.d(TAG, "vpn host error");
            return false;
        }
        long host = VpnCommon.ipToLong(iAddr.getHostAddress());
        int port = 443;
        if (sfAuth.vpnInit(host, port) == false) {
            //Log.d(TAG, "vpn init fail, errno is " + sfAuth.vpnGeterr());
            return false;
        }
        //Log.d(TAG, "current vpn status is " + sfAuth.vpnQueryStatus());
        return true;
    }

    /**
     * 处理认证，通过传入认证类型（需要的话可以改变该接口传入一个hashmap的参数用户传入认证参数）.
     * 也可以一次性把认证参数设入，这样就如果认证参数全满足的话就可以一次性认证通过，可见下面屏蔽代码
     *
     * @param authType
     *            认证类型
     */
    private void doVpnLogin(int authType) {

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

    public void vpnCallback(int vpnResult, int authType) { //////////////不能删除，只作为覆盖，不执行操作

    }

    public void vpnRndCodeCallback(byte[] data) {

    }

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
}
