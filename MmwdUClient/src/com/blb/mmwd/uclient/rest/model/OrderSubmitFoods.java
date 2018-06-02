package com.blb.mmwd.uclient.rest.model;

import java.util.ArrayList;
import java.util.List;

/*
 * "list": [
 {
 "productId": 0,
 "count": 0
 }
 */

public class OrderSubmitFoods {
    public List<OrderSubmitFoodItem> list;
    public OrderSubmitFoods() {
        list = new ArrayList<OrderSubmitFoodItem>();
    }

    public void clear() {
        if (list != null) {
            list.clear();
        }
    }
    
    public void updateFood(int id, int count, String n) {
        for (OrderSubmitFoodItem f : list) {
            if (f.productId == id) {
                f.count = count;
                f.note = n;
                return;
            }
        }
        list.add(new OrderSubmitFoodItem(id, count, n));
    }
    
    public static class OrderSubmitFoodItem {
        public int productId;
        public int count;
        public String note;

        public OrderSubmitFoodItem(int pid, int c, String n) {
            productId = pid;
            count = c;
            note = n;
        }
    }

}
