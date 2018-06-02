package com.blb.mmwd.uclient.ui.adapter;

import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.model.FavoriteMmShop;
import com.blb.mmwd.uclient.ui.MmShopDetailActivity;
import com.blb.mmwd.uclient.ui.filler.MmShopFavoriteViewFiller;
import com.blb.mmwd.uclient.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MmShopFavoriteListAdapter extends BaseAdapter {
    private List<FavoriteMmShop> mMmShopItems;;
    private LayoutInflater mInflater;
    private static class ViewHolder {
        ImageView actionImg;
        ImageView mmShopImg;
        TextView name;
        MmShopFavoriteViewFiller filler;
        // ImageView mSelectedImage;
    }
    
    public MmShopFavoriteListAdapter(Context context, List<FavoriteMmShop> list) {
        mMmShopItems = list;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mMmShopItems != null ? mMmShopItems.size() : 0;
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
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_favorited_mm_shop, parent, false);
            holder.actionImg = (ImageView) view.findViewById(R.id.favorited_mm_shop_action);
            holder.name = (TextView) view.findViewById(R.id.favorited_mm_shop_img_name);
            holder.mmShopImg = (ImageView) view.findViewById(R.id.favorited_mm_shop_img);
            // holder.mSelectedImage = (ImageView)
            // view.findViewById(R.id.select_item_selected_image);
            view.setTag(holder);
            holder.filler = new MmShopFavoriteViewFiller((ImageView) view.findViewById(R.id.favorited_mm_shop_action),
                    view.findViewById(R.id.favorited_mm_shop_action_loading));
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final FavoriteMmShop item = mMmShopItems.get(position);
        holder.name.setText(item.mmname);
        ImageLoader.getInstance().displayImage(
                HttpManager.getInstance().getRealImageUrl(item.mmurl),
                holder.mmShopImg, Util.sMmShopImageOptions);
        holder.filler.setMmShopId(item.mmid);
        holder.filler.fill(true);
        
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.startActivity(arg0.getContext(), MmShopDetailActivity.class, item.mmid);
            }
            
        });
        return view;
    }

}
