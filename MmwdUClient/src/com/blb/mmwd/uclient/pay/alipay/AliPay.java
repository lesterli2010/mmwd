package com.blb.mmwd.uclient.pay.alipay;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.pay.IPay;
import com.blb.mmwd.uclient.pay.PayResult;
import com.blb.mmwd.uclient.util.Util;

public class AliPay implements IPay {

    
  //商户PID
    private static final String PARTNER = "2088911161460618";
    //商户收款账号
    private static final String SELLER = "qdblb2015@163.com";
    //商户私钥，pkcs8格式
    private static final String RSA_PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMiEgfQNDBUnA5WC6Bew5Omn2BmZ4xHx4/S3VcmFM3lVxRdCBp9r022RS0yv1OjsX3+ZlVyaeZ14U6g1GpjApGP8OznvbBNcsJZkh6byX7emyyw4IZm0ESwINraC4NJMjcCkxBhqEruKNhvO4QHgTvp9HP9r7yhHkYDINKpFpbv3AgMBAAECgYEAotdxzif2Yws/DuGz6OGA1fy4M/pUfRNLhEaLhdAGAIjWOdAmHrvrhXTUiR/WsZ6c47xvnzfYgjjg/564zcrM9O/755jwQmkNMYx3vS/pShcOJT0V3amTgOWoeJmNW5zJ6vDAaTfz8CAe1jyyvfEONfr6YT4dCm+yMY7gEbx/JgkCQQD88zwtjRS2B54zj33dG2T0Jxqzl31MPoSSN1Y/Vz6RYBgqkR3ZCT1D9iiysOj4a403K5fZdw7rrSGIraprZxWjAkEAyu9utJAIbF/9cz/FzrAtMqHucAn62qF60G63LLlZRTO/slHoKq910XgdHOKrHqJvQ3z5TnR/olrVyf98Wb4dnQJAQjVgcQPWgfbof9J1PWSoPgfmQ0/JqiVLCgKV/qpVZDPk329wgpiagqlx6aPItw1fvysX6gHx09pTRrU+QDKRRQJBAIcIOsm67V/vn14ImT6my+xdA6NZgKnpuSboBMqlqlj5zhyBk9KTXfo7ymNmECcaPp7RkbI6opMNjiQS9FNixaECQQC6s0e6nTpmES6cqJAua0lrhpph+wAzbHnK3Hyvmd/G8rDN7YIVguEBgQGG2raGILOOd2sNC363tvLgy4vI7pnm";
    //支付宝公钥
    private static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
    
    private WeakReference<Activity> mActivity;
    
    public AliPay(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }
    
    @Override
    public void pay(String orderNo, String subject, String body, String price) {
     // 订单
        String orderInfo = getOrderInfo(orderNo, subject, body, price);

        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                
                PayTask alipay = new PayTask(mActivity.get());
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo);
                
                Intent intent = new Intent(PayResult.INTENT_ACTION_PAY_RESULT);
                intent.putExtra(PayResult.EXTRA_PAY_RESULT, result);
                mActivity.get().sendBroadcast(intent);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
        
    }
    /**
     * get the sdk version. 获取SDK版本号
     * 
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(mActivity.get());
        String version = payTask.getVersion();
       // Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }

    /**
     * create the order info. 创建订单信息
     * 
     */
    public String getOrderInfo(String orderNo, String subject, String body, String price) {
        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + orderNo + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + HttpManager.ALIPAY_NOTIFY_URL
                + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     * 
     *
    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }*/

    /**
     * sign the order info. 对订单信息进行签名
     * 
     * @param content
     *            待签名订单信息
     */
    public String sign(String content) {
        return Util.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     * 
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

}
