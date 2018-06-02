package com.blb.mmwd.uclient.ui.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.util.Util;

public class AddCommentDialog extends MmwdDialog {
    private EditText mComment;
    private TextView mTotalWord;
    private EditText mContact;
    private User mCurrentUser;
    
    public AddCommentDialog(Context context, Runnable positiveListener) {
        super(context, Util.getString(R.string.dialog_title_add_comment), Util.getString(R.string.dialog_send), null, positiveListener);
        super.mPositiveInprogStr = Util.getString(R.string.dialog_send_inprog);
        mComment = (EditText) findViewById(R.id.comment_input_text);
        mTotalWord = (TextView) findViewById(R.id.word_total_tips);
        mContact = (EditText) findViewById(R.id.comment_phone_input_text);
        
        mCurrentUser = ConfigManager.getInstance().getCurrentUser();
        if (mCurrentUser != null && mCurrentUser.bindedPhone != null) {
            mContact.setText(mCurrentUser.bindedPhone);
        }
                
        mComment.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int arg1,
                    int arg2, int arg3) {
                mTotalWord.setText(s.length()+"/500");
            }

        });
    }
   

    @Override
    protected void onOkClicked() {
        String comment = mComment.getText().toString().trim();
        if (TextUtils.isEmpty(comment) || comment.length() < 5) {
            Util.sendToast(R.string.msg_comment_error);
            return;
        }
        
        
        /*
        String contact = mContact.getText().toString().trim();
        if (!Util.validPhone(contact) &&
                !Util.validEmail(contact)) {
            Util.sendToast(R.string.msg_contact_error);
            return;
        }
        */
        refreshOkBtn(false);
        HttpManager.getInstance().getRestAPIClient().addComment(
                ConfigManager.getInstance().getCurrentSession(), 
                comment, new HttpCallback<ResponseHead>(new Runnable() {

                    @Override
                    public void run() {
                        refreshOkBtn(true);
                        
                    }
                    
                }) {

                    @Override
                    protected boolean processData(ResponseHead t) {
                        Util.hideSoftKeyboard(getOwnerActivity());
                        dismissWithPostiveAction();
                        Util.sendToast(R.string.msg_add_succ);
                        return true;
                    }
                    
                });
    }


    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_add_comment;
    }

}
