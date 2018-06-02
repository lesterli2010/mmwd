package com.blb.mmwd.uclient.ui.dialog;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.api.ClientRestAPI;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.util.SecondDownTimer;
import com.blb.mmwd.uclient.util.Util;

public class RequestVCodeDialog extends MmwdDialog {
    public static enum RequestVCodeType {
        INVALID,
        LOGIN, // If there is no user on server, then create user; else reset password to verification code
        RESETPASS,
        CHANGEPHONE
    }
    
    private EditText mPhoneEditText;
    private EditText mVerifyCodeText;
    private Button mRequestVCodeBtn;
    private CountDownTimer mTimer;
    private RequestVCodeType mType;
    public RequestVCodeDialog(Context context, Runnable listener, RequestVCodeType type) {
        
        super(context, type == RequestVCodeType.LOGIN ? context.getString(R.string.cell_phone_login) : context.getString(R.string.change_phone), 
                type == RequestVCodeType.LOGIN ? context.getString(R.string.dialog_login) : context.getString(R.string.dialog_ok), null, listener);
        mType = type;
        mPhoneEditText = (EditText) findViewById(R.id.dialog_edt_phone);
        mVerifyCodeText = (EditText) findViewById(R.id.dialog_edt_verify_code);
        if (mType == RequestVCodeType.CHANGEPHONE) {
            mPhoneEditText.setHint(R.string.hint_input_new_phone);
            mPositiveInprogStr = Util.getString(R.string.dialog_change_inprog);
        } else if (mType == RequestVCodeType.LOGIN) {
            mPositiveInprogStr = Util.getString(R.string.dialog_login_inprog);
        }
        mRequestVCodeBtn = (Button) findViewById(R.id.dialog_get_code_btn);
        mRequestVCodeBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                requestVerifyCode();
                
            }
            
        });
        mTimer = new SecondDownTimer(Util.VERIFY_CODE_INTERVAL, mRequestVCodeBtn);
    }
    
    private void requestVerifyCode() {
        String phone = mPhoneEditText.getText().toString().trim();
        if (!Util.validPhone(phone)) {
            Util.sendToast(R.string.msg_invalid_phone);
            return;
        }
        mRequestVCodeBtn.setEnabled(false);
        mVerifyCodeText.requestFocus();
        HttpManager.getInstance().getRestAPIClient().requestVerifyCode(phone,
                mType.ordinal(),
                new HttpCallback<ResponseHead>(new Runnable() {

                    @Override
                    public void run() {
                        mRequestVCodeBtn.setEnabled(true);
                      //  Util.sendToast(R.string.msg_send_vcode_error);
                    }
                    
                }) {

                    @Override
                    protected boolean processData(ResponseHead t) {
                        Util.sendToast(R.string.msg_send_vcode_succ);
                        mTimer.start();
                        return true;
                    }
            
        });
        // send 
    }

    
    @Override
    protected void onOkClicked() {
        // new user
        final String phone = mPhoneEditText.getText().toString().trim();
        if (!Util.validPhone(phone)) {
            Util.sendToast(R.string.msg_invalid_phone);
            return;
        }
        final String code = mVerifyCodeText.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Util.sendToast(R.string.msg_invalid_code);
            return;
        }
        
        refreshOkBtn(false);
        final String encodedCode = Util.MD5(code);
        switch(mType) {
        case CHANGEPHONE:
            HttpManager.getInstance().getRestAPIClient().changeMobile(ConfigManager.getInstance().getCurrentSession(),
                    phone, encodedCode, new HttpCallback<ResponseHead>(new Runnable() {

                        @Override
                        public void run() {
                            // 验证失败，请重新发送
                            //Util.sendToast(R.string.change_binded_phone_fail);
                            refreshOkBtn(true);
                        }
                        
                    }) {

                        @Override
                        protected boolean processData(ResponseHead t) {
                            ConfigManager.getInstance().setBindedPhone(phone);
                            dismissWithPostiveAction();
                            Util.sendToast(R.string.change_binded_phone_succ);
                            return true;
                        }
                
            });
            break;
        case LOGIN:
            ConfigManager.getInstance().userLoginMobile(phone, encodedCode, new Runnable() {

                @Override
                public void run() {
                    dismissWithPostiveAction();
                    Util.sendToast(R.string.msg_login_succ);
                }
                
            }, new Runnable() {

                @Override
                public void run() {
                    refreshOkBtn(true);
                    
                }
                
            });
            break;
            default:
            break;
        }
        
    }
    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_request_vcode;
    }

}
