package com.blb.mmwd.uclient.ui.filler;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.util.Util;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PrevNextViewFiller {
    
    private Button mPrevBtn;
    private Button mNextBtn;
    private View mZone;
    public PrevNextViewFiller(View v, OnClickListener prevClick, OnClickListener nextClick) {
        mZone = v;
        mPrevBtn = (Button) v.findViewById(R.id.prev_btn);
        mNextBtn = (Button) v.findViewById(R.id.next_btn);
        mPrevBtn.setOnClickListener(prevClick);
        mNextBtn.setOnClickListener(nextClick);
        mZone.setVisibility(View.GONE);
    }
    /**
     * 
     * @param currentPage
     * @param count
     */
    public void fill(int currentPage, int count) {
        mPrevBtn.setEnabled(currentPage > 0);
        mNextBtn.setEnabled((count % Util.MAX_ITEM_NUM_ONE_PAGE) == 0);
        if (currentPage == 0 && count < Util.MAX_ITEM_NUM_ONE_PAGE) {
            mZone.setVisibility(View.GONE);
        } else {
            mZone.setVisibility(View.VISIBLE);
        }
        
    }
}
