package com.blb.mmwd.uclient.rest.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.rest.model.ConfigItem;

/**
 * Bean class to store user's current running time configuration
 * 
 * @author lizhiqiang3
 * 
 */
public class SelectionConfigData {

    // Default is -1 city - zoneId
    private int mSelectedZoneIds = -1; // equals mmqid, ÂèÂèÈ¦Id
    private int mSelectedSeqId = -1;
    private int mSelectedPriceId = -1;
    private int mTamSelectedTypeId = -1;

    public int getZoneId() {
        if (mSelectedZoneIds == -1) {
            List<ConfigItem> list = ConfigManager.getInstance()
                    .getSelectableZones();
            if (list != null && !list.isEmpty()) {
                mSelectedZoneIds = list.get(0).id; // The first one
            }
        }
        return mSelectedZoneIds;
    }

    public void setZoneId(int zoneId) {
        mSelectedZoneIds = zoneId;
        // mSelectedZoneIds.put(city, zoneId);
    }

    public int getSequenceId() {
        if (mSelectedSeqId == -1) {
            List<ConfigItem> list = ConfigManager.getInstance()
                    .getSelectableSequences();
            if (list != null && !list.isEmpty()) {
                mSelectedSeqId = list.get(0).id;
            }
        }
        return mSelectedSeqId;
    }

    public void setSequenceId(int sequenceId) {
        mSelectedSeqId = sequenceId;
    }
    /*
     * public int getPriceId() { if (mSelectedPriceId == -1) { List<ConfigItem>
     * list = ConfigManager.getInstance().getSelectablePrices(); if (list !=
     * null && !list.isEmpty()) { mSelectedPriceId = list.get(0).id; } } return
     * mSelectedPriceId; }
     * 
     * public void setPriceId(int priceId) { mSelectedPriceId = priceId; }
     * 
     * public int getTypeId() { if (mTamSelectedTypeId == -1) { List<ConfigItem>
     * list = ConfigManager.getInstance().getSelectableTypes(); if (list != null
     * && !list.isEmpty()) { mTamSelectedTypeId = list.get(0).id; } } return
     * mTamSelectedTypeId; }
     * 
     * public void setTypeId(int typeId) { mTamSelectedTypeId = typeId; }
     */
}
