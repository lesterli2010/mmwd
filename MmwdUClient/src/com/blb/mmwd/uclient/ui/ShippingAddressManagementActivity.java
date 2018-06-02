package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.ShippingAddressManager;
import com.blb.mmwd.uclient.rest.model.ShippingAddress;
import com.blb.mmwd.uclient.ui.adapter.ShippingAddressListAdapter;
import com.blb.mmwd.uclient.ui.dialog.EditShippingAddressDialog;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.util.Util;

import android.app.Dialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ShippingAddressManagementActivity extends TopCaptionActivity {
    private final static String TAG = "ShippingAddressManagementActivity";
    
    public final static int REQ_SELECT_ADDR = 1;
    public final static int RES_SELECT_ADDR_SUCC = 1;
    public final static String EXTRA_SEL_ADDR_ACTIVITY_TYPE = "extra_sel_addr_activity_type";
    public final static String EXTRA_SEL_ADDR_ACTIVITY_CROSS_AREA_ID = "extra_sel_addr_activity_cross_area_id";
    
    private int mCaptionStrId;
    private ListView mAddressList;
    private BaseAdapter mAdapter;
    private boolean mSelectAddress; // Indicate whether activity is to manage or select addr
    private int mCrossAreaId; // Indicate whether activity is to manage or select addr
    private List<ShippingAddress> mAddresses;
    private MessageViewFiller mMsgViewFiller;
    private int mSelectedAddrId;
    private boolean mAllowCreateCrossAreaAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCaptionStrId = R.string.select_shipping_address;
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                savedInstanceState = intent.getExtras();
            }
        }

        if (savedInstanceState != null) {
            mSelectAddress = savedInstanceState.getInt(EXTRA_SEL_ADDR_ACTIVITY_TYPE, 0) != 0;
            mCrossAreaId = savedInstanceState.getInt(EXTRA_SEL_ADDR_ACTIVITY_CROSS_AREA_ID, -1);
        }

        mAddressList = (ListView) findViewById(R.id.shipping_address_list);
        mMsgViewFiller = new MessageViewFiller(findViewById(R.id.error_msg_zone), mAddressList);
        if (mSelectAddress) {
            mCaptionStrId = R.string.select_shipping_address;
            mAddresses = ShippingAddressManager.getInstance().getZoneShippingAddress(
                    ConfigManager.getInstance().getConfigData().getZoneId(), mCrossAreaId);
            mAddressList.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> l, View v, int position,
                        long id) {
                    if (position >=0 && position <= mAddresses.size()) {
                        mSelectedAddrId = mAddresses.get(position).id;
                        finish();
                    }
                }
            });
            mAllowCreateCrossAreaAddress = mCrossAreaId != -1;
        } else {
            mAllowCreateCrossAreaAddress = true;
            mCaptionStrId = R.string.manage_shipping_address;
            mAddresses = ShippingAddressManager.getInstance().getShippingAddresses();
        }
        
        if (mAddresses == null || mAddresses.isEmpty()) {
            refreshList();
        } else {
            mMsgViewFiller.showMain();
        }
        
        mCaptionText.setText(Util.getString(mCaptionStrId));
        
        mAdapter = new ShippingAddressListAdapter(this, mAddresses);
        mAddressList.setAdapter(mAdapter);
        
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {  
                if (mAddresses == null || mAddresses.isEmpty()) {
                    mMsgViewFiller.fill(0, Util.getString(R.string.msg_no_shipping_address), Util.getString(R.string.btn_click_to_refresh), new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                        	refreshList();
                            
                        }
                    	
                    }, null, null);
                }
            } 
        });
        
        findViewById(R.id.add_new_shipping_address).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Dialog dialog = new EditShippingAddressDialog(
                        ShippingAddressManagementActivity.this, new Runnable() {

                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                mMsgViewFiller.showMain();
                            }
                    
                }, mAllowCreateCrossAreaAddress);

                dialog.show();
            }
            
        });
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(EXTRA_SEL_ADDR_ACTIVITY_TYPE, mSelectAddress ? 1 : 0);
        outState.putInt(EXTRA_SEL_ADDR_ACTIVITY_CROSS_AREA_ID, mCrossAreaId);
    }
    
    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(Util.EXTRA_ID, mSelectedAddrId);
        setResult(RES_SELECT_ADDR_SUCC, data);
        super.finish();
    }

    private void refreshList() {
    	mMsgViewFiller.fill(null);
        ShippingAddressManager.getInstance().getShippingAddresses(new Runnable() {

            @Override
            public void run() {
                if (mAddresses == null || mAddresses.isEmpty()) {
                    mMsgViewFiller.fill(0, Util.getString(R.string.msg_no_shipping_address), Util.getString(R.string.btn_click_to_refresh), new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            refreshList();
                        }
                        }, null, null);
                } else {
                    mAdapter.notifyDataSetChanged();
                    mMsgViewFiller.showMain();
                }
            }
        }, new Runnable() {

            @Override
            public void run() {
                mMsgViewFiller.fill(0, Util.getString(R.string.msg_no_shipping_address), Util.getString(R.string.btn_click_to_refresh), new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        refreshList();
                        
                    }}, null, null);
                
            }
            
        });
    }
    
    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_shipping_address_management;
    }

}
