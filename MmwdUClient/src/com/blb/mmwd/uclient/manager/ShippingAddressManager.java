package com.blb.mmwd.uclient.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import android.util.Log;

import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.ShippingAddress;
import com.blb.mmwd.uclient.rest.model.response.Addresses;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;

public class ShippingAddressManager {
    private static final String TAG = "CartManager";
    private static ShippingAddressManager sAddressManager;
    // private Context mContext;
    // private ContentResolver mContentResolver;
    private List<ShippingAddress> mAddresses;
    private Map<Integer, ArrayList<ShippingAddress>> mZoneAddresses;

    public static ShippingAddressManager getInstance() {
        if (sAddressManager == null) {
            sAddressManager = new ShippingAddressManager();
            sAddressManager.mAddresses = new ArrayList<ShippingAddress>();
            sAddressManager.mZoneAddresses = new HashMap<Integer, ArrayList<ShippingAddress>>();
            // HandlerManager.getInstance().sendEmptyMessage(false,
            // HandlerManager.MSG_INIT_SHIPPING_ADDR, 0);
            
        }
        return sAddressManager;
    }

    /**
     * init data from db
     */
    public void init() {
        // System.currentTimeMillis()
        // First delete history data
        // mContext = UClientApplication.sSharedInstance;
        // mContentResolver = mContext.getContentResolver();
        /*
         * new Thread() {
         * 
         * @Override public void run() { Cursor c =
         * mContentResolver.query(Address.CONTENT_URI, null, null, null, null);
         * 
         * if (c == null) { return; }
         * 
         * for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
         * ShippingAddressItem item = new ShippingAddressItem(); item.addressId
         * = c.getInt(c.getColumnIndex(Address._ID)); item.phone =
         * c.getString(c.getColumnIndex(Address.COLUMN_ADDRESS_PHONE));
         * item.communityId =
         * c.getInt(c.getColumnIndex(Address.COLUMN_ADDRESS_COMMUNITY_ID));
         * item.communityName =
         * c.getString(c.getColumnIndex(Address.COLUMN_ADDRESS_COMMUNITY_NAME));
         * item.details =
         * c.getString(c.getColumnIndex(Address.COLUMN_ADDRESS_DETAILS));
         * item.isDefault =
         * c.getInt(c.getColumnIndex(Address.COLUMN_ADDRESS_IS_DEFAULT)) != 0;
         * mAddresses.add(item); } c.close(); } }.start();
         */
    }

    public ShippingAddress getDefaultAddress(int crossAreaId) {
        Log.d(TAG, "get Default Address: crossAreaId:" + crossAreaId);
        if (crossAreaId < 0) {
            // only get current zone's address
        List<ShippingAddress> list = mZoneAddresses.get(ConfigManager
                .getInstance().getConfigData().getZoneId());
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0); // by default it is the first one
        } else {
            List<ShippingAddress> list = mZoneAddresses.get(crossAreaId);
            if (list == null || list.isEmpty()) {
                list = mZoneAddresses.get(ConfigManager
                        .getInstance().getConfigData().getZoneId());
            }
            if (list == null || list.isEmpty()) {
                return null;
            }
            return list.get(0); // by default it is the first one
        }
    }

    public void resetDefault() {
        // reset all to non-default
        /*
         * ContentValues cv = new ContentValues();
         * cv.put(Address.COLUMN_ADDRESS_IS_DEFAULT, 0);
         * mContentResolver.update(Address.CONTENT_URI, cv, null, null);
         * 
         * for (ShippingAddressItem item : mAddresses) { if (item.isDefault) {
         * item.isDefault = false; } }
         */
    }

    public void addAddress(final ShippingAddress addr, final Runnable succAction,
            final Runnable failAction) {
        HttpManager
                .getInstance()
                .getRestAPIClient()
                .addShippingAddress(ConfigManager.getInstance().getCurrentSession(), addr,
                        new HttpCallback<ResponseIntValue>(succAction, failAction) {

                            @Override
                            protected boolean processData(ResponseIntValue t) {
                                addr.id = t.value;
                                addAddressItem(addr);
                                return true;
                            }
                        });
        /*
         * resetDefault();
         * 
         * ContentValues cv = new ContentValues();
         * cv.put(Address.COLUMN_ADDRESS_COMMUNITY_ID, item.communityId);
         * cv.put(Address.COLUMN_ADDRESS_COMMUNITY_NAME, item.communityName);
         * cv.put(Address.COLUMN_ADDRESS_DETAILS, item.details);
         * cv.put(Address.COLUMN_ADDRESS_IS_DEFAULT, item.isDefault ? 1 : 0);
         * cv.put(Address.COLUMN_ADDRESS_PHONE, item.phone);
         * 
         * Uri uri = mContentResolver.insert(Address.CONTENT_URI, cv);
         * item.addressId = Integer.parseInt(uri.getLastPathSegment());
         */
        // mAddresses.add(item);
    }
    
    public void modifyAddress(final ShippingAddress address, final Runnable succAction,
            final Runnable failAction) {
        HttpManager.getInstance().getRestAPIClient().modifyShippingAddress(ConfigManager.getInstance().getCurrentSession(), address,
                new HttpCallback<ResponseHead>(succAction, failAction) {

                    @Override
                    protected boolean processData(ResponseHead t) {
                        for (ShippingAddress item : mAddresses) {
                            if (item.id == address.id) {
                                // refresh current data
                                item.copyFrom(address);
                                break;
                            }
                        }
                        
                        return true;
                    }
            
        });
    }

    public void delAddress(final int addrId, final Runnable succAction, final Runnable failAction) {
        HttpManager.getInstance().getRestAPIClient().delShippingAddress(ConfigManager.getInstance().getCurrentSession(),
                addrId, new HttpCallback<ResponseHead>(succAction, failAction) {

                    @Override
                    protected boolean processData(ResponseHead t) {
                        delAddressItem(addrId);
                        return true;
                    }
            
        });
        /*
         * Uri url = Uri.withAppendedPath(Address.CONTENT_URI,
         * String.valueOf(id)); mContentResolver.delete(url, null, null); for
         * (ShippingAddressItem item : mAddresses) { if (item.addressId == id) {
         * mAddresses.remove(item); break; } }
         */
    }
    /*
    public void modifyAddress(ShippingAddressItem item) {
        /*
         * ContentValues cv = new ContentValues();
         * cv.put(Address.COLUMN_ADDRESS_COMMUNITY_ID, item.communityId);
         * cv.put(Address.COLUMN_ADDRESS_COMMUNITY_NAME, item.communityName);
         * cv.put(Address.COLUMN_ADDRESS_DETAILS, item.details);
         * cv.put(Address.COLUMN_ADDRESS_IS_DEFAULT, item.isDefault ? 1 : 0);
         * cv.put(Address.COLUMN_ADDRESS_PHONE, item.phone);
         * 
         * Uri url = Uri.withAppendedPath(Address.CONTENT_URI,
         * String.valueOf(item.addressId)); mContentResolver.update(url, cv,
         * null, null);
         
    }
*/

    public void getShippingAddress(final int zid, final Runnable succAction,
            final Runnable failAction) {
        HttpManager.getInstance().getRestAPIClient().getShippingAddressList(ConfigManager.getInstance().getCurrentSession(),
                zid, new HttpCallback<Addresses>(succAction, failAction) {

                    @Override
                    protected boolean processData(Addresses a) {

                        if (a.list == null || a.list.isEmpty()) {
                            return false;
                        }

                        clear(zid);
                        for (ShippingAddress item : a.list) {
                            addAddressItem(item);
                        }
                        return true;
                    }
            
        });
    }
    public void getShippingAddresses(final Runnable succAction,
            final Runnable failAction) {
        HttpManager
                .getInstance()
                .getRestAPIClient()
                .getShippingAddressList(
                        ConfigManager.getInstance().getCurrentSession(),
                        new HttpCallback<Addresses>(succAction, failAction) {

                            @Override
                            protected boolean processData(Addresses a) {

                                if (a.list == null || a.list.isEmpty()) {
                                    return false;
                                }

                                clear();
                                for (ShippingAddress item : a.list) {
                                    addAddressItem(item);
                                }
                                return true;
                            }

                        });
    }

    private void addAddressItem(ShippingAddress item) {
        mAddresses.add(0, item);
        ArrayList list = mZoneAddresses.get(item.qid);
        if (list == null) {
            list = new ArrayList<ShippingAddress>();
            mZoneAddresses.put(item.qid, list);
        }
        list.add(0, item);
    }
    
    private void delAddressItem(int addrId) {
        for (ShippingAddress item : mAddresses) {
            if (item.id == addrId) {
                List<ShippingAddress> list = mZoneAddresses.get(item.qid);
                if (list != null) {
                    for (ShippingAddress item2 : list) {
                        if (item2.id == addrId) {
                            list.remove(item2);
                            break;
                        }
                    }
                }
                mAddresses.remove(item);
                break;
            }
        }
    }

    private void clear() {
        if (mAddresses != null) {
            mAddresses.clear();
        }
        if (mZoneAddresses != null) {

            Iterator<Integer> it = mZoneAddresses.keySet().iterator();
            while (it.hasNext()) {
                List<ShippingAddress> l = mZoneAddresses.get(it.next());
                l.clear();
            }
            //mZoneAddresses.clear();
        }
    }
    
    private void clear(int zid) {
        if (mAddresses != null) {
            for (ShippingAddress item : mAddresses) {
                if (item.qid == zid) {
                    mAddresses.remove(item);
                }
            }
        }
        
        List<ShippingAddress> l = mZoneAddresses.get(zid);
        if (l != null) {
            l.clear();
        }
        mZoneAddresses.remove(zid);
    }
    
    public ShippingAddress getShippingAddress(int addrId) {
        for (ShippingAddress item : mAddresses) {
            if (item.id == addrId) {
                return item;
            }
        }
        return null;
    }
    
    public ShippingAddress getShippingAddressFromZone(int zid) {
        List<ShippingAddress> list = mZoneAddresses.get(zid);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0); // always get the first one
    }

    public List<ShippingAddress> getShippingAddresses() {
        return mAddresses;
    }
    
    public List<ShippingAddress> getZoneShippingAddress(int zid, int crossAreaId) {
        List<ShippingAddress> list = new ArrayList<ShippingAddress>();
        List<ShippingAddress> listCrossArea = mZoneAddresses.get(crossAreaId);
        List<ShippingAddress> listZone = mZoneAddresses.get(zid);
        if (listCrossArea != null) {
            list.addAll(listCrossArea);
        }
        if (listZone != null) {
            list.addAll(listZone);
        }
        
        return list;
    }
}
