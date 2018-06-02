package com.blb.mmwd.uclient.ui.dialog;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.ui.UserLoginActivity;
import com.blb.mmwd.uclient.util.Util;

public class ChangeUserNameDialog extends MmwdDialog {
    private EditText mUserNameDditText;
    private WeakReference<Context> mContext;
    public ChangeUserNameDialog(Context context, Runnable listener) {
        super(context, context.getString(R.string.dialog_modify), 
                context.getString(R.string.dialog_modify), null, listener);
        mContext = new WeakReference<Context>(context);
        mUserNameDditText = (EditText) findViewById(R.id.dialog_edt_user_name);
    }
    
    protected void onOkClicked() {
        // contact server
        
        final String username = mUserNameDditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            Util.sendToast(R.string.msg_user_name_empty);
            return;
        }
        
        refreshOkBtn(false);
        HttpManager.getInstance().getRestAPIClient().changeUserName(
                ConfigManager.getInstance().getCurrentSession(),
                username, new HttpCallback<ResponseHead>(new Runnable() {

                    @Override
                    public void run() {
                        refreshOkBtn(true);
                    }
                    
                }) {

                    @Override
                    protected boolean processData(ResponseHead t) {
                        
                        ConfigManager.getInstance().setCurrentUser(username, null);
                        ConfigManager.getInstance().clearSession();
                        // need to re-login
                        ConfigManager.getInstance().userLoginUserName(username,
                                ConfigManager.getInstance().getCurrentPassword(), new Runnable() {

                                    @Override
                                    public void run() {
                                        dismissWithPostiveAction();
                                        Util.sendToast("用户名修改成功~");
                                    }
                            
                        }, new Runnable() {

                                    @Override
                                    public void run() {
                                        Util.sendToast("您需要重新登录~");
                                        Util.startActivity(mContext.get(), UserLoginActivity.class);
                                        dismissWithPostiveAction();
                                    }
                            
                        });
                        return true;
                    }
                    
                });
    }
    
    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_change_user_name;
    }

}
