package com.blb.mmwd.uclient.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.ClientApplication;
import com.blb.mmwd.uclient.db.MmwdContentContract;
import com.blb.mmwd.uclient.db.MmwdContentContract.Cart;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.MmShop;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.OrderSubmitFoods;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.FoodPriceRestInfos;
import com.blb.mmwd.uclient.rest.model.response.FoodPriceRestInfos.FoodPriceRestInfo;
import com.blb.mmwd.uclient.rest.model.response.MmShops;
import com.blb.mmwd.uclient.util.Util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

public class CartManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "CartManager";
    private static CartManager sCartManager;
    private Context mContext;
    private ContentResolver mContentResolver;
    private Map<Integer, ArrayList<OrderFoodItem>> mCartItems = new LinkedHashMap<Integer, ArrayList<OrderFoodItem>>();
    private float mTotalFee;
    private long mUpdateTime;
    private boolean mSubmitting;
    private int mUsedScore; // current order's score
    
    public static CartManager getInstance() {
        if (sCartManager == null) {
            sCartManager = new CartManager();
            HandlerManager.getInstance().sendEmptyMessage(false, HandlerManager.MSG_INIT_CART, 0);
        }
        return sCartManager;
    }

    /**
     * init data from db
     */
    public void init() {
        // System.currentTimeMillis()
        // First delete history data
        mContext = ClientApplication.sSharedInstance;
        mContentResolver = mContext.getContentResolver();
        // First, clean data in cart of yesterday
        new Thread() {
            @Override
            public void run() {
                StringBuilder where = new StringBuilder(Cart.COLUMN_CART_TIME);
                where.append("<").append(Util.getStartSecondOfToday()).append(" or ")
                .append(Cart.COLUMN_FOOD_COUNT).append("=0");
                int count = mContentResolver.delete(Cart.CONTENT_URI,
                        where.toString(), null);
                Log.d(TAG, "delete old card data:" + count + ", where:" + where.toString());

                Cursor c = mContentResolver.query(Cart.CONTENT_URI, null, null,
                        null, null);

                if (c == null) {
                    return;
                }

                OrderSubmitFoods queryFoods = new OrderSubmitFoods();
                
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    OrderFoodItem item = OrderFoodItem.fromCursor(c);
                    
                    queryFoods.updateFood(item.food.id, item.count, item.note);
                    add(item);
                    Log.d(TAG, "read from db:" + item);

                }
                
                c.close();
                if (!queryFoods.list.isEmpty()) {
                    // not empyt, query the latest food price
                    HttpManager.getInstance().getRestAPIClient().getFoodPriceRestList(queryFoods, new HttpCallback<FoodPriceRestInfos>(new Runnable() {

                        @Override
                        public void run() {
                            // Error
                            clear();
                        }
                        
                    }) {

                        @Override
                        protected boolean processData(FoodPriceRestInfos t) {
                            if (t.list == null || t.list.isEmpty()) {
                                return false;
                            }
                            for (FoodPriceRestInfo i : t.list) {
                                if (!i.on) {
                                    // delete this food
                                    removeFood(0, i.id);
                                    continue;
                                }
                                OrderFoodItem item = getCartItem(0, i.id);
                                if (item == null) {
                                    continue;
                                }
                                // rest is not enough
                                if (item.count > i.rest) {
                                    item.count = i.rest;
                                    updateFood(item);
                                }
                                item.food.rest = i.rest;
                                item.food.price = i.price;
                                //item.food.crossArea = i.crossArea;
                            }
                            calculateTotalFee();
                            dump();
                            return true;
                        }
                        
                    });
                }
            }
        }.start();
    }

    private OrderFoodItem getCartItem(int mmShopId, int foodId) {
        if (mmShopId == 0) {
            // query according to foodId only
            Iterator<Integer> it = mCartItems.keySet().iterator();
            while (it.hasNext()) {
                List<OrderFoodItem> list = mCartItems.get(it.next());
                if (list != null && !list.isEmpty()) {
                    for (OrderFoodItem item : list) {
                        if (item.food.id == foodId) {
                            return item;
                        }
                    }
                }
            }
        } else {
            // first get mmShop's list
            List<OrderFoodItem> list = mCartItems.get(mmShopId);
            if (list == null) {
                return null;
            }
            for (OrderFoodItem item : list) {
                if (item.food.id == foodId) {
                    return item;
                }
            }
        }
        return null;
    }
    
    public int getFoodCount(int mmShopId, int foodId) {
        OrderFoodItem item = getCartItem(mmShopId, foodId);
        if (item != null) {
            return item.count;
        }
        return 0;
    }

    private void calculateTotalFee() {
        Iterator<Integer> it = mCartItems.keySet().iterator();
        this.mTotalFee = 0L;
        while (it.hasNext()) {
            List<OrderFoodItem> list = mCartItems.get(it.next());
            if (list != null && !list.isEmpty()) {
                for (OrderFoodItem item : list) {
                    mTotalFee += item.count * item.food.price;
                  //  Log.d(TAG, "mmShopId:" + item + ", total:" + mTotalMoney);
                }
            }
        }
    }
    
    private void dump() {
        if (!DEBUG) {
            return;
        }
        Iterator<Integer> it = mCartItems.keySet().iterator();
       // this.mTotalMoney = 0L;
        while (it.hasNext()) {
            int shopId = it.next();
            Log.d(TAG, "dump: shopId:" + shopId);
            List<OrderFoodItem> list = mCartItems.get(shopId);
            if (list != null && !list.isEmpty()) {
                for (OrderFoodItem item : list) {
                    Log.d(TAG, "-- item:" + item);
                    
                    //mTotalMoney += item.foodAmount * item.foodPrice;
                  //  Log.d(TAG, "mmShopId:" + item + ", total:" + mTotalMoney);
                }
            }
        }
    }
    
    private void add(final OrderFoodItem cartItem) {
        ArrayList<OrderFoodItem> list = mCartItems
                .get(cartItem.food.mmid);

        if (list == null) {
            list = new ArrayList<OrderFoodItem>();
            mCartItems.put(cartItem.food.mmid, list);
        }
        list.add(cartItem);
    }
    
    
    public void clear(final int mmShopId) {
        if (mSubmitting) {
            Util.sendToast(R.string.msg_submitting_no_clean);
            return;
        }
        
        HandlerManager.getInstance().getBgHandler().post(new Runnable() {

            @Override
            public void run() {
                StringBuilder where = new StringBuilder(Cart.COLUMN_MM_SHOP_ID);
                where.append("=").append(String.valueOf(mmShopId));
                mContentResolver.delete(Cart.CONTENT_URI,
                        where.toString(), null);
            }
            
        });
        
        mUsedScore = 0;
        ArrayList<OrderFoodItem> list = mCartItems.get(mmShopId);
        if (list != null) {
            list.clear();
        }
        mCartItems.remove(mmShopId);
        calculateTotalFee();
        mUpdateTime = SystemClock.uptimeMillis();
    }
    
    public void clear(final boolean crossArea) {
        if (mSubmitting) {
            Util.sendToast(R.string.msg_submitting_no_clean);
            return;
        }
        mUsedScore = 0;
        
        HandlerManager.getInstance().getBgHandler().post(new Runnable() {

            @Override
            public void run() {
                StringBuilder where = new StringBuilder(Cart.COLUMN_FOOD_CROSS_AREA);
                where.append("=").append(crossArea ? "1" : "0");
                mContentResolver.delete(Cart.CONTENT_URI,
                        where.toString(), null);
            }
            
        });
        
        Iterator<Integer> it = mCartItems.keySet().iterator();
        while (it.hasNext()) {
            ArrayList<OrderFoodItem> list = mCartItems.get(it.next());
            if (list != null && !list.isEmpty() && list.get(0).food.crossArea == crossArea) {
                list.clear();
                it.remove();
            }
        }
        calculateTotalFee();
        mUpdateTime = SystemClock.uptimeMillis();
    }
    /**
     * Below operations shall be made when 
     */
    public void clear() {
        if (mSubmitting) {
            Util.sendToast(R.string.msg_submitting_no_clean);
            return;
        }
        mUsedScore = 0;
        HandlerManager.getInstance().getBgHandler().post(new Runnable() {

            @Override
            public void run() {
                mContentResolver.delete(Cart.CONTENT_URI, null, null);
            }
            
        });

        Iterator<Integer> it = mCartItems.keySet().iterator();
        while (it.hasNext()) {
            mCartItems.get(it.next()).clear();
        }
        mCartItems.clear();
        mTotalFee = 0L;
        mUpdateTime = SystemClock.uptimeMillis();
    }
    
    public void removeFood(int mmId, final int foodId) {
        if (mSubmitting) {
            Util.sendToast(R.string.msg_submitting_no_delete);
            return;
        }
        
        if (mmId == 0) {
            OrderFoodItem item = getCartItem(mmId, foodId);
            if (item != null) {
                mmId = item.food.mmid;
            }
        }
       
        List<OrderFoodItem> list = mCartItems.get(mmId);
        if (list == null) {
            return;
        }
        for (OrderFoodItem item : list) {
            if (item.food.id == foodId) {
                list.remove(item);
                calculateTotalFee();
                break;
            }
        }
        mUpdateTime = SystemClock.uptimeMillis();
    }

    /**
     * update information for one batch of MmShops, now it is for main food.
     * @return
     */
    public boolean updateFood(MmShops shops) {
        if (mSubmitting) {
            Util.sendToast(R.string.msg_submitting_no_order);
            return false;
        }
        
        if (shops == null || shops.mms == null) {
            return false;
        }
        
        for (MmShop s : shops.mms) {
            if (s.ps == null || s.ps.isEmpty()) {
                continue;
            }
            
            for (Food f : s.ps) {
                int count = this.getFoodCount(f.mmid, f.id);
                updateFood(new OrderFoodItem(count, null, f));
            }
        }
        return true;
    }
    
    public boolean updateFood(final OrderFoodItem cartItem) {
        if (mSubmitting) {
            Util.sendToast(R.string.msg_submitting_no_order);
            return false;
        }
        try {
            Log.d(TAG, "DEBUG, updateFood:" + cartItem);
            final OrderFoodItem item = getCartItem(cartItem.food.mmid,
                    cartItem.food.id);
            if (item != null) {
                // modify existing
                item.copyFrom(cartItem);
                HandlerManager.getInstance().getBgHandler()
                        .post(new Runnable() {

                            @Override
                            public void run() {
                                final ContentValues cv = item.toContentValues();
                                StringBuffer where = new StringBuffer(Cart._ID);
                                where.append("=").append(item.id);
                                mContentResolver.update(Cart.CONTENT_URI, cv,
                                        where.toString(), null);
                            }
                        });
                Log.d(TAG, "update existing:" + item);
            } else {
                // add new one
                add(cartItem);
                
                HandlerManager.getInstance().getBgHandler()
                        .post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                                final ContentValues cv = cartItem.toContentValues();

                                Uri uri = mContentResolver.insert(
                                        Cart.CONTENT_URI, cv);
                                cartItem.id = Integer.parseInt(uri
                                        .getLastPathSegment());
                            }

                        });
                Log.d(TAG, "insert new one:" + cartItem);
            }

            calculateTotalFee();
            mUpdateTime = SystemClock.uptimeMillis();
           // dump();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Exception happens", e);
            return false;
        }
    }
    
    public float getTotalFee() {
        return mTotalFee;
    }
    
    public Map<Integer, ArrayList<OrderFoodItem>> getOrderFoods() {
        return mCartItems;
    }
    
    public boolean isEmpty() {
        return mTotalFee == 0F;
    }
    
    public long getUpdateTime() {
        return mUpdateTime;
    }
    
    public void setSubmitting(boolean submitting) {
        mSubmitting = submitting;
    }
    
    public void setUsedScore(int score) {
        mUsedScore = score;
    }
    
    public int getUsedScore() {
        return mUsedScore;
    }
    
    /**
     * If it contains non-cross Area food, then it doesn't allow crossArea,
     * otherwise allow
     * @return
     */
    public boolean isCrossAreaShippingAllowed() {
        Iterator<Integer> it = mCartItems.keySet().iterator();
        // this.mTotalMoney = 0L;
         while (it.hasNext()) {
             int shopId = it.next();
             
             List<OrderFoodItem> list = mCartItems.get(shopId);
             if (list != null && !list.isEmpty()) {
                 for (OrderFoodItem item : list) {
                     if (item.count > 0 && !item.food.crossArea) {
                         return false;
                     }
                 }
             }
         }
         return true;
    }
    
    public boolean isSelectSettlementNeeded() {
        Set<Integer> mmShopIds = mCartItems.keySet();
        if (mmShopIds.size() <= 1) {
            // only one mm shop, don't need
            return false;
        }
        Iterator<Integer> it = mmShopIds.iterator();
        // this.mTotalMoney = 0L;
        
         while (it.hasNext()) {
             int shopId = it.next();
             
             List<OrderFoodItem> list = mCartItems.get(shopId);
             if (list != null && !list.isEmpty()) {
                 for (OrderFoodItem item : list) {
                     if (item.count > 0 && item.food.crossArea) {
                         return true;
                     }
                 }
             }
         }
         return false;
    }
    
    /*
     * for cross area mmshop, it is in one item of returned list
     * for non-cross area is is in a single item
     */
    public List<HashMap<Integer, ArrayList<OrderFoodItem>>> getFoodMapForSettlementSelection() {

            Iterator<Integer> it = mCartItems.keySet().iterator();
            // this.mTotalMoney = 0L;
            List<HashMap<Integer, ArrayList<OrderFoodItem>>> retList = new ArrayList<HashMap<Integer, ArrayList<OrderFoodItem>>>();
            HashMap<Integer, ArrayList<OrderFoodItem>> nonCrossAreaItems = new HashMap<Integer, ArrayList<OrderFoodItem>>();
             while (it.hasNext()) {
                 int shopId = it.next();
                 
                 List<OrderFoodItem> list = mCartItems.get(shopId);
                 if (list != null && !list.isEmpty()) {
                     for (OrderFoodItem item : list) {
                         if (item.count <= 0) {
                             continue;
                         }
                         if (item.food.crossArea) {
                             HashMap<Integer, ArrayList<OrderFoodItem>> map = new HashMap<Integer, ArrayList<OrderFoodItem>>();
                             map.put(item.food.mmid, (ArrayList<OrderFoodItem>) list);
                             retList.add(map);
                             
                         } else {
                             nonCrossAreaItems.put(item.food.mmid, (ArrayList<OrderFoodItem>) list);
                         }
                         break;
                     }
                 }
             }
             if (!nonCrossAreaItems.isEmpty()) {
                 retList.add(nonCrossAreaItems);
             }
             return retList;
        
    }
    
}
