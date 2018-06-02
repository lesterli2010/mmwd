package com.blb.mmwd.uclient.rest.model.response;

public class Food extends ResponseHead {
    public int id;
    public String name;
    public String img;
    public String descr;
    public float price;
    public int rest;
    public int mmid;
    public String mmName;
    public String mmImg;
    public boolean active;
    public int zan;
    public int wanted;
    public boolean crossArea;
    public String content;
    public boolean reserable;
    
    /**
     * convert from food information in order
     */
    public static Food fromOrderFood(Order.OrderFood orderFood) {
        Food food = new Food();
        food.id = orderFood.productId;
        food.name = orderFood.productName;
        food.img = orderFood.productImg;
        food.price = orderFood.prodcutPrice;
        food.mmid = orderFood.mmId;
        food.mmImg = orderFood.mmImg;
        food.mmName = orderFood.mmName;
        food.zan = orderFood.zan;
        
        return food;
    }
    
    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        }
        
        if (o instanceof Food) {
            Food f = (Food) o;
            return id == f.id && mmid == f.mmid;
        }
        return false;
    }

    @Override
    public String toString() {
       
        return "Food, id:" + id + ", name:" + name + ", img:" + img + 
                ", descr:" + descr + ", price:" + price + ", rest:" + rest +
                ", mmid:" + mmid + ", mmName:" + mmName + ", mmImg:" + mmImg +
                ", active:" + active + ", zan:" + zan + ", wanted:" + wanted + ", crossArea:" + crossArea;
    }

    @Override
    public int hashCode() {
        return id;
    }
    
    
}
