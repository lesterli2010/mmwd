package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.Comment;
import com.blb.mmwd.uclient.rest.model.response.Comments;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.rest.model.response.Orders;
import com.blb.mmwd.uclient.ui.adapter.CommentFeedbackListAdapter;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter;
import com.blb.mmwd.uclient.ui.adapter.OrderHistoryDetailListAdapter;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.ui.filler.PrevNextViewFiller;
import com.blb.mmwd.uclient.util.Util;

public class OrderHistoryActivity extends TopCaptionActivity {
    private RecyclerView mList;
    private RecyclerView.Adapter<OrderHistoryDetailListAdapter.ViewHolder> mAdapter;
    private List<Order> mOrders = new ArrayList<Order>();
    private int mCurrentPage;
    private PrevNextViewFiller mPrevNextFiller;
    private MessageViewFiller mMsgFiller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mList = (RecyclerView) findViewById(R.id.history_order_item_detail_list);
        mList.setHasFixedSize(true);
        // use a linear layout manager

        mMsgFiller = new MessageViewFiller(findViewById(R.id.error_msg_zone),
                mList);
        mPrevNextFiller = new PrevNextViewFiller(
                findViewById(R.id.prev_next_zone), new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (mCurrentPage > 0) {
                            refreshView(mCurrentPage - 1);
                        }

                    }

                }, new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        refreshView(mCurrentPage + 1);

                    }

                });

        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new OrderHistoryDetailListAdapter(this, mOrders);
        mList.setAdapter(mAdapter);

        refreshView(mCurrentPage);
    }

    private void refreshView(final int pageNum) {
        mMsgFiller.fill(null);
        HttpManager
                .getInstance()
                .getRestAPIClient()
                .getOrderList(ConfigManager.getInstance().getCurrentSession(),
                        pageNum, Util.MAX_ITEM_NUM_ONE_PAGE,
                        new HttpCallback<Orders>(new Runnable() {

                            @Override
                            public void run() {
                                mMsgFiller.fill(
                                        0,
                                        "当前无历史订单~",
                                        Util.getString(R.string.btn_click_to_refresh),
                                        new OnClickListener() {

                                            @Override
                                            public void onClick(View arg0) {
                                                refreshView(pageNum);

                                            }

                                        }, null, null);

                            }

                        }) {

                            @Override
                            protected boolean processData(Orders t) {

                                if (t.list == null || t.list.isEmpty()) {
                                    // not the first page
                                    if (pageNum > 0) {
                                        mMsgFiller.showMain();
                                        Util.sendToast(R.string.msg_no_more);
                                        return true;
                                    }
                                    return false;
                                }
                                mCurrentPage = pageNum;
                                mPrevNextFiller.fill(mCurrentPage,
                                        t.list.size());
                                mOrders.clear();
                                mAdapter.notifyDataSetChanged();
                                for (Order o : t.list) {
                                    o.convert();

                                    mOrders.add(o);

                                }
                                mAdapter.notifyDataSetChanged();
                                mMsgFiller.showMain();
                                return true;
                            }

                        });
    }

    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_order_history;
    }

}
