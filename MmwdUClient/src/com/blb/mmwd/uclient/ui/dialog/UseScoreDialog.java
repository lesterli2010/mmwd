package com.blb.mmwd.uclient.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.util.Util;



public class UseScoreDialog extends MmwdDialog {
    private final static int SCORE_INTERVAL = 10;
    private Button mIncBtn;
    private Button mDecBtn;
    private EditText mAmountInputText;
    private TextView mCurrentScore;

    public UseScoreDialog(Context context,  Runnable listener) {
        super(context, "使用积分", null, null, listener);
        
        mIncBtn = (Button) findViewById(R.id.submit_info_amount_inc);
        mDecBtn = (Button) findViewById(R.id.submit_info_amount_dec);
        mAmountInputText = (EditText) findViewById(R.id.submit_info_amount_edit);
        mCurrentScore = (TextView) findViewById(R.id.use_score_current);
        mCurrentScore.setText(String.valueOf(ConfigManager.getInstance().getCurrentScore()));
        
        mAmountInputText.setText(String.valueOf(ConfigManager.getInstance().getCurrentScore()));
        mIncBtn.setEnabled(false);
        if (ConfigManager.getInstance().getCurrentScore() < SCORE_INTERVAL) {
            mDecBtn.setEnabled(false);
        }
        mAmountInputText.requestFocus();
        mIncBtn.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                handleScore(true);
            }
        });

        mDecBtn.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                handleScore(false);

            }
        });
    }
    
    private void handleScore(final boolean inc) {
        int toUseScore;
        try {
            toUseScore = Integer.parseInt(mAmountInputText.getText().toString());
        } catch (Exception e) {
            toUseScore = 0;
        }
        
        int current = ConfigManager.getInstance().getCurrentScore();
        if (inc) {
            toUseScore += SCORE_INTERVAL;
            if (toUseScore > current) {
                Util.sendToast("积分已不足~");
                return;
            } else if ((toUseScore + SCORE_INTERVAL) > current) {
                mIncBtn.setEnabled(false);
            }
            
            if (!mDecBtn.isEnabled()) {
                mDecBtn.setEnabled(true);
            }
            mAmountInputText.setText(String.valueOf(toUseScore));
        } else {
            toUseScore -= SCORE_INTERVAL;
            if (toUseScore < 0) {
                Util.sendToast("使用积分已为0");
                return;
            } else if ((toUseScore - SCORE_INTERVAL) < 0) {
                mDecBtn.setEnabled(false);
            }
            
            if (!mIncBtn.isEnabled()) {
                mIncBtn.setEnabled(true);
            }
            mAmountInputText.setText(String.valueOf(toUseScore));
        }
    }
    
    @Override
    protected void onOkClicked() {
        int toUseScore;
        try {
            toUseScore = Integer.parseInt(mAmountInputText.getText().toString());
        } catch (Exception e) {
            toUseScore = 0;
        }
        
        int current = ConfigManager.getInstance().getCurrentScore();
        if (toUseScore > current) {
            Util.sendToast("当前积分不足，当前积分为" + current);
            return;
        }
        
        CartManager.getInstance().setUsedScore(toUseScore);
        super.onOkClicked();
    }
    
    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_use_score;
    }
}
