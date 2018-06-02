package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.ConfigItem;
import com.blb.mmwd.uclient.rest.model.response.Districts;
import com.blb.mmwd.uclient.rest.model.response.Zones;
import com.blb.mmwd.uclient.ui.adapter.SimpleSelectionListAdapter;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.util.Util;

public class SelectCityZoneActivity extends TopCaptionActivity {
    private final static String TAG = "SelectCityZoneActivity";
    
    public final static int REQ_CODE_SELECT_ZONE = 1;
    public final static int RES_CODE_SELECT_ZONE_SUCC = 1;
    
    private int mCityId;
    private String mProvinceName;
    private String mCityName;
    private ListView mDistrictList;
    private BaseAdapter mDistrictAdapter;

    private ListView mZoneList;
    private BaseAdapter mZoneAdapter;

    private MessageViewFiller mDistrictMsgViewFiller;
    private MessageViewFiller mZoneMsgViewFiller;

    private List<ConfigItem> mDistricts;
    private List<ConfigItem> mZones;

    private int mSelectedDistrictId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                savedInstanceState = intent.getExtras();
            }
        }

        if (savedInstanceState != null) {
            mCityId = savedInstanceState.getInt(Util.EXTRA_ID, 0);
            mProvinceName = savedInstanceState
                    .getString(Util.EXTRA_PROVINCE_NAME);
            mCityName = savedInstanceState.getString(Util.EXTRA_CITY_NAME);
        }

        mDistrictList = (ListView) findViewById(R.id.sel_city_districts_list);
        mZoneList = (ListView) findViewById(R.id.sel_city_zone_list);
        mDistrictMsgViewFiller = new MessageViewFiller(findViewById(
                R.id.sel_city_district_zone).findViewById(R.id.error_msg_zone),
                findViewById(R.id.sel_city_district_main_zone));
        mZoneMsgViewFiller = new MessageViewFiller(findViewById(
                R.id.sel_city_zone_zone).findViewById(R.id.error_msg_zone),
                findViewById(R.id.sel_city_zone_main_zone));

        if (mCityId != 0) {
            mDistricts = ConfigManager.getInstance().getDistricts(mCityId);
            if (mDistricts == null || mDistricts.isEmpty()) {
                getDistrictList(mCityId);
            } else {
                refreshDistrictList();
            }
        } else {
            HandlerManager.getInstance().getBgHandler().post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "get cityId, mProvinceName:" + mProvinceName
                            + ", cityname:" + mCityName);
                    mCityId = ConfigManager.getInstance().getCityId(
                            mProvinceName, mCityName);
                    getDistrictList(mCityId);

                }
            });
        }

        mDistrictList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position,
                    long id) {
                if (mDistricts != null && !mDistricts.isEmpty()
                        && position >= 0 && position < mDistricts.size()) {
                    ConfigItem item = mDistricts.get(position);
                    mSelectedDistrictId = item.id;
                    if (mZones != null) {
                        mZones.clear();
                    }
                    mZones = ConfigManager.getInstance().getDistrictZones(
                            mSelectedDistrictId);
                    if (mZones != null && !mZones.isEmpty()) {
                        refreshZoneList();
                    } else {
                        getZoneList(item.id);
                    }
                }
            }
        });

        mZoneList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position,
                    long id) {
                if (mZones != null && !mZones.isEmpty() && position >= 0
                        && position < mZones.size()) {
                    Intent data = new Intent();
                    data.putExtra(Util.EXTRA_ID, mCityId);
                    data.putExtra(Util.EXTRA_CITY_NAME, mCityName);
                    data.putExtra(Util.EXTRA_PROVINCE_NAME, mProvinceName);
                    data.putExtra(Util.EXTRA_DISTRICT_ID, mSelectedDistrictId);
                    data.putExtra(Util.EXTRA_ZONE_ID, mZones.get(position).id);
                    setResult(RES_CODE_SELECT_ZONE_SUCC, data);
                    finish();
                }

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Util.EXTRA_ID, mCityId);
        outState.putString(Util.EXTRA_PROVINCE_NAME, mProvinceName);
        outState.putString(Util.EXTRA_CITY_NAME, mCityName);
    }

    private void refreshDistrictList() {
        mDistrictAdapter = new SimpleSelectionListAdapter(
                SelectCityZoneActivity.this, mDistricts, 0);
        mDistrictList.setAdapter(mDistrictAdapter);
        mDistrictMsgViewFiller.showMain();
        if (mDistricts != null && !mDistricts.isEmpty()) {
            mSelectedDistrictId = mDistricts.get(0).id;
            getZoneList(mSelectedDistrictId);
        }
    }

    private void refreshZoneList() {

        mZoneAdapter = new SimpleSelectionListAdapter(
                SelectCityZoneActivity.this, mZones, 0);
        mZoneList.setAdapter(mZoneAdapter);
        mZoneMsgViewFiller.showMain();
    }

    private void getZoneList(final int did) {
        mZoneMsgViewFiller.fill(null);
        HttpManager.getInstance().getRestAPIClient()
                .getZoneList(did, new HttpCallback<Zones>(null, new Runnable() {

                    @Override
                    public void run() {
                        mZoneMsgViewFiller.fill(0,
                                Util.getString(R.string.msg_not_covered), null,
                                null, null, null);

                    }

                }) {

                    @Override
                    protected boolean processData(Zones z) {
                        ConfigManager.getInstance().setDistrictZones(did,
                                (ArrayList) z.qs);
                        if (z.qs != null && !z.qs.isEmpty()) {

                            if (mZones != null) {
                                mZones.clear();
                            }
                            mZones = z.qs;
                            refreshZoneList();
                            return true;
                        }
                        return false;
                    }

                });
    }

    private void getDistrictList(final int cid) {
        ConfigManager.getInstance().getDistricts(mCityId, new Runnable() {

            @Override
            public void run() {
                mDistricts = ConfigManager.getInstance().getDistricts(mCityId);
                refreshDistrictList();
            }

        }, new Runnable() {

            @Override
            public void run() {
                mDistrictMsgViewFiller.fill(0,
                        Util.getString(R.string.msg_no_data), null, null, null,
                        null);
                mZoneMsgViewFiller.fill(0,
                        Util.getString(R.string.msg_not_covered), null, null,
                        null, null);

            }

        });
    }

    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_select_city_zone;
    }

}
