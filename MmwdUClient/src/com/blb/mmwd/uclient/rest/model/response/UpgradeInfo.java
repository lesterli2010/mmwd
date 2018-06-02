package com.blb.mmwd.uclient.rest.model.response;

/*
 * {
  "status": 0,
  "reason": "",
 "version": "",
  "url": "",
  "descr": "",
  "versionCode": 0
}
 */
public class UpgradeInfo extends ResponseHead {
    public String version;
    public String url;
    public String descr;
    public int versionCode;
    
}
