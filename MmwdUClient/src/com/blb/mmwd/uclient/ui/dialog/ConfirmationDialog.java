package com.blb.mmwd.uclient.ui.dialog;

import com.blb.mmwd.uclient.R;

import android.content.Context;
import android.widget.TextView;

public class ConfirmationDialog extends MmwdDialog {
    private Runnable mNegativeActionListener;
    
    public ConfirmationDialog(Context context, String title, String content, String positiveStr, String negativeStr, Runnable listener, Runnable negativeListener) {
        this(context, title, content, positiveStr, negativeStr, listener);
        mNegativeActionListener = negativeListener;
    }
    
    public ConfirmationDialog(Context context, String title, String content, String positiveStr, String negativeStr, Runnable listener) {
        super(context, title, positiveStr, negativeStr, listener);
        TextView contentTextView = (TextView)findViewById(R.id.confirmation_content);
        contentTextView.setText(content);
    }
    
    public ConfirmationDialog(Context context, String title, String content, String positiveStr, Runnable listener) {
        this(context, title, content, positiveStr, null, listener);
    }

    @Override
    protected void onCancelClicked() {
        if (mNegativeActionListener != null) {
            mNegativeActionListener.run();
        }
        super.onCancelClicked();
    }

    @Override
    protected int getViewResourceId() {
        return R.layout.dialog_confirmation;
    }

}
