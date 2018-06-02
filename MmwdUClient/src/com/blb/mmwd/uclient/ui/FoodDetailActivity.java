package com.blb.mmwd.uclient.ui;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.filler.FoodViewFiller;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

// show browser
public class FoodDetailActivity extends TopCaptionActivity {
    private final static boolean DEBUG = true;
    private final static String TAG = "FoodDetailActivity";

    private WebView mWebView;
    private MessageViewFiller mMsgFiller;
    private int mFoodId;
    private String mFoodName;
    private Food mFood;
    private FoodViewFiller mFoodViewFiller;
    
    private View mSettlementBar;
    private TextView mTotalMoney;
    private ImageButton mClearSettlementBtn;
    
    private View mMmShopView;
    private ImageView mMmShopImg;
    private TextView mMmShopName;
    
    private ContentListAdapter.FoodCartListener mFoodCartListener = new ContentListAdapter.FoodCartListener() {
        @Override
        public void updateFood(OrderFoodItem item) {

            CartManager.getInstance().updateFood(item);
            refreshSettlementBar();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                savedInstanceState = intent.getExtras();
            }
        }

        if (savedInstanceState != null) {
            mFoodId = savedInstanceState.getInt(Util.EXTRA_ID, 0);
            mFoodName = savedInstanceState
                    .getString(Util.EXTRA_NAME);
        }
        
        if (mFoodName != null) {
            mCaptionText.setText(mFoodName);
        }
        
        mWebView = (WebView) findViewById(R.id.food_detail_web);
        mMsgFiller = new MessageViewFiller(findViewById(R.id.error_msg_zone), findViewById(R.id.food_detail_main));
        mFoodViewFiller = new FoodViewFiller(this, findViewById(R.id.content_list_food), mFoodCartListener, FoodViewFiller.SHOW_MM_SHOP_VIEW | FoodViewFiller.SHOW_WANTED_VIEW);
        mSettlementBar = findViewById(R.id.settlement_bar);
        mTotalMoney = (TextView) mSettlementBar.findViewById(R.id.total_amount);
        mClearSettlementBtn = (ImageButton) mSettlementBar
                .findViewById(R.id.settlement_clear_btn);// settlement_clear_btn
        ((Button) findViewById(R.id.settlement_btn))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (Util.checkUserLogin(FoodDetailActivity.this, true)) {
                            if (CartManager.getInstance().isSelectSettlementNeeded()) {
                                Util.startActivity(FoodDetailActivity.this, OrderSelectSettlementActivity.class);
                            } else {
                                Util.startActivity(FoodDetailActivity.this, OrderSettlementActivity.class);
                            }
                        }
                    }

                });
        mClearSettlementBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // String title, String content, String positiveStr, Runnable
                // listener) {
                Dialog d = new ConfirmationDialog(FoodDetailActivity.this, Util
                        .getString(R.string.dialog_clear_cart_title), Util
                        .getString(R.string.dialog_clear_cart_content), null,
                        new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                CartManager.getInstance().clear();
                                refreshSettlementBar();
                                mFoodViewFiller.fill(mFood, true);
                            }

                        });
                d.show();
            }
        });
                
        mMmShopView = findViewById(R.id.mm_shop_name_zone);
        mMmShopImg = (ImageView) findViewById(R.id.mm_shop_img);
        mMmShopName = (TextView) findViewById(R.id.mm_shop_name);
        
        refreshView();
    }
    
    private void refreshView() {
        mMsgFiller.fill(mFoodName + "详情加载中...");
        HttpManager.getInstance().getRestAPIClient().getFoodDetail(mFoodId, new HttpCallback<Food>(new Runnable() {

            @Override
            public void run() {
                mMsgFiller.fill(0, mFoodName + "没有更多内容了", Util.getString(R.string.btn_click_to_refresh), new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        refreshView();
                        
                    }
                    
                }, null, null);
                
            }
            
        }) {

            @Override
            protected boolean processData(Food t) {
                mFood = t;
                mFoodViewFiller.fill(mFood, true);
                mMsgFiller.showMain();
                
                ImageLoader.getInstance().displayImage(
                        HttpManager.getInstance().getRealImageUrl(mFood.mmImg),
                        mMmShopImg, Util.sMmShopImageOptions);
                mMmShopName.setText(mFood.mmName);
                mMmShopView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Util.startActivity(FoodDetailActivity.this,
                                MmShopDetailActivity.class, mFood.mmid);
                    }

                });
                
                String encodedContent = mFood.content;
                if (!TextUtils.isEmpty(encodedContent)) {
                    // Convert from Base64 to local
                    
                    byte[] b = Base64.decode(encodedContent, Base64.DEFAULT);
                    String decodedHtml = new String(b);
                   // if (DEBUG)
                   // Log.d(TAG, "decodedHtml:" + decodedHtml);
                    mWebView.getSettings().setJavaScriptEnabled(true);
                    mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
                    mWebView.loadDataWithBaseURL(HttpManager.getInstance().getBaseUrl(), decodedHtml, "text/html", null, null);
                }
                refreshSettlementBar();
                return true;
            }
            
        });
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Util.EXTRA_ID, mFoodId);
        outState.putString(Util.EXTRA_NAME, mFoodName);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mFood != null) {
            refreshSettlementBar();
            mFoodViewFiller.fill(mFood, true);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_food_detail;
    }
    
    
    
    

}
