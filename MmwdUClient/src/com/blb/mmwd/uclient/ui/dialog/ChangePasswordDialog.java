package com.blb.mmwd.uclient.ui.dialog;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.util.Util;

public class ChangePasswordDialog extends MmwdDialog {

    private EditText mPassEdit1;
    private EditText mPassEdit2;
    private Runnable mNegativeActionListener;
    public ChangePasswordDialog(Context context, Runnable listener, Runnable negativeListener) {
        this(context, listener);
        mNegativeActionListener = negativeListener;
    }
    
    public ChangePasswordDialog(Context context, Runnable listener) {
        super(context, Util.getString(R.string.dialog_title_change_pass), Util
                .getString(R.string.dialog_modify), null, listener);
        mPassEdit1 = (EditText) findViewById(R.id.dialog_edt_input_pass_1);
        mPassEdit2 = (EditText) findViewById(R.id.dialog_edt_input_pass_2);
        mPositiveInprogStr = Util.getString(R.string.dialog_modify_inprog);
    }

    @Override
    protected void onCancelClicked() {
        if (mNegativeActionListener != null) {
            mNegativeActionListener.run();
        }
        super.onCancelClicked();
    }

    protected void onOkClicked() {
        // contact server
        String pass1 = mPassEdit1.getText().toString();
        String pass2 = mPassEdit2.getText().toString();
        if (TextUtils.isEmpty(pass1) || TextUtils.isEmpty(pass2)) {
            Util.sendToast(R.string.msg_pass_empty);
            return;
        }

        if (!pass1.equals(pass2)) {
            Util.sendToast(R.string.msg_pass_not_same);
            return;
        }


        User u = ConfigManager.getInstance().getCurrentUser();
        if (u == null || !u.isLogined()) {
            Util.sendToast(R.string.msg_user_unauthorized);
            return;
        }
        refreshOkBtn(false);
        final String newPass = Util.MD5(pass1);
        HttpManager
                .getInstance()
                .getRestAPIClient()
                .changePassword(u.session, u.password, newPass,
                        new HttpCallback<ResponseHead>(new Runnable() {

                            @Override
                            public void run() {
                                Util.sendToast(R.string.msg_pass_change_succ);
                                ConfigManager.getInstance().setCurrentUser(null, newPass);
                                dismissWithPostiveAction();
                            }

                        }, new Runnable() {

                            @Override
                            public void run() {
                                Util.sendToast(R.string.msg_pass_change_fail);
                                refreshOkBtn(true);

                            }

                        }) {
                            @Override
                            protected boolean processData(ResponseHead t) {
                                return true;
                            }
                        });
    }

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_change_password;
    }

}
