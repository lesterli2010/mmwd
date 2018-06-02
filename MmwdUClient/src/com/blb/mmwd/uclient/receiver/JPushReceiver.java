package com.blb.mmwd.uclient.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import com.blb.mmwd.uclient.ui.CommentActivity;
import com.blb.mmwd.uclient.ui.MainActivity;
import com.blb.mmwd.uclient.ui.OrderDetailActivity;
import com.blb.mmwd.uclient.util.Util;
import com.google.gson.Gson;

import cn.jpush.android.api.JPushInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class JPushReceiver extends BroadcastReceiver {
    private final static boolean DEBUG = true;
    private final String TAG = "JPushReceiver";
    
    private static class ExtraData {
        public String msg_type;
        public String id;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        
        if (DEBUG)
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
        
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...
                        
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            if (DEBUG)
            Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            processCustomMessage(context, bundle);
        
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {

            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            
            
            if (DEBUG)
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
            
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {

            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            if (DEBUG)
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知, extras:" + extras);
            
            ExtraData data = new Gson().fromJson(extras, ExtraData.class);
            
            if (DEBUG)
                Log.d(TAG, "[MyReceiver] 用户点击打开了通知, extras:" + extras + ", data.id:" + data.id);

            // 打开自定义的Activity
            Intent i = null;

            if ("order".equals(data.msg_type)) {
                i = new Intent(context, OrderDetailActivity.class);
                i.putExtra(Util.EXTRA_ID, Integer.parseInt(data.id));
            } else if ("reply".equals(data.msg_type)) {
                i = new Intent(context, CommentActivity.class);
            }
            
            if (i != null) {
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
            
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            if (DEBUG)
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
            
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            if (DEBUG)
            Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
            if (DEBUG)
            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            }else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } 
            else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }
    
    //send msg to MainActivity
    private void processCustomMessage(Context context, Bundle bundle) {
        
        //if (MainActivity.isForeground) {
        /*
            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
            msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
            if (!ExampleUtil.isEmpty(extras)) {
                try {
                    JSONObject extraJson = new JSONObject(extras);
                    if (null != extraJson && extraJson.length() > 0) {
                        msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
                    }
                } catch (JSONException e) {

                }

            }
            context.sendBroadcast(msgIntent);
            */
       // }
    
    }
}
