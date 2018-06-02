package com.blb.mmwd.uclient.ui;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.manager.UpgradeManager;

public class AboutUsActivity extends TopCaptionActivity {
    private final static String TAG = "AboutUsActivity";
    private TextView mIntroductionText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String version = ConfigManager.getInstance().getCurrentVersionName();
        if (version != null) {
            ((TextView) findViewById(R.id.current_version)).setText(getString(R.string.current_version, version));
        }
        mIntroductionText = (TextView) findViewById(R.id.about_us_introduction_txt);
        findViewById(R.id.about_us_check_update).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                UpgradeManager.getInstance().checkUpgrade(
                        AboutUsActivity.this, false);
            }
            
        });
        
    }
    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_about_us;
    }

}
