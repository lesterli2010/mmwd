package com.blb.mmwd.uclient.rest.model.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.ShippingAddress;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.OrderState;
/*
 * 
{
  "status": 0,
  "reason": "",
  "list": [
    {
      "status": 0,
      "reason": "",
      "id": 0,
      "orderNum": "",
      "createTime": "",
      "address": "",
      "receiver": "",
      "phone": "",
      "state": "",
      "stateInt": 0,
      "totalPrice": 0,
      "payInfo": "",
      "finishTime": "",
      "payType": 0,
      "payTypeString": "",
      "closeReason": "",
      "foods": [
        {
          "id": 0,
          "state": "",
          "deliver": "",
          "deliverPhone": "",
          "mmName": "",
          "mmImg": "",
          "mmId": 0,
          "productId": 0,
          "prodcutPrice": 0,
          "productName": "",
          "productImg": "",
          "productCount": 0,
          "createTime": "",
          "mmStartTime": "",
          "mmFinishTime": "",
          "kdStartTime": "",
          "kdFinishTime": ""
        }
      ]
    }
  ]
}
 */
import com.blb.mmwd.uclient.util.Util.PaymentType;

/*
 * {
  "status": 0,
  "reason": "",
  "list": [
    {
     "status": 0,
      "reason": "",
      "id": 0,
      "orderNum": "",
      "createTime": "",
      "address": "",
      "receiver": "",
      "phone": "",
      "state": "",
      "stateInt": 0,
      "totalPrice": 0,
      "payInfo": "",
      "finishTime": "",
      "payType": 0,
      "payTypeString": "",
      "closeReason": "",
      "isConsumeRank": false,
      "consumeRank": 0,
      "rankReducePrice": 0,
      "realPayPrice": 0,
      ]
    }
  ]
}
 */
public class Order extends ResponseHead {
    public int id;
    public String orderNum;
    public String createTime;
    public String address;
    public String receiver;
    public String phone;
    public String state;
    public int stateInt;
    public float totalPrice; //所有菜品金额
    public String payInfo;
    public String finishTime;
    public int payType;
    public String payTypeString;
    public String closeReason;
    public boolean isConsumeRank;
    public int consumeRank;
    public float rankReducePrice;
    public float kdPrice;
    public float realPayPrice; //实际支付金额
    
    public List<OrderFood> foods; // the data received from server
    
    // ----------------------
    // Regarding the data received from server,
    // need to convert it to below to display on APP
    public OrderState orderState = OrderState.UNKNOWN;
    public PaymentType paymentType = PaymentType.UNKNOWN;
    public Map<Integer, ArrayList<OrderFoodItem>> orderFoods; // mmShopId - food // list
    public ShippingAddress shippingAddress;
  //  public String startCookingTime;
    // -----------------------

    /*
    foods": [
        {
          "id": 0,
          "state": "",
          "deliver": "",
          "deliverPhone": "",
          "mmName": "",
          "mmImg": "",
          "mmId": 0,
          "productId": 0,
          "prodcutPrice": 0,
          "productName": "",
          "productImg": "",
          "productCount": 0,
          "zan": 0,
          "createTime": "",
          "mmStartTime": "",
          "mmFinishTime": "",
          "kdStartTime": "",
          "kdFinishTime": ""
        }
     */
    
    // 小订单
    public static class OrderFood {
        public int id;
        public String state;
        public String deliver;
        public String deliverPhone;
        public String mmName;
        public String mmImg;
        public int mmId;
        public int productId;
        public float prodcutPrice;
        public String productName;
        public String productImg;
        public int productCount;
        public int zan;
        public String createTime;
        public String mmStartTime;
        public String mmFinishTime;
        public String kdStartTime;
        public String kdFinishTime;
        public String note;
    }

    public Order() {
        this(false);
    }

    public Order(boolean newOrder) {
        if (newOrder) {
            orderFoods = CartManager.getInstance().getOrderFoods();
            orderState = OrderState.INIT;
            stateInt = orderState.ordinal();
            state = Util.getStatusText(orderState);
        }
    }
    
    public static Order fromCart() {
        return new Order(true);
    }
    
    public static Order fromCart(boolean isCrossArea, int mmShopId) {
        Order order = new Order();
        order.orderState = OrderState.INIT;
        order.stateInt = OrderState.INIT.ordinal();
        order.state = Util.getStatusText(OrderState.INIT);
        List<HashMap<Integer, ArrayList<OrderFoodItem>>> list = CartManager.getInstance().getFoodMapForSettlementSelection();
        if (list != null && !list.isEmpty()) {
            for (HashMap<Integer, ArrayList<OrderFoodItem>> map : list) {
                Iterator<Integer> it = map.keySet().iterator();
                boolean crossAreaList = false;
                int shopId = 0;
                while (it.hasNext()) {
                    List<OrderFoodItem> foodList = map.get(it.next());
                    Food f = foodList.get(0).food;
                    if (f.crossArea) {
                        crossAreaList = true;
                        shopId = f.mmid;
                    } else {
                        crossAreaList = false;
                        shopId = 0;
                    }
                    break;
                }
                
                if ((isCrossArea && crossAreaList && mmShopId == shopId) || (!isCrossArea && !crossAreaList)) {
                    order.orderFoods = map;
                    break;
                }
            }
        }
        
        return order;
    }
   
    /**
     * The data of orderList and order need to be converted to local data
     */
    public void convert() {
        // convert int to enum
        try {
            orderState = OrderState.values()[stateInt];
        } catch (Exception e){}
        
        // convert int to enum
        try {
            paymentType = PaymentType.values()[payType];
        } catch (Exception e){}
        
        // convert shipping address
        shippingAddress = new ShippingAddress();
        shippingAddress.rname = receiver;
        shippingAddress.addr = address;
        shippingAddress.phone = phone;
        
        if (foods == null || foods.isEmpty()) {
            return;
        }
        
        orderFoods = new LinkedHashMap<Integer, ArrayList<OrderFoodItem>>();
        
     //   long finishTime = 0;
     //   long time;
        for (OrderFood food : foods) {
            /*
            if (!TextUtils.isEmpty(food.kdFinishTime) &&
                    TextUtils.isDigitsOnly(food.kdFinishTime)) {
                time = Long.parseLong(food.kdFinishTime);
                if (finishTime == 0 || finishTime < time) {
                    finishTime = time;
                    finishTime = food.mmStartTime; // The earliest one
                }
            }*/
            ArrayList list = orderFoods.get(food.mmId);
            if (list == null) {
                list = new ArrayList<OrderFoodItem>();
                orderFoods.put(food.mmId, list);
            }
            
            OrderFoodItem item = new OrderFoodItem(food.productCount, food.note, Food.fromOrderFood(food));
            list.add(item);
        }
    }
}
