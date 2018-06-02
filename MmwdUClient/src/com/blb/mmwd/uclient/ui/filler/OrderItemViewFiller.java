package com.blb.mmwd.uclient.ui.filler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;
import com.blb.mmwd.uclient.ui.FoodDetailActivity;
import com.blb.mmwd.uclient.ui.MmShopDetailActivity;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter.FoodCartListener;
import com.blb.mmwd.uclient.ui.dialog.AddFoodNoteDialog;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.util.StringUtil;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.OrderState;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OrderItemViewFiller {
    private WeakReference<Context> mContext;
    private LayoutInflater mInflater;
    private Order mOrder;
    private LinearLayout mOrderItemList;
    private TextView mTotalMoney;
 //   private TextView mOrderTime;
    private TextView mOrderStatus;
    private ImageView mOrderStatusImg;
    private Runnable mFoodUpdateListener;
    
    class ViewHolder {
        protected View mMmShopView;
        protected TextView mMmShopName;
        protected TextView mMmShopTotalMoney;
        protected ImageView mMmShopImg;
        protected LinearLayout mFoodList;
        protected View mDashLine;
        protected TextView mFoodNames; // only for compact version

        public ViewHolder(View v) {
            mMmShopView = v.findViewById(R.id.order_item_mm_shop_zone);
            mMmShopImg = (ImageView) v
                    .findViewById(R.id.order_item_mm_shop_img);
            mMmShopName = (TextView) v
                    .findViewById(R.id.order_item_mm_shop_name);
            mMmShopTotalMoney = (TextView) v
                    .findViewById(R.id.order_item_mm_shop_total_money);
            mDashLine = v.findViewById(R.id.order_item_dash_divider_line);
            
            // for detail version
            mFoodList = (LinearLayout) v.findViewById(R.id.order_food_list);
            // for compact version
            mFoodNames = (TextView) v.findViewById(R.id.order_item_food_names);
        }

    }
    
    public OrderItemViewFiller(View view) {
        this(view, null);
    }

    public OrderItemViewFiller(View view, Order order) {
        this(view, order, null);
    }
    
    public OrderItemViewFiller(View view, Order order, Runnable foodUpdateListener) {
        mOrder = order;
        mFoodUpdateListener = foodUpdateListener;
        mContext = new WeakReference<Context>(view.getContext());
        mInflater = LayoutInflater.from(mContext.get());
     //   mOrderTime = (TextView) view.findViewById(R.id.order_time);

        mOrderStatus = (TextView) view.findViewById(R.id.order_status_txt);
        mOrderStatusImg = (ImageView) view.findViewById(R.id.order_status_img);
        mTotalMoney = (TextView) view.findViewById(R.id.order_total_money);

        mOrderItemList = (LinearLayout) view.findViewById(R.id.order_item_list);
    }

    public void fillViews() {
        fillViews(false);
    }

    private FoodCartListener mFoodCartListener = new FoodCartListener() {
        @Override
        public void updateFood(OrderFoodItem item) {
            CartManager.getInstance().updateFood(item);
            fillViews(false);
            if (mFoodUpdateListener != null) {
                mFoodUpdateListener.run();
            }
        }
    };
    
    public void fillViews(boolean compact) {
        fillViews(compact, mOrder);
    }
    /**
     * 
     * @param compact
     *            for compact version, don't need to list every food
     */
    public void fillViews(boolean compact, Order order) {
        mOrder = order;
        
        // Fill zone_order_items title part
    //    mOrderTime.setText(Util.toReadableDate(mOrder.createTime, Util.sDateFormat));
        if (mOrderStatus != null) {
            mOrderStatus.setText(mOrder.state);//Util.getStatusText(mContext.get(),
                    //mOrder.orderState));
        }
        
        if (mOrderStatusImg != null) {
            mOrderStatusImg.setImageResource(Util.getStatusImgRes(mOrder.orderState));
        }
        
        if (mTotalMoney != null) {
            float totalFee = order.orderState == OrderState.INIT ? CartManager.getInstance()
                    .getTotalFee() : order.realPayPrice + order.kdPrice; //实际支付金额
            mTotalMoney.setText(Util.getMoneyText(totalFee));
        }

        // Fill each item, per mmshop
        mOrderItemList.removeAllViews();

        Iterator<Integer> it = mOrder.orderFoods.keySet().iterator();
        
        View dashLine = null;
        boolean singleMm = mOrder.orderFoods.keySet().size() == 1;
        while (it.hasNext()) {
            ArrayList<OrderFoodItem> foodItems = mOrder.orderFoods.get(it
                    .next());
            
            if (foodItems == null || foodItems.isEmpty()) {
                continue;
            }

            // decode mmShopView
            // for compact, it only one single line, which includes all food's name
            // for detail, it contains each food
            View mmShopView = mInflater.inflate(
                    compact ? R.layout.item_order_items_compact
                            : R.layout.item_order_items_detail, null);
            ViewHolder vh = new ViewHolder(mmShopView);
            dashLine = vh.mDashLine;
            String mmShopName = foodItems.get(0).food.mmName;
            final int mmShopId = foodItems.get(0).food.mmid;
            
            vh.mMmShopView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Util.startActivity(mContext.get(), MmShopDetailActivity.class, mmShopId);
                    
                }
                
            });
            // initialize food name, img
            vh.mMmShopName.setText(mmShopName);
            //if (mmShopId == OrderFoodItem.MMSHOP_ID_MMWD_PROVIDED_FOOD) {
            //    vh.mMmShopImg.setImageResource(R.drawable.ic_main_food_rice);
            //} else 
            if (!TextUtils.isEmpty(foodItems.get(0).food.mmImg)) {
                ImageLoader.getInstance().displayImage(HttpManager.getInstance().getRealImageUrl(foodItems.get(0).food.mmImg),
                        vh.mMmShopImg, Util.sMmShopImageOptions);
            }
           
            
            float toalMoney = 0L;
            if (!compact) {
                // For detail version, need to fill every food
                // first get each food line's layout
                int layoutResId = mOrder.orderState == OrderState.INIT ? R.layout.item_order_food_init
                        : R.layout.item_order_food;
                vh.mFoodList.removeAllViews();
                for (final OrderFoodItem item : foodItems) {
                    View view = mInflater.inflate(layoutResId, null);
                    view.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Intent intent = new Intent(mContext.get(),
                                    FoodDetailActivity.class);
                            intent.putExtra(Util.EXTRA_ID, item.food.id);
                            intent.putExtra(Util.EXTRA_NAME, item.food.name);
                            mContext.get().startActivity(intent);
                            
                        }
                        
                    });
                    TextView foodName = (TextView) view
                            .findViewById(R.id.order_food_name);
                    
                    foodName.setText(item.food.name);
                    TextView note = (TextView) view.findViewById(R.id.order_food_note);
                    if (mOrder.orderState == OrderState.INIT) {
                        FoodSelectionViewFiller selectionFiller = ((FoodSelectionViewFiller)view.getTag());
                        if (selectionFiller == null) {
                            selectionFiller = new FoodSelectionViewFiller(view.findViewById(R.id.submit_info_amount_area), 
                                mFoodCartListener);
                            view.setTag(selectionFiller);
                        }
                        selectionFiller.fill(item.food);
                        ImageView deleteImg = (ImageView) view.findViewById(R.id.delete_food_img);
                        deleteImg.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
//                                String title, String content, String positiveStr, Runnable listener) {
                                Dialog diag = new ConfirmationDialog(arg0.getContext(), 
                                        Util.getString(R.string.dialog_clear_food_title),
                                        Util.getString(R.string.dialog_clear_food_content, item.food.name),
                                        null, new Runnable() {

                                            @Override
                                            public void run() {
                                                CartManager.getInstance().removeFood(item.food.mmid, item.food.id);
                                                fillViews(false);
                                            }
                                    
                                });
                                diag.show();
                                
                            }
                            
                        });
                        
                        if (TextUtils.isEmpty(item.note)) {
                            note.setText("添加备注");
                        } else {
                            note.setText(item.note);
                        }
                        
                        note.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // TODO Auto-generated method stub
                                Dialog diag = new AddFoodNoteDialog(arg0.getContext(),
                                        item.note, item.count, item.food, new Runnable() {

                                            @Override
                                            public void run() {
                                                fillViews(false);
                                                
                                            }
                                    
                                });
                                diag.show();
                            }
                            
                        });
                    } else {
                        FoodZanViewFiller zanFiller = new FoodZanViewFiller(view.findViewById(R.id.food_zan_zone));
                        zanFiller.fill(item.food.id, item.food.zan, ConfigManager.getInstance().isFoodLikedToday(item.food.id));
                        if (TextUtils.isEmpty(item.note)) {
                            note.setVisibility(View.GONE);
                        } else {
                            note.setText(item.note);
                        }
                    }
                    TextView foodCount = (TextView) view
                            .findViewById(R.id.order_food_amount);
                    foodCount.setText(Util.getFoodCountText(item.count));
                    
                    TextView foodTotalMoney = (TextView) view
                            .findViewById(R.id.order_food_total_money);
                    float foodTotalMoneyF = item.count * item.food.price;
                    foodTotalMoney.setText(Util.getMoneyText(foodTotalMoneyF));
                    
                    vh.mFoodList.addView(view);
                    
                    toalMoney += foodTotalMoneyF;
                }
                
            } else {
                // For compact version, just put all food name in one single
                // line, no need to list every food
                StringUtil foodNames = new StringUtil();
                for (OrderFoodItem item : foodItems) {
                    foodNames.append(item.food.name);
                    toalMoney += (item.count * item.food.price);
                }
                vh.mFoodNames.setText(foodNames.toString());
            }
            if (singleMm) {
                vh.mMmShopTotalMoney.setVisibility(View.GONE);
            } else {
                vh.mMmShopTotalMoney.setText(Util.getMoneyText(toalMoney));
            }
            mOrderItemList.addView(mmShopView);
        }
        
        // Hide the last divider line
        if (dashLine != null) {
            dashLine.setVisibility(View.GONE);
        }
    }
}
