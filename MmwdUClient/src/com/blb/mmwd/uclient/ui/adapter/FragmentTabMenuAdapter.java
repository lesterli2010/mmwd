package com.blb.mmwd.uclient.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.ui.fragment.SingleViewFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

/**
 * This is a helper class that implements the management of tabs and all details
 * of connecting a ViewPager with associated TabHost. It relies on a trick.
 * Normally a tab host has a simple API for supplying a View or Intent that each
 * tab will show. This is not sufficient for switching between pages. So instead
 * we make the content part of the tab host 0dp high (it is not shown) and the
 * TabsAdapter supplies its own dummy view to show as the tab content. It
 * listens to changes in tabs, and takes care of switch to the correct paged in
 * the ViewPager whenever the selected tab changes.
 */
public class FragmentTabMenuAdapter extends FragmentPagerAdapter implements
        TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private final static String TAG = "FragmentTabMenuAdapter";
    private final Context mContext;
    private final FragmentTabHost mTabHost;
    private final ViewPager mViewPager;
    private final List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private List<FragmentInfo> mFragmentInfoList;
    private int mTabItemLayoutId;
    private LayoutInflater mLayoutInflater;
    private FragmentManager mFragmentManager;

    public static class FragmentInfo {
        Class<?> mFragmentCls;
        String mName;
        int mBtnDrawable;
        Bundle mBundleArgs;

        public FragmentInfo(Class<?> cls, String name, Bundle args) {
            this(cls, name, 0, args);
        }

        public FragmentInfo(Class<?> cls, String name, int btnDrawable) {
            this(cls, name, btnDrawable, null);
        }

        public FragmentInfo(Class<?> cls, String name, int btnDrawable,
                Bundle args) {
            mFragmentCls = cls;
            mName = name;
            mBtnDrawable = btnDrawable;
            mBundleArgs = args;
        }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    public FragmentTabMenuAdapter(FragmentActivity activity,
            FragmentManager fragmentManager, FragmentTabHost tabHost,
            List<FragmentInfo> fragmentInfoList, ViewPager pager) {
        this(activity, fragmentManager, tabHost, fragmentInfoList, 0, pager);
    }

    public FragmentTabMenuAdapter(FragmentActivity activity,
            FragmentManager fragmentManager, FragmentTabHost tabHost,
            List<FragmentInfo> fragmentInfoList, int tabItemLayoutId) {
        this(activity, fragmentManager, tabHost, fragmentInfoList,
                tabItemLayoutId, null);
    }

    public FragmentTabMenuAdapter(FragmentActivity activity,
            FragmentManager fragmentManager, FragmentTabHost tabHost,
            List<FragmentInfo> fragmentInfoList, int tabItemLayoutId,
            ViewPager pager) {
        super(activity.getSupportFragmentManager());
        mContext = activity;
        mFragmentManager = fragmentManager;
        mLayoutInflater = LayoutInflater.from(mContext);
        mTabHost = tabHost;
        mTabHost.setup(mContext, fragmentManager, android.R.id.tabcontent);
        mTabHost.setOnTabChangedListener(this);
        mTabHost.getTabWidget().setDividerDrawable(null);
        mFragmentInfoList = fragmentInfoList;
        mTabItemLayoutId = tabItemLayoutId;
        mViewPager = pager;
        if (mViewPager != null) {
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }
    }

    public void init() {
        if (mFragmentInfoList == null || mFragmentInfoList.isEmpty()) {
            return;
        }
        for (FragmentInfo fi : mFragmentInfoList) {
            TabSpec tabSpec = mTabHost.newTabSpec((fi.mName)).setIndicator(
                    getTabItemView(fi));
            addTab(tabSpec, fi.mFragmentCls, fi.mBundleArgs);
            // …Ë÷√Tab∞¥≈•µƒ±≥æ∞ no need now
            // mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_dialog_bottom_txtbtn);
        }
    }

    private View getTabItemView(FragmentInfo fi) {
        View view = null;

        if (mTabItemLayoutId == R.layout.item_main_bottom_tab) {
            view = mLayoutInflater.inflate(R.layout.item_main_bottom_tab, null);

            ImageView imageView = (ImageView) view
                    .findViewById(R.id.tab_btn_image);
            imageView.setImageResource(fi.mBtnDrawable);
            view.setBackgroundResource(R.drawable.bottom_bar_bg);
        } else {
            view = mLayoutInflater.inflate(R.layout.item_simple_menu, null);

            TextView textView = (TextView) view.findViewById(R.id.menu_name);
            textView.setText(fi.mName);
            
            view.setBackgroundResource(R.drawable.fragment_bar_bg);
        }
        return view;
    }

    private void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(mContext));

        if (mViewPager != null) {
            // For slip
            // Add dummy fragment for TabHost(The second parameter), otherwise,
            // there will be two HomeFragment at the same time.
            // It's implemented by viewerpager and FragmentTabHost.
            Fragment fragment = Fragment.instantiate(mContext, clss.getName(),
                    args);
            mTabHost.addTab(tabSpec, Fragment.class, null);
            mFragmentList.add(fragment);
            notifyDataSetChanged();
        } else {
            mTabHost.addTab(tabSpec, clss, args);
        }
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);

    }

    @Override
    public void onTabChanged(String tabId) {
        int position = mTabHost.getCurrentTab();
        Log.d("HomeFragment", "onTabChanged" + tabId);
        if (mViewPager != null) {
            mViewPager.setCurrentItem(position);
            // For layout which has view pager, notify data change when tab is changed.
            notifyCurrentFragmentDataChange();
        }

        // ((RadioButton) mTabRg.getChildAt(position)).setChecked(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        // Unfortunately when TabHost changes the current tab, it kindly
        // also takes care of putting focus on it when not in touch mode.
        // The jerk.
        // This hack tries to prevent this from pulling focus out of our
        // ViewPager.
        Log.d("HomeFragment", "onPageSelected" + position);
        TabWidget widget = mTabHost.getTabWidget();
        int oldFocusability = widget.getDescendantFocusability();
        widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mTabHost.setCurrentTab(position);
        widget.setDescendantFocusability(oldFocusability);
        /*
         * mTabHost.getTabWidget().getChildAt(arg0)
         * .setBackgroundResource(R.drawable.selector_tab_background);
         */
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * Notify current Fragment that need to refresh data
     */
    public void notifyCurrentFragmentDataChange() {
        Fragment f = getCurrentFragment();
        if (f != null && f instanceof SingleViewFragment) {
            ((SingleViewFragment) f).refreshView();
        }
    }

    private Fragment getCurrentFragment() {
        if (mViewPager != null) {
            int pos = mTabHost.getCurrentTab();
            if (pos >= 0 && pos < mFragmentList.size()) {
                return mFragmentList.get(mTabHost.getCurrentTab());
            } else {
                return null;
            }
        } else {
            return mFragmentManager.findFragmentByTag(mTabHost
                    .getCurrentTabTag());
        }
    }
}
