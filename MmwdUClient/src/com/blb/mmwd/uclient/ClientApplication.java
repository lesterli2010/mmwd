/**
 * 
 */
package com.blb.mmwd.uclient;

import cn.jpush.android.api.JPushInterface;

import com.blb.mmwd.uclient.manager.ShippingAddressManager;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.manager.LocationManager;
import com.blb.mmwd.uclient.manager.UpgradeManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * @author lizhiqiang3
 * 
 */
public class ClientApplication extends Application implements
        Thread.UncaughtExceptionHandler {
    private static final String TAG = "UClientApplication";
    private Activity mCurrentActivity;
    public static ClientApplication sSharedInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        sSharedInstance = this;

        if (isMainProcess()) {
            
            LocationManager.getInstance().init(this);
            HandlerManager.getInstance().sendEmptyMessage(false,
                    HandlerManager.MSG_START_LOCATION, 0);
            // Init below
            HttpManager.getInstance();
            ConfigManager.getInstance();
            UpgradeManager.getInstance();
            CartManager.getInstance();
            ShippingAddressManager.getInstance();
            
            // Initialize UIL Universial Image Loader
            ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)  
            .writeDebugLogs() //打印log信息  
            .build();
            //Initialize ImageLoader with configuration.  
            ImageLoader.getInstance().init(configuration);
            
            JPushInterface.setDebugMode(true);  // 设置开启日志,发布时请关闭日志
            JPushInterface.init(this);             // 初始化 JPush
        }
    }

    /**
     * Check whether current process is main process
     * which has name same with package name.
     * 
     * Because Baidu location SDK will start a process ":remote",
     * I don't want to initialize data in Baidu SDK's process.
     * @return
     */
    private boolean isMainProcess() {
        
        // Main process name is package name
        String mainProcessName = getClass().getPackage().getName();
        
        String curProcessName = null;
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                curProcessName = appProcess.processName;
                break;
            }
        }
        Log.d(TAG, "mainProcessName: " + mainProcessName + ", curProcessName:" + curProcessName);
        if (mainProcessName.equals(curProcessName)) {
            return true;
        }
        return false;
    }
    
    public void quitApplication() {
        ConfigManager.getInstance().userLogout(null, null);
        HandlerManager.getInstance().quit();
        
        Thread.setDefaultUncaughtExceptionHandler(this);
        /*
         * Notify the system to finalize and collect all objects of the
         * application on exit so that the process running the application can
         * be killed by the system without causing issues. NOTE: If this is set
         * to true then the process will not be killed until all of its threads
         * have closed.
         */
        System.runFinalization();
        /*
         * * Force the system to close the application down completely instead
         * of * retaining it in the background. The process that runs the
         * application * will be killed. The application will be completely
         * created as a new * application in a new process if the user starts
         * the application * again.
         */
        System.exit(0);
    }

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        System.exit(0);
    }
    
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }
    
    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }
}
