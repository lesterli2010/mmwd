package com.blb.mmwd.uclient.ui.filler;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.util.Util;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

// to fill zone_message.
/**
 * There are two part, msg and main content
 * msg: loading or error msg
 * main: main content
 * @author lizhiqiang3
 *
 */
public class MessageViewFiller {
    private View mMsgView;
    private View mMainContentView;
    private View mContent;
    private View mLoading;
    
    private ImageView mMsgImage;
    private TextView mMsgText;
    private Button mMsgBtn1;
    private Button mMsgBtn2;
    
    //private ProgressBar mLoadingImg;
    private TextView mLoadingText;
    
    
    public MessageViewFiller(View root, View mainContentView) {
        mMsgView = root;
        mMainContentView = mainContentView;
        Util.showHideView(mMsgView, mMainContentView);
        
        mContent = root.findViewById(R.id.error_msg_content_zone);
        mLoading = root.findViewById(R.id.error_msg_loading_zone);
        
        mMsgImage = (ImageView) mContent.findViewById(R.id.error_msg_img);
        mMsgText = (TextView) mContent.findViewById(R.id.error_msg_txt);
        mMsgBtn1 = (Button) mContent.findViewById(R.id.error_msg_act_btn_1);
        mMsgBtn2 = (Button) mContent.findViewById(R.id.error_msg_act_btn_2);
        
        //mLoadingImg = (ProgressBar) mLoading.findViewById(R.id.error_msg_loading_img);
        mLoadingText = (TextView) mLoading.findViewById(R.id.error_msg_loading_txt);
        Util.showHideView(mLoading, mContent);
    }
    
    public void fill(String loadingText) {
        
        if (!TextUtils.isEmpty(loadingText)) {
            mLoadingText.setText(loadingText);
        }
        Util.showHideView(mLoading, mContent);
        Util.showHideView(mMsgView, mMainContentView);
    }
    
    public void hideAll() {
        mMainContentView.setVisibility(View.GONE);
        mMsgView.setVisibility(View.GONE);
    }
    
    public void showMain() {
        Util.showHideView(mMainContentView, mMsgView);
    }
    public void fill(int msgImgResId, String msg,
            String btn1String, OnClickListener btn1Listener,
            String btn2String, OnClickListener btn2Listener) {
        if (msgImgResId != 0) {
            mMsgImage.setImageResource(msgImgResId);
            mMsgImage.setVisibility(View.VISIBLE);
        } else {
            mMsgImage.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(msg)) {
            mMsgText.setText(msg);
        }
        
        if (btn1String != null && btn1Listener != null) {
            mMsgBtn1.setText(btn1String);
            mMsgBtn1.setOnClickListener(btn1Listener);
            mMsgBtn1.setVisibility(View.VISIBLE);
        } else {
            mMsgBtn1.setVisibility(View.GONE);
        }
        
        if (btn2String != null && btn2Listener != null) {
            mMsgBtn2.setText(btn2String);
            mMsgBtn2.setOnClickListener(btn2Listener);
            mMsgBtn2.setVisibility(View.VISIBLE);
        } else {
            mMsgBtn2.setVisibility(View.GONE);
        }
        Util.showHideView(mContent, mLoading);
        Util.showHideView(mMsgView, mMainContentView);
    }
}
