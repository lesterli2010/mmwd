package com.blb.mmwd.uclient.ui.filler;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;
import com.blb.mmwd.uclient.ui.FoodDetailActivity;
import com.blb.mmwd.uclient.ui.MmShopDetailActivity;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter.FoodCartListener;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.ContentListType;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * fill item_content_list_food
 * @author lizhiqiang3
 *
 */
public class FoodViewFiller {
    private final static boolean DEBUG = true;
    private final static String TAG = "FoodViewFiller";
    public static final int SHOW_MM_SHOP_VIEW = 0x01 << 0; //是否显示右上角妈妈name
    public static final int SHOW_WANTED_VIEW = 0x01 << 1; //是否显示我想吃
    public static final int CLICKABLE = 0x01 << 2; //是否可以点击打开详情
    public static final int NOT_SHOW_INACTIVE_ALPHA = 0x01 << 3; //是否在菜品未上架时不需要显示alpha,比如mmshop已经显示了alpha
    
    private View mRootView;
    private ImageView foodImg;
    private TextView foodName;
    private View foodCrossArea;
    private View foodReservable;
    //private TextView foodZan;
    private TextView foodDesc;
    private TextView foodRest;
    private TextView foodPrice;
    private View foodRestViews;

    private View buyViews;

    private TextView foodWanted;
    private View foodWantedLoading;
    private View divider;
    private FoodSelectionViewFiller foodSelectionFiller;
    private FoodZanViewFiller foodZanFiller;

    private WeakReference<Context> mContext;
    private FoodCartListener mFoodCartListener; 
    private int mDisplayType;
    public FoodViewFiller(Context context, 
            View v, 
            FoodCartListener foodCartListener,
            int displayType) {
        mContext = new WeakReference<Context>(context);
        mRootView = v;
        mDisplayType = displayType;
        mFoodCartListener = foodCartListener;
        
        foodImg = (ImageView) v.findViewById(R.id.food_img);
        foodName = (TextView) v.findViewById(R.id.food_name);
        foodCrossArea = v.findViewById(R.id.food_cross_area);
        foodReservable = v.findViewById(R.id.food_reservable);
       
        /* Don't show mm shop view
        if (checkType(SHOW_MM_SHOP_VIEW)) {
            foodMmShopImg = (ImageView) foodMmShopViews
                    .findViewById(R.id.mm_shop_img_in_food);
            foodMmShopName = (TextView) foodMmShopViews
                    .findViewById(R.id.mm_shop_name_in_food);

            foodMmShopViews.setVisibility(View.VISIBLE);
        } else {
            foodMmShopViews.setVisibility(View.GONE);
        }*/
       // foodZan = (TextView) v.findViewById(R.id.food_zan);
        foodDesc = (TextView) v.findViewById(R.id.food_desc);

        foodPrice = (TextView) v.findViewById(R.id.food_price);

        buyViews = v.findViewById(R.id.submit_info_amount_area);

        foodRestViews = v.findViewById(R.id.food_rest_zone);
        foodRest = (TextView) foodRestViews.findViewById(R.id.food_rest);
        foodWanted = (TextView) v.findViewById(R.id.food_want_eat_text);
        foodWantedLoading = v.findViewById(R.id.food_want_eat_loading);
        divider = (View) v.findViewById(R.id.dash_divider_line);

        foodSelectionFiller = new FoodSelectionViewFiller(buyViews,
                foodRest, mFoodCartListener);
        foodZanFiller = new FoodZanViewFiller(v.findViewById(R.id.food_zan_zone));
    }
    
    public boolean checkType(int type) {
        return (mDisplayType & type) == type;
    }

    public void fill(final Food food, boolean hideBottomDivider) {
        if (checkType(CLICKABLE)) {
            mRootView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(mContext.get(),
                            FoodDetailActivity.class);
                    intent.putExtra(Util.EXTRA_ID, food.id);
                    intent.putExtra(Util.EXTRA_NAME, food.name);
                    mContext.get().startActivity(intent);
                }

            });
        }
        ImageLoader.getInstance().displayImage(
                HttpManager.getInstance().getRealImageUrl(food.img),
                foodImg, Util.sFoodImageOptions);
        /*
        if (checkType(SHOW_MM_SHOP_VIEW)) {
            ImageLoader.getInstance().displayImage(
                    HttpManager.getInstance().getRealImageUrl(food.mmImg),
                    foodMmShopImg, Util.sMmShopImageOptions);
            foodMmShopName.setText(food.mmName);
            foodMmShopViews.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Util.startActivity(arg0.getContext(),
                            MmShopDetailActivity.class, food.mmid);
                }

            });
        }*/

        /* Log.d(TAG, "foodid:" + food.id + ", name:" + food.name + ", zan:" + food.zan);
        if (food.zan <= 0) {
            foodZan.setVisibility(View.GONE);
        } else {
            foodZan.setText(String.valueOf(food.zan));
        }*/
        foodZanFiller.fill(food.id, food.zan, ConfigManager.getInstance().isFoodLikedToday(food.id));
        
        foodDesc.setText(food.descr);
        foodPrice.setText(Util.getMoneyText(food.price));

        // TODO: temp code
        // food.active = true;
        if (food.crossArea) {
            foodCrossArea.setVisibility(View.VISIBLE);
        } else {
            foodCrossArea.setVisibility(View.GONE);
        }
        
        if (food.reserable) {
            foodReservable.setVisibility(View.VISIBLE);
        } else {
            foodReservable.setVisibility(View.GONE);
        }
        
        if (food.active) {
            foodName.setText(food.name);
            foodSelectionFiller.fill(food);
            Util.showHideView(buyViews, foodWanted);
        } else {
            // 未上架菜品,MMShop Detail
            foodName.setText(food.name + Util.getString(R.string.not_provided));
            if(!checkType(NOT_SHOW_INACTIVE_ALPHA)) {
                ViewHelper.setAlpha(mRootView, Util.INACTIVE_ALPHA);
            }
            
            if (checkType(SHOW_WANTED_VIEW)) {
                foodWanted.setText(String.valueOf(food.wanted));
                foodWanted.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (!Util.checkUserLogin(arg0.getContext(), false)) {
                            return;
                        }
                        Util.showHideView(foodWantedLoading, foodWanted);
                        HttpManager
                                .getInstance()
                                .getRestAPIClient()
                                .addFoodWanted(
                                        ConfigManager.getInstance()
                                                .getCurrentSession(),
                                        food.id,
                                        new HttpCallback<ResponseIntValue>(
                                                new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        Util.showHideView(
                                                                foodWanted,
                                                                foodWantedLoading);
                                                        // Util.sendToast(Util.getString(R.string.msg_wanted_fail));

                                                    }

                                                }) {

                                            @Override
                                            protected boolean processData(
                                                    ResponseIntValue t) {
                                                if (t.value == food.wanted) {
                                                    Util.sendToast(Util
                                                            .getString(R.string.msg_wanted_fail));
                                                    return false;
                                                }
                                                food.wanted = t.value;
                                                foodWanted.setText(String
                                                        .valueOf(food.wanted));
                                                Util.sendToast(Util
                                                        .getString(
                                                                R.string.msg_wanted_succ,
                                                                food.name));
                                                Util.showHideView(
                                                        foodWanted,
                                                        foodWantedLoading);
                                                return true;
                                            }

                                        });

                    }

                });

                Util.showHideView(foodWanted, buyViews);
                Util.showHideView(foodWanted, foodWantedLoading);
            } else {
                buyViews.setVisibility(View.GONE);
                foodWanted.setVisibility(View.GONE);
                foodWantedLoading.setVisibility(View.GONE);
            }
            foodRestViews.setVisibility(View.GONE);
        }
        if (hideBottomDivider) {
            divider.setVisibility(View.GONE);
        } else {
            divider.setVisibility(View.VISIBLE);
        }

    }
}
