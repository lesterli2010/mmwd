package com.blb.mmwd.uclient.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Adapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.LocationManager;
import com.blb.mmwd.uclient.rest.model.ConfigItem;
import com.blb.mmwd.uclient.ui.adapter.SimpleSelectionListAdapter;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.SelectionType;
import com.blb.mmwd.uclient.R;

public class SelectionListFragment extends SelectionFragment {
    private final static String TAG = "SelectListFragment";
    private ListView mListView;
    private SimpleSelectionListAdapter mAdapter;
    private SelectionType mSelectionType = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container,
                savedInstanceState);
        
        return mRootView;
    }

    // Resize list view height to allow a part of shadow
    private void resizeListHeight() {

        // Calculate the actual list view height
        int count = mAdapter.getCount();
        if (count <= 0) {
            return;
        }
        View listItem = mAdapter.getView(0, null, mListView);
        listItem.measure(0, 0);
        // each item's height + divider height
        int totalHeight = (listItem.getMeasuredHeight() * count)
                + (mListView.getDividerHeight() * (count - 1));
        int maxHeight = ConfigManager.getInstance().getMaxSelectionListHeight(
                getActivity());
        if (totalHeight > maxHeight) {
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = maxHeight;
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.fragment_selection_list;
    }

    @Override
    public void refreshView() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void initView() {
        mListView = (ListView) mRootView.findViewById(R.id.selection_list);

        int selectedId = -1;
        List<ConfigItem> list = null;
        if (this.mSavedInstanceState == null) {
            mSavedInstanceState = getArguments();
        }
        if (mSavedInstanceState != null) {
            mSelectionType = Util.SelectionType.values()[mSavedInstanceState.getInt(Util.EXTRA_SELECT_LIST_TYPE)];
            switch(mSelectionType) {
            
            case SELECT_TYPE_ZONE:
                list = ConfigManager.getInstance().getSelectableZones();
                selectedId = ConfigManager.getInstance().getConfigData().getZoneId();
                break;
                
            case SELECT_TYPE_SEQUENCE:
                list = ConfigManager.getInstance().getSelectableSequences();
                selectedId = ConfigManager.getInstance().getConfigData().getSequenceId();
                break;
            default:
                break;
            }
        }

        mAdapter = new SimpleSelectionListAdapter(getActivity(), list, selectedId);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position,
                    long id) {
               // Log.e(TAG, "onItemClick:" + position);
               // Toast.makeText(getActivity(),
                //        "click zone fragment:" + position, Toast.LENGTH_LONG)
                 //       .show();
                boolean changed = false;
                int selectedId = ((SimpleSelectionListAdapter.ViewHolder)(v.getTag())).mId;
                switch (mSelectionType) {
                case SELECT_TYPE_ZONE:
                   // String city = LocationManager.getInstance().getCity();
                    if (selectedId != ConfigManager.getInstance().getConfigData().getZoneId()) {
                        // Config changed - update data
                        //loadData();
                        changed = true;
                        ConfigManager.getInstance().getConfigData().setZoneId(selectedId);
                        ConfigManager.getInstance().getCrossArea(selectedId, null, null);
                        ConfigManager.getInstance().getDistrictName(selectedId);
                    }
                    break;
                case SELECT_TYPE_SEQUENCE:
                    if (selectedId != ConfigManager.getInstance().getConfigData().getSequenceId()) {
                  //      loadData();
                        changed = true;
                        ConfigManager.getInstance().getConfigData().setSequenceId(selectedId);
                    }
                    break;
                default:
                    break;
                }
                if (changed && mSelectionActionListener != null) {
                    mSelectionActionListener.notifySelection();
                }
            }

        });
        // adjust listview height
        resizeListHeight();
        
    }

}
