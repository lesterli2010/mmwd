package com.blb.mmwd.uclient.ui;

import cn.jpush.android.api.JPushInterface;

import com.blb.mmwd.uclient.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public abstract class TopCaptionActivity extends FragmentActivity {
    protected TextView mCaptionText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        View back = findViewById(R.id.app_top_back);
        if (back != null) {
            back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    finish();
                }
            });
        }
        
        mCaptionText = (TextView)findViewById(R.id.app_top_caption);
        if (mCaptionText != null) {
            mCaptionText.setText(getTitle());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }
    
    protected abstract int getLayoutResourceId();
}
