package com.blb.mmwd.uclient.ui;

import java.util.List;

import retrofit.client.Response;

import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.manager.LocationManager;
import com.blb.mmwd.uclient.manager.LocationManager.LocationStatus;
import com.blb.mmwd.uclient.manager.LocationManager.MmwdLocation;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.ConfigItem;
import com.blb.mmwd.uclient.rest.model.response.Cities;
import com.blb.mmwd.uclient.ui.adapter.SimpleSelectionListAdapter;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LocationManuallyActivity extends TopCaptionActivity {
    private final static String TAG = "LocationManuallyActivity";
    private Button mLocateBtn;
    private TextView mCurrentAddrText;
    private View mLocatedCityZone;
    private Button mLocatedCityBtn;
    // private Context mContext;

    private ListView mList;
    private List<ConfigItem> mHotCities;
    private BaseAdapter mAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Util.INTENT_ACTION_REFRESH_DATA.equals(intent.getAction())) {
                HandlerManager.getInstance().getUiHandler()
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                refreshView(true);
                            }
                        });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocateBtn = (Button) this.findViewById(R.id.loc_man_locate_btn);
        mLocateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HandlerManager.getInstance().sendEmptyMessage(false,
                        HandlerManager.MSG_START_LOCATION, 0);
                mLocateBtn.setEnabled(false);
                mLocateBtn.setText(getString(R.string.location_inprog));
            }
        });
        mCurrentAddrText = (TextView) findViewById(R.id.loc_man_current_addr_text);

        mLocatedCityZone = findViewById(R.id.loc_man_located_city_zone);

        mLocatedCityBtn = (Button) mLocatedCityZone
                .findViewById(R.id.loc_man_located_city);

        mLocatedCityBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MmwdLocation loc = LocationManager.getInstance().getLocation();
                startSelectZoneActivity(0, loc.cityName, loc.provinceName);
            }

        });

        mList = (ListView) findViewById(R.id.loc_man_hot_city_list);
        mHotCities = ConfigManager.getInstance().getHotCities();

        MmwdLocation loc = LocationManager.getInstance().getLocation();
        int selectedCityId = loc.status == LocationStatus.MANUALLY_LOCATED ? loc.cityId
                : 0;
        if (mHotCities != null && !mHotCities.isEmpty()) {
            mAdapter = new SimpleSelectionListAdapter(this, mHotCities,
                    selectedCityId);
            mList.setAdapter(mAdapter);
        } else {
            // query new hot cities list
            HttpManager.getInstance().getRestAPIClient()
                    .getHotCityList(new HttpCallback<Cities>(null, null) {

                        @Override
                        protected boolean processData(Cities cities) {
                            if (cities.cs != null) {
                                ConfigManager.getInstance().setHotCities(
                                        cities.cs);
                                mHotCities = cities.cs;
                                mAdapter = new SimpleSelectionListAdapter(
                                        LocationManuallyActivity.this,
                                        cities.cs, 0);
                                mList.setAdapter(mAdapter);
                            }
                            return true;
                        }

                    });
        }

        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position,
                    long id) {
                Log.e(TAG, "onItemClick:" + position);
                if (mHotCities != null && position >= 0
                        && position < mHotCities.size()) {
                    ConfigItem item = mHotCities.get(position);
                    startSelectZoneActivity(item.id, item.name, null);
                }
            }
        });

        refreshView(false);
    }

    private void refreshView(boolean showToast) {
        MmwdLocation loc = LocationManager.getInstance().getLocation();
        if (loc.status == LocationStatus.AUTO_LOCATED) {
            mCurrentAddrText.setVisibility(View.VISIBLE);
            mCurrentAddrText.setText(loc.address);
            mLocatedCityZone.setVisibility(View.VISIBLE);
            mLocatedCityBtn.setText(loc.cityName);
            mLocateBtn.setText(getString(R.string.re_location));
            if (showToast) {
                Toast.makeText(this,
                        getString(R.string.auto_location_succ_info),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            mLocatedCityZone.setVisibility(View.GONE);
            mCurrentAddrText.setVisibility(View.GONE);
            mLocateBtn.setText(getString(R.string.location_to_current));
            if (showToast) {
                Toast.makeText(this,
                        getString(R.string.auto_location_fail_info),
                        Toast.LENGTH_SHORT).show();
            }
        }
        mLocateBtn.setEnabled(true);
    }

    private void startSelectZoneActivity(int cityId, String cityName,
            String provinceName) {
        Intent intent = new Intent(this, SelectCityZoneActivity.class);
        intent.putExtra(Util.EXTRA_ID, cityId);
        intent.putExtra(Util.EXTRA_CITY_NAME, cityName);
        intent.putExtra(Util.EXTRA_PROVINCE_NAME, provinceName);
        startActivityForResult(intent, SelectCityZoneActivity.REQ_CODE_SELECT_ZONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // outState.putInt(Util.EXTRA_SELECT_LOC_TYPE, mLocationType);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Util.INTENT_ACTION_REFRESH_DATA);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_location_manually;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SelectCityZoneActivity.RES_CODE_SELECT_ZONE_SUCC) {
            int cid = data.getExtras().getInt(Util.EXTRA_ID);
            int did = data.getExtras().getInt(Util.EXTRA_DISTRICT_ID, 0);
            int zid = data.getExtras().getInt(Util.EXTRA_ZONE_ID, 0);
            String cityName = data.getExtras().getString(Util.EXTRA_CITY_NAME);
            LocationManager.getInstance().setManualLocation(cityName, cid, did,
                    zid);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
