package com.blb.mmwd.uclient.util;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.ResponseStrValue;
import com.blb.mmwd.uclient.ui.FteActivity;
import com.blb.mmwd.uclient.ui.MainActivity;
import com.blb.mmwd.uclient.ui.UserLoginActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Utility
 * 
 * @author lizhiqiang3
 * 
 */
public final class Util {
    private final static String TAG = "Util";
    public final static SimpleDateFormat sDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat sDateFormat_HM = new SimpleDateFormat(
            "HH:mm");
    public final static SimpleDateFormat sDateFormat_YMDHM = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");
    
    public final static SimpleDateFormat sDateFormat_MDHM = new SimpleDateFormat(
            "MM-dd HH:mm");

    // Define const timers
    // public final static String EXTRA_SELECT_LOC_TYPE =
    // "extra_select_loc_type";
    // public final static int EXTRA_SELECT_LOC_TYPE_CITY = 0;
    // public final static int EXTRA_SELECT_LOC_TYPE_ZONE = 1;

    // public final static String EXTRA_SELECTED_ID = "extra_selected_id";
    // for select list type: area or

    // @string/order_status
    public static enum OrderState {
        INIT, SUBMITTED, PAIED, CONFIRMED, COOKING_FINISH, TRANSPORTING, FINISHED, CLOSED, UNKNOWN // IMPORTANT: UNKNOWN must be the last
    }

    public static int[] sOrderStatusImg = { R.drawable.ic_order_status_init,
            R.drawable.ic_order_status_init,
            R.drawable.ic_order_status_paied,
            R.drawable.ic_order_status_confirmed,
            R.drawable.ic_order_status_confirmed,
            R.drawable.ic_order_status_transporting,
            R.drawable.ic_order_status_finished,
            R.drawable.ic_order_status_finished,
            R.drawable.ic_order_status_init};

    
    // 货到付款，支付宝
    public static enum PaymentType {
        ALIPAY, COD, UNKNOWN // IMPORTANT: UNKNOWN must be the last
    }

    public final static long VERIFY_CODE_INTERVAL = 60 * 1000;
    public final static int MAX_ITEM_NUM_ONE_PAGE = 10; // 10 items for each
                                                        // page
    public final static long REFRESH_TIME_PERIOD = 3 * 60 * 1000; // 3 minutes
    public final static long REFRESH_MY_ORDER_TIME_PERIOD = 30 * 1000; // 1 minute
    // food type, used in cart item

    // Below is main food provided by mmwd

    public final static String EXTRA_SELECT_LIST_TYPE = "extra_select_list_type";
    public final static String EXTRA_CONTENT_LIST_TYPE = "extra_content_list_type";
    public final static String EXTRA_ID = "extra_id";
    public final static String EXTRA_NAME = "extra_name";
    public final static String EXTRA_CITY_NAME = "extra_city_name";
    public final static String EXTRA_PROVINCE_NAME = "extra_province_name";
    public final static String EXTRA_DISTRICT_ID = "extra_district_id";
    public final static String EXTRA_ZONE_ID = "extra_zone_id";

    public final static int ACT_REQ_CODE_SELECT_ADDR = 1;
    public final static int ACT_RES_CODE_SELECT_ADDR_SUCC = 1;

    public final static String INTENT_ACTION_REFRESH_DATA = "com.blb.mmwd.uclient.REFRESH_DATA";
    
//    public final static int MAX_FOOD_IN_SHOP = 2;

    public final static long _1K = 1024;
    public final static long _1M = 1024 * 1024;
    public final static long _1G = 1024 * 1024 * 1024;
    public final static DecimalFormat sNumberFormat = new DecimalFormat("#.##");
    
    /**
     * 在菜品或mmshop未 active时调整alpha
     */
    public final static float INACTIVE_ALPHA = 0.6f;

    private final static int TOTAL_SECONDS_ONE_DAY = 24 * 60 * 60;
    
    // Define selection type in home fragment: zone, sequqnce, food. Three
    // filter buttons
    public static enum SelectionType {
        SELECT_TYPE_NONE, //
        SELECT_TYPE_ZONE, // 选择区域
        SELECT_TYPE_SEQUENCE, // 选择排序
        SELECT_TYPE_FILTER // 筛选
    }

    public static enum SelectionTypeFilter {
        SELECT_TYPE_FILTER_PRICE, //
        SELECT_TYPE_FILTER_TYPE
    }

    // Content List: 最火妈妈，最热菜品，妈妈详情页中已上架菜品，妈妈详情页中未上架菜品
    public static enum ContentListType {
        ALL_HOT_MM_SHOP, ALL_HOT_FOOD, MM_SHOP_ACTIVE_FOOD, MM_SHOP_INACTIVE_FOOD
    }
    
    public final static String EXTRA_CURRENT_TAB = "extra_current_tab";

    public final static long QUIT_APP_INTERVAL = 2000;
    public final static long LOCATION_TIMEOUT_INTERVAL = 8000;
    private final static String PREF_NEED_FTE = "pref_need_fte";

    public final static String MISC_USER_NAME = "username";
    public final static String MISC_PASSWORD = "password";

    public static DisplayImageOptions sMmShopImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_mm_shop)
            .showImageOnFail(R.drawable.ic_mm_shop).cacheInMemory(true)
            .cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();

    public static DisplayImageOptions sFoodImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_food)
            .showImageOnFail(R.drawable.ic_food)
            .cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();

    public static DisplayImageOptions sMmwdImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_launcher)
            .showImageOnFail(R.drawable.ic_launcher)
            .cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();;

    public static boolean needFte(Context context) {
        return false; // not need FTE so far;
        /*
        SharedPreferences preference = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean fte = preference.getBoolean(PREF_NEED_FTE, true);
        return fte;
        */
    }

    public static void updateFteFlag(Context context, boolean need) {
        SharedPreferences preference = PreferenceManager
                .getDefaultSharedPreferences(context);
        preference.edit().putBoolean(PREF_NEED_FTE, need).commit();
    }

    /**
     * 
     * @param context
     * @param start whether need to start login activity
     * @return
     */
    public static boolean checkUserLogin(Context context, boolean startActivity) {
        return checkUserLogin(context, startActivity, !startActivity);
    }
    
    /**
     * 
     * @param context
     * @param start whether need to start login activity
     * @return false: not login; true: logined
     */
    public static boolean checkUserLogin(Context context, boolean startActivity, boolean sendToast) {
        if (!ConfigManager.getInstance().isUserLogined()) {
            if (startActivity) {
                startActivity(context, UserLoginActivity.class);
            } else if (sendToast) {
                Util.sendToast(R.string.msg_not_logined);
            }
            return false;
        }
        return true;
    }

    public static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        // intent.putExtra(Util.EXTRA_SELECT_LOC_TYPE, selectLocType);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Intent intent) {
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Class<?> cls, int id) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }
    
    public static void startActivity(Context context, Class<?> cls, String id) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }

    public static void callServiceNumber(final Context context) {
        String number = ConfigManager.getInstance().getServiceNumber();
        if (number == null) {
            HttpManager.getInstance().getRestAPIClient().getServiceNumber(new HttpCallback<ResponseStrValue>(new Runnable() {

                @Override
                public void run() {
                    callServiceNumber(context, ConfigManager.getInstance().getDefaultServiceNumber());
                }
                
            }) {

                @Override
                protected boolean processData(ResponseStrValue t) {
                    if (TextUtils.isEmpty(t.value)) {
                        return false;
                    } 
                        
                    ConfigManager.getInstance().setServiceNumber(t.value);
                    callServiceNumber(context, t.value);
                    return true;
                }
                
            });
        } else {
            callServiceNumber(context, number);
        }
    }
    
    private static void callServiceNumber(final Context context, final String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
                + number));
        context.startActivity(intent);
    }
    
    public static void sendBroadcast(String action) {
        sendBroadcast(null, action);
    }

    public static void sendBroadcast(Context context, String action) {
        if (context == null) {
            context = ClientApplication.sSharedInstance;
        }

        context.sendBroadcast(new Intent(action));
    }

    /**
     * Convert file size to 1M, 1K, etc
     * 
     * @param size
     * @return
     */
    public static String formatSize(long size) {
        String fileSize = null;
        if (size < _1K) {
            fileSize = size + "B";
        } else if (size >= _1K && size < _1M) {
            fileSize = sNumberFormat.format((float) (size / _1K)) + "K";
        } else if (size >= _1M && size < _1G) {
            fileSize = sNumberFormat.format((float) (size / _1M)) + "M";
        } else {
            fileSize = sNumberFormat.format((float) (size / _1G)) + "G";
        }
        return fileSize;
    }

    /**
     * Is network available
     * 
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) ClientApplication.sSharedInstance
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null ? info.isAvailable() : false;
        }

        return false;
    }

    /**
     * Show view 1 and hide view 2
     * 
     * @param v1
     * @param v2
     */
    public static void showHideView(View v1, View v2) {
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
    }

    public static String getString(int id) {
        return ClientApplication.sSharedInstance.getResources().getString(id);
    }

    public static String getString(int id, Object... formatArgs) {
        return ClientApplication.sSharedInstance.getResources().getString(id,
                formatArgs);
    }

    public static void startMainActivity() {
        Activity activity = ClientApplication.sSharedInstance
                .getCurrentActivity();
        /*
         * Intent intent = new Intent(); if (SplashActivity.sSharedInstance !=
         * null) { context = SplashActivity.sSharedInstance; Log.e(TAG,
         * "SplashActivity exist"); SplashActivity.sSharedInstance.finish(); }
         * else { context = UClientApplication.sSharedInstance;
         * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); Log.e(TAG,
         * "SplashActivity doesn't exist"); }
         */
        if (activity != null) {
            Intent intent = new Intent();
            if (Util.needFte(activity)) {
                intent.setClass(activity, FteActivity.class);
                Util.updateFteFlag(activity, false);
            } else {
                intent.setClass(activity, MainActivity.class);
            }
            Log.d(TAG, "Start Main Activity");
            activity.startActivity(intent);
            activity.finish();
        } else {
            Log.e(TAG, "SplashActivity doesn't exist");
        }
    }

    /*
     * get start second of today
     */
    public static long getStartSecondOfToday() {
        long now = getCurrentSecond();
        return now - (getCurrentSecond() % TOTAL_SECONDS_ONE_DAY);
    }

    public static long getCurrentSecond() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getMoneyText(float f) {
        return ClientApplication.sSharedInstance.getString(R.string.money,
                sNumberFormat.format(f));
    }

    
    public static String getStatusText(OrderState status) {
        return ClientApplication.sSharedInstance.getResources().getStringArray(R.array.order_status)[status
                .ordinal()];
    }

    public static int getStatusImgRes(OrderState status) {
        return sOrderStatusImg[status.ordinal()];
    }

    // add 'x' before amount
    public static String getFoodCountText(int amount) {
        return "x" + amount;
    }

    public static void sendToast(final String msg) {
        HandlerManager.getInstance().getUiHandler().post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ClientApplication.sSharedInstance, msg,
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        
        if(activity != null && activity.getCurrentFocus()!=null)  
        {  
            ((InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE))  
            .hideSoftInputFromWindow(activity.getCurrentFocus()  
                    .getWindowToken(),  
                    InputMethodManager.HIDE_NOT_ALWAYS);   
        }
        /*
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);*/
    }

    public static void sendToast(final int msg) {
        sendToast(Util.getString(msg));
    }

    public final static String MD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getStarImgRes(int star) {
        switch (star) {
        case 0:
            return 0;
        case 1:
            return R.drawable.star_1;
        case 2:
            return R.drawable.star_2;
        case 3:
            return R.drawable.star_3;
        case 4:
            return R.drawable.star_4;
        case 5:
            return R.drawable.star_5;
        default:
            return R.drawable.star_5;
        }
    }
    
    /*
     * check if it is valid phone number
     */
    public static boolean validPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        Pattern p = Pattern.compile("^1[3|4|5|8][0-9]{9}$");
        Matcher m = p.matcher(phone); 
        return m.matches();
    }
    
    public static boolean validEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher m = p.matcher(email); 
        return m.matches();
    }
    /**
     * 
     */
    public static boolean validUserName(String userName) {
        return true;
    }
    
    /**
     * 
     */
    
    public static boolean validPass(String pass) {
        return true;
    }
    
    
    private static final String ALGORITHM = "RSA";

    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static String sign(String content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
                    Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance(ALGORITHM, "BC");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                    .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));

            byte[] signed = signature.sign();

            return Base64.encode(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * convert string format ms (e.g. 1427859547000) to readable string
     * 
     */
    public static String toReadableDate(String ms, SimpleDateFormat format) {
        if (ms == null) {
            return null;
        }
        try {
            Date d = new Date(Long.parseLong(ms));
            if (d != null) {
                return format.format(d);
            }
        } catch (Exception e){}
        return null;
    }
    
}
