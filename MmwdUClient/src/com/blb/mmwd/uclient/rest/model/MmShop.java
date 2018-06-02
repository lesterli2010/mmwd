package com.blb.mmwd.uclient.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.blb.mmwd.uclient.rest.model.response.Food;

/**
 * Bean class for MM's shop
 * @author lizhiqiang3
 * same as interface /app/index/hot/mmlist/{mmqid}/{page}/{limit}
 * 
 *
 */
public class MmShop {
    public int id;
    public String img;
    public int star;
    public String name;
    public boolean on;
    public boolean crossArea;
    public List<Food> ps;
}
