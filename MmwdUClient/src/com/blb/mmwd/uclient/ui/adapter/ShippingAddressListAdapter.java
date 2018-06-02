package com.blb.mmwd.uclient.ui.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ShippingAddressManager;
import com.blb.mmwd.uclient.rest.model.ShippingAddress;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.dialog.EditShippingAddressDialog;
import com.blb.mmwd.uclient.ui.dialog.InformationDialog;
import com.blb.mmwd.uclient.util.Util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ShippingAddressListAdapter extends BaseAdapter {
    private final static String TAG = "ShippingAddressListAdapter";
    private List<ShippingAddress> mShippingAddresses;
    private EditShippingAddressDialog mEditDialog;
    private WeakReference<Context> mContext;
    private LayoutInflater mInflater;
    public ShippingAddressListAdapter(Context context, List<ShippingAddress> shippingAddresses) {
        mInflater = LayoutInflater.from(context);
        mShippingAddresses = shippingAddresses;
        mContext = new WeakReference<Context>(context);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            OnClickListener {
        int mAddrId;
        private TextView mPhone;
        private TextView mAddress;
       // private WeakReference<Context> mContext;
        private PopupWindow mEditMenuWindow;

        public ViewHolder(View v) {
            super(v);
         //   mContext = new WeakReference<Context>(context);
            mPhone = (TextView) v.findViewById(R.id.txt_phone);
            mAddress = (TextView) v.findViewById(R.id.txt_address);
            // TODO Auto-generated constructor stub
            v.findViewById(R.id.ll_toolbar).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mEditMenuWindow == null) {
                View menuView = mInflater.inflate(
                        R.layout.menu_edit_del_address, null);

                mEditMenuWindow = new PopupWindow(menuView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                
                mEditMenuWindow.setOutsideTouchable(true);
                mEditMenuWindow.setFocusable(true);
                mEditMenuWindow
                        .setBackgroundDrawable(mContext
                                .get()
                                .getResources()
                                .getDrawable(
                                        R.drawable.abc_ab_stacked_transparent_light_holo));
                menuView.findViewById(R.id.tv_edit).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                mEditMenuWindow.dismiss();
                                openEditShippingAddressDialog(mContext.get(), mAddrId);
                            }

                        });
                menuView.findViewById(R.id.tv_del).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // TODO Auto-generated method stub
                                mEditMenuWindow.dismiss();
                                Dialog dialog = new ConfirmationDialog(mContext.get(), 
                                        Util.getString(R.string.dialog_confirm_del_addr_title), 
                                        Util.getString(R.string.dialog_confirm_del_addr_content),
                                        null, new Runnable() {

                                            @Override
                                            public void run() {
                                                final Dialog infoDiag = new InformationDialog(mContext.get(), R.string.dialog_del_inprog);
                                                infoDiag.show();
                                                ShippingAddressManager.getInstance().delAddress(mAddrId, new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        infoDiag.dismiss();
                                                        ShippingAddressListAdapter.this.notifyDataSetChanged();
                                                    }
                                                    
                                                }, new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        infoDiag.dismiss();
                                                        Util.sendToast(R.string.msg_del_fail);
                                                        
                                                    }
                                                    
                                                });
                                                
                                            }
                                    
                                });
                                dialog.show();
                            }
                            
                        });
            }
            if (mEditMenuWindow.isShowing()) {
                mEditMenuWindow.dismiss();
            } else {
               // mEditMenuWindow.showAtLocation(v,  Gravity.RIGHT | Gravity.BOTTOM, 10,0); 
                mEditMenuWindow.showAsDropDown(v, -20, 0);
            }
        }

    }

    
    private void openEditShippingAddressDialog(Context context, int addrId) {
        if (addrId == 0) {
            // error case
            Util.sendToast(R.string.edit_shipping_address_dialog_failure);
            return;
        }
        
        mEditDialog = new EditShippingAddressDialog(context, addrId, new Runnable() {

            @Override
            public void run() {
                ShippingAddressListAdapter.this.notifyDataSetChanged();
            }
            
        });
        mEditDialog.show();
        
    }

    @Override
    public int getCount() {
        return mShippingAddresses != null ? mShippingAddresses.size() : 0;
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
        Log.d(TAG, "getView:" + position);
        if (view == null) {
            
            view = mInflater.inflate(R.layout.item_shipping_address, parent, false);
            holder = new ViewHolder(view);
         //   holder.mIcon = (ImageView) view.findViewById(R.id.mine_menu_icon);
          //  holder.mName = (TextView) view.findViewById(R.id.mine_menu_name);
            // holder.mSelectedImage = (ImageView)
            // view.findViewById(R.id.select_item_selected_image);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        ShippingAddress addr = mShippingAddresses.get(position);
        holder.mPhone.setText(addr.phone);
        holder.mAddress.setText(addr.getFullAddress());
        holder.mAddrId = addr.id;
        return view;
    }

}
