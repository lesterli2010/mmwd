package com.blb.mmwd.uclient.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.UpgradeManager;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.ui.AboutUsActivity;
import com.blb.mmwd.uclient.ui.CommentActivity;
import com.blb.mmwd.uclient.ui.MmShopFavoriteManagementActivity;
import com.blb.mmwd.uclient.ui.ShippingAddressManagementActivity;
import com.blb.mmwd.uclient.ui.UserInformationActivity;
import com.blb.mmwd.uclient.ui.UserLoginActivity;
import com.blb.mmwd.uclient.ui.adapter.ListMenuAdapter;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.dialog.MmwdDialog;
import com.blb.mmwd.uclient.ui.dialog.RequestVCodeDialog;
import com.blb.mmwd.uclient.util.Util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MineFragment extends SingleViewFragment {
    private final static String TAG = "MineFragment";
    private TextView mLoginText;
    private ListView mMenu;
    private ListMenuAdapter mAdapter;
    private User mCurrentUser;
    private Button mLoginBtn;
    private View mAlreadyLoginView;

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.fragment_mine;
    }

    @Override
    public void refreshView() {
        if (!mInitialized) {
            return;
        }
        mCurrentUser = ConfigManager.getInstance().getCurrentUser();
        if (mCurrentUser != null && mCurrentUser.isLogined()) {
            this.mLoginText.setText(getActivity().getString(R.string.user_already_login, mCurrentUser.username));
           Util.showHideView(mAlreadyLoginView, mLoginBtn);
        } else {
            Util.showHideView(mLoginBtn, mAlreadyLoginView);
        }
    }

    @Override
    protected void initView() {
        mLoginText = (TextView) mRootView.findViewById(R.id.login_txt);
        mLoginBtn = (Button) mRootView.findViewById(R.id.click_to_login);
        mAlreadyLoginView = mRootView.findViewById(R.id.user_already_login_zone);
        
        mLoginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.startActivity(getActivity(), UserLoginActivity.class);
            }
            
        });
        mLoginText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.startActivity(getActivity(), UserInformationActivity.class);
                
            }
            
        });
        
        mMenu = (ListView) mRootView.findViewById(R.id.mine_menu);
        List<ListMenuAdapter.ListMenu> menus = new ArrayList<ListMenuAdapter.ListMenu>();

        menus.add(new ListMenuAdapter.ListMenu(
                R.drawable.ic_mine_menu_shipping_address,
                getString(R.string.mine_menu_shipping_address),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (Util.checkUserLogin(getActivity(), false)) {
                            Util.startActivity(getActivity(),
                                    ShippingAddressManagementActivity.class);
                        }
                    }

                }));
        
        menus.add(new ListMenuAdapter.ListMenu(
                R.drawable.ic_mine_menu_favorite,
                getString(R.string.mine_menu_favorite),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (Util.checkUserLogin(getActivity(), false)) {
                            Util.startActivity(getActivity(),
                                    MmShopFavoriteManagementActivity.class);
                        }
                    }

                }));
        
        menus.add(new ListMenuAdapter.ListMenu(
                R.drawable.ic_mine_menu_upgrade,
                getString(R.string.mine_menu_upgrade),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        UpgradeManager.getInstance().checkUpgrade(
                                getActivity(), false);
                    }

                }));
        
        menus.add(new ListMenuAdapter.ListMenu(
                R.drawable.ic_mine_menu_service_call,
                getString(R.string.mine_menu_service_call),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Util.callServiceNumber(getActivity());
                    }

                }));
        
        menus.add(new ListMenuAdapter.ListMenu(
                R.drawable.ic_mine_menu_comment,
                getString(R.string.mine_menu_comment),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (Util.checkUserLogin(getActivity(), false)) {
                            Util.startActivity(getActivity(), CommentActivity.class);
                        }
                    }

                }));
        
        menus.add(new ListMenuAdapter.ListMenu(
                R.drawable.ic_mine_menu_about,
                getString(R.string.mine_menu_about),
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Util.startActivity(getActivity(), AboutUsActivity.class);
                    }

                }));
        
        mAdapter = new ListMenuAdapter(getActivity(), menus);
        mMenu.setAdapter(mAdapter);
    }
}
