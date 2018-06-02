package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.LocationManager;
import com.blb.mmwd.uclient.manager.UpgradeManager;
import com.blb.mmwd.uclient.ui.adapter.FragmentTabMenuAdapter;
import com.blb.mmwd.uclient.ui.fragment.HomeFragment;
import com.blb.mmwd.uclient.ui.fragment.MineFragment;
import com.blb.mmwd.uclient.ui.fragment.OrderFragment;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    private boolean mAllowQuitApp;
    private FragmentTabHost mTabHost;
    FragmentTabMenuAdapter mTabMenuAdapter;
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Util.INTENT_ACTION_REFRESH_DATA.equals(intent.getAction())) {
                // Toast.makeText(context, "receive broadcast on MainActivity refresh data", Toast.LENGTH_LONG).show();
                HandlerManager.getInstance().getUiHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mTabMenuAdapter.notifyCurrentFragmentDataChange();
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState
                    .getString(Util.EXTRA_CURRENT_TAB));
        }
        
        if (Util.isNetworkAvailable()) {
            UpgradeManager.getInstance().checkUpgrade(this, true);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Util.INTENT_ACTION_REFRESH_DATA);
        registerReceiver(mReceiver, filter);
        
        ConfigManager.getInstance().setMainActivityStarted(true);
    }

    private void initView() {
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
       // ViewPager mViewPager = null;//(ViewPager) findViewById(R.id.view_pager);
        
        List<FragmentTabMenuAdapter.FragmentInfo> fragmentInfoList = new ArrayList<FragmentTabMenuAdapter.FragmentInfo>();
        String[] names = getResources().getStringArray(R.array.tab_names);
        fragmentInfoList.add(new FragmentTabMenuAdapter.FragmentInfo(HomeFragment.class, names[0], R.drawable.tab_home_btn));
        fragmentInfoList.add(new FragmentTabMenuAdapter.FragmentInfo(OrderFragment.class, names[1], R.drawable.tab_order_btn));
        fragmentInfoList.add(new FragmentTabMenuAdapter.FragmentInfo(MineFragment.class, names[2], R.drawable.tab_mine_btn));
        
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabMenuAdapter = new FragmentTabMenuAdapter(this, getSupportFragmentManager(), mTabHost, fragmentInfoList, R.layout.item_main_bottom_tab);
        mTabMenuAdapter.init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Util.EXTRA_CURRENT_TAB, mTabHost.getCurrentTabTag());
    }

    @Override
    protected void onResume() {
        super.onResume();
      //  Log.d(TAG, "onResume");
        mTabMenuAdapter.notifyCurrentFragmentDataChange();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }
    @Override
    public void onBackPressed() {
        if (!mAllowQuitApp) {
            Toast.makeText(this, getString(R.string.quit_app_warning),
                    Toast.LENGTH_SHORT).show();
            mAllowQuitApp = true;
            HandlerManager.getInstance().getBgHandler()
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAllowQuitApp = false;
                        }
                    }, Util.QUIT_APP_INTERVAL);
        } else {
            // Finish current activity
            finish();
            // Quit application
            ClientApplication.sSharedInstance.quitApplication();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ConfigManager.getInstance().setMainActivityStarted(false);
        unregisterReceiver(mReceiver);
       // Log.d(TAG, "onDestroy");
    }
}
