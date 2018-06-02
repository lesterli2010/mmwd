package com.blb.mmwd.uclient.util;

public class StringUtil {
    private String mStr;
    private char mDimmer;

    public StringUtil() {
        this(null, ',');
    }
    public StringUtil(String str, char dimmer) {
        mStr = str;
        mDimmer = dimmer;
    }

    /**
     * append with ,
     * 
     * @param str
     */
    public void append(String str) {
        if (mStr == null) {
            mStr = str;
        } else {
            mStr += (mDimmer + str);
        }
    }

    @Override
    public String toString() {
        return mStr;
    }

    public static void main(String[] args) {
        StringUtil u = new StringUtil();
        u.append("abc");
        u.append("123");
        System.out.println(u.toString());
    }
}
