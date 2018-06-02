package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.filler.FoodSelectionViewFiller;
import com.blb.mmwd.uclient.ui.filler.FoodZanViewFiller;
import com.blb.mmwd.uclient.util.StringUtil;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.OrderState;
import com.nostra13.universalimageloader.core.ImageLoader;


public class OrderSelectSettlementActivity extends TopCaptionActivity {

    private final static String TAG = "OrderSelectSettlementActivity";
    private final static boolean DEBUG = true;
    
    private Button mOpenSettlementBtn;
    private ViewGroup mSettlementList;
    private LayoutInflater mInflater;
    private List<HashMap<Integer, ArrayList<OrderFoodItem>>> mFoodMapList;
    private List<ImageView> mSelectImgList = new ArrayList<ImageView>();
    
    private int mSelectedMapIndex = -1; // the selected index of mFoodMapList
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CartManager.getInstance().isEmpty()) {
            Toast.makeText(this, this.getString(R.string.error_msg_cart_empty),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        mOpenSettlementBtn = (Button) findViewById(R.id.open_settlement_btn);
        mSettlementList = (ViewGroup) findViewById(R.id.settlement_list);
        mInflater = LayoutInflater.from(this);
        
        mFoodMapList = CartManager.getInstance().getFoodMapForSettlementSelection();
        
        mOpenSettlementBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                openOrderSettlement();
            }
            
        });
        
        initView();
        
    }
    
    private void openOrderSettlement() {
        if (mSelectedMapIndex == -1) {
            Util.sendToast("请首先选择美食~");
            return;
        }
        
        HashMap<Integer, ArrayList<OrderFoodItem>> map = mFoodMapList.get(mSelectedMapIndex);
        
        Iterator<Integer> it = map.keySet().iterator();
        int mmShopId = 0;
        boolean isCrossArea = false;;
        while(it.hasNext()) {
            ArrayList<OrderFoodItem> list = map.get(it.next());
            if (list != null && !list.isEmpty()) {
                if (list.get(0).count <= 0) {
                    continue;
                }
                
                Food f = list.get(0).food;
                isCrossArea = f.crossArea;
                if (isCrossArea) {
                    mmShopId = f.mmid;
                }
                break;
            }
        }
        Intent intent = new Intent(this, OrderSettlementActivity.class);
        intent.putExtra(OrderSettlementActivity.EXTRA_MM_SHOP_ID, mmShopId);
        intent.putExtra(OrderSettlementActivity.EXTRA_IS_CROSS_AREA, isCrossArea);
        intent.putExtra(OrderSettlementActivity.EXTRA_IS_SETTLEMENT_SELECTED, true);
        
        startActivity(intent);
        finish();
    }
    
    private void clickSettlement(int index) {
        if (mSelectedMapIndex == index) {
            mSelectedMapIndex = -1;
            mSelectImgList.get(index).setImageResource(R.drawable.checkbox_uncheck_normal);
        } else {
            if (mSelectedMapIndex != -1) {
                mSelectImgList.get(mSelectedMapIndex).setImageResource(R.drawable.checkbox_uncheck_normal);
            }
            mSelectedMapIndex = index;
            mSelectImgList.get(mSelectedMapIndex).setImageResource(R.drawable.checkbox_checked_normal);
        }
    }
    
    private void initView() {
        
        int index = 0;
        for (HashMap<Integer, ArrayList<OrderFoodItem>> map : mFoodMapList) {
            View v = mInflater.inflate(R.layout.item_order_settlement_selection, null);
            
            ImageView selectImg = (ImageView) v.findViewById(R.id.order_settlement_select_img);
            mSelectImgList.add(selectImg);
            View selectView = v.findViewById(R.id.order_settlemetn_select_zone);
            final int imgIndex = index;
            index++;
            selectView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    clickSettlement(imgIndex);
                    
                }
                
            });
        
            
            ViewGroup mmShopList = (ViewGroup) v.findViewById(R.id.order_item_list);
            fillMmShop(mmShopList, map);
            
            mSettlementList.addView(v);
        }
    }
    
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
    
    
    private void fillMmShop(ViewGroup mmShopList, HashMap<Integer, ArrayList<OrderFoodItem>> map) {
        Iterator<Integer> it = map.keySet().iterator();
        
        View dashLine = null;
        boolean singleMm = map.keySet().size() == 1;
        while (it.hasNext()) {
            ArrayList<OrderFoodItem> foodItems = map.get(it
                    .next());
            
            if (foodItems == null || foodItems.isEmpty()) {
                continue;
            }

            // decode mmShopView
            // for compact, it only one single line, which includes all food's name
            // for detail, it contains each food
            View mmShopView = mInflater.inflate(R.layout.item_order_items_detail, null);
            ViewHolder vh = new ViewHolder(mmShopView);
            dashLine = vh.mDashLine;
            String mmShopName = foodItems.get(0).food.mmName;
            final int mmShopId = foodItems.get(0).food.mmid;
            
            vh.mMmShopView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Util.startActivity(OrderSelectSettlementActivity.this, MmShopDetailActivity.class, mmShopId);
                    
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
            
           
                // For detail version, need to fill every food
                // first get each food line's layout
                int layoutResId = R.layout.item_order_food;
                vh.mFoodList.removeAllViews();
                for (final OrderFoodItem item : foodItems) {
                    View view = mInflater.inflate(layoutResId, null);
                    view.findViewById(R.id.order_food_note).setVisibility(View.GONE);
                    view.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Intent intent = new Intent(OrderSelectSettlementActivity.this,
                                    FoodDetailActivity.class);
                            intent.putExtra(Util.EXTRA_ID, item.food.id);
                            intent.putExtra(Util.EXTRA_NAME, item.food.name);
                            OrderSelectSettlementActivity.this.startActivity(intent);
                            
                        }
                        
                    });
                    TextView foodName = (TextView) view
                            .findViewById(R.id.order_food_name);
                    
                    foodName.setText(item.food.name);
                    
                        FoodZanViewFiller zanFiller = new FoodZanViewFiller(view.findViewById(R.id.food_zan_zone));
                        zanFiller.fill(item.food.id, item.food.zan, ConfigManager.getInstance().isFoodLikedToday(item.food.id));
                    
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
                
            
            if (singleMm) {
                vh.mMmShopTotalMoney.setVisibility(View.GONE);
            } else {
                vh.mMmShopTotalMoney.setText(Util.getMoneyText(toalMoney));
            }
            mmShopList.addView(mmShopView);
        }
        
        // Hide the last divider line
        if (dashLine != null) {
            dashLine.setVisibility(View.GONE);
        }
    }
    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_order_select_settlement;
    }

}
