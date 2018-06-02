package com.blb.mmwd.uclient.ui.dialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.ShippingAddressManager;
import com.blb.mmwd.uclient.rest.model.ConfigItem;
import com.blb.mmwd.uclient.rest.model.ShippingAddress;
import com.blb.mmwd.uclient.util.Util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class EditShippingAddressDialog extends MmwdDialog {

    private EditText mEditPhone;
    private EditText mEditAddress;
    private Spinner mAreas;
    private Spinner mZones;
    private boolean mIsNew;
    private int mZoneId;
    private ConfigItem mCrossArea;
    private ArrayAdapter<String> mCommunityAdapter;
    private ArrayAdapter<String> mZoneAdapter;
    private List<ConfigItem> mAreaList; // 小圈地址：小区名；大圈地址：行政区名
    private WeakReference<Context> mContext;
    private int mSelectedAreaPos = 0;
    private int mSelectedZonePos = 0;
    private ShippingAddress mAddress;
    private View mCommunitiesLoading;
    private boolean mAllowCross; // Whether allow cross
    private View mAreaRow;
    private boolean mIsCrossAddress;

    public EditShippingAddressDialog(Context context, Runnable listener,
            boolean allowCross) {
        this(true, 0, allowCross, context, listener);
    }

    public EditShippingAddressDialog(Context context, int addrId,
            Runnable listener) {
        this(false, addrId, false, context, listener);
    }

    public EditShippingAddressDialog(boolean isNew, int addrId,
            boolean allowCross, Context context, Runnable listener) {

        super(context, null, null, null, listener);
        mContext = new WeakReference<Context>(context);
        mIsNew = isNew;
        mAllowCross = allowCross;

        mEditPhone = (EditText) findViewById(R.id.dialog_edt_phone);
        mEditAddress = (EditText) findViewById(R.id.dialog_edt_address);
        mAreas = (Spinner) findViewById(R.id.dialog_addr_communities);
        mCommunitiesLoading = findViewById(R.id.dialog_addr_communities_loading);
        mZones = (Spinner) findViewById(R.id.dialog_addr_zones);
        mAreaRow = findViewById(R.id.dialog_area_row);

        if (mIsNew) {
            mTitleView.setText(context.getResources().getString(
                    R.string.dialog_shipping_address_create));
            Drawable drawable = context.getResources().getDrawable(
                    R.drawable.ic_add);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                    drawable.getMinimumHeight());
            mTitleView.setCompoundDrawables(drawable, null, null, null);
            mOkBtn.setText(context.getResources().getString(
                    R.string.dialog_create));
            mPositiveInprogStr = Util.getString(R.string.dialog_add_inprog);
            mZoneId = ConfigManager.getInstance().getConfigData().getZoneId();
            if (ConfigManager.getInstance().getCurrentUser() != null) {
                mEditPhone
                        .setText(ConfigManager.getInstance().getCurrentUser().bindedPhone);
            }
            mCrossArea = ConfigManager.getInstance().getCrossArea();
        } else {
            mOkBtn.setText(context.getResources().getString(
                    R.string.dialog_modify));
            mTitleView.setText(context.getResources().getString(
                    R.string.dialog_shipping_address_modify));
            mPositiveInprogStr = Util.getString(R.string.dialog_modify_inprog);
            // mZoneId = current location
            mAddress = ShippingAddressManager.getInstance().getShippingAddress(
                    addrId);
            if (mAddress == null) {
                // doesn't exist
                Util.sendToast(R.string.edit_shipping_address_dialog_failure);
                dismiss();
                return;
            }
            
            mAllowCross = mAddress.cross;
            if (mAllowCross) {
                mCrossArea = new ConfigItem(mAddress.qid, ConfigManager.getInstance().getCrossAreaName(mAddress.qid));
            } else {
                mZoneId = mAddress.qid;
            }
            mEditPhone.setText(mAddress.phone);
            mEditAddress.setText(mAddress.addr);
        }

        // mZones.setText(ConfigManager.getInstance().getZoneName(mZoneId));

        refreshZones(); // init zone： current zone or
        refreshAreas(); // init communities or

    }

    private void disable() {
        Util.sendToast(R.string.msg_no_shipping);
        super.onCancelClicked();
    }

    private void refreshZones() {
        List<String> zoneNames = new ArrayList<String>();
        if (mIsNew) {
            zoneNames.add(ConfigManager.getInstance().getZoneName(mZoneId));
            if (mAllowCross) {
                if (mCrossArea != null && mCrossArea.name != null) {
                    zoneNames.add(mCrossArea.name);
                } else {
                    zoneNames.add("全城配送");
                }
            }
        } else {
            if (mAllowCross) {
                if (mCrossArea != null && mCrossArea.name != null) {
                    zoneNames.add(mCrossArea.name);
                } else {
                    zoneNames.add("全城配送");
                }
            } else {
                zoneNames.add(ConfigManager.getInstance().getZoneName(mZoneId));
            }
        }

        mZoneAdapter = new ArrayAdapter<String>(mContext.get(),
                android.R.layout.simple_spinner_item, zoneNames);
        mZoneAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mZones.setAdapter(mZoneAdapter);

        mZones.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> l, View v, int position,
                    long id) {
                mSelectedZonePos = position;
                refreshAreas();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
    }

    private void refreshAreas() {

        mIsCrossAddress = mSelectedZonePos > 0
                || (mSelectedZonePos == 0 && !mIsNew && mAllowCross);
 
        if (mIsCrossAddress) {
            // query districts as area list
            if (mCrossArea == null) {
                mCrossArea = ConfigManager.getInstance().getCrossArea();
            }
            if (mCrossArea == null) {
                mAreaList = null;
                // hide area list
                mAreaRow.setVisibility(View.GONE);
                return;
            }
            mAreaRow.setVisibility(View.VISIBLE);
            mAreaList = ConfigManager.getInstance().getCrossAreaDistricts(
                    mCrossArea.id);
        } else {
            mAreaRow.setVisibility(View.VISIBLE);
            mAreaList = ConfigManager.getInstance().getCommunities(mZoneId);
        }

        if (mAreaList == null || mAreaList.isEmpty()) {
            if (mIsCrossAddress) {

                ConfigManager.getInstance().getCrossAreaDistricts(mCrossArea.id,
                        new Runnable() {

                            @Override
                            public void run() {
                                mAreaList = ConfigManager.getInstance()
                                        .getCrossAreaDistricts(mCrossArea.id);
                                if (mAreaList == null || mAreaList.isEmpty()) {
                                    disable();
                                    return;
                                }
                                refreshAreaSpinner();
                            }

                        }, new Runnable() {

                            @Override
                            public void run() {
                                disable();

                            }

                        });
            } else {
                ConfigManager.getInstance().getCommunities(mZoneId,
                        new Runnable() {

                            @Override
                            public void run() {
                                mAreaList = ConfigManager.getInstance()
                                        .getCommunities(mZoneId);
                                if (mAreaList == null || mAreaList.isEmpty()) {
                                    disable();
                                    return;
                                }
                                refreshAreaSpinner();
                            }

                        }, new Runnable() {

                            @Override
                            public void run() {
                                disable();

                            }

                        });
            }
        } else {
            refreshAreaSpinner();
        }
    }

    private void refreshAreaSpinner() {
        mCommunitiesLoading.setVisibility(View.GONE);
        mAreas.setVisibility(View.VISIBLE);

        List<String> areaNames = new ArrayList<String>();
        for (ConfigItem item : mAreaList) {
            areaNames.add(item.name);
        }
        mCommunityAdapter = new ArrayAdapter<String>(mContext.get(),
                android.R.layout.simple_spinner_item, areaNames);
        mCommunityAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAreas.setAdapter(mCommunityAdapter);
        if (!mIsNew && mAddress != null) {
            String areaName;
            for (int i = 0; i < areaNames.size(); i++) {
                areaName = mAddress.cross ? mAddress.dname : mAddress.cname;
                if (areaName != null && areaName.equals(areaNames.get(i))) {
                    mAreas.setSelection(i);
                    break;
                } 
            }
        }
        mAreas.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> l, View v, int position,
                    long id) {
                mSelectedAreaPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

    }

    @Override
    protected void onOkClicked() {

        /*
         * if (mPositiveCb != null) { mPositiveCb.setData(new ShippingAddress(0,
         * mEditPhone.getText().toString(), mEditAddress.getText().toString()));
         * mPositiveCb.run(); }
         */

        Util.hideSoftKeyboard((Activity) mContext.get());

        ShippingAddress address = new ShippingAddress();

        if (mSelectedAreaPos >= 0
                && mSelectedAreaPos < mAreaList.size()) {
            if (mIsCrossAddress) {
                address.did = mAreaList.get(mSelectedAreaPos).id;
                address.dname = mAreaList.get(mSelectedAreaPos).name;
                address.cid = 0;
                address.cname = "";
            } else {
                address.cid = mAreaList.get(mSelectedAreaPos).id;
                address.cname = mAreaList.get(mSelectedAreaPos).name;
                address.did = 0;
                if (mIsNew) {
                    address.dname = ConfigManager.getInstance().getDistrictNameByZoneId(mZoneId);
                }
                
            }
        } else {
            Util.sendToast(R.string.msg_no_community);
            return;
        }

        address.phone = mEditPhone.getText().toString().trim();
        address.addr = mEditAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address.phone)) {
            Util.sendToast(R.string.msg_no_phone);
            return;
        }
        if (TextUtils.isEmpty(address.addr)) {
            Util.sendToast(R.string.msg_no_addr);
            return;
        }

        address.rname = ConfigManager.getInstance().getCurrentUserName();
        address.qid = mIsCrossAddress ? mCrossArea.id : mZoneId;
        address.cross = mIsCrossAddress;
        refreshOkBtn(false);
        if (mIsNew) {
            ShippingAddressManager.getInstance().addAddress(address,
                    new Runnable() {

                        @Override
                        public void run() {

                            dismissWithPostiveAction();
                        }

                    }, new Runnable() {
                        @Override
                        public void run() {
                            refreshOkBtn(true);
                        }

                    });

            // ShippingAddressManager.getInstance().addAddress(item);
        } else {
            address.id = mAddress.id;
            ShippingAddressManager.getInstance().modifyAddress(address,
                    new Runnable() {

                        @Override
                        public void run() {
                            dismissWithPostiveAction();
                        }

                    }, new Runnable() {

                        @Override
                        public void run() {
                            Util.sendToast(R.string.msg_mod_fail);
                            refreshOkBtn(true);

                        }

                    });
        }
    }

    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_edit_shipping_address;
    }
}
