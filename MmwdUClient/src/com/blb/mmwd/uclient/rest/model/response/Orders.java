package com.blb.mmwd.uclient.rest.model.response;

import java.util.List;


/*
{
  "status": 0,
  "reason": "",
  "list": [
    {
      "id": 0,
      "orderNo": "",
      "createDate": "",
      "state": "",
      "stateName": "",
      "price": 0,
      "subList": [
        {
          "id": 0,
          "productId": 0,
          "mmId": 0,
          "mmName": "",
          "mmImg": "",
          "price": 0,
          "productName": "",
          "productImg": "",
          "productCount": 0,
          "state": ""
        }
      ]
    }
  ]
}
 */
public class Orders extends ResponseHead {
    public List<Order> list;
}
