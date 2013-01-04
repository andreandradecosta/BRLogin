package com.andrecosta.weblogin;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ConnectionHelper {
    private static final String KNOWN_NETS = "LCM1104";

    private Context context;

    public ConnectionHelper(Context context) {
        this.context = context;
    }
    
    public boolean isConnectedToKnownWifi() {
        Log.d(MainActivity.LOG_TAG, "isConnectedToKnownWifi");
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo != null) {
            Log.d(MainActivity.LOG_TAG, "   > WiFiInfo:" + wifiInfo.toString());
            return isNetNameKnown(wifiInfo);
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
    
    private boolean isNetNameKnown(WifiInfo wifiInfo) {
        String ssid = wifiInfo.getSSID();
        ssid = ssid.replace("\"", "");
        return KNOWN_NETS.contains(ssid);
    }

    public String getSSDIName() {
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        }
        return "";
    }

    public boolean isNetConnectionPossible() {
        Log.d(MainActivity.LOG_TAG, "isNetConnectionPossible");
        try {
            Document response = makeTestHttpRequest();
            Log.d(MainActivity.LOG_TAG, "   > Response received");
            Elements result = response.select("title");
            if (!result.isEmpty()) {
                boolean responseOK = result.first().text().contains("Google");
                Log.d(MainActivity.LOG_TAG, "   > " + responseOK);
                return responseOK;
            }
        } catch (Exception e) {
            Log.d(MainActivity.LOG_TAG, "   > " + e.getMessage());
        }
        Log.d(MainActivity.LOG_TAG, "   > false");
        return false;
    }

    private Document makeTestHttpRequest() throws Exception {
        HttpGet httpGet = new HttpGet("http://www.google.com");
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 2000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 2000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);
        return Jsoup.parse(result);
    }

    
    public void authenticate()  {
        Log.d(MainActivity.LOG_TAG, "authenticate");
        try {
            Document response = makeTestHttpRequest();
            Elements result = extractFormAction(response);
            if (!result.isEmpty()) {
                Element form = result.first();
                Log.d(MainActivity.LOG_TAG, "   > " + form.attr("action"));
                String user = SettingsUtil.getUser(context);
                String password = SettingsUtil.getPassword(context);
                makeAuthenticationRequest(form, user, password);
                Log.d(MainActivity.LOG_TAG, "   > auth complete");
            }
       } catch (Exception e) {
           Log.d(MainActivity.LOG_TAG, "   > " + e.getMessage());
        }
    }


    private Elements extractFormAction(Document response) {
        return response.select("form[action*=petrobras]");
    }
    
    private void makeAuthenticationRequest(Element form, String user, String password) throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = extractURL(form);
        HttpPost post = new HttpPost(url);
        List<NameValuePair> nvps = buildParameters(form, user, password);
        post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        httpClient.execute(post);
//        String result = EntityUtils.toString(response.getEntity());
    }

        

    private String extractURL(Element form) {
        return form.attr("action");
    }
    
    private List<NameValuePair> buildParameters(Element form, String user, String password) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Elements inputs = form.select("input");
        for (Element element : inputs) {
            if (element.attr("type").equalsIgnoreCase("text")) {
                nvps.add(new BasicNameValuePair(element.attr("name"), user));
            } else  if (element.attr("type").equalsIgnoreCase("password")) {
                nvps.add(new BasicNameValuePair(element.attr("name"), password));
            } else if (!element.attr("type").equalsIgnoreCase("button")) {
                nvps.add(new BasicNameValuePair(element.attr("name"), element.attr("value")));
            }
        }
        return nvps;
    }

    
}
