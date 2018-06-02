package com.blb.mmwd.uclient.rest.model.response;

/*
 * "status": 0,
  "reason": "",
  "session": "",
  "img": "",
  "mobile": "",
  "username": "",
  "rank": 0
 */
public class Login extends ResponseHead {
    public String session;
    public String img;
    public String mobile;
    public String username;
    public int rank;
}
