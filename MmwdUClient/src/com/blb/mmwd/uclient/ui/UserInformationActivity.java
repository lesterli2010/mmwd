package com.blb.mmwd.uclient.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.ui.dialog.RequestVCodeDialog.RequestVCodeType;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.ui.adapter.ListMenuAdapter;
import com.blb.mmwd.uclient.ui.dialog.RequestVCodeDialog;
import com.blb.mmwd.uclient.ui.dialog.ChangePasswordDialog;
import com.blb.mmwd.uclient.ui.dialog.ChangeUserNameDialog;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.dialog.MmwdDialog;
import com.blb.mmwd.uclient.util.Util;

public class UserInformationActivity extends TopCaptionActivity implements Runnable {

    private ListView mList;
    private ListMenuAdapter mAdapter;
    private List<ListMenuAdapter.ListMenu> mMenus;
    private ListMenuAdapter.ListMenu mUserNameMenu;
    private ListMenuAdapter.ListMenu mChangePhoneMenu;
    private Button mLogoutBtn;
    private User mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentUser = ConfigManager.getInstance().getCurrentUser();
        if (mCurrentUser == null) {
            // Error
            finish();
            return;
        }
        mLogoutBtn = (Button) findViewById(R.id.user_logout_btn);
        mLogoutBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // dialog to confirm?
                Dialog dialog = new ConfirmationDialog(UserInformationActivity.this,
                        Util.getString(R.string.diag_logout_title),
                        Util.getString(R.string.diag_logout_content),
                        null, new Runnable() {

                            @Override
                            public void run() {
                                refreshLogoutBtn(false);
                                ConfigManager.getInstance().userLogout(new Runnable() {
                                    @Override
                                    public void run() {
                                        Util.sendToast(Util.getString(R.string.msg_logout_succ));
                                       // refreshLogoutBtn(true);
                                        finish();
                                    }
                                }, new Runnable() {

                                    @Override
                                    public void run() {
                                        refreshLogoutBtn(true);
                                        Util.sendToast(Util.getString(R.string.msg_logout_fail));
                                    }
                                });
                            }
                });
                dialog.show();
                
                
            }
            
        });
        mList = (ListView) findViewById(R.id.user_info_menu);
        mMenus = new ArrayList<ListMenuAdapter.ListMenu>();
        mUserNameMenu = new ListMenuAdapter.ListMenu(R.drawable.ic_mine_menu_user,
                mCurrentUser.username, 
                new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!Util.checkUserLogin(UserInformationActivity.this, false)) {
                    return;
                }
                Dialog dialog = new ChangeUserNameDialog(UserInformationActivity.this, UserInformationActivity.this);
                dialog.show();
            }
        });
        mMenus.add(mUserNameMenu);
        
        mChangePhoneMenu = new ListMenuAdapter.ListMenu(R.drawable.ic_mine_menu_service_call, 
                Util.getString(R.string.binded_phone, mCurrentUser.bindedPhone), new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!Util.checkUserLogin(UserInformationActivity.this, false)) {
                    return;
                }
                Dialog dialog = new RequestVCodeDialog(UserInformationActivity.this, mChangePhoneSuccCallback, RequestVCodeType.CHANGEPHONE);
                dialog.show();
            }
    
});
        mMenus.add(mChangePhoneMenu);
        
        mMenus.add(new ListMenuAdapter.ListMenu(R.drawable.ic_mine_menu_score, 
                getString(R.string.user_score, String.valueOf(ConfigManager.getInstance().getCurrentScore())), null));
        mMenus.add(new ListMenuAdapter.ListMenu(R.drawable.ic_mine_menu_password,
                getString(R.string.change_pass), new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (!Util.checkUserLogin(UserInformationActivity.this, false)) {
                            return;
                        }
                        Dialog dialog = new ChangePasswordDialog(UserInformationActivity.this, null);
                        dialog.show();
                    }
        }));
        mAdapter = new ListMenuAdapter(this, mMenus);
        mList.setAdapter(mAdapter);
    }
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_user_information;
    }
    
    private Runnable mChangePhoneSuccCallback = new Runnable() {

        @Override
        public void run() {
            mChangePhoneMenu.name = Util.getString(R.string.binded_phone, mCurrentUser.bindedPhone);
            mAdapter.notifyDataSetChanged();
            
            promptToChangePass();
            
        }
        
    };
    
    private void promptToChangePass() {
        // ontext context, String title, String content, String positiveStr, String negativeStr, Runnable listener
        Dialog dialog = new ConfirmationDialog(this, 
                "建议修改密码", 
                "您好，账户登录密码已经被重置为您刚收到的验证码，建议您立即修改以确保账户安全，谢谢。", "立即修改", "以后再说", 
                
                new Runnable() {

                    @Override
                    public void run() {
                        Dialog changePassDialog = new ChangePasswordDialog(UserInformationActivity.this, null);
                        changePassDialog.show();
                    }
            
        });
        dialog.show();
    }
    // Runnable
    @Override
    public void run() {
            mUserNameMenu.name = ConfigManager.getInstance().getCurrentUserName();
            mChangePhoneMenu.name = Util.getString(R.string.binded_phone, mCurrentUser.bindedPhone);
            mAdapter.notifyDataSetChanged();
        
    }
    
    private void refreshLogoutBtn(boolean enable) {
        mLogoutBtn.setEnabled(enable);
        if (enable) {
            mLogoutBtn.setText(Util.getString(R.string.dialog_logout));
        } else {
            mLogoutBtn.setText(Util.getString(R.string.dialog_logout_inprog));
        }
    }
}
