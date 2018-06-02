package com.blb.mmwd.uclient.manager;


import com.blb.mmwd.uclient.util.Util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class HandlerManager {
    private static final String TAG = "HandlerManager";
    private static HandlerManager sHandlerManager;

    private Handler mUiHandler;
    private Handler mBgHandler;
    private HandlerThread mBgHandlerThread;

    // Define msg type - UI
    public final static int MSG_ENTER_MAIN = 1;

    // Define msg type - non UI
    public final static int MSG_START_LOCATION = 1; // start location position
    public final static int MSG_CHECK_LOCATION_TIMEOUT = 2; // check if location
                                                            // got    
    
    public final static int MSG_LOCATION_FINISHED = 3; // location finished
    public final static int MSG_INIT_CONFIG = 4;
    public final static int MSG_INIT_SHIPPING_ADDR = 5;
    public final static int MSG_INIT_CART = 6;
    
    public static HandlerManager getInstance() {
        if (sHandlerManager == null) {
            sHandlerManager = new HandlerManager();
            sHandlerManager.init();
        }
        return sHandlerManager;
    }

    public Handler getUiHandler() {
        return mUiHandler;
    }

    public Handler getBgHandler() {
        return mBgHandler;
    }

    public boolean sendEmptyMessage(boolean isUi, int what, long delay) {
        Handler handler = isUi ? mUiHandler : mBgHandler;
        synchronized (this) {
            if (!handler.hasMessages(what)) {
                return handler.sendMessageDelayed(handler.obtainMessage(what),
                        delay);
            }
        }
        return false;
    }
    
    public void removeMessage(boolean isUi, int what) {
        Handler handler = isUi ? mUiHandler : mBgHandler;
        handler.removeMessages(what);
    }

    public void quit() {
        if (mBgHandlerThread != null) {
            mBgHandlerThread.quit();
        }
    }
    
    private void init() {
        mUiHandler = new UiHandler();
        // Create non UI background handler
        mBgHandlerThread = new HandlerThread("BgHandlerThread");
        mBgHandlerThread.start();
        Log.d(TAG, "HandlerManager init:" + mBgHandlerThread.getId() + ", " + hashCode());
        mBgHandler = new BgHandler(mBgHandlerThread.getLooper());
    }

    private static class UiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "UiHandler:" + msg.what + ","
                    + Thread.currentThread().getId());
            switch (msg.what) {
            case MSG_ENTER_MAIN:
                Util.startMainActivity();
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    }

    private static class BgHandler extends Handler {
        public BgHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "BgHandler:" + msg.what + ","
                    + Thread.currentThread().getId()
                    + "hash code:" + HandlerManager.getInstance().hashCode());
            switch (msg.what) {
            case MSG_START_LOCATION:
                if (Util.isNetworkAvailable()) {
                    // network is ok, start location service
                    LocationManager.getInstance().startLocation();
                    HandlerManager.getInstance().sendEmptyMessage(false, MSG_CHECK_LOCATION_TIMEOUT, Util.LOCATION_TIMEOUT_INTERVAL);
                } else {
                    HandlerManager.getInstance().sendEmptyMessage(false, MSG_LOCATION_FINISHED, 800);
                }
                /* for demo
                LocationManager.getInstance().setManualLocation("ÇàµºÊÐ", 136, 1289, 6);
                HandlerManager.getInstance().sendEmptyMessage(false, MSG_LOCATION_FINISHED, 1000);
                */
                break;
            case MSG_CHECK_LOCATION_TIMEOUT:
                if (!LocationManager.getInstance().isLocationAvailable()) {
                    // Stop
                    LocationManager.getInstance().stopLocation();
                    HandlerManager.getInstance().sendEmptyMessage(false, MSG_LOCATION_FINISHED, 0);
                }
                break;
            case MSG_LOCATION_FINISHED:
                HandlerManager.getInstance().removeMessage(false, MSG_CHECK_LOCATION_TIMEOUT);
                if (ConfigManager.getInstance().isMainActivityStarted()) {
                    // Main activity started, send broadcase
                    Util.sendBroadcast(null, Util.INTENT_ACTION_REFRESH_DATA);
                } else {
                    HandlerManager.getInstance().sendEmptyMessage(true, MSG_ENTER_MAIN, 0);
                }
                break;
            case MSG_INIT_CONFIG:
                ConfigManager.getInstance().init();
                break;
            case MSG_INIT_SHIPPING_ADDR:
                ShippingAddressManager.getInstance().init();
                break;
            case MSG_INIT_CART:
                CartManager.getInstance().init();
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    }
}
