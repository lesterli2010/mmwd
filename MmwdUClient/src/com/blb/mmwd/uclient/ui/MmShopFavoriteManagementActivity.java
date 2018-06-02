package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.FavoriteMmShop;
import com.blb.mmwd.uclient.rest.model.response.FavoriteMmShops;
import com.blb.mmwd.uclient.ui.adapter.MmShopFavoriteListAdapter;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.dialog.MmwdDialog;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.util.Util;

public class MmShopFavoriteManagementActivity extends TopCaptionActivity {

    private List<FavoriteMmShop> mMmShopList = new ArrayList<FavoriteMmShop>();
    private BaseAdapter mAdapter;
    private ListView mList;
    private MessageViewFiller mMsgViewFiller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tmp, shall query from server
        mList = (ListView) findViewById(R.id.favorite_mm_shop_list);
        mAdapter = new MmShopFavoriteListAdapter(this, mMmShopList);
        mList.setAdapter(mAdapter);
        mMsgViewFiller = new MessageViewFiller(findViewById(R.id.error_msg_zone), mList);
        refreshList();
        /*
        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                    long arg3) {
                Util.sendToast("pos:" + pos + ", size: " + mMmShopList.size());
                if (pos >= 0 && pos < mMmShopList.size()) {
                    Util.startActivity(MmShopFavoriteManagementActivity.this, 
                            MmShopDetailActivity.class, mMmShopList.get(pos).mmid);
                }
                
            }
            
        });*/
        //this.findViewById(R.id.cancel_favorite_btn).setOnClickListener(new OnClickListener() {

          //  @Override
           // public void onClick(View arg0) {
            //    cancelFavoriteDialog();
                
            //}
            
        //});
    }
    
    private Runnable mFailListener = new Runnable() {

        @Override
        public void run() {
            mMsgViewFiller.fill(0, Util.getString(R.string.msg_no_favorite_mmshop),
                    "·µ »Ø", new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    finish();
                    
                }
        
    },
                    "Ë¢ ÐÂ", new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            refreshList();
                            
                        }
                
            });
            
        }
     
    };
    
    private void refreshList() {
        mMsgViewFiller.fill(Util.getString(R.string.loading_inprog_hard));
        HttpManager.getInstance().getRestAPIClient().getFavoriteMmShopList(ConfigManager.getInstance().getCurrentSession(),
                new HttpCallback<FavoriteMmShops>(mFailListener) {

                    @Override
                    protected boolean processData(FavoriteMmShops t) {
                        if (t.list != null && !t.list.isEmpty()) {
                            mMmShopList.clear();
                            for (FavoriteMmShop item : t.list) {
                                mMmShopList.add(item);
                                mAdapter.notifyDataSetChanged();
                                mMsgViewFiller.showMain();
                            }
                            return true;
                        }
                        return false;
                    }
            
        });
        
    }
    
    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_mm_shop_favorite;
    }

}
