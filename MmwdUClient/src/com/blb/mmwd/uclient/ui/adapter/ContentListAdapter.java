package com.blb.mmwd.uclient.ui.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.MmShop;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;
import com.blb.mmwd.uclient.ui.FoodDetailActivity;
import com.blb.mmwd.uclient.ui.MmShopDetailActivity;
import com.blb.mmwd.uclient.ui.filler.FoodSelectionViewFiller;
import com.blb.mmwd.uclient.ui.filler.FoodViewFiller;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.ContentListType;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ContentListAdapter extends
        RecyclerView.Adapter<ContentListAdapter.ViewHolder> {
    private final static boolean DEBUG = true;
    private WeakReference<Context> mContext;
    private final static String TAG = "ContentListAdapter";
    private FoodCartListener mFoodCartListener;
    private List<Object> mDataList; // MmShop or Food
    private ContentListType mContentType;

    public interface FoodCartListener {
        public void updateFood(OrderFoodItem item);
    }

   

    // Provide a reference to the type of views that you are using
    // (custom viewholder)
    public class ViewHolder extends RecyclerView.ViewHolder {
        // WeakReference<Context> mContext;

        public View mRootView;

        public class MmShopViews {
            public View mMmShopNameZone;
            public TextView mMmShopName;
            public ImageView mMmShopImage;
            public ViewGroup mFoodList;
            // public ViewGroup mMoreFoodList;
            // public ViewGroup mMoreFoodZone;
        }

        public MmShopViews mMmShopViews = new MmShopViews();
        public View mFoodViews;

        // public ImageView mSelectedImage;
        // public int mSelectionItemId;

        public ViewHolder(View v) {
            super(v);
            mRootView = v;
            // mContext = new WeakReference<Context>(context);
            if (mContentType == Util.ContentListType.ALL_HOT_MM_SHOP) {
                mMmShopViews.mMmShopNameZone = v
                        .findViewById(R.id.mm_shop_name_zone);
                mMmShopViews.mMmShopName = (TextView) v
                        .findViewById(R.id.mm_shop_name);
                mMmShopViews.mMmShopImage = (ImageView) v
                        .findViewById(R.id.mm_shop_img);
                mMmShopViews.mFoodList = (ViewGroup) v
                        .findViewById(R.id.food_list_in_mm_shop);
            } else {
                mFoodViews = v;
            }
        }
    }

    public void setContentListType(ContentListType type) {
        mContentType = type;
    }

    public ContentListAdapter(Context context, ContentListType type,
            List dataList, FoodCartListener foodListener) {
        mContext = new WeakReference<Context>(context);
        mContentType = type;
        mDataList = dataList;


        mFoodCartListener = foodListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ContentListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        // create a new view
        View v = null;
        if (mContentType == Util.ContentListType.ALL_HOT_MM_SHOP) {
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_content_list_mm_shop, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_content_list_food, parent, false);
        }
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (DEBUG)
            Log.d(TAG, "onBindViewHolder, position:" + position);
        if (mContentType == Util.ContentListType.ALL_HOT_MM_SHOP) {

            final MmShop shop = (MmShop) mDataList.get(position);
            // Init shop part
            holder.mMmShopViews.mMmShopNameZone
                    .setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.startActivity(arg0.getContext(),
                                    MmShopDetailActivity.class, shop.id);

                        }

                    });
            if (shop.on) {
                holder.mMmShopViews.mMmShopName.setText(shop.name);
                ViewHelper.setAlpha(holder.mRootView, 1f);
            } else {
                holder.mMmShopViews.mMmShopName.setText(shop.name
                        + Util.getString(R.string.resting));
                ViewHelper.setAlpha(holder.mRootView, Util.INACTIVE_ALPHA); // fo inactive
            }

            ImageLoader.getInstance().displayImage(
                    HttpManager.getInstance().getRealImageUrl(shop.img),
                    holder.mMmShopViews.mMmShopImage, Util.sMmShopImageOptions);
            // Init food part
            List<Food> foodList = shop.ps;
            holder.mMmShopViews.mFoodList.removeAllViews();
            // holder.mMmShopViews.mMoreFoodList.removeAllViews();
            // holder.mMmShopViews.mMoreFoodList.setVisibility(View.GONE);
            int maxItems = foodList.size();// > Util.MAX_FOOD_IN_SHOP ? Util.MAX_FOOD_IN_SHOP
                    //: foodList.size();
            for (int i = 0; i < maxItems; i++) {
                final Food food = foodList.get(i);
                if (!shop.on && food.active) {
                    food.active = false; // all foods are non-active
                }
                
                // Copy mm Shop's data 
                food.mmid = shop.id;
                food.mmImg = shop.img;
                food.mmName = shop.name;
                
                View view = LayoutInflater.from(mContext.get()).inflate(
                        R.layout.item_content_list_food, null);

                FoodViewFiller filler = (FoodViewFiller) view.getTag();
                if (filler == null) {
                    int displayType = FoodViewFiller.CLICKABLE;
                    if (!shop.on) {
                        displayType |= FoodViewFiller.NOT_SHOW_INACTIVE_ALPHA;
                    }
                    filler = new FoodViewFiller(mContext.get(), view, mFoodCartListener, displayType);
                    view.setTag(filler);
                }
                filler.fill(food, (i == (maxItems - 1)));
                holder.mMmShopViews.mFoodList.addView(view);
            }
        } else {

            FoodViewFiller filler = (FoodViewFiller) holder.mFoodViews
                    .getTag();
            if (filler == null) {
                int displayType = 0;
                if (mContentType == ContentListType.ALL_HOT_FOOD) {
                    displayType = FoodViewFiller.SHOW_MM_SHOP_VIEW;
                } else if (mContentType == ContentListType.MM_SHOP_INACTIVE_FOOD) {
                    displayType = FoodViewFiller.SHOW_WANTED_VIEW;
                }
                
                filler = new FoodViewFiller(mContext.get(), holder.mFoodViews, mFoodCartListener, displayType | FoodViewFiller.CLICKABLE);
                holder.mFoodViews.setTag(filler);
            }
            final Food food = (Food) mDataList.get(position);
            filler.fill(food, position == (mDataList.size() - 1));
        }

        if (DEBUG)
            Log.d(TAG, "onBindViewHolder, position:" + position
                    + ", getItemCount:" + getItemCount());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }
}
