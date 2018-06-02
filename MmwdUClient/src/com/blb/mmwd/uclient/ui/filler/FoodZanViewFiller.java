package com.blb.mmwd.uclient.ui.filler;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;
import com.blb.mmwd.uclient.util.AnimationUtil;
import com.blb.mmwd.uclient.util.Util;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodZanViewFiller {
    private View mRootView;
    private ImageView mImg;
    private TextView mTxt;
    
    private int mZan;
    private int mFoodId;
    private boolean mLiked; // 已赞?
    
    public FoodZanViewFiller(View view) {
        mRootView = view;
        mImg = (ImageView) view.findViewById(R.id.food_zan_img);
        mTxt = (TextView) view.findViewById(R.id.food_zan);
    }
    
    public void fill(int foodId, int zan, boolean liked) {
        mZan = zan;
        mFoodId = foodId;
        mLiked = liked;
        if (mZan == 0) {
            mTxt.setVisibility(View.GONE);
        } else {
            mTxt.setText(String.valueOf(mZan));
        }
        
        if (!mLiked) {
        mRootView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!ConfigManager.getInstance().isUserLogined()) {
                    Util.sendToast("您还未登陆，请先登录才能点赞~");
                    return;
                }
                HttpManager.getInstance().getRestAPIClient().addFoodZan(
                        ConfigManager.getInstance().getCurrentSession(), 
                        mFoodId, 
                        new HttpCallback<ResponseIntValue>(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mLiked = true;
                                fill(mFoodId, mZan, mLiked);
                                AnimationUtil.startScaleAnimation(mImg);
                                ConfigManager.getInstance().addLikedFood(mFoodId);
                            }
                            
                        }) {

                            @Override
                            protected boolean processData(ResponseIntValue t) {
                                mLiked = true;
                                mZan = t.value;
                                fill(mFoodId, mZan, mLiked);
                                AnimationUtil.startScaleAnimation(mImg);
                                ConfigManager.getInstance().addLikedFood(mFoodId);
                                return true;
                            }
                            
                        });
            }
            
        });
        } else {
            mRootView.setOnClickListener(null);
            mImg.setImageResource(R.drawable.ic_like_liked);
            mTxt.setTextColor(mRootView.getContext().getResources()
                    .getColor(R.color.main_color));
        }
    }
    
    

}
