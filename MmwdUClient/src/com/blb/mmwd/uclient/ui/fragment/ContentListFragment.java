package com.blb.mmwd.uclient.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.MmShop;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.Foods;
import com.blb.mmwd.uclient.rest.model.response.MmShops;
import com.blb.mmwd.uclient.ui.FoodDetailActivity;
import com.blb.mmwd.uclient.ui.MmShopDetailActivity;
import com.blb.mmwd.uclient.ui.OrderSelectSettlementActivity;
import com.blb.mmwd.uclient.ui.OrderSettlementActivity;
import com.blb.mmwd.uclient.ui.UserLoginActivity;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.ContentListType;

public class ContentListFragment extends SingleViewFragment {
    private final static String TAG = "ContentListFragment";
    private final static boolean DEBUG = true;
    private ContentListType mContentListType = ContentListType.ALL_HOT_MM_SHOP;
    private View mSettlementBar;
    private TextView mTotalMoney;
    private ImageButton mClearSettlementBtn;
    private RecyclerView mContentList;
    private ContentListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<MmShop> mMmShops; // List of MM shop
    private List<Food> mFoods;
    private int mNextPage = 1;
    private long mLastRefreshTime;
    private int mZoneId;
    private SwipeRefreshLayout mSwiperRefresher;
    private int mMmShopId;
    private View mMoreLoading;
    private MessageViewFiller mMsgViewFiller;
    private boolean mLoading;

    

    private ContentListAdapter.FoodCartListener mFoodCartListener = new ContentListAdapter.FoodCartListener() {
        @Override
        public void updateFood(OrderFoodItem item) {

            CartManager.getInstance().updateFood(item);
            refreshSettlementBar();
            Log.d(TAG, "updateFood called");
            // Toast.makeText(getActivity(), "updateFood" + item.foodName + ","
            // + item.foodAmount + ",total:" +
            // CartManager.getInstance().getTotalMoney(),
            // Toast.LENGTH_LONG).show();
        }
    };

    private void refreshSettlementBar() {
        if (CartManager.getInstance().isEmpty()) {
            mSettlementBar.setVisibility(View.GONE);
        } else {
            mSettlementBar.setVisibility(View.VISIBLE);
            mTotalMoney.setText(Util.getMoneyText(CartManager
                    .getInstance().getTotalFee()));
        }
    }

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.fragment_content_list;
    }

    @Override
    protected void initView() {
        Log.d(TAG, "initView: " + this.mContentListType);
        if (mSavedInstanceState == null) {
            mSavedInstanceState = getArguments();
        }
        if (mSavedInstanceState != null) {
            mContentListType = ContentListType.values()[mSavedInstanceState
                    .getInt(Util.EXTRA_CONTENT_LIST_TYPE)];
            mMmShopId = mSavedInstanceState.getInt(Util.EXTRA_ID);
            Log.d(TAG, "initView - " + mContentListType + " , mMmShopId:"
                    + mMmShopId);
        }

        mMoreLoading = mRootView.findViewById(R.id.content_list_more_loading);

        mMsgViewFiller = new MessageViewFiller(
                mRootView.findViewById(R.id.error_msg_zone),
                mRootView.findViewById(R.id.main_content_zone));

        // Test for content List
        mContentList = (RecyclerView) mRootView.findViewById(R.id.content_list);
        mContentList.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mContentList.setLayoutManager(mLayoutManager);
        mContentList.setItemAnimator(new DefaultItemAnimator());
        mContentList.setOnScrollListener(new OnScrollListener() {
            boolean isSlidingToLast = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                     //获取最后一个完全显示的ItemPosition
                    int lastVisibleItem = ((LinearLayoutManager)mLayoutManager).findLastCompletelyVisibleItemPosition();
                    int totalItemCount = ((LinearLayoutManager)mLayoutManager).getItemCount();
              //      if (DEBUG)
               //         Log.d(TAG, "onScrollStateChanged, lastVisibleItem:" + lastVisibleItem + ", totalItemCount:" + totalItemCount + ",isSlidingToLast:" + isSlidingToLast);
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == (totalItemCount -1) &&
                            isSlidingToLast && !mLoading &&
                            (totalItemCount % Util.MAX_ITEM_NUM_ONE_PAGE) == 0) {
                            mLoading = true;
                            mMoreLoading.setVisibility(View.VISIBLE);
                            httpGetData(true, false);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
               // if (DEBUG) Log.d(TAG, "onScrolled: dx:" + dx + ",dy:" + dy);
                //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
                if(dy > 0){
                     //大于0表示，正在向下滚动
                    isSlidingToLast = true;
                } else {
                     //小于等于0 表示停止或向左滚动
                    isSlidingToLast = false;
                }

            }
        });

        if (mContentListType != ContentListType.ALL_HOT_MM_SHOP) {
            mContentList
                    .setBackgroundResource(R.drawable.bg_white_with_pink_border);
            mFoods = new ArrayList<Food>();
            mAdapter = new ContentListAdapter(getActivity(), mContentListType, mFoods, mFoodCartListener);
        } else {
            mMmShops = new ArrayList<MmShop>();
            mAdapter = new ContentListAdapter(getActivity(), mContentListType, mMmShops, mFoodCartListener);
        }
        mContentList.setAdapter(mAdapter);

        mSwiperRefresher = (SwipeRefreshLayout) mRootView
                .findViewById(R.id.content_swipe_refresher);
        mSwiperRefresher.setColorSchemeResources(R.color.main_color);
        mSwiperRefresher.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                //Log.d(TAG, "mSwiperRefresher onRefresh");
                httpGetData(false, true);
            }

        });
        // mContentList.setAdapter(mMmShopAdapter);
        mSettlementBar = mRootView.findViewById(R.id.settlement_bar);
        mTotalMoney = (TextView) mSettlementBar.findViewById(R.id.total_amount);
        mClearSettlementBtn = (ImageButton) mSettlementBar
                .findViewById(R.id.settlement_clear_btn);// settlement_clear_btn
        ((Button) mRootView.findViewById(R.id.settlement_btn))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (Util.checkUserLogin(getActivity(), true)) {
                            if (CartManager.getInstance().isSelectSettlementNeeded()) {
                                Util.startActivity(getActivity(), OrderSelectSettlementActivity.class);
                            } else {
                                Util.startActivity(getActivity(), OrderSettlementActivity.class);
                            }
                        }
                    }

                });
        mClearSettlementBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // String title, String content, String positiveStr, Runnable
                // listener) {
                Dialog d = new ConfirmationDialog(getActivity(), Util
                        .getString(R.string.dialog_clear_cart_title), Util
                        .getString(R.string.dialog_clear_cart_content), null,
                        new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                CartManager.getInstance().clear();
                                refreshSettlementBar();
                                mAdapter.notifyDataSetChanged();
                            }

                        });
                d.show();
            }
        });
    }

    private void clearData() {
        if (mMmShops != null) {
            for (MmShop m : mMmShops) {
                if (m.ps != null) {
                    m.ps.clear();
                }
            }
            mMmShops.clear();
        }
        if (mFoods != null) {
            mFoods.clear();
        }
        mAdapter.notifyDataSetChanged();
        mNextPage = 1;
    }

    private void onDataLoadFinish(boolean succ, List result, boolean more,
            boolean pullDownRefresh) {
        final boolean totallyRefresh = !more && !pullDownRefresh;
        if (!succ) {
            // loading failed, no data or network error
            String msg = null;
            if (totallyRefresh) {
                clearData();
                switch (mContentListType) {
                case ALL_HOT_MM_SHOP:
                    msg = Util.getString(R.string.msg_not_covered);
                    break;
                case ALL_HOT_FOOD:
                    msg = Util.getString(R.string.msg_no_data);
                    break;
                case MM_SHOP_ACTIVE_FOOD:
                    msg = Util.getString(R.string.msg_mmshop_detail_no_food);
                    break;
                case MM_SHOP_INACTIVE_FOOD:
                    msg = Util
                            .getString(R.string.msg_mmshop_detail_no_not_on_food);
                    break;
                }
                mMsgViewFiller.fill(0, msg,
                        Util.getString(R.string.btn_click_to_refresh),
                        new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                httpGetData(false, false);

                            }

                        }, null, null);
            }/* else if (pullDownRefresh) {
                Util.sendToast(R.string.msg_no_new);
            } else if (more) {
                Util.sendToast(R.string.msg_no_more);
            }*/
        } else {
            // loading success
            List currentList = (mContentListType == ContentListType.ALL_HOT_MM_SHOP) ? mMmShops
                    : mFoods;

            if (!more) {
                clearData();
            } else {
                mNextPage++;
            }
            for (Object m : result) {
                currentList.add(m);
            }
            currentList = null;
            mAdapter.notifyDataSetChanged();
            if (totallyRefresh) {
                mMsgViewFiller.showMain();
            }
        }

        if (pullDownRefresh && mSwiperRefresher.isRefreshing()) {
            mSwiperRefresher.setRefreshing(false);
        }
        if (more) {
            mLoading = false;
            mMoreLoading.setVisibility(View.GONE);
        }

    }

    // Whether need to append data
    /**
     * 
     * @param zid
     * @param more
     *            , add more data
     * @param swipe
     *            , swipe to refresh pull down
     */
    private void httpGetData(final boolean more, final boolean pullDownRefresh) {

        final boolean totallyRefresh = !more && !pullDownRefresh;
        if (totallyRefresh) {
            this.mMsgViewFiller.fill(Util
                    .getString(R.string.loading_inprog_hard));
        }

        int pageNum = more ? mNextPage : 0;
        switch (mContentListType) {
        case ALL_HOT_MM_SHOP:
            HttpManager
                    .getInstance()
                    .getRestAPIClient()
                    .getMmShopList(mZoneId, pageNum,
                            Util.MAX_ITEM_NUM_ONE_PAGE,
                            new HttpCallback<MmShops>(new Runnable() {
                                @Override
                                public void run() {
                                    onDataLoadFinish(false, null, more,
                                            pullDownRefresh);
                                }
                            }) {

                                @Override
                                protected boolean processData(MmShops data) {

                                    if (data.mms == null || data.mms.isEmpty()) {
                                        return false;
                                    }
                                    onDataLoadFinish(true, data.mms, more,
                                            pullDownRefresh);
                                    return true;
                                }

                            });
            break;
        case ALL_HOT_FOOD:
            HttpManager
                    .getInstance()
                    .getRestAPIClient()
                    .getHotFoodList(mZoneId, pageNum,
                            Util.MAX_ITEM_NUM_ONE_PAGE,
                            new HttpCallback<Foods>(new Runnable() {

                                @Override
                                public void run() {
                                    onDataLoadFinish(false, null, more,
                                            pullDownRefresh);

                                }

                            }) {

                                @Override
                                protected boolean processData(Foods t) {
                                    if (t.ps == null || t.ps.isEmpty()) {
                                        return false;
                                    }
                                    onDataLoadFinish(true, t.ps, more,
                                            pullDownRefresh);

                                    return true;
                                }

                            });
            break;
        case MM_SHOP_ACTIVE_FOOD:
            HttpManager
                    .getInstance()
                    .getRestAPIClient()
                    .getMmShopDetailFoods(mMmShopId, true,
                            new HttpCallback<Foods>(new Runnable() {

                                @Override
                                public void run() {
                                    onDataLoadFinish(false, null, more,
                                            pullDownRefresh);
                                }

                            }) {

                                @Override
                                protected boolean processData(Foods t) {
                                    if (t.ps == null || t.ps.isEmpty()) {
                                        return false;
                                    }
                                    onDataLoadFinish(true, t.ps, more,
                                            pullDownRefresh);

                                    return true;
                                }

                            });
            break;
        case MM_SHOP_INACTIVE_FOOD: // 未上架
            HttpManager
                    .getInstance()
                    .getRestAPIClient()
                    .getMmShopDetailFoods(mMmShopId, false,
                            new HttpCallback<Foods>(new Runnable() {

                                @Override
                                public void run() {
                                    onDataLoadFinish(false, null, more,
                                            pullDownRefresh);
                                }

                            }) {

                                @Override
                                protected boolean processData(Foods t) {
                                    if (t.ps == null || t.ps.isEmpty()) {
                                        return false;
                                    }
                                    onDataLoadFinish(true, t.ps, more,
                                            pullDownRefresh);
                                    return true;
                                }

                            });
            break;

        }

    }

    @Override
    public void refreshView() {
        if (!mInitialized) {
            return;
        }
        final int zoneId = ConfigManager.getInstance().getConfigData()
                .getZoneId();
        if (mZoneId != zoneId) {
            // zone changed, refresh
            mLastRefreshTime = 0;
            mZoneId = zoneId;
        }

        // avoid refresh too often
        long now = SystemClock.uptimeMillis();
        if (DEBUG)
        Log.d(TAG, "refreshView, now:" + now + ", mLastRefreshTime:"
                + mLastRefreshTime + ", cart updateTime:"
                + CartManager.getInstance().getUpdateTime());
        if (mLastRefreshTime == 0
                || (now - mLastRefreshTime) > Util.REFRESH_TIME_PERIOD
                || CartManager.getInstance().getUpdateTime() > mLastRefreshTime) {
            refreshSettlementBar();
            mLastRefreshTime = now;

            httpGetData(false, false);

            Log.d(TAG, "refreshView of ContentList:" + mContentListType
                    + ", mInitialized:" + mInitialized + ", currentZoneId:"
                    + zoneId);

        }
    }

}
