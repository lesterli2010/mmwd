package com.blb.mmwd.uclient.manager;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import com.blb.mmwd.uclient.rest.api.ClientRestAPI;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

/**
 * Use open source Retrofit for http access http://square.github.io/retrofit/
 * 
 * @author lizhiqiang3
 * 
 */
public class HttpManager {
    private final static String TAG = "HttpManager";

    private final static String SERVER_IMAGE_UTL = "http://121.42.12.140";
    private final static String SERVER_REST_API_URL = "http://121.42.12.140/mmwd";
    public final static String ALIPAY_NOTIFY_URL = "http://121.42.12.140/mmwd/app/alipay/notify";
    
    private static HttpManager sHttpManager;

    public static HttpManager getInstance() {
        if (sHttpManager == null) {
            sHttpManager = new HttpManager();
            sHttpManager.init();
        }
        return sHttpManager;
    }


    private ClientRestAPI mRestAPIClient;
    private RestAdapter mRestAdapter;

    private void init() {
        mRestAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(SERVER_REST_API_URL)
                .build();
        mRestAPIClient = mRestAdapter.create(ClientRestAPI.class);
    }

    public ClientRestAPI getRestAPIClient() {
        return mRestAPIClient;
    }
    
    public String getRealImageUrl(String url) {
        return SERVER_IMAGE_UTL + url;
    }
    
    public String getBaseUrl() {
        return SERVER_IMAGE_UTL;
    }
}
