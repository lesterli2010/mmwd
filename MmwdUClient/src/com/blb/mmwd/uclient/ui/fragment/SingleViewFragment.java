package com.blb.mmwd.uclient.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class SingleViewFragment extends Fragment {
    private final static String TAG = "SingleViewFragment";
    protected View mRootView;
    protected Bundle mSavedInstanceState;
    protected boolean mInitialized;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (mRootView == null) {
            Log.d(TAG, "onCreateView, create view");
            mRootView = inflater.inflate(getViewResourceId(), null);
            mSavedInstanceState = savedInstanceState;
            initView();
            mInitialized = true;
        } else {
            Log.d(TAG, "onCreateView, not create view, reuse");
            ViewGroup parent = (ViewGroup) mRootView.getParent();  
            if (parent != null)  
            {  
                parent.removeView(mRootView);  
            }  
        }
        refreshView();
        return mRootView;
    }
        
    /**
     * Update UI according to data from network
     */
    public abstract void refreshView();
    
    /**
     * Init view data when fragment is create at the first time
     */
    protected abstract void initView();
    
    /**
     * Get resource Id
     * @return
     */
    protected abstract int getViewResourceId();    
}
