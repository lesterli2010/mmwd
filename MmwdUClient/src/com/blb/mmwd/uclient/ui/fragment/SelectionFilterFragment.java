package com.blb.mmwd.uclient.ui.fragment;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.rest.model.ConfigItem;
import com.blb.mmwd.uclient.rest.model.SelectionConfigData;
import com.blb.mmwd.uclient.util.Util;

public class SelectionFilterFragment extends SelectionFragment {
    private RadioGroup mPriceRadioGrp;
    private RadioGroup mTypeRadioGrp;
    private LayoutInflater mInflater;
    private int mSelectedPriceId;
    private int mSelectedTypeId;
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup rg, int id) {
            Util.SelectionTypeFilter typeFilter = (Util.SelectionTypeFilter) rg
                    .getTag();
            switch (typeFilter) {
            case SELECT_TYPE_FILTER_PRICE:
                mSelectedPriceId = id;
                break;
            case SELECT_TYPE_FILTER_TYPE:
                mSelectedTypeId = id;
                break;
            default:
                break;
            }
        }

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mPriceRadioGrp = (RadioGroup) mRootView
                .findViewById(R.id.filter_radio_group_price);
        mTypeRadioGrp = (RadioGroup) mRootView
                .findViewById(R.id.filter_radio_group_type);
        mInflater = inflater;
        /*
        loadRadioButtons(ConfigManager.getInstance().getSelectablePrices(),
                mPriceRadioGrp, ConfigManager.getInstance().getConfigData()
                        .getPriceId());
        loadRadioButtons(ConfigManager.getInstance().getSelectableTypes(),
                mTypeRadioGrp, ConfigManager.getInstance().getConfigData()
                        .getTypeId());
                        */
        mSelectedPriceId = -1;
        mSelectedTypeId = -1;
        mPriceRadioGrp
                .setTag(Util.SelectionTypeFilter.SELECT_TYPE_FILTER_PRICE);
        mPriceRadioGrp.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mTypeRadioGrp.setTag(Util.SelectionTypeFilter.SELECT_TYPE_FILTER_TYPE);
        mTypeRadioGrp.setOnCheckedChangeListener(mOnCheckedChangeListener);
        ((Button) mRootView.findViewById(R.id.selection_filter_finish))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        boolean changed = false;
                        SelectionConfigData cd = ConfigManager.getInstance()
                                .getConfigData();
                        /*
                        if (mSelectedPriceId != -1
                                && mSelectedPriceId != cd.getPriceId()) {
                            cd.setPriceId(mSelectedPriceId);
                            changed = true;
                        }

                        if (mSelectedTypeId != -1
                                && mSelectedTypeId != cd.getTypeId()) {
                            cd.setTypeId(mSelectedTypeId);
                            changed = true;
                        }*/
                        if (changed && mSelectionActionListener != null) {
                            mSelectionActionListener.notifySelection();
                        } else {
                            mSelectionActionListener.cancelSelection();
                        }
                    }
                });
        ((View) mRootView.findViewById(R.id.selection_filter_radio_sec)).setOnClickListener(null);
        return mRootView;
    }

    private void loadRadioButtons(List<ConfigItem> list, RadioGroup radioGroup,
            int selectedId) {
        for (ConfigItem ci : list) {
            RadioButton rb = (RadioButton) mInflater.inflate(
                    R.layout.btn_selection_radio, null);
            if (ci.id == selectedId) {
                rb.setChecked(true);
            } else {
                rb.setChecked(false);
            }
            rb.setId(ci.id);
            rb.setText(ci.name);
            radioGroup.addView(rb);
        }

    }

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.fragment_selection_filter;
    }

    @Override
    public void refreshView() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void initView() {
        // TODO Auto-generated method stub
        
    }

}
