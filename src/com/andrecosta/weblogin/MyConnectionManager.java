package com.andrecosta.weblogin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MyConnectionManager {

    private static final String URL_VISITANTES = "visitantes.petrobras.com.br";
    private static final String[] KNOWN_NETS = {"LCM1104"};
    private static final List<String> KNOWN_NETS_LIST = Arrays.asList(KNOWN_NETS);
    
    private Context context;

    public MyConnectionManager(Context context) {
        this.context = context;
    }
    
    public boolean isConnectedToKnownWifi() {
        Log.d(MainActivity.LOG_TAG, "Checking wifi");
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo != null) {
            Log.d(MainActivity.LOG_TAG, "\tWiFiInfo:" + wifiInfo.toString());
            return checkNetworkName(wifiInfo);
        }
        return false;
    }
    
    private WifiInfo getWifiInfo() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return wifiManager.getConnectionInfo();
        }
        return null;
    }
    
    private boolean checkNetworkName(WifiInfo wifiInfo) {
        return KNOWN_NETS_LIST.contains(wifiInfo.getSSID());
    }

    public boolean isNetConnectionPossible() {
        Log.d(MainActivity.LOG_TAG, "Checking network...");
        try {
            String responseContent = makeTestHttpRequest();
            Log.d(MainActivity.LOG_TAG, "\tResponse received");
            return !responseContent.contains(URL_VISITANTES);
        } catch (Exception e) {
            Log.d(MainActivity.LOG_TAG, "\tError received:" + e.getMessage());
        }
        return false;
    }

    public String getSSDIName() {
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        }
        return "";
    }

    private String makeTestHttpRequest() throws IOException, ClientProtocolException {
        HttpGet httpGet = new HttpGet("http://www.google.com");
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 1000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 1000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String result = parseResponse(entity);
        return result;
    }

    private String parseResponse(HttpEntity entity) throws IOException {
        InputStream is = entity.getContent();
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }     
        String result = builder.toString();
        return result;
    }
    
    public void authenticate() throws AuthenticationException {
        String user = SettingsUtil.getUser(context);
        String password = SettingsUtil.getPassword(context);
        makeAuthenticationRequest(user, password);
    }

    public void makeAuthenticationRequest(String user, String password) throws AuthenticationException {
        try {
            HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            DefaultHttpClient client = new DefaultHttpClient();

            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", socketFactory, 443));
            SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
            DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

            // Set verifier     
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);            
            
            HttpGet get = new HttpGet("https://visitantes.petrobras.com.br/login.html?username=" + user + "&password=" + password + "&buttonClicked=4");
            HttpResponse response = httpClient.execute(get);

            String result = parseResponse(response.getEntity());
            Log.d(MainActivity.LOG_TAG, "auth response:" + result);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Exception:" + e.getMessage(), e);
            throw new AuthenticationException();
        }        
        if (!isNetConnectionPossible()) {
            throw new AuthenticationException();
        }
    }

}
