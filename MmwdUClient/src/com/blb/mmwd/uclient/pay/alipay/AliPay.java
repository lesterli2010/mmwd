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

    
  //�̻�PID
    private static final String PARTNER = "2088911161460618";
    //�̻��տ��˺�
    private static final String SELLER = "qdblb2015@163.com";
    //�̻�˽Կ��pkcs8��ʽ
    private static final String RSA_PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMiEgfQNDBUnA5WC6Bew5Omn2BmZ4xHx4/S3VcmFM3lVxRdCBp9r022RS0yv1OjsX3+ZlVyaeZ14U6g1GpjApGP8OznvbBNcsJZkh6byX7emyyw4IZm0ESwINraC4NJMjcCkxBhqEruKNhvO4QHgTvp9HP9r7yhHkYDINKpFpbv3AgMBAAECgYEAotdxzif2Yws/DuGz6OGA1fy4M/pUfRNLhEaLhdAGAIjWOdAmHrvrhXTUiR/WsZ6c47xvnzfYgjjg/564zcrM9O/755jwQmkNMYx3vS/pShcOJT0V3amTgOWoeJmNW5zJ6vDAaTfz8CAe1jyyvfEONfr6YT4dCm+yMY7gEbx/JgkCQQD88zwtjRS2B54zj33dG2T0Jxqzl31MPoSSN1Y/Vz6RYBgqkR3ZCT1D9iiysOj4a403K5fZdw7rrSGIraprZxWjAkEAyu9utJAIbF/9cz/FzrAtMqHucAn62qF60G63LLlZRTO/slHoKq910XgdHOKrHqJvQ3z5TnR/olrVyf98Wb4dnQJAQjVgcQPWgfbof9J1PWSoPgfmQ0/JqiVLCgKV/qpVZDPk329wgpiagqlx6aPItw1fvysX6gHx09pTRrU+QDKRRQJBAIcIOsm67V/vn14ImT6my+xdA6NZgKnpuSboBMqlqlj5zhyBk9KTXfo7ymNmECcaPp7RkbI6opMNjiQS9FNixaECQQC6s0e6nTpmES6cqJAua0lrhpph+wAzbHnK3Hyvmd/G8rDN7YIVguEBgQGG2raGILOOd2sNC363tvLgy4vI7pnm";
    //֧������Կ
    private static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
    
    private WeakReference<Activity> mActivity;
    
    public AliPay(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }
    
    @Override
    public void pay(String orderNo, String subject, String body, String price) {
     // ����
        String orderInfo = getOrderInfo(orderNo, subject, body, price);

        // �Զ�����RSA ǩ��
        String sign = sign(orderInfo);
        try {
            // �����sign ��URL����
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // �����ķ���֧���������淶�Ķ�����Ϣ
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // ����PayTask ����
                
                PayTask alipay = new PayTask(mActivity.get());
                // ����֧���ӿڣ���ȡ֧�����
                String result = alipay.pay(payInfo);
                
                Intent intent = new Intent(PayResult.INTENT_ACTION_PAY_RESULT);
                intent.putExtra(PayResult.EXTRA_PAY_RESULT, result);
                mActivity.get().sendBroadcast(intent);
            }
        };

        // �����첽����
        Thread payThread = new Thread(payRunnable);
        payThread.start();
        
    }
    /**
     * get the sdk version. ��ȡSDK�汾��
     * 
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(mActivity.get());
        String version = payTask.getVersion();
       // Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }

    /**
     * create the order info. ����������Ϣ
     * 
     */
    public String getOrderInfo(String orderNo, String subject, String body, String price) {
        // ǩԼ���������ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // ǩԼ����֧�����˺�
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // �̻���վΨһ������
        orderInfo += "&out_trade_no=" + "\"" + orderNo + "\"";

        // ��Ʒ����
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // ��Ʒ����
        orderInfo += "&body=" + "\"" + body + "\"";

        // ��Ʒ���
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // �������첽֪ͨҳ��·��
        orderInfo += "&notify_url=" + "\"" + HttpManager.ALIPAY_NOTIFY_URL
                + "\"";

        // ����ӿ����ƣ� �̶�ֵ
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // ֧�����ͣ� �̶�ֵ
        orderInfo += "&payment_type=\"1\"";

        // �������룬 �̶�ֵ
        orderInfo += "&_input_charset=\"utf-8\"";

        // ����δ����׵ĳ�ʱʱ��
        // Ĭ��30���ӣ�һ����ʱ���ñʽ��׾ͻ��Զ����رա�
        // ȡֵ��Χ��1m��15d��
        // m-���ӣ�h-Сʱ��d-�죬1c-���죨���۽��׺�ʱ����������0��رգ���
        // �ò�����ֵ������С���㣬��1.5h����ת��Ϊ90m��
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_tokenΪ���������Ȩ��ȡ����alipay_open_id,���ϴ˲����û���ʹ����Ȩ���˻�����֧��
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // ֧��������������󣬵�ǰҳ����ת���̻�ָ��ҳ���·�����ɿ�
        orderInfo += "&return_url=\"m.alipay.com\"";

        // �������п�֧���������ô˲���������ǩ���� �̶�ֵ ����ҪǩԼ���������п����֧��������ʹ�ã�
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. �����̻������ţ���ֵ���̻���Ӧ����Ψһ�����Զ����ʽ�淶��
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
     * sign the order info. �Զ�����Ϣ����ǩ��
     * 
     * @param content
     *            ��ǩ��������Ϣ
     */
    public String sign(String content) {
        return Util.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. ��ȡǩ����ʽ
     * 
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

}
