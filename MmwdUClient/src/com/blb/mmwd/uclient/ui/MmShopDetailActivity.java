package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.MmShop;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.MmShopDetails;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter;
import com.blb.mmwd.uclient.ui.adapter.FragmentTabMenuAdapter;
import com.blb.mmwd.uclient.ui.filler.MmShopFavoriteViewFiller;
import com.blb.mmwd.uclient.ui.fragment.ContentListFragment;
import com.blb.mmwd.uclient.util.Util;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class MmShopDetailActivity extends TopCaptionActivity {
    private final static String TAG = "MmShopDetailActivity";
    private SliderLayout mImageScroll;
    
    private FragmentTabHost mTabHost;
    private FragmentTabMenuAdapter mTabMenuAdapter;
    
    private int mMmShopId;
    private MmShopDetails mMmShopDetail;
    private MmShopFavoriteViewFiller mFavoriteFiller;

    private ImageView mFavoriteImg;
    

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
            mMmShopId = savedInstanceState.getInt(Util.EXTRA_ID, 0);
        }
        
        HttpManager.getInstance().getRestAPIClient().getMmShopDetailBase(
                ConfigManager.getInstance().getCurrentSession(),
                mMmShopId, 
                new HttpCallback<MmShopDetails>(new Runnable() {

            @Override
            public void run() {
                Util.sendToast(R.string.msg_no_mmshop);
                
            }
            
        }) {

            @Override
            protected boolean processData(MmShopDetails t) {
                mMmShopDetail = t;
                refreshView();
                return true;
            }
            
        });
                    
        
        initFragmentTab();
        
        mFavoriteImg = (ImageView) findViewById(R.id.mm_shop_detail_favorite_img);
        mFavoriteFiller = new MmShopFavoriteViewFiller(mMmShopId, mFavoriteImg, findViewById(R.id.mm_shop_detail_favorite_loading));
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Util.EXTRA_ID, mMmShopId);
    }

    private void initFragmentTab() {
        List<FragmentTabMenuAdapter.FragmentInfo> fragmentInfoList = new ArrayList<FragmentTabMenuAdapter.FragmentInfo>();
        String[] names = getResources().getStringArray(
                R.array.mm_shop_select_content_type);

        Bundle arg1 = new Bundle();
        arg1.putInt(Util.EXTRA_CONTENT_LIST_TYPE,
                Util.ContentListType.MM_SHOP_ACTIVE_FOOD.ordinal());
        arg1.putInt(Util.EXTRA_ID, mMmShopId);
        Bundle arg2 = new Bundle();
        arg2.putInt(Util.EXTRA_CONTENT_LIST_TYPE,
                Util.ContentListType.MM_SHOP_INACTIVE_FOOD.ordinal());
        arg2.putInt(Util.EXTRA_ID, mMmShopId);

        fragmentInfoList.add(new FragmentTabMenuAdapter.FragmentInfo(
                ContentListFragment.class, names[0], arg1));
        fragmentInfoList.add(new FragmentTabMenuAdapter.FragmentInfo(
                ContentListFragment.class, names[1], arg2));

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabMenuAdapter = new FragmentTabMenuAdapter(this,
                getSupportFragmentManager(), mTabHost, fragmentInfoList,
                (ViewPager) findViewById(R.id.view_pager));
        mTabMenuAdapter.init();
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
    }
    
    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_mm_shop_detail;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mTabMenuAdapter.notifyCurrentFragmentDataChange();
    }
    
    private void refreshView() {
        
        mCaptionText.setText(mMmShopDetail.name);
        ImageLoader.getInstance().displayImage(HttpManager.getInstance().getRealImageUrl(mMmShopDetail.photo),
                (ImageView) findViewById(R.id.mm_shop_detail_img), Util.sMmwdImageOptions);
                
        ((TextView) findViewById(R.id.mm_shop_detail_name)).setText(mMmShopDetail.name);
        int starImgRes = Util.getStarImgRes(mMmShopDetail.star);
        if (starImgRes == 0) {
           ((ImageView) findViewById(R.id.mm_shop_detail_rating)).setVisibility(View.GONE);
        } else {
            ((ImageView) findViewById(R.id.mm_shop_detail_rating)).setImageResource(Util.getStarImgRes(mMmShopDetail.star));
            
        }
        mFavoriteFiller.fill(mMmShopDetail.isFaved);
        
        initImageScroll();
    }
    
    private void initImageScroll() {
        mImageScroll  = (SliderLayout)findViewById(R.id.image_slider);
        
        HashMap<String,String> url_maps = new HashMap<String, String>();
        for (int i = 0; mMmShopDetail.imgs != null && i < mMmShopDetail.imgs.length; i++) {
            url_maps.put("³ø·¿ÕÕÆ¬", HttpManager.getInstance().getRealImageUrl(mMmShopDetail.imgs[i]));
        
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                   // .description(name)
                    .image(HttpManager.getInstance().getRealImageUrl(mMmShopDetail.imgs[i]))
                    .setScaleType(BaseSliderView.ScaleType.Fit);
//                    .setOnSliderClickListener(this);

            //add your extra information
         //   textSliderView.getBundle()
          //          .putString("extra",name);

            mImageScroll.addSlider(textSliderView);
            
        }
        
        mImageScroll.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mImageScroll.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mImageScroll.setCustomAnimation(new DescriptionAnimation());
        mImageScroll.setDuration(4000);
    }

}
