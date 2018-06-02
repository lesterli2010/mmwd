package com.blb.mmwd.uclient.ui.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.ui.OrderDetailActivity;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter.ViewHolder;
import com.blb.mmwd.uclient.util.StringUtil;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.OrderState;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Include mmShop - food, 
 * @author lizhiqiang3
 *
 */
public class OrderHistoryCompactListAdapter extends BaseAdapter {
    private final static String TAG = "OrderItemAdapter";
    private WeakReference<Context> mContext;
    private LayoutInflater mInflater;
    private List<Order> mOrders;
    public class ViewHolder extends RecyclerView.ViewHolder {
        WeakReference<Context> mContext;
        private View mView;
        protected TextView mOrderTime;
        protected TextView mOrderMmShopNames;
     //   protected TextView mOrderFoodNames;
        protected TextView mOrderStatus;
        protected TextView mOrderTotalMoney;
        protected View mDashLine;
        public ViewHolder(final View v) {
            super(v);
            mView = v;
            mOrderTime = (TextView) v.findViewById(R.id.order_item_time);
            mOrderMmShopNames = (TextView) v.findViewById(R.id.order_item_mm_shop_names);
       //     mOrderFoodNames = (TextView) v.findViewById(R.id.order_item_food_names);
            mOrderStatus = (TextView) v.findViewById(R.id.order_item_status);
            mOrderTotalMoney = (TextView) v.findViewById(R.id.order_item_total_money);
            mDashLine = v.findViewById(R.id.order_item_dash_divider_line);
            
        }
        
    }
    
    public OrderHistoryCompactListAdapter(Context context, List<Order> orders) {
        mContext = new WeakReference<Context>(context);
        mOrders = orders;
        mInflater = LayoutInflater.from(context);
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mOrders != null ? mOrders.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        if (view == null) {
            
            view = mInflater.inflate(R.layout.item_order_history_items_compact, parent, false);
            holder = new ViewHolder(view);
         //   holder.mIcon = (ImageView) view.findViewById(R.id.mine_menu_icon);
          //  holder.mName = (TextView) view.findViewById(R.id.mine_menu_name);
            // holder.mSelectedImage = (ImageView)
            // view.findViewById(R.id.select_item_selected_image);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        Order order = mOrders.get(position);
        StringUtil mmShopNames = new StringUtil();
      //  String foodNames = "";
        
        holder.mOrderTime.setText(Util.toReadableDate(order.createTime, Util.sDateFormat_MDHM));
        Iterator<Integer> it = order.orderFoods.keySet().iterator();
        int tmp = 1;
        while(it.hasNext()) {
            Integer mmShopId = it.next();
            List<OrderFoodItem> foodList = order.orderFoods.get(mmShopId);
            if (foodList == null || foodList.isEmpty()) {
                continue;
            }
            
            mmShopNames.append(foodList.get(0).food.mmName);
        }
        //    tmp++; //tmp
         //   order.orderId = tmp;// tmp
            final int oid = order.id;
            holder.mView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Util.startActivity(mContext.get(), OrderDetailActivity.class, oid);
                }
                
            });
        
        
        holder.mOrderMmShopNames.setText(mmShopNames.toString());
       // holder.mOrderFoodNames.setText(foodNames);
        holder.mOrderStatus.setText(order.state);//Util.getStatusText(mContext.get(), order.orderState));
        holder.mOrderTotalMoney.setText(Util.getMoneyText(order.totalPrice + order.kdPrice));
        if (position == (mOrders.size() - 1)) {
            holder.mDashLine.setVisibility(View.GONE);
        } else {
            holder.mDashLine.setVisibility(View.VISIBLE);
        }
        return view;
    }
}
