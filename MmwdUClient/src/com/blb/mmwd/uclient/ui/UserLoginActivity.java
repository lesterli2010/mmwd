package com.blb.mmwd.uclient.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.ui.dialog.RequestVCodeDialog.RequestVCodeType;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.ui.dialog.ChangePasswordDialog;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.dialog.RequestVCodeDialog;
import com.blb.mmwd.uclient.ui.dialog.MmwdDialog;
import com.blb.mmwd.uclient.util.Util;

public class UserLoginActivity extends TopCaptionActivity {
    private EditText mUserNameEditText;
    private EditText mPassEditText;
    private Button mLoginBtn;
    private View mLoginedView;
    private View mLoginView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLoginedView = findViewById(R.id.user_logined_zone);
        mLoginView = findViewById(R.id.user_login_zone);
        
        mLoginedView.findViewById(R.id.user_logined_back).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
                
            }
            
        });
        
        mUserNameEditText = (EditText) findViewById(R.id.dialog_edt_user_name);
        mPassEditText = (EditText) findViewById(R.id.dialog_edt_password);
        String userName = ConfigManager.getInstance().getCurrentUserName();
        
        if (userName != null) {
            mUserNameEditText.setText(userName);
        }
        findViewById(R.id.dialog_login_forget_pass_txt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                openDialog();
            }
        });
        
        findViewById(R.id.dialog_login_cell_phone_login_txt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                openDialog();
            }
        });
        mLoginBtn = (Button) findViewById(R.id.user_login_btn);
        mLoginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Hide input soft keyboard
                Util.hideSoftKeyboard(UserLoginActivity.this);
                
                String username = mUserNameEditText.getText().toString().trim(); // phone or username
                String pass = mPassEditText.getText().toString().trim();
                        
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
                    Util.sendToast(Util.getString(R.string.msg_login_user_name_pass_empty));
                    return;
                }
                pass = Util.MD5(pass);
                
                refreshLoginBtn(false);
                ConfigManager.getInstance().userLoginUserName(username, pass, new Runnable() {
                    @Override
                    public void run() {
                        //refreshLoginBtn(true);
                        Util.sendToast(Util.getString(R.string.msg_login_succ));
                        Util.hideSoftKeyboard(UserLoginActivity.this);
                        finish();
                    }
                }, new Runnable() {

                    @Override
                    public void run() {
                        refreshLoginBtn(true);
                      //  Util.sendToast(Util.getString(R.string.msg_login_error));
                    }
                    
                });
            }
        });
    }
    
    private void refreshLoginBtn(boolean enable) {
        mLoginBtn.setEnabled(enable);
        if (enable) {
            mLoginBtn.setText(Util.getString(R.string.dialog_login));
        } else {
            mLoginBtn.setText(Util.getString(R.string.dialog_login_inprog));
        }
    }
    
    private void openDialog() {
        Dialog dialog = new RequestVCodeDialog(this, new Runnable() {

            @Override
            public void run() {
                Util.showHideView(mLoginedView, mLoginView);
                promptToChangePass();
            }
            
        }, RequestVCodeType.LOGIN);
        dialog.show();
    }
    
    private void promptToChangePass() {
        // ontext context, String title, String content, String positiveStr, String negativeStr, Runnable listener
        Dialog dialog = new ConfirmationDialog(this, 
                "建议修改密码", 
                "您好，您收到的验证码将作为账户登录密码，建议您立即修改以确保账户安全，谢谢。", "立即修改", "以后再说", 
                
                new Runnable() {

                    @Override
                    public void run() {
                        Dialog changePassDialog = new ChangePasswordDialog(UserLoginActivity.this, new Runnable() {

                            @Override
                            public void run() {
                                finish();
                            }
                            
                        }, new Runnable() {

                            @Override
                            public void run() {
                                finish();
                            }
                            
                        });
                        changePassDialog.show();
                    }
            
        }, new Runnable() {

            @Override
            public void run() {
                finish();
                
            }
            
        });
        dialog.show();
    }
    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_user_login;
    }

}
