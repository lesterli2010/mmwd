package com.blb.mmwd.uclient.ui;

import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FteActivity extends Activity {
    private Button mEnterMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fte);
        
        mEnterMain = (Button)findViewById(R.id.enter_main);
        mEnterMain.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        ClientApplication.sSharedInstance.quitApplication();
    }
}
