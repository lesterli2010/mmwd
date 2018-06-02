package com.blb.mmwd.uclient.ui.filler;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.util.Util;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MmShopFavoriteViewFiller {
    private int mMmShopId;
    private ImageView mFavoriteImg;
    private View mFavoriteLoading;
    
    private OnClickListener mFavoriteListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            if (!Util.checkUserLogin(arg0.getContext(), false)) {
                return;
            }
            Util.showHideView(mFavoriteLoading, mFavoriteImg);
            HttpManager.getInstance().getRestAPIClient().addFavoriteMmShop(ConfigManager.getInstance().getCurrentSession(),
                    mMmShopId, new HttpCallback<ResponseHead>(new Runnable() {
                        @Override
                        public void run() {
                            Util.sendToast(R.string.msg_favorite_mmshop_fail);
                            Util.showHideView(mFavoriteImg, mFavoriteLoading);
                        }
                    }) {

                        @Override
                        protected boolean processData(ResponseHead t) {
                            Util.sendToast(R.string.msg_favorite_mmshop_succ);
                            fill(true);
                            return true;
                        }
                
            });
        }
        
    };
    
    private OnClickListener mUnFavoriteListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            if (!Util.checkUserLogin(arg0.getContext(), false)) {
                return;
            }
            Util.showHideView(mFavoriteLoading, mFavoriteImg);
            HttpManager.getInstance().getRestAPIClient().delFavoriteMmShop(ConfigManager.getInstance().getCurrentSession(),
                    mMmShopId, new HttpCallback<ResponseHead>(new Runnable() {
                        @Override
                        public void run() {
                            Util.sendToast(R.string.msg_unfavorite_mmshop_fail);
                            Util.showHideView(mFavoriteImg, mFavoriteLoading);
                        }
                    }) {

                        @Override
                        protected boolean processData(ResponseHead t) {
                            Util.sendToast(R.string.msg_unfavorite_mmshop_succ);
                            fill(false);
                            
                            return true;
                        }
                
            });
        
        }
        
    };
   
    public MmShopFavoriteViewFiller(final ImageView favoriteImg, final View favoriteLoading) {
        mFavoriteImg = favoriteImg;
        mFavoriteLoading = favoriteLoading;
        Util.showHideView(mFavoriteImg, mFavoriteLoading);
    }
    
    public MmShopFavoriteViewFiller(final int mmShopId, final ImageView favoriteImg, final View favoriteLoading) {
        mMmShopId = mmShopId;
        mFavoriteImg = favoriteImg;
        mFavoriteLoading = favoriteLoading;
        Util.showHideView(mFavoriteImg, mFavoriteLoading);
    }
    
    public void setMmShopId(final int id) {
        mMmShopId = id;
    }
    
    public void fill(final boolean favorited) {
        if (favorited) {
            mFavoriteImg.setImageResource(R.drawable.ic_favorite_favorited);
            mFavoriteImg.setOnClickListener(mUnFavoriteListener);
        } else {
            mFavoriteImg.setImageResource(R.drawable.ic_favorite_normal);
            mFavoriteImg.setOnClickListener(mFavoriteListener);
        }
        Util.showHideView(mFavoriteImg, mFavoriteLoading);
    }
}
