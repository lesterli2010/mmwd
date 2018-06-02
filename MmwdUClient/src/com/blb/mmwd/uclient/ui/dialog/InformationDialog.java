package com.blb.mmwd.uclient.ui.dialog;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.pay.PayResult;
import com.blb.mmwd.uclient.util.Util;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class InformationDialog extends MmwdDialog {
    private ImageView mInfoImg;
    private TextView mInfoTxt;

    public static InformationDialog fromPayResult(Context context, PayResult.Status status) {
        switch (status) {
        case SUCCESS:
            return new InformationDialog(context, R.drawable.ic_pay_status_success, R.string.diag_info_pay_succ);
        case CONFIRMING:
            return new InformationDialog(context, R.drawable.ic_pay_status_wait, R.string.diag_info_pay_inprog);
        case FAIL:
            return new InformationDialog(context, R.drawable.ic_pay_status_warn, R.string.diag_info_pay_fail);
        }
        return null;
    }
    
    public InformationDialog(Context context, int loadingInfo) {
        super(context, null, null, null, null);
        mInfoImg = (ImageView) findViewById(R.id.dialog_info_img);
        mInfoTxt = (TextView) findViewById(R.id.dialog_info_text);
        
        mInfoImg.setVisibility(View.GONE);
        findViewById(R.id.dialog_info_loading).setVisibility(View.VISIBLE);
        mInfoTxt.setText(loadingInfo);
    }
    
    public InformationDialog(Context context, int imgRes, int info) {
        super(context, null, null, null, null);
        mInfoImg = (ImageView) findViewById(R.id.dialog_info_img);
        mInfoTxt = (TextView) findViewById(R.id.dialog_info_text);
        
        mInfoImg.setImageResource(imgRes);
        mInfoTxt.setText(info);
    }

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_information;
    }

}
