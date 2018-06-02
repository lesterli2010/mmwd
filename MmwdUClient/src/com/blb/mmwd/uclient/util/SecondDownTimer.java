package com.blb.mmwd.uclient.util;

import com.blb.mmwd.uclient.R;

import android.os.CountDownTimer;
import android.widget.Button;

public class SecondDownTimer extends CountDownTimer {

    private Button mBtn;
    private CharSequence mText;
    public SecondDownTimer(long millisInFuture, Button btn) {
        super(millisInFuture, 1000);
        mBtn = btn;
        mText = mBtn.getText();
    }

    @Override
    public void onFinish() {
        mBtn.setText(mText);
        mBtn.setEnabled(true);

    }

    @Override
    public void onTick(long arg0) {
        mBtn.setText(Util.getString(R.string.vcode_second_down, String.valueOf(arg0/1000)));

    }

}
