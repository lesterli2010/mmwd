package com.blb.mmwd.uclient.rest.model.response;

import java.util.List;

/*
 * {
  "status": 0,
  "reason": "",
  "list": [
    {
      "id": 0,
      "rest": 0,
      "price": 0,
      "on": false
    }
  ]
}
 */
public class FoodPriceRestInfos extends ResponseHead {
    public List<FoodPriceRestInfo> list;
    public static class FoodPriceRestInfo {
        public int id;
        public int rest;
        public float price;
        public boolean on;
        public boolean crossArea;
    }
}
