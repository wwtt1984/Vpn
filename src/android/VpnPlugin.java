package com.vpn.vpn;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import com.sangfor.vpn.IVpnDelegate;
import com.sangfor.vpn.SFException;
import com.sangfor.vpn.auth.SangforNbAuth;
import com.sangfor.vpn.common.VpnCommon;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.widget.Toast;


public class VpnPlugin extends CordovaPlugin implements IVpnDelegate {

    private final static String TAG = VpnPlugin.class.getSimpleName();
    public static String ACTION = "Vpn";
    public static String User = "";
    public static String Pwd = "";

    public boolean execute(String action, JSONArray data,
            CallbackContext callbackContext) throws JSONException {
        if (ACTION.equals(action)) {
            User = data.getString(0);
            Pwd = data.getString(1);
            VpnLogin(data.getString(0),data.getString(1),callbackContext);
        }
        return false;
    }

    public synchronized void vpnLogin(final String user,final String pwd,CallbackContext callbackContext) {

        Toast.makeText(this.cordova.getActivity(), "1234",3000).show();

        final CordovaInterface cordova = this.cordova;
        try {
     		SangforNbAuth.getInstance().init(cordova.getActivity(), this);
     	}
     	catch (SFException e) {
     		e.printStackTrace();
     	}
        if (initSslVpn() == false) {
             Log.e(TAG, "init ssl vpn fail.");
        }
        doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
    }

    private boolean initSslVpn() {
        SangforNbAuth sfAuth = SangforNbAuth.getInstance();

        InetAddress iAddr = null;
        try {
            iAddr = InetAddress.getByName("115.236.68.195");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (iAddr == null || iAddr.getHostAddress() == null) {
            Log.d(TAG, "vpn host error");
            return false;
        }
        long host = VpnCommon.ipToLong(iAddr.getHostAddress());
        int port = 443;

        if (sfAuth.vpnInit(host, port) == false) {
            Log.d(TAG, "vpn init fail, errno is " + sfAuth.vpnGeterr());
            return false;
        }

        Log.d(TAG, "current vpn status is " + sfAuth.vpnQueryStatus());

        return true;
    }

    private void doVpnLogin(int authType) {
        Log.d(TAG, "doVpnLogin authType " + authType);

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
            Log.w(TAG, "default authType " + authType);
            break;
        }

        if (ret == true) {
            Log.i(TAG, "success to call login method");
        } else {
            Log.i(TAG, "fail to call login method");
        }
    }

    public void vpnCallback(int vpnResult, int authType) {
        SangforNbAuth sfAuth = SangforNbAuth.getInstance();
        switch (vpnResult) {
        case IVpnDelegate.RESULT_VPN_INIT_FAIL:
            /**
             * 初始化vpn失败
             */
            Log.i(TAG, "RESULT_VPN_INIT_FAIL, error is " + sfAuth.vpnGeterr());
            break;

        case IVpnDelegate.RESULT_VPN_INIT_SUCCESS:
            /**
             * 初始化vpn成功，接下来就需要开始认证工作了
             */
            Log.i(TAG, "RESULT_VPN_INIT_SUCCESS, current vpn status is " + sfAuth.vpnQueryStatus());
            break;

        case IVpnDelegate.RESULT_VPN_AUTH_FAIL:
            /**
             * 认证失败，有可能是传入参数有误，具体信息可通过sfAuth.vpnGeterr()获取
             */
            Log.i(TAG, "RESULT_VPN_AUTH_FAIL, error is " + sfAuth.vpnGeterr());
            break;

        case IVpnDelegate.RESULT_VPN_AUTH_SUCCESS:
            /**
             * 认证成功，认证成功有两种情况，一种是认证通过，可以使用sslvpn功能了，另一种是前一个认证（如：用户名密码认证）通过，
             * 但需要继续认证（如：需要继续证书认证）
             */
            if (authType == IVpnDelegate.AUTH_TYPE_NONE) {
                Log.i(TAG, "welcom to sangfor sslvpn!");
            } else {
                Log.i(TAG, "auth success, and need next auth, next auth type is " + authType);

                doVpnLogin(authType);
            }
            break;
        case IVpnDelegate.RESULT_VPN_AUTH_LOGOUT:
            /**
             * 主动注销（自己主动调用logout接口）或者被动注销（通过控制台把用户踢掉）均会调用该接口
             */
            Log.i(TAG, "RESULT_VPN_AUTH_LOGOUT");
            break;
        default:
            /**
             * 其它情况，不会发生，如果到该分支说明代码逻辑有误
             */
            Log.i(TAG, "default result, vpn result is " + vpnResult);
            break;
        }
    }
}
