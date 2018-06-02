package com.blb.mmwd.uclient.ui;

import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.pay.IPay;
import com.blb.mmwd.uclient.pay.PayResult;
import com.blb.mmwd.uclient.pay.alipay.AliPay;
import com.blb.mmwd.uclient.pay.alipay.AliPayResult;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.OrderSubmitFoods.OrderSubmitFoodItem;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.ui.dialog.InformationDialog;
import com.blb.mmwd.uclient.ui.filler.OrderItemViewFiller;
import com.blb.mmwd.uclient.util.StringUtil;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.OrderState;
import com.blb.mmwd.uclient.util.Util.PaymentType;

public class OrderDetailActivity extends TopCaptionActivity {
    private final static boolean DEBUG = true;
    private final static String TAG = "OrderDetailActivity";
    
    private int mOrderId;
    private Order mOrder;
    private OrderItemViewFiller mOrderItemViewFiller;
    private View mOrderItemsZoneView;
    private ViewGroup mOrderDetailAttrList;
    private LayoutInflater mLayoutInflater;
    private Button mServiceBtn;
    private Button mRepayBtn;
    
    private Dialog mInfoDialog;
    
    private IPay mPay;
    private PayResult mPayResult;
    
    private boolean mRegistered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                savedInstanceState = intent.getExtras();
            }
        }

        if (savedInstanceState != null) {
            mOrderId = savedInstanceState.getInt(Util.EXTRA_ID);
        }
        
        //Toast.makeText(this, "Order id:" + mOrderId, Toast.LENGTH_SHORT).show();
        
        
        
        mOrderItemsZoneView = findViewById(R.id.zone_order_items);
        
        mOrderDetailAttrList = (ViewGroup) findViewById(R.id.order_detail_attribute_list);
        mLayoutInflater = LayoutInflater.from(this);
        
        mServiceBtn = (Button) findViewById(R.id.service_btn);
        mServiceBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.callServiceNumber(OrderDetailActivity.this);
            }
            
        });
        
        mRepayBtn = (Button) findViewById(R.id.re_pay_btn);
        
        refreshView();
    }
    
    /**
     * After pay task finishes
     * 
     * @author lizhiqiang3
     * 
     */
    private class PayResultRunnable implements Runnable {
        private String mResult;

        public PayResultRunnable(String result) {
            mResult = result;
        }

        @Override
        public void run() {
            mPayResult.parseResult(mResult);
            // Util.sendToast("pay result pay result:" + mPayResult.toString());
            if (mPayResult == null) {
                return;
            }
            if (DEBUG)
                Log.d(TAG, "pay result pay result:" + mPayResult.toString());
            showInfoDialog(mPayResult.getStatus());
            switch (mPayResult.getStatus()) {
            case SUCCESS:
                int orderId = 0;
                try {
                    orderId = Integer.parseInt(mPayResult.getOrderId());
                } catch (Exception e) {
                }
                if (orderId > 0) {
                    mInfoDialog.dismiss();
                    final int oid = orderId;
                    HandlerManager.getInstance().getUiHandler()
                            .postDelayed(new Runnable() {

                                @Override
                                public void run() {

                                    refreshView();

                                   // finish();
                                }
                            }, 1000);// delay 2s to finish current activity
                } else {
                    Util.sendToast("±ß«∏£¨÷ß∏∂±¶÷ß∏∂ ß∞‹£¨«Î…‘∫Û÷ÿ ‘~");
                    mInfoDialog.dismiss();
                }
                break;
            case CONFIRMING:
                break;
            case FAIL:
                Util.sendToast("±ß«∏£¨÷ß∏∂±¶÷ß∏∂ ß∞‹£¨«Î…‘∫Û÷ÿ ‘~");
                mInfoDialog.dismiss();
                break;
            }

        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (PayResult.INTENT_ACTION_PAY_RESULT.equals(intent.getAction())) {
                PayResultRunnable resultTask = new PayResultRunnable(
                        intent.getStringExtra(PayResult.EXTRA_PAY_RESULT));
                HandlerManager.getInstance().getUiHandler().post(resultTask);
            }
        }
    };
    
    private void showInfoDialog(PayResult.Status status) {
        if (mInfoDialog != null && mInfoDialog.isShowing()) {
            mInfoDialog.dismiss();
        }
        mInfoDialog = InformationDialog.fromPayResult(this, status);
        mInfoDialog.show();
    }
    
    private void refreshView() {
     // get Order information
        if (mOrderId == 0) {
            mOrder = Order.fromCart();
        } else {
            
            HttpManager.getInstance().getRestAPIClient().getOrderDetail(ConfigManager.getInstance().getCurrentSession(), mOrderId, new HttpCallback<Order>(new Runnable() {

                @Override
                public void run() {
                    Util.sendToast("∂©µ•" + mOrderId + "≤ª¥Ê‘⁄");
                    finish();
                }
                
            }) {

                @Override
                protected boolean processData(Order t) {
                    t.convert();
                    mOrder = t;
                    mOrderItemViewFiller = new OrderItemViewFiller(mOrderItemsZoneView, mOrder);
                    mOrderItemViewFiller.fillViews();
                    initDetailAttr();
                    initRepay();
                    return true;
                }
                
            });
        }
    }
    private void initRepay() {
        if (mOrder.paymentType == PaymentType.ALIPAY &&
                mOrder.orderState == OrderState.SUBMITTED) {
            if (mPay == null) {
                mPay = new AliPay(this);
            
            }
            if (mPayResult == null) {
                mPayResult = new AliPayResult();
            }
            mRepayBtn.setVisibility(View.VISIBLE);
            mRepayBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    showInfoDialog(PayResult.Status.CONFIRMING);
                    
                    StringUtil mmShopNames = new StringUtil();
                    StringUtil foodNames = new StringUtil();

                    Iterator it = mOrder.orderFoods.keySet().iterator();
                    boolean addMmName = false;
                    while (it.hasNext()) {
                        List<OrderFoodItem> foodList = mOrder.orderFoods.get(it.next());
                        if (foodList == null || foodList.isEmpty()) {
                            continue;
                        }

                        addMmName = false;
                        for (OrderFoodItem item : foodList) {
                            if (item.count == 0) {
                                continue;
                            }
                            if (!addMmName) {
                                mmShopNames.append(item.food.mmName);
                                addMmName = true;
                            }
                            foodNames.append(item.food.name);
                        }
                    }

                    // Default is Ali
                    // TODO: shall be according to pay method type

                    final String subject = mmShopNames.toString() + ':'
                            + foodNames.toString();
                    final String body = foodNames.toString(); 
                    
                    mPay.pay(String.valueOf(mOrder.id), subject,
                            body, String.valueOf(mOrder.realPayPrice));
                }
                
            });
            
            if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(PayResult.INTENT_ACTION_PAY_RESULT);
            registerReceiver(mReceiver, filter);
            mRegistered = true;
            }
        } else {
            mRepayBtn.setVisibility(View.GONE);
        }
    }
    private void initDetailAttr() {
        // order id:
        mOrderDetailAttrList.removeAllViews();
        fillDetailAttr(getString(R.string.order_detail_id), mOrder.orderNum);
        fillDetailAttr(getString(R.string.order_detail_state), mOrder.state);
        
        String time = Util.toReadableDate(mOrder.createTime, Util.sDateFormat);
        if (time != null) {
            fillDetailAttr(getString(R.string.order_detail_create_time), time);
        }
        
        // INIT, SUBMITTED, PAIED, CONFIRMED, COOKING_FINISH, TRANSPORTING, FINISHED, CLOSED, UNKNOWN
        time = Util.toReadableDate(mOrder.finishTime, Util.sDateFormat);
        switch(mOrder.orderState) {
        case FINISHED:
            if (time != null) {
                fillDetailAttr(getString(R.string.order_detail_finish_time), time);
            }
            break;
        case CLOSED:
            if (time != null) {
                fillDetailAttr(getString(R.string.order_detail_close_time), time);
            }
            if (!TextUtils.isEmpty(mOrder.closeReason)) {
                fillDetailAttr(getString(R.string.order_detail_close_reason), mOrder.closeReason);
            }
            break;
        default:
            break;
        }
        
        fillDetailAttr(getString(R.string.order_detail_pay_type), mOrder.payTypeString);
      /*
        if (!TextUtils.isEmpty(mOrder.payInfo)) {
            fillDetailAttr(getString(R.string.order_detail_pay_info), mOrder.payInfo);
        }
*/
        if (mOrder.isConsumeRank) {
            fillDetailAttr(getString(R.string.order_detail_use_score), String.valueOf(mOrder.consumeRank));
            fillDetailAttr(getString(R.string.order_detail_score_discount), Util.getMoneyText(mOrder.rankReducePrice));
        }
        
        fillDetailAttr(getString(R.string.order_detail_shipping_fee), Util.getMoneyText(mOrder.kdPrice));
        fillDetailAttr(getString(R.string.order_detail_real_pay), Util.getMoneyText(mOrder.realPayPrice));
        fillDetailAttr(getString(R.string.order_detail_receiver), mOrder.receiver + ' ' + mOrder.phone);
        fillDetailAttr(getString(R.string.order_detail_address), mOrder.address);
        
        
    }
    
    private void fillDetailAttr(String name, String value) {
        View v = mLayoutInflater.inflate(R.layout.item_order_detail_attribute, null);
        TextView nameView = (TextView) v.findViewById(R.id.order_detail_attr_name);
        TextView valueView = (TextView) v.findViewById(R.id.order_detail_attr_value);
        nameView.setText(name);
        valueView.setText(value);
        mOrderDetailAttrList.addView(v);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Util.EXTRA_ID, mOrderId);
        //outState.putInt(Util.EXTRA_SELECT_LOC_TYPE, mLocationType);
    }
    
    @Override
    protected void onDestroy() {
        if (this.mRegistered) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
        
    }

    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_order_detail;
    }

}
