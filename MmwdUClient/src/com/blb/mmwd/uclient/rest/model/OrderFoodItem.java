package com.blb.mmwd.uclient.rest.model;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.db.MmwdContentContract.Cart;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.util.Util;

public class OrderFoodItem {
    
    /*
    // type:
    public final static int FOOD_TYPE_MM_PROVIDED = 0;
    public final static int FOOD_TYPE_MMWD_PROVIDED = 1;
    
    // MMWD provided food mmShop id
    public final static int MMSHOP_ID_MMWD_PROVIDED_FOOD = -1;
    // id:
    public final static int FOOD_ID_MMWD_MAIN_FOOD_RICE = 1; // TODO temp
    // price
    public final static float FOOD_PRICE_MMWD_MAIN_FOOD_RICE = 2.0f; // TODO temp
    public final static int FOOD_REST_MMWD_MAIN_FOOD_RICE = 10; // TODO temp
    // 
    */
    public int id;
    public int time; // creation time
    public int count;
    public String note; // ±¸×¢
    
    public Food food;
    public OrderFoodItem() {
        id = 0;
        time = (int)Util.getCurrentSecond();
    }
    
    public OrderFoodItem(int count, String note, Food food) {
        this();
        this.count = count;
        this.note = note;
        this.food = food;
    }
    
    public void copyFrom(OrderFoodItem item) {
        this.time = item.time;
        this.count = item.count;
        this.food = item.food;
        if (!TextUtils.isEmpty(item.note)) {
            this.note = item.note;
        }
    }
    
    public ContentValues toContentValues() {
        final ContentValues cv = new ContentValues();
        cv.put(Cart.COLUMN_CART_TIME, time);
        cv.put(Cart.COLUMN_FOOD_COUNT, count);
        
        cv.put(Cart.COLUMN_FOOD_ID, food.id);
        cv.put(Cart.COLUMN_FOOD_NAME, food.name);
        cv.put(Cart.COLUMN_MM_SHOP_ID, food.mmid);
        cv.put(Cart.COLUMN_MM_SHOP_NAME, food.mmName);
        cv.put(Cart.COLUMN_MM_SHOP_IMG, food.mmImg);
        cv.put(Cart.COLUMN_FOOD_CROSS_AREA, food.crossArea ? 1 : 0);
        cv.put(Cart.COLUMN_FOOD_NOTE, note);
        return cv;
    }
    
    public static OrderFoodItem fromCursor(Cursor c) {
        OrderFoodItem item = new OrderFoodItem();
        item.food = new Food();
        item.id = c.getInt(c.getColumnIndex(Cart._ID));
        item.time = c.getInt(c
                .getColumnIndex(Cart.COLUMN_CART_TIME));
        item.count = c.getInt(c
                .getColumnIndex(Cart.COLUMN_FOOD_COUNT));
        item.note = c.getString(c
                .getColumnIndex(Cart.COLUMN_FOOD_NOTE));
        
        item.food.mmid = c.getInt(c
                .getColumnIndex(Cart.COLUMN_MM_SHOP_ID));
        item.food.mmName = c.getString(c
                .getColumnIndex(Cart.COLUMN_MM_SHOP_NAME));
        item.food.mmImg = c.getString(c
                .getColumnIndex(Cart.COLUMN_MM_SHOP_IMG));
        item.food.id = c.getInt(c
                .getColumnIndex(Cart.COLUMN_FOOD_ID));
        item.food.name = c.getString(c
                .getColumnIndex(Cart.COLUMN_FOOD_NAME));
        item.food.crossArea = c.getInt(c
                .getColumnIndex(Cart.COLUMN_FOOD_CROSS_AREA)) == 1 ? true : false;
        return item;
    }
    
    /*
    public static OrderFoodItem createMainFoodRice() {
        OrderFoodItem item = new OrderFoodItem();
        item.count = 0;
        
        item.food = new Food();
        item.food.mmid = MMSHOP_ID_MMWD_PROVIDED_FOOD; //
        item.food.mmName = Util.getString(R.string.mmwd_main_food);
        item.food.mmImg = null;
        item.food.id = FOOD_ID_MMWD_MAIN_FOOD_RICE;
        item.food.name = Util.getString(R.string.mmwd_main_food_rice);
        item.food.price = FOOD_PRICE_MMWD_MAIN_FOOD_RICE;
        item.food.rest = FOOD_REST_MMWD_MAIN_FOOD_RICE;
        return item;
    }
    */
    
    @Override
    public String toString() {
        return "CartItem id:" + id + ", time:" + time + ", count:" + count + ", food:" + (food != null ? food.toString() : "null");
    }
    
}
  