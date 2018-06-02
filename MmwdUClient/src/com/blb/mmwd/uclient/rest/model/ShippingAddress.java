package com.blb.mmwd.uclient.rest.model;

import android.text.TextUtils;


/*
 * {
  "id": 0,
  "cid": 0,
  "qid": 0,
  "did": 0,
  "dname": "",
  "cname": "",
  "phone": "",
  "rname": "",
  "addr": "",
  "cross": false
}
 */
public class ShippingAddress {
    public int id;
    public int cid; // community id
    public int qid; // zone id ย่ย่ศฆ
    public int did;
    public String dname; // district name
    public String cname; // community name
    public String phone;
    public String rname; // receiver name
    public String addr;
    public boolean cross;
    
    public void copyFrom(ShippingAddress item) {
        cid = item.cid;
        qid = item.qid;
        did = item.did;
        dname = item.dname;
        cname = item.cname;
        phone = item.phone;
        rname = item.rname;
        addr = item.addr;
        cross = item.cross;
    }
    
    public String getFullAddress() {
        return cross ? (dname + addr) : (TextUtils.isEmpty(dname) ? (cname + addr) : (dname + cname + addr));
    }
}
