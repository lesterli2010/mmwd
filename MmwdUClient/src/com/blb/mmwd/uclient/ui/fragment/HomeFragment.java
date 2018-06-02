package com.blb.mmwd.uclient.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.baidu.location.BDLocation;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.ConfigManager.ConfigStatus;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.manager.LocationManager;
import com.blb.mmwd.uclient.manager.LocationManager.LocationStatus;
import com.blb.mmwd.uclient.manager.LocationManager.MmwdLocation;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.MmShop;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.Zones;
import com.blb.mmwd.uclient.ui.LocationManuallyActivity;
import com.blb.mmwd.uclient.ui.MmShopDetailActivity;
import com.blb.mmwd.uclient.ui.OrderSettlementActivity;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter;
import com.blb.mmwd.uclient.ui.adapter.FragmentTabMenuAdapter;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter.ViewHolder;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.util.AnimationUtil;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.SelectionType;
import com.blb.mmwd.uclient.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends SingleViewFragment {
    private final static String TAG = "HomeFragment";

    private SelectionType mSelectionType = SelectionType.SELECT_TYPE_NONE;
    private Fragment mSelectionFragment;

    private Button mCityBtn;

    private TextView mCurrentZoneText;

    private FragmentTabHost mTabHost;
    private FragmentTabMenuAdapter mTabMenuAdapter;

    private MessageViewFiller mMsgViewFiller;
    private SelectionFragment.SelectionActionListener mSelectionActionListener = new SelectionFragment.SelectionActionListener() {

        @Override
        public void notifySelection() {
            // Toast.makeText(getActivity(), "update selection:" + selectedId,
            // Toast.LENGTH_LONG).show();
            /*
             * switch (mSelectionType) { case SELECT_TYPE_ZONE: String city =
             * LocationManager.getInstance().getCity(); if (selectedId !=
             * ConfigManager.getInstance().getConfigData().getZoneId(city)) { //
             * Config changed - update data loadData();
             * ConfigManager.getInstance().getConfigData().setZoneId(city,
             * selectedId); } break; case SELECT_TYPE_SEQUENCE: if (selectedId
             * != ConfigManager.getInstance().getConfigData().getSequenceId()) {
             * loadData();
             * ConfigManager.getInstance().getConfigData().setSequenceId
             * (selectedId); } break; }
             */
            
            // change zone, clean cart
            CartManager.getInstance().clear();
            closeSelectionFragment();
            refreshView();
        }

        @Override
        public void cancelSelection() {
            closeSelectionFragment();
        }

    };

    private void initFragmentTab() {
        List<FragmentTabMenuAdapter.FragmentInfo> fragmentInfoList = new ArrayList<FragmentTabMenuAdapter.FragmentInfo>();
        String[] names = getActivity().getResources().getStringArray(
                R.array.main_select_content_type);

        Bundle arg1 = new Bundle();
        arg1.putInt(Util.EXTRA_CONTENT_LIST_TYPE,
                Util.ContentListType.ALL_HOT_MM_SHOP.ordinal());
        Bundle arg2 = new Bundle();
        arg2.putInt(Util.EXTRA_CONTENT_LIST_TYPE,
                Util.ContentListType.ALL_HOT_FOOD.ordinal());

        fragmentInfoList.add(new FragmentTabMenuAdapter.FragmentInfo(
                ContentListFragment.class, names[0], arg1));
        fragmentInfoList.add(new FragmentTabMenuAdapter.FragmentInfo(
                ContentListFragment.class, names[1], arg2));

        mTabHost = (FragmentTabHost) mRootView
                .findViewById(android.R.id.tabhost);
        mTabMenuAdapter = new FragmentTabMenuAdapter(getActivity(),
                getChildFragmentManager(), mTabHost, fragmentInfoList,
                (ViewPager) mRootView.findViewById(R.id.view_pager));
        mTabMenuAdapter.init();
    }

    @Override
    public void onPause() {
      //  Log.d(TAG, "onPause");
        closeSelectionFragment();
        super.onPause();
    }

    private void handleSelectionFragment(SelectionType selectionType) {
        if (selectionType != mSelectionType) {
            closeSelectionFragment(); // Close current
        } else if (mSelectionFragment != null) {
            closeSelectionFragment();
            return;
        }

        Fragment fragment = SelectionFragment.newInstance(selectionType,
                mSelectionActionListener);

        if (fragment != null) {
            // First check if there is data
            final FragmentManager fragmentManager = this.getActivity()
                    .getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.selection_fragment, fragment)
                    .commitAllowingStateLoss();
            Log.e(TAG,
                    "handleSelectionFragment, show fragment:" + this.hashCode());
            mSelectionFragment = fragment;
            mSelectionType = selectionType;

            // Add animation effect
            // mSelectionFragmentView.setAnimation(AnimationUtil.createSelectionFragmentAnimation(mPivotX,
            // mPivotY));
        }
    }

    private void closeSelectionFragment() {
        if (mSelectionFragment != null) {
            final FragmentManager fragmentManager = this.getActivity()
                    .getSupportFragmentManager();
            fragmentManager.beginTransaction().remove(mSelectionFragment)
                    .hide(mSelectionFragment).commitAllowingStateLoss();

            mSelectionFragment = null;
            mSelectionType = SelectionType.SELECT_TYPE_NONE;
        }
    }

    @Override
    protected int getViewResourceId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        Log.d(TAG, "initView");
        mRootView.findViewById(R.id.zone_bar).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        handleSelectionFragment(SelectionType.SELECT_TYPE_ZONE);
                    }
                });

        mCityBtn = (Button) mRootView.findViewById(R.id.select_city);
        mCityBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Util.startActivity(getActivity(),
                        LocationManuallyActivity.class);
            }
        });

        mMsgViewFiller = new MessageViewFiller(
                mRootView.findViewById(R.id.error_msg_zone),
                mRootView.findViewById(R.id.main_content_zone));

        mCurrentZoneText = (TextView) mRootView
                .findViewById(R.id.current_zone_text);
        initFragmentTab();

    }

    private void onLocateFailed() {
        mMsgViewFiller.fill(0,
                Util.getString(R.string.auto_location_fail_info),
                Util.getString(R.string.manual_location),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Util.startActivity(getActivity(),
                                LocationManuallyActivity.class);
                    }

                }, Util.getString(R.string.re_location), new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        mMsgViewFiller.fill(Util
                                .getString(R.string.loading_inprog));
                        HandlerManager.getInstance().sendEmptyMessage(false,
                                HandlerManager.MSG_START_LOCATION, 0);
                    }

                });
    }

    private void onLocationNotCovered() {
        mMsgViewFiller.fill(0, Util.getString(R.string.msg_not_covered),
                Util.getString(R.string.manual_location),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Util.startActivity(getActivity(),
                                LocationManuallyActivity.class);
                    }

                }, null, null);
    }

    @Override
    public void refreshView() {
        ConfigStatus status = ConfigManager.getInstance().getConfigStatus();
        Log.d(TAG, "refreshView:" + status);

        String city = LocationManager.getInstance().getCity();
        MmwdLocation loc = LocationManager.getInstance().getLocation();
        if (loc.status == LocationStatus.NO_LOCATION || TextUtils.isEmpty(city)) {
            city = getActivity().getString(R.string.locate_manually);
        }
        mCityBtn.setText(city);

        switch (status) {
        // Get locatino information (auto or manually).
        case LOCATION_OK:
            mMsgViewFiller.fill(getActivity().getString(
                    R.string.loading_inprog_hard));
            switch (loc.status) {
            case AUTO_LOCATED:
                // Get Zoned
                HttpManager
                        .getInstance()
                        .getRestAPIClient()
                        .getZoneList(loc.lng, loc.lat,
                                new HttpCallback<Zones>(null, new Runnable() {

                                    @Override
                                    public void run() {
                                        onLocationNotCovered();

                                    }

                                }) {

                                    @Override
                                    protected boolean processData(Zones zones) {
                                        if (zones.qs == null
                                                || zones.qs.isEmpty()) {
                                            onLocationNotCovered();
                                            return false;
                                        }
                                        ConfigManager.getInstance()
                                                .setSelectableZones(zones.qs);
                                        ConfigManager
                                                .getInstance()
                                                .setConfigStatus(
                                                        ConfigStatus.CONFIG_DATA_OK);
                                        refreshView();
                                        return true;
                                    }
                                });
                break;
            case MANUALLY_LOCATED:
                final int zoneId = loc.zoneId;
                HttpManager
                        .getInstance()
                        .getRestAPIClient()
                        .getZoneList(loc.districtId,
                                new HttpCallback<Zones>(null, new Runnable() {

                                    @Override
                                    public void run() {
                                        onLocationNotCovered();

                                    }

                                }) {

                                    @Override
                                    protected boolean processData(Zones zones) {
                                        if (zones.qs == null
                                                || zones.qs.isEmpty()) {
                                            return false;
                                        }
                                        ConfigManager.getInstance()
                                                .setSelectableZones(zones.qs);
                                        ConfigManager.getInstance()
                                                .getConfigData()
                                                .setZoneId(zoneId);
                                        ConfigManager.getInstance().getCrossArea(zoneId, null, null);
                                        ConfigManager.getInstance().getDistrictName(zoneId);
                                        ConfigManager
                                                .getInstance()
                                                .setConfigStatus(
                                                        ConfigStatus.CONFIG_DATA_OK);
                                        refreshView();
                                        return true;
                                    }
                                });
                break;
            default:
                onLocateFailed();
                break;
            }
            break;
        case CONFIG_DATA_OK:
            mMsgViewFiller.showMain();
            String zone = ConfigManager.getInstance().getCurrentZoneName();
            if (zone != null) {
                mCurrentZoneText.setText(zone);
                mTabMenuAdapter.notifyCurrentFragmentDataChange();
            } else {
                mCurrentZoneText.setText(getActivity().getString(
                        R.string.find_current_zone_inprog));
            }
            break;

        case NO_NETWORK:
            mMsgViewFiller.fill(0, Util.getString(R.string.msg_no_network),
                    Util.getString(R.string.reload), new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            mMsgViewFiller.fill(Util
                                    .getString(R.string.loading_inprog));
                            HandlerManager.getInstance()
                                    .sendEmptyMessage(false,
                                            HandlerManager.MSG_START_LOCATION,
                                            0);
                        }

                    }, null, null);
            break;
        case NO_LOCATION:
            onLocateFailed();
            break;
        default:
            break;
        }
    }
}
