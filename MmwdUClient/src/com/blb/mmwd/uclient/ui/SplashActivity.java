package com.blb.mmwd.uclient.ui;



import java.util.Set;

import cn.jpush.android.api.JPushInterface;

import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.LocationManager;
import com.blb.mmwd.uclient.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends Activity {
    private final String TAG = "SplashActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
    
    @Override
    protected void onPause() {
        if (isFinishing()) {
            ClientApplication.sSharedInstance.setCurrentActivity(null);
        }
        super.onPause();
        JPushInterface.onPause(this);
    }
    
    @Override
    protected void onDestroy() {
        ClientApplication.sSharedInstance.setCurrentActivity(null);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        ClientApplication.sSharedInstance.setCurrentActivity(this);
        if (LocationManager.getInstance().isLocationAvailable()) {
            Log.d(TAG, "onResume - location is available, enter main");
            HandlerManager.getInstance().sendEmptyMessage(true, HandlerManager.MSG_ENTER_MAIN, 1000);
        }
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    public void onBackPressed() {
        ClientApplication.sSharedInstance.quitApplication();
    }
}
