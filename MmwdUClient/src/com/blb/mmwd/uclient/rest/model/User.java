package com.blb.mmwd.uclient.rest.model;

import android.text.TextUtils;

public class User {
    public int id;
    public String username;
    public String password;
    public String bindedPhone;
    public int score; //»ý·Ö
    public String session; // not null mean user logined
    
    public User(int id, String name, String bindedPhone, int score) {
        this.id = id;
        this.username = name;
        this.bindedPhone = bindedPhone;
        this.score = score;
    }
    
    public User(String name, String pass) {
        this.username = name;
        this.password = pass;
    }
    
    public boolean isLogined() {
        return !TextUtils.isEmpty(session);
    }
}
