package com.blb.mmwd.uclient.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

// API doc http://developer.baidu.com/map/loc_refer/index.html
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager.ConfigStatus;

public class LocationManager {
    private final static String TAG = "LocationManager";
    private LocationClient mLocationClient;
    private LocationListener mLocationListener;
    private MmwdLocation mLocation = new MmwdLocation();
    
    public static enum LocationStatus {
        NO_LOCATION,
        AUTO_LOCATED, // 自动定位
        MANUALLY_LOCATED //手工定位
    }

    public static class MmwdLocation {
        public LocationStatus status = LocationStatus.NO_LOCATION; // 0, 
        public int cityId;
        public String provinceName;
        public String cityName;
        public String address;
        public int districtId;
        public int zoneId;
        public double lng;
        public double lat;
        
        public void reset() {
            status = LocationStatus.NO_LOCATION;
            cityId = 0;
            cityName = null;
            provinceName = null;
            address = null;
            districtId = 0;
            zoneId = 0;
            lng = 0.0;
            lat = 0.0;
        }
    }

    private static LocationManager sLocationManager;  
    public static LocationManager getInstance() {  
        if (sLocationManager == null) {
            sLocationManager = new LocationManager();
        }
        return sLocationManager;  
    }
    
    public void init(Context context) {
        mLocationClient = new LocationClient(context);
        mLocationListener = new LocationListener();
        mLocationClient.registerLocationListener(mLocationListener);
      //  mGeofenceClient = new GeofenceClient(context);

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
        option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
        option.setTimeOut(8000); // 7s for timeout
        Log.d(TAG, "init, Timeout:" + option.getTimeOut() + ", addrType:" + option.getAddrType());
        option.setIsNeedAddress(true);
       
        mLocationClient.setLocOption(option);
    }
    
    public String getCity() {
        return mLocation.cityName;//!TextUtils.isEmpty(mLocation.cityName) ? mSelectedCity : mLocAvailable ? mLocatedCity : null;
    }
    
    public void setManualLocation(String cityName, int cityId, int districtId, int zoneId) {
        if (!TextUtils.isEmpty(cityName)) {
           // mSelectedCity = city;
            mLocation.status = LocationStatus.MANUALLY_LOCATED;
            mLocation.cityId = cityId;
            mLocation.districtId = districtId;
            mLocation.zoneId = zoneId;
            mLocation.cityName = cityName;
            
            ConfigManager.getInstance().setConfigStatus(ConfigStatus.LOCATION_OK);
            HandlerManager.getInstance().sendEmptyMessage(false, HandlerManager.MSG_LOCATION_FINISHED, 0);
            this.stopLocation();
        }
    }
    
    public String getLocatedCity() {
        return mLocation.status == LocationStatus.AUTO_LOCATED ? mLocation.cityName : null;
    }

    public class LocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // Debug purpose:
            StringBuffer sb = new StringBuffer(256);
            
            sb.append("location : " );
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\ndirection : ");
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append(location.getDirection());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
            }
           // logMsg(sb.toString());
            Log.i(TAG, "mLocAvailable:" + mLocation.status + ", " + sb.toString());
            
            if (mLocation.status == LocationStatus.AUTO_LOCATED) {
                // location already available, ignore it
                return;
            }

            // XXX路XXX号
            // String street = TextUtils.isEmpty(location.getStreet()) ? "" : location.getStreet();
            // String streetNum = TextUtils.isEmpty(location.getStreetNumber()) ? "" : location.getStreetNumber();
           
            if (!TextUtils.isEmpty(location.getCity())) {
                mLocation.status = LocationStatus.AUTO_LOCATED;
                mLocation.cityName = location.getCity().trim();
                mLocation.provinceName = location.getProvince().trim();
                mLocation.lat = location.getLatitude();
                mLocation.lng = location.getLongitude();
                mLocation.address = location.getAddrStr();
               // location.getl
               // Log.d(TAG, "cityId:" + ConfigManager.getInstance().getCityId(location.getProvince(), mLocation.cityName));
                
                ConfigManager.getInstance().setConfigStatus(ConfigStatus.LOCATION_OK);
                mLocationClient.stop();
                HandlerManager.getInstance().sendEmptyMessage(false, HandlerManager.MSG_LOCATION_FINISHED, 0);
            } else {
                mLocationClient.requestLocation();
            }
        }
    }
    
    public void startLocation() {
        ConfigManager.getInstance().setConfigStatus(ConfigStatus.NO_LOCATION);
        mLocation.reset();
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        } else {
            mLocationClient.requestLocation();
        }
    }
    
    public void stopLocation() {
        //  try {
              mLocationClient.stop();
         // } catch (InterruptedException e) {
              
         // }
          
      }
    
    public boolean isLocationAvailable() {
        return mLocation.status != LocationStatus.NO_LOCATION;
    }
    
    public MmwdLocation getLocation() {
        return mLocation;
    }

}
