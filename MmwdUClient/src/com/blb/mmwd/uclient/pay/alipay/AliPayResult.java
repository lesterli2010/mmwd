package com.blb.mmwd.uclient.pay.alipay;

import android.text.TextUtils;

import com.blb.mmwd.uclient.pay.PayResult;

public class AliPayResult extends PayResult {

    @Override
    public void parseResult(String rawResult) {
        if (TextUtils.isEmpty(rawResult))
            return;

        String[] resultParams = rawResult.split(";");
        for (String resultParam : resultParams) {
            if (resultParam.startsWith("resultStatus")) {
                resultStatus = gatValue(resultParam, "resultStatus");
                if ("9000".equals(resultStatus)) {
                    status = Status.SUCCESS;
                } else if ("8000".equals(resultStatus)) {
                    status = Status.CONFIRMING;
                } else {
                    status = Status.FAIL;
                }
            }
            if (resultParam.startsWith("result")) {
                result = gatValue(resultParam, "result");
                orderId = getValue("out_trade_no"); // alipay, get order id
                
            }
            if (resultParam.startsWith("memo")) {
                memo = gatValue(resultParam, "memo");
            }
        }
        
    }
    
    private String getValue(String name) {
        String[] strSplited = result.split("&");
        for (String s : strSplited) {
            String[] nvs = s.split("=");
            if (nvs.length != 2) {
                continue;
            }
            if (name.equals(nvs[0]) && !TextUtils.isEmpty(nvs[1])) {
                return nvs[1].replace("\"", "");// remove "
            }
        }
        return null;
    }

}
