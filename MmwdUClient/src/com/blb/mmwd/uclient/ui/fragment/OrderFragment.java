package com.blb.mmwd.uclient.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.MmShops;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.rest.model.response.Orders;
import com.blb.mmwd.uclient.ui.OrderDetailActivity;
import com.blb.mmwd.uclient.ui.OrderHistoryActivity;
import com.blb.mmwd.uclient.ui.OrderSelectSettlementActivity;
import com.blb.mmwd.uclient.ui.OrderSettlementActivity;
import com.blb.mmwd.uclient.ui.UserLoginActivity;
import com.blb.mmwd.uclient.ui.adapter.OrderHistoryCompactListAdapter;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.ui.filler.OrderItemViewFiller;
import com.blb.mmwd.uclient.util.Util;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

public class OrderFragment extends SingleViewFragment {
    private final static boolean DEBUG = true;
    private final static String TAG = "OrderFragment";
    private OrderItemViewFiller mOrderItemViewFiller;
    private Order mOrder;
    private ListView mHistoryOrderList;
    private BaseAdapter mHistoryOrderAdapter;
    private List<Order> mHistoryOrders = new ArrayList<Order>();;
    private View mOrderItemsZoneView;
    private View mHistoryOrderItemsZoneView;
    private Button mMoreHistoryBtn;
    private View mCurrentOrderView;
    private View mHistoryOrderView;

    private MessageViewFiller mMainMsgViewFiller;
    private MessageViewFiller mCurrentOrderMsgViewFiller;
    private MessageViewFiller mHistoryOrderMsgViewFiller;

    private long mLastRefreshTime;

    @Override
    public void refreshView() {
        if (!mInitialized) {
            return;
        }

        // Check if login?
        if (!Util.checkUserLogin(getActivity(), false, false)) {
            mMainMsgViewFiller.fill(0,
                    Util.getString(R.string.msg_user_unauthorized),
                    Util.getString(R.string.dialog_login),
                    new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.startActivity(getActivity(),
                                    UserLoginActivity.class);

                        }

                    }, null, null);
            return;
        }
        mMainMsgViewFiller.showMain();

        // avoid refresh too often
        long now = SystemClock.uptimeMillis();
        if (DEBUG)
            Log.d(TAG, "refreshView, now:" + now + ", mLastRefreshTime:"
                    + mLastRefreshTime + ", cart updateTime:"
                    + CartManager.getInstance().getUpdateTime());
        if (!(mLastRefreshTime == 0
                || (now - mLastRefreshTime) > Util.REFRESH_MY_ORDER_TIME_PERIOD || CartManager
                .getInstance().getUpdateTime() > mLastRefreshTime)) {
            // no need to refresh
            return;
        }

        mLastRefreshTime = now;
        
        // fill current order
        mCurrentOrderMsgViewFiller.fill(null);
        if (!CartManager.getInstance().isEmpty()) {
            mOrderItemViewFiller.fillViews(true, Order.fromCart());
            mCurrentOrderMsgViewFiller.showMain();
            mCurrentOrderView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    
                    if (CartManager.getInstance().isSelectSettlementNeeded()) {
                        Util.startActivity(getActivity(), OrderSelectSettlementActivity.class);
                    } else {
                        Util.startActivity(getActivity(), OrderSettlementActivity.class);
                    }

                }

            });
        } else {
            // Get the latest one

            HttpManager
                    .getInstance()
                    .getRestAPIClient()
                    .getUnfinishedOrderList(
                            ConfigManager.getInstance().getCurrentSession(), 0,
                            1, new HttpCallback<Orders>(new Runnable() {

                                @Override
                                public void run() {
                                    mCurrentOrderMsgViewFiller.fill(
                                            0,
                                            Util.getString(R.string.msg_no_open_order),
                                            null, null, null, null);

                                }

                            }) {

                                @Override
                                protected boolean processData(Orders t) {
                                    if (t.list == null || t.list.isEmpty()) {
                                        return false;
                                    }

                                    final Order order = t.list.get(0);
                                    if (order == null) {
                                        return false;
                                    }
                                    order.convert();

                                    mOrderItemViewFiller.fillViews(true, order);
                                    mCurrentOrderMsgViewFiller.showMain();
                                    mCurrentOrderView
                                            .setOnClickListener(new OnClickListener() {

                                                @Override
                                                public void onClick(View arg0) {
                                                    Util.startActivity(
                                                            getActivity(),
                                                            OrderDetailActivity.class,
                                                            order.id);
                                                }

                                            });
                                    return true;
                                }

                            });
        }

        mHistoryOrderMsgViewFiller.fill(Util
                .getString(R.string.loading_inprog_history_order));
        mHistoryOrders.clear();
        mHistoryOrderAdapter.notifyDataSetChanged();

        HttpManager
                .getInstance()
                .getRestAPIClient()
                .getOrderList(ConfigManager.getInstance().getCurrentSession(),
                        0, Util.MAX_ITEM_NUM_ONE_PAGE,
                        new HttpCallback<Orders>(new Runnable() {

                            @Override
                            public void run() {
                                mHistoryOrderView.setVisibility(View.GONE);

                            }

                        }) {

                            @Override
                            protected boolean processData(Orders t) {
                                if (t.list == null || t.list.isEmpty()) {
                                    return false;
                                }
                                for (Order order : t.list) {
                                    order.convert();
                                    mHistoryOrders.add(order);
                                }
                                mHistoryOrderMsgViewFiller.showMain();
                                mHistoryOrderView.setVisibility(View.VISIBLE);
                                mHistoryOrderAdapter.notifyDataSetChanged();
                                mHistoryOrderMsgViewFiller.showMain();
                                return true;
                            }

                        });
    }

    @Override
    protected void initView() {
        Log.d(TAG, "initView");
        View main = mRootView.findViewById(R.id.order_content_zone);
        mMainMsgViewFiller = new MessageViewFiller(
                mRootView.findViewById(R.id.order_main_msg), main);

        mCurrentOrderView = main.findViewById(R.id.order_current_zone);
        mHistoryOrderView = main.findViewById(R.id.order_history_zone);

        mOrderItemsZoneView = mCurrentOrderView
                .findViewById(R.id.zone_order_items);
        mHistoryOrderItemsZoneView = mHistoryOrderView
                .findViewById(R.id.zone_order_history_items);

        mCurrentOrderMsgViewFiller = new MessageViewFiller(
                mCurrentOrderView.findViewById(R.id.error_msg_zone),
                mOrderItemsZoneView);
        mHistoryOrderMsgViewFiller = new MessageViewFiller(
                mHistoryOrderView.findViewById(R.id.error_msg_zone),
                mHistoryOrderItemsZoneView);

        mOrderItemViewFiller = new OrderItemViewFiller(mOrderItemsZoneView);

        mHistoryOrderList = (ListView) mHistoryOrderItemsZoneView
                .findViewById(R.id.history_order_item_compact_list);

        mMoreHistoryBtn = (Button) mHistoryOrderItemsZoneView
                .findViewById(R.id.history_order_more_btn);
        mMoreHistoryBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.startActivity(getActivity(), OrderHistoryActivity.class);

            }

        });
        /*
         * mHistoryOrderList.setHasFixedSize(true); use a linear layout manager
         * mLayoutManager = new LinearLayoutManager(this.getActivity());
         * mHistoryOrderList.setLayoutManager(mLayoutManager);
         * mHistoryOrderList.setItemAnimator(new DefaultItemAnimator());
         */
        mHistoryOrderAdapter = new OrderHistoryCompactListAdapter(
                getActivity(), mHistoryOrders);
        mHistoryOrderList.setAdapter(mHistoryOrderAdapter);
    }

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.fragment_order;
    }
}
