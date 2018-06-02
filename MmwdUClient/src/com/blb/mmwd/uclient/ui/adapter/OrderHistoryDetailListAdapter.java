package com.blb.mmwd.uclient.ui.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.ui.OrderDetailActivity;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter.ViewHolder;
import com.blb.mmwd.uclient.ui.filler.OrderItemViewFiller;
import com.blb.mmwd.uclient.util.Util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OrderHistoryDetailListAdapter extends
        RecyclerView.Adapter<OrderHistoryDetailListAdapter.ViewHolder> {
    // private List<String> mDataset;
    private final static String TAG = "ContentListAdapter";
    private List<Order> mOrders;
    private LayoutInflater mInflater;
    public class ViewHolder extends RecyclerView.ViewHolder implements
            OnClickListener {
        View mOrderItemZone;
        public ViewHolder(View v) {
            super(v);
            mOrderItemZone = v.findViewById(R.id.zone_order_items);
        }

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            
        }
    }
    
    public OrderHistoryDetailListAdapter(Context context, List<Order> orders) {
        mOrders = orders;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return mOrders != null ? mOrders.size() : 0;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order order = mOrders.get(position);
        OrderItemViewFiller filler = new OrderItemViewFiller(holder.mOrderItemZone, order);
        filler.fillViews(true);
        
        final int oid = order.id;
        holder.mOrderItemZone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.startActivity(arg0.getContext(), OrderDetailActivity.class, oid);
                
            }
            
        });
        
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        View v = mInflater.inflate(
                R.layout.item_order_history_items_detail, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

}
