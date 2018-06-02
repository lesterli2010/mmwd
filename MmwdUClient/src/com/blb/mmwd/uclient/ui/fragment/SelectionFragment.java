package com.blb.mmwd.uclient.ui.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.LocationManager;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.SelectionType;

public abstract class SelectionFragment extends SingleViewFragment {
    protected SelectionActionListener mSelectionActionListener;
    private ImageView mShadowImage;
    // Define selection type in home fragment: zone, sequqnce, food
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container,
                savedInstanceState);
        mShadowImage = (ImageView) mRootView
                .findViewById(R.id.selection_shadow_img);
        mShadowImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mSelectionActionListener != null) {
                    mSelectionActionListener.cancelSelection();
                }
            }

        });
        return mRootView;
    }

    static interface SelectionActionListener {
        public void notifySelection();

        public void cancelSelection();
    }

    public void registerSelectionActionListener(SelectionActionListener listener) {
        mSelectionActionListener = listener;
    }

    public static SelectionFragment newInstance(SelectionType selectionType,
            SelectionActionListener listener) {
        SelectionFragment fragment = null;
        switch (selectionType) {
        case SELECT_TYPE_ZONE:
        case SELECT_TYPE_SEQUENCE:
            fragment = new SelectionListFragment();
            Bundle args = new Bundle();
            args.putInt(Util.EXTRA_SELECT_LIST_TYPE, selectionType.ordinal());
            fragment.setArguments(args);
            break;
        case SELECT_TYPE_FILTER:
            fragment = new SelectionFilterFragment();
            break;
        default:
            return null;
        }
        if (fragment != null) {
            fragment.registerSelectionActionListener(listener);
        }

        return fragment;
    }
}
