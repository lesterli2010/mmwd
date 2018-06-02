package com.blb.mmwd.uclient.ui.dialog;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.util.Util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * self defined dialog with unifed style
 * 
 * @author lizhiqiang3
 * 
 */
public abstract class MmwdDialog extends Dialog {
    protected TextView mTitleView;
    private Button mCancelBtn;
    private Runnable mPositiveActionListener;
    protected Button mOkBtn;
    private String mPositiveStr;
    protected String mPositiveInprogStr;

    /**
     * 
     * @param context
     * @param title
     *            the title of dialog
     * @param positiveStr
     *            for exampe ok, 确认
     * @param negativeStr
     *            ， for example, cancel, 取消，返回， etc
     */
    public MmwdDialog(Context context, String title, String positiveStr,
            String negativeStr, Runnable listener) {
        super(context, R.style.DialogTheme);
        mPositiveActionListener = listener;
        // TODO Auto-generated constructor stub
        setContentView(getViewResourceId());
        // setCanceledOnTouchOutside(true);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ConfigManager.getInstance().getMaxDialogWidth(context);// ViewGroup.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.CENTER_VERTICAL);
        mTitleView = (TextView) findViewById(R.id.dialog_title);
        mOkBtn = (Button) findViewById(R.id.dialog_btn_ok);
        mCancelBtn = (Button) findViewById(R.id.dialog_btn_cancel);
        fillTextView(mTitleView, title);
        fillTextView(mOkBtn, positiveStr);
        fillTextView(mCancelBtn, negativeStr);
        mPositiveStr = positiveStr;

        if (mCancelBtn != null) {
            mCancelBtn
                    .setOnClickListener(new android.view.View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            onCancelClicked();
                        }
                    });
        }

        if (mOkBtn != null) {
            mOkBtn.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    onOkClicked();
                }
            });
        }

    }

    protected void onCancelClicked() {
        dismiss();
    }

    protected void onOkClicked() {
        dismissWithPostiveAction();
    }

    private void fillTextView(TextView view, String value) {
        if (value != null && view != null) {
            view.setText(value);
        }
    }
    
    protected void dismissWithPostiveAction() {
        dismiss();
        if (mPositiveActionListener != null) {
            mPositiveActionListener.run();
        }
    }

    protected void refreshOkBtn(boolean enabled) {
        if (mOkBtn == null) {
            return;
        }

        mOkBtn.setEnabled(enabled);
        if (enabled) {
            if (mPositiveStr != null) {
                mOkBtn.setText(mPositiveStr);
            } else {
                mOkBtn.setText(Util.getString(R.string.dialog_ok));
            }
        } else {
            if (mPositiveInprogStr == null) {
                mOkBtn.setText(Util.getString(R.string.dialog_inprog));
            } else {
                mOkBtn.setText(mPositiveInprogStr);

            }
        }
    }

    protected abstract int getViewResourceId();
}
