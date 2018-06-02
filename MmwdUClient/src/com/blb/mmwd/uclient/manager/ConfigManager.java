package com.blb.mmwd.uclient.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.db.MmwdContentContract.Misc;
import com.blb.mmwd.uclient.manager.LocationManager.MmwdLocation;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.ConfigItem;
import com.blb.mmwd.uclient.rest.model.SelectionConfigData;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.rest.model.response.Cities;
import com.blb.mmwd.uclient.rest.model.response.Communities;
import com.blb.mmwd.uclient.rest.model.response.Districts;
import com.blb.mmwd.uclient.rest.model.response.Login;
import com.blb.mmwd.uclient.rest.model.response.Provinces;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;
import com.blb.mmwd.uclient.rest.model.response.ResponseNVValue;
import com.blb.mmwd.uclient.rest.model.response.ResponseStrValue;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.ContentListType;
import com.blb.mmwd.uclient.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ConfigManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "ConfigManager";
    private static ConfigManager sConfigManager;
    private Context mContext;
    private ContentResolver mContentResolver;
    private int mMaxSelectionListHeight = 0;
    private int mMaxDialogWidth = 0;
    private User mCurrentUser; // user information, it may store in db
    private int mCurrentVersionCode = 0;
    private String mCurrentVersionName;
    private boolean mMainActivityStarted;
 //   private MmwdLocation mLastLocation = new MmwdLocation();
    private Map<String, String> mMiscAttrs = new HashMap<String, String>();
    
    private String mServiceNumber;
    private String mDefaultServiceNumber = "18661640013";
    
    private float mScoreExchangeRate = 0L; // How much  (.yuan) for 1 score
    private Set<Integer> mLikedFoods = new HashSet<Integer>();

    // selection list data that will be in selection fragment
    // The data shall be downloaded from server. Put it as local data for now
    private List<ConfigItem> mSelectableProvinces = new ArrayList<ConfigItem>();

    // Provice-Id ---- City list (省-城市)
    // private Map<Integer, ArrayList<ConfigItem>> mSelectableCities = new
    // HashMap<Integer, ArrayList<ConfigItem>>();
    // City-Id ---- District list (城市-区县)
    private Map<Integer, ArrayList<ConfigItem>> mCityDistricts = new HashMap<Integer, ArrayList<ConfigItem>>();
    // District
    private Map<Integer, ArrayList<ConfigItem>> mDistrictZones = new HashMap<Integer, ArrayList<ConfigItem>>();

    // Zone-Id ---- Selectable Living communities (妈妈圈-配送小区)
    private Map<Integer, ArrayList<ConfigItem>> mCommunities = new HashMap<Integer, ArrayList<ConfigItem>>();

    // crossArea-id -- district 大圈 - 行政区
    private Map<Integer, ArrayList<ConfigItem>> mCrossAreaDistricts  = new HashMap<Integer, ArrayList<ConfigItem>>();
    // Mapping between zone id and cross area id
    private Map<Integer, ConfigItem> mZoneCrossAreas = new HashMap<Integer, ConfigItem>();
    
    // Mappgin between zone id and district name
    private Map<Integer, String> mZoneDistrictNames = new HashMap<Integer, String>();
    
    private List<ConfigItem> mHotCities = new ArrayList<ConfigItem>();
    private List<ConfigItem> mSelectableZones = new ArrayList<ConfigItem>();
    private List<ConfigItem> mSelectableSequences = new ArrayList<ConfigItem>();

    // Map cityname-cityid, to get cityid quickly
    private Map<String, Integer> mCityNameIdMap = new HashMap<String, Integer>();
    /*
     * private List<ConfigItem> mSelectablePrices = new ArrayList<ConfigItem>();
     * private List<ConfigItem> mSelectableTypes = new ArrayList<ConfigItem>();
     */
    private ConfigStatus mConfigStatus = ConfigStatus.NO_NETWORK;
    // Store the user's current selection config
    private SelectionConfigData mConfigData = new SelectionConfigData();

    public static enum ConfigStatus {
        NO_NETWORK, // No network, location not start yet
        NO_LOCATION, // has network, but location failed
        LOCATION_OK, // location success
        CONFIG_DATA_OK // get selectable zones, can query data of mm shop
    }

    public static ConfigManager getInstance() {
        if (sConfigManager == null) {
            sConfigManager = new ConfigManager();
            HandlerManager.getInstance().sendEmptyMessage(false,
                    HandlerManager.MSG_INIT_CONFIG, 0);
        }
        return sConfigManager;
    }

    public void init() {
        // Query db misc to get data
        Log.d(TAG, "ConfigManager init");
        mContext = ClientApplication.sSharedInstance;
        mContentResolver = mContext.getContentResolver();
        Cursor c = mContentResolver.query(Misc.CONTENT_URI, null, null, null,
                null);

        if (c == null) {
            return;
        }
        String name;
        String value;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            name = c.getString(c.getColumnIndex(Misc.COLUMN_NAME));
            value = c.getString(c.getColumnIndex(Misc.COLUMN_VALUE));
            Log.d(TAG, " ConfigManager constructor, init misc, name:" + name
                    + ", value:" + value);
            mMiscAttrs.put(name, value);
        }

        c.close();
        name = mMiscAttrs.get(Util.MISC_USER_NAME);
        String pass = mMiscAttrs.get(Util.MISC_PASSWORD);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass)) {
            mCurrentUser = new User(name, pass);
            userLoginUserName(name, pass, null, null);
        }

        // auto login

        // init location data, provinces, districts, zones, etc.
        initLocationData();
        // get nearby zones:

        Log.d(TAG,
                "HandlerManager ConfigManager constructor:" + this.hashCode());

        // mSelectableZones.put("青岛市", zoneList);
        // mSelectableZones.put("北京市", zoneList2);

        // ArrayList<AttrIdName> seqs = new ArrayList<AttrIdName>();
        // keep it
        mSelectableSequences.add(new ConfigItem(ContentListType.ALL_HOT_MM_SHOP
                .ordinal(), "最火妈妈"));
        mSelectableSequences.add(new ConfigItem(
                ContentListType.ALL_HOT_FOOD.ordinal(), "最热菜品"));

    }

    private void setJPushAlias() {
        JPushInterface.setAlias(ClientApplication.sSharedInstance, 
                getCurrentUserName(), new TagAliasCallback() {

            @Override
            public void gotResult(int arg0, String arg1,
                    Set<String> arg2) {
                if (DEBUG)
                    Log.d(TAG, "arg0:" + arg0 + ", arg1:" + arg1 + ", arg2:" + arg2);
            }
            
        });
    }
    /**
     * 通过用户名/手机号 +密码登陆
     * @param userName
     * @param password
     * @param succAction
     * @param failAction
     */
    public void userLoginUserName(final String userName, final String password,
            final Runnable succAction, final Runnable failAction) {
        HttpManager.getInstance().getRestAPIClient()
                .userLoginUserName(userName, password, new HttpCallback<Login>(succAction, failAction) {

                    @Override
                    protected boolean processData(Login data) {
                        if (!TextUtils.isEmpty(data.session)) {
                            final String session = data.session;
                            setCurrentUser(userName, password);
                            mCurrentUser.session = session;
                            mCurrentUser.bindedPhone = data.mobile;
                            mCurrentUser.score = data.rank;
                            setJPushAlias();
                            ShippingAddressManager.getInstance().getShippingAddresses(null, null);
                            return true;
                        }
                        return false;
                    }
                });
    }
    
    /**
     * 通过手机号 +验证码登陆
     * @param userName
     * @param password
     * @param succAction
     * @param failAction
     */
    public void userLoginMobile(final String mobile, final String encodedCode,
            final Runnable succAction, final Runnable failAction) {
        HttpManager.getInstance().getRestAPIClient()
                .userLoginMobile(mobile, encodedCode, new HttpCallback<Login>(succAction, failAction) {

                    @Override
                    protected boolean processData(Login data) {
                        if (!TextUtils.isEmpty(data.session)) {
                            final String session = data.session;
                            setCurrentUser(data.username, encodedCode);
                            mCurrentUser.session = session;
                            mCurrentUser.bindedPhone = data.mobile;
                            mCurrentUser.score = data.rank;
                            setJPushAlias();
                            ShippingAddressManager.getInstance().getShippingAddresses(null, null);
                            return true;
                        }
                        return false;
                    }

                    
                });
    }

    public void userLogout(final Runnable succAction, final Runnable failAction) {
        if (mCurrentUser == null || !mCurrentUser.isLogined()) {
            // already logout
            if (succAction != null) {
                succAction.run();
            }
            return;
        }
        HttpManager
                .getInstance()
                .getRestAPIClient()
                .userLogout(mCurrentUser.session,
                        new HttpCallback<ResponseHead>(succAction, failAction) {
                            @Override
                            protected boolean processData(ResponseHead t) {
                                mCurrentUser.session = null;
                                return true;
                            }
                        });
    }
    
    public void clearSession() {
        if (mCurrentUser != null) {
            mCurrentUser.session = null;
        }
    }

    /**
     * get city list per province id
     * 
     * @param pid
     */
    private void httpGetCityList(final int pid) {
        HttpManager.getInstance().getRestAPIClient()
                .getCityList(pid, new HttpCallback<Cities>(null, null) {
                    @Override
                    protected boolean processData(Cities cities) {
                        if (cities.cs != null) {
                            synchronized (mCityNameIdMap) {
                                for (ConfigItem item : cities.cs) {
                                    Log.d(TAG,
                                            "httpGetCityList, put mCityNameIdMap, name:"
                                                    + item.name + ", id:"
                                                    + item.id);
                                    mCityNameIdMap.put(item.name, item.id);

                                }
                            }
                        }
                        return true;
                    }
                });
    }

    public int getCityId(String province, String city) {
        Integer id = 0;
        try {
            id = mCityNameIdMap.get(city);
            // Log.d(TAG, "getCityId, id:" + (id != null ? id : "null") +
            // ", province:" + province + ",city:" + city);
            if (id == null && province != null) {
                // First get
                int pid;
                for (ConfigItem item : mSelectableProvinces) {
                    // Log.d(TAG, "getCityId, province:" + province + ", city:"
                    // + city + ", id:" + item.id + ", item.name:" + item.name);
                    if (province.contains(item.name)) {
                        // Log.d(TAG, "getCityId, province:" + province +
                        // ", city:" + city + ", id:" + item.id);
                        Cities c = HttpManager.getInstance().getRestAPIClient()
                                .getCityList(item.id);
                        synchronized (mCityNameIdMap) {
                            for (ConfigItem i : c.cs) {
                                mCityNameIdMap.put(i.name, i.id);

                            }
                        }
                        Integer idInteger = mCityNameIdMap.get(city);
                        return idInteger != null ? idInteger : 0;
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception e", e);
        }
        return id;
    }

    private void initLocationData() {
        final String[] preDefinedProvinces = mContext.getResources()
                .getStringArray(R.array.pre_defined_provinces);
        // Getprovince list
        HttpManager.getInstance().getRestAPIClient()
                .getProviceList(new HttpCallback<Provinces>(null, null) {
                    @Override
                    protected boolean processData(Provinces provinces) {
                        mSelectableProvinces = provinces.ps;
                        if (mSelectableProvinces != null) {
                            for (ConfigItem item : mSelectableProvinces) {

                                Log.d(TAG, "getProvince, id:" + item.id
                                        + ", name:" + item.name);
                                for (String p : preDefinedProvinces) {
                                    if (p.equals(item.name)) {
                                        httpGetCityList(item.id);
                                    }
                                }
                            }
                        }
                        return true;
                    }

                });
    }

    public int getMaxSelectionListHeight(Context context) {
        if (mMaxSelectionListHeight == 0) {
            mMaxSelectionListHeight = (int) (context.getResources()
                    .getDisplayMetrics().heightPixels * 0.5);
        }
        return mMaxSelectionListHeight;
    }

    public int getMaxDialogWidth(Context context) {
        if (mMaxDialogWidth == 0) {
            mMaxDialogWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        }
        return mMaxDialogWidth;
    }

    public void setSelectableZones(List<ConfigItem> zones) {
        if (mSelectableZones != null) {
            mSelectableZones.clear();
        }
        mSelectableZones = zones;
        if (mSelectableZones != null && !mSelectableZones.isEmpty()) {
            getCrossArea(mSelectableZones.get(0).id, null, null);
            getDistrictName(mSelectableZones.get(0).id);
        }
        mConfigData.setZoneId(-1); // reset
    }

    public List<ConfigItem> getSelectableZones() {
        return mSelectableZones;
    }

    public List<ConfigItem> getSelectableSequences() {
        return mSelectableSequences;
    }

    public SelectionConfigData getConfigData() {
        return mConfigData;
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }
    
    public boolean isUserLogined() {
        return mCurrentUser != null ? mCurrentUser.isLogined() : false;
    }

    public String getCurrentUserName() {
        return mCurrentUser != null ? mCurrentUser.username : null;
    }
    
    public String getCurrentPassword() {
        return mCurrentUser != null ? mCurrentUser.password : null;
    }

    public String getCurrentSession() {
        return mCurrentUser != null ? mCurrentUser.session : null;
    }
    
    public int getCurrentScore() {
//        return 1200;
        return mCurrentUser != null ? mCurrentUser.score : 0;
    }
    
    public void reduceCurrentScore(int reduce) {
        if (mCurrentUser != null) {
            mCurrentUser.score -= reduce;
            if (mCurrentUser.score < 0) {
                mCurrentUser.score = 0;
            }
        }
    }

    public String getZoneName(int zid) {
        if (mSelectableZones != null) {
            for (ConfigItem item : mSelectableZones) {
                if (item.id == zid) {
                    return item.name;
                }
            }
        }
        return null;
    }
    public String getCurrentZoneName() {
        return getZoneName(mConfigData.getZoneId());
    }

    public void setHotCities(List<ConfigItem> cities) {
        mHotCities = cities;
    }

    public List<ConfigItem> getHotCities() {
        return mHotCities;
    }

    public void setBindedPhone(final String phone) {
        if (mCurrentUser != null) {
            mCurrentUser.bindedPhone = phone;
        }
    }
    public void setCurrentUser(final String username, final String password) {
        boolean updateUserName = false;
        boolean updatePass = false;
        if (mCurrentUser != null) {
            if (!TextUtils.isEmpty(username)) {
                mCurrentUser.username = username;
                updateUserName = true;
            }
            if (!TextUtils.isEmpty(password)) {
                mCurrentUser.password = password;
                updatePass = true;
            }
        } else {
            mCurrentUser = new User(username, password);
            updateUserName = true;
            updatePass = true;
        }

        if (updateUserName) {
            HandlerManager.getInstance().getBgHandler().post(new Runnable() {
                @Override
                public void run() {
                    updateMisc(Util.MISC_USER_NAME, username);
                }
            });
        }
        if (updatePass) {
            HandlerManager.getInstance().getBgHandler().post(new Runnable() {
                @Override
                public void run() {
                    updateMisc(Util.MISC_PASSWORD, password);
                }
            });
        }
    }

    public void updateMisc(String name, String value) {
        ContentValues cv = new ContentValues();
        cv.put(Misc.COLUMN_NAME, name);
        cv.put(Misc.COLUMN_VALUE, value);
        Uri url = Uri.withAppendedPath(Misc.CONTENT_URI, name);
        mContentResolver.update(url, cv, Misc.COLUMN_NAME + "=?",
                new String[] { name });
    }

    private synchronized void retrieveCurrentVersion() {
        try {
            Context context = ClientApplication.sSharedInstance;
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            mCurrentVersionCode = packInfo.versionCode;
            mCurrentVersionName = packInfo.versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Exception:", e);
        }
    }

    public int getCurrentVersionCode() {
        if (mCurrentVersionCode == 0) {
            retrieveCurrentVersion();
        }

        return mCurrentVersionCode;
    }

    public String getCurrentVersionName() {
        if (mCurrentVersionName == null) {
            retrieveCurrentVersion();
        }

        return mCurrentVersionName;
    }

    public void getCommunities(final int zid, final Runnable succAction, final Runnable failAction) {
        List<ConfigItem> list = mCommunities.get(zid);
        if (list != null && !list.isEmpty()) {
            if (succAction != null) {
                succAction.run();
            }
        } else {
            HttpManager.getInstance().getRestAPIClient()
                    .getCommunityList(zid, new HttpCallback<Communities>(succAction, failAction) {
                        @Override
                        protected boolean processData(Communities t) {
                            mCommunities.put(zid, (ArrayList) t.coms);
                            return true;
                        }
                    });
        }
    }

    public List<ConfigItem> getCommunities(int zid) {
        return mCommunities.get(zid);
    }
    
    
    public void getCrossAreaDistricts(final int crossAreaId, final Runnable succAction, final Runnable failAction) {
        List<ConfigItem> list = mCrossAreaDistricts.get(crossAreaId);
        if (list != null && !list.isEmpty()) {
            if (succAction != null) {
                succAction.run();
            }
        } else {
            HttpManager.getInstance().getRestAPIClient()
                    .getCrossAreaDistrictList(crossAreaId, new HttpCallback<Districts>(succAction, failAction) {
                        @Override
                        protected boolean processData(Districts t) {
                            mCrossAreaDistricts.put(crossAreaId, (ArrayList) t.ds);
                            return true;
                        }
                    });
        }
    }

    public List<ConfigItem> getCrossAreaDistricts(int crossAreaId) {
        return mCrossAreaDistricts.get(crossAreaId);
    }
    
    public String getCrossAreaName(int crossAreaId) {
        Iterator<Integer> it = mZoneCrossAreas.keySet().iterator();
        while(it.hasNext()) {
            ConfigItem item = mZoneCrossAreas.get(it.next());
            if (item.id == crossAreaId) {
                return item.name;
            }
        }
        return null;
    }
    
    public String getDistrictName() {
        return getDistrictNameByZoneId(mConfigData.getZoneId());
    }
    
    public String getDistrictNameByZoneId(final int zid) {
        return mZoneDistrictNames.get(zid);
    }
    
    public void getDistrictName(final int zid) {
        HttpManager.getInstance().getRestAPIClient().getDistrictName(mConfigData.getZoneId(), new HttpCallback<ResponseStrValue>() {

            @Override
            protected boolean processData(ResponseStrValue t) {
                mZoneDistrictNames.put(zid, t.value);
                return true;
            }
            
        });
    }
    
    public int getCrossAreaId() {
        ConfigItem item = getCrossArea();
        if (item != null) {
            return item.id;
        }
        return -1;
    }
    
    /**
     * Get cross area id per current zone id
     * @param zid
     * @return
     */
    public ConfigItem getCrossArea() {
        return mZoneCrossAreas.get(ConfigManager.getInstance().getConfigData().getZoneId());
    }
    
    public void getCrossArea(final int zid, Runnable succAction, Runnable failAction) {
        HttpManager.getInstance().getRestAPIClient().getCrossAreaByZoneId(zid, new HttpCallback<ResponseNVValue>(succAction, failAction) {

            @Override
            protected boolean processData(ResponseNVValue t) {
                putCrossAreaId(zid, t.value, t.name);
                return true;
            }
            
        });
    }
    private void putCrossAreaId(int zid, int crossAreaId, String name) {
        mZoneCrossAreas.put(zid, new ConfigItem(crossAreaId, name));
    }

    public void getDistricts(final int cid, final Runnable succAction,
            final Runnable failAction) {
        HttpManager.getInstance().getRestAPIClient()
                .getDistrictList(cid, new HttpCallback<Districts>(succAction, failAction) {
                    @Override
                    protected boolean processData(Districts d) {
                        mCityDistricts.put(cid, (ArrayList) d.ds);
                        if (d.ds != null && !d.ds.isEmpty()) {
                            return true;
                        }
                        
                        return false;
                    }

                });
    }

    public List<ConfigItem> getDistricts(final int cid) {
        return this.mCityDistricts.get(cid);
    }

    public List<ConfigItem> getDistrictZones(int did) {
        return this.mDistrictZones.get(did);
    }

    public void setDistrictZones(int did, ArrayList<ConfigItem> data) {
        this.mDistrictZones.put(did, data);
    }

    public void setConfigStatus(ConfigStatus status) {
        mConfigStatus = status;
    }

    public ConfigStatus getConfigStatus() {
        return mConfigStatus;
    }

    public boolean isMainActivityStarted() {
        return mMainActivityStarted;
    }

    public void setMainActivityStarted(boolean started) {
      //  Log.d(TAG, "mainstarted" + this.hashCode());
        mMainActivityStarted = started;
    }
    
    public String getServiceNumber() {
        return mServiceNumber;
    }
    
    public void setServiceNumber(String number) {
        mServiceNumber = number;
    }
    
    public String getDefaultServiceNumber() {
        return mDefaultServiceNumber;
    }
    
    public boolean isFoodLikedToday(int foodId) {
        return mLikedFoods.contains(foodId);
    }
    
    public void addLikedFood(int foodId) {
        mLikedFoods.add(foodId);
    }
    
    public float getScoreExchangeRate() {
        return mScoreExchangeRate;
    }
    
    public void setScoreExchangeRate(float rate) {
        mScoreExchangeRate = rate;
    }
}
