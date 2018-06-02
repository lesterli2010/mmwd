package com.blb.mmwd.uclient.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HandlerManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.manager.ShippingAddressManager;
import com.blb.mmwd.uclient.pay.IPay;
import com.blb.mmwd.uclient.pay.PayResult;
import com.blb.mmwd.uclient.pay.alipay.AliPay;
import com.blb.mmwd.uclient.pay.alipay.AliPayResult;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.ConfigItem;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.OrderSubmitFoods;
import com.blb.mmwd.uclient.rest.model.OrderSubmitFoods.OrderSubmitFoodItem;
import com.blb.mmwd.uclient.rest.model.ShippingAddress;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.MmShops;
import com.blb.mmwd.uclient.rest.model.response.Order;
import com.blb.mmwd.uclient.rest.model.response.OrderSubmitResult;
import com.blb.mmwd.uclient.rest.model.response.ResponseIntValue;
import com.blb.mmwd.uclient.ui.adapter.OrderHistoryCompactListAdapter;
import com.blb.mmwd.uclient.ui.dialog.EditShippingAddressDialog;
import com.blb.mmwd.uclient.ui.dialog.InformationDialog;
import com.blb.mmwd.uclient.ui.dialog.UseScoreDialog;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.ui.filler.OrderItemViewFiller;
import com.blb.mmwd.uclient.util.StringUtil;
import com.blb.mmwd.uclient.util.Util;
import com.blb.mmwd.uclient.util.Util.OrderState;
import com.blb.mmwd.uclient.util.Util.PaymentType;
import com.google.gson.Gson;

public class OrderSettlementActivity extends TopCaptionActivity {
    private final static String TAG = "OrderSettlementActivity";
    private final static boolean DEBUG = true;
    
    public final static String EXTRA_MM_SHOP_ID = "extra_mm_shop_id";
    public final static String EXTRA_IS_CROSS_AREA = "extra_is_cross_area";
    public final static String EXTRA_IS_SETTLEMENT_SELECTED = "extra_is_settlement_selected";
    
    private boolean mIsCrossArea;
    private int mMmShopId; // for cross area shipping
    private boolean mIsSettlementSelected;

    private ShippingAddress mShippingAddress;
    private View mOrderShippingAddress;
    private View mNoShippingAddressText;
    private TextView mOrderAddressPhone;
    private TextView mOrderAddressDetails;
    private View mShippingAddressZone;
    private View mShippingAddressLoading;
    private Order mOrder;
    private OrderItemViewFiller mOrderItemViewFiller;
    private MessageViewFiller mMsgViewFiller;
    private Button mPayBtn;

    private IPay mPay;
    private PayResult mPayResult;

    private Dialog mInfoDialog;
    
    private View mUseScore;
    private View mUseScoreInfoLoading;
    private View mUseScoreInfo;
    private TextView mUseScoreCount;
    private TextView mUseScoreDiscount;
    private TextView mUseScoreNeedPay;
    
    private boolean mGetShippingFeeInprogress;
    private float mShippingFee;
    
    private float mRealPayFee; //The real pay fee
    private float mDiscount;

    // paytype
    private PaymentType mPayType = PaymentType.COD;
    private View mCodView;
    private ImageView mCodImage;
    private View mAlipayView;
    private ImageView mAlipayImage;
    
    // shipping fee
    private View mShippingFeeZone;
    private View mShippingFeeLoading;
    private View mShippingFeeResult;
    private TextView mShippingFeeAmount;

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
                    final int oid = orderId;
                    HandlerManager.getInstance().getUiHandler()
                            .postDelayed(new Runnable() {

                                @Override
                                public void run() {

                                    Util.startActivity(
                                            OrderSettlementActivity.this,
                                            OrderDetailActivity.class, oid);

                                    mInfoDialog.dismiss();
                                    finish();
                                }
                            }, 2000);// delay 2s to finish current activity
                } else {
                    Util.sendToast("抱歉，支付宝支付失败，请稍后重试~");
                    mInfoDialog.dismiss();
                    finish();
                }
                break;
            case CONFIRMING:
                break;
            case FAIL:
                Util.sendToast("抱歉，支付宝支付失败，请稍后重试~");
                mInfoDialog.dismiss();
                finish();
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

    private OnClickListener mCreateNewAddrListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            EditShippingAddressDialog dialog = new EditShippingAddressDialog(
                    OrderSettlementActivity.this, new Runnable() {
                        @Override
                        public void run() {
                            Util.hideSoftKeyboard(OrderSettlementActivity.this);
                            refreshAddress(true, false);
                        }

                    }, mIsCrossArea);
            dialog.show();
        }
    };

    private OnClickListener mSelectAddrListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            startSelectAddrActivity();
        }
    };
    
    // selected score
    private Runnable mUseScoreRunnable = new Runnable() {

        @Override
        public void run() {
            refreshUseScoreInfo();
            
        }
        
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CartManager.getInstance().isEmpty()) {
            Toast.makeText(this, this.getString(R.string.error_msg_cart_empty),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        /*
         * public final static String EXTRA_MM_SHOP_ID = "extra_mm_shop_id";
    public final static String EXTRA_IS_CROSS_AREA = "extra_is_cross_area";
    public final static String EXTRA_IS_SETTLEMENT_SELECTED = "extra_is_settlement_selected";
    
    private boolean mIsCrossArea;
    private boolean mMmShopId;
    private boolean mIsSettlementSelected;
         */
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                savedInstanceState = intent.getExtras();
            }
        }

        if (savedInstanceState != null) {
            mMmShopId = savedInstanceState.getInt(EXTRA_MM_SHOP_ID, 0);
            mIsCrossArea = savedInstanceState.getBoolean(EXTRA_IS_CROSS_AREA, false);
            mIsSettlementSelected = savedInstanceState.getBoolean(EXTRA_IS_SETTLEMENT_SELECTED, false);
        }
        
      
        
        mPayBtn = (Button) findViewById(R.id.submit_order_btn);

        mPayBtn.setFocusable(true);
        mPayBtn.requestFocus();
        mPayBtn.setFocusableInTouchMode(true);

        
        Log.d(TAG, "OrderSettlementActivity, mMmShopId:" + mMmShopId + ", mIsCrossArea:" + mIsCrossArea + ", mIsSettlementSelected:" + mIsSettlementSelected);
        if (mIsSettlementSelected) {
            mOrder = Order.fromCart(mIsCrossArea, mMmShopId);
        } else {
            mIsCrossArea = CartManager.getInstance().isCrossAreaShippingAllowed();
            
            mOrder = Order.fromCart();
            Iterator<Integer> it = mOrder.orderFoods.keySet().iterator();
            if (it.hasNext()) {
                mMmShopId = it.next();
            }
        }

        mShippingAddressZone = this
                .findViewById(R.id.order_shipping_address_zone);
        mShippingAddressLoading = this
                .findViewById(R.id.order_shipping_address_loading);

        mOrderShippingAddress = findViewById(R.id.order_address_zone);
        mNoShippingAddressText = findViewById(R.id.order_create_address_text);
        mOrderAddressPhone = (TextView) findViewById(R.id.order_address_phone);
        mOrderAddressDetails = (TextView) findViewById(R.id.order_address_detail);

     // shipping fee
        mShippingFeeZone = findViewById(R.id.shipping_fee_zone);
        mShippingFeeLoading = mShippingFeeZone.findViewById(R.id.shipping_fee_loading);
        mShippingFeeResult = mShippingFeeZone.findViewById(R.id.shipping_fee_result);
        mShippingFeeAmount = (TextView) mShippingFeeZone.findViewById(R.id.shipping_fee_amount);
        
        refreshAddress(true, true);

        View main = findViewById(R.id.zone_order_items);
        mMsgViewFiller = new MessageViewFiller(
                findViewById(R.id.error_msg_zone), main);
        mOrderItemViewFiller = new OrderItemViewFiller(main, mOrder, new Runnable() {

            @Override
            public void run() {
                fillScoreInfoView();
                
            }
            
        });
        
        // don't try to get self product
        mMsgViewFiller.showMain();
        
/*
        HttpManager
                .getInstance()
                .getRestAPIClient()
                .getSelfMmShops(
                        ConfigManager.getInstance().getConfigData().getZoneId(),
                        new HttpCallback<MmShops>(new Runnable() {

                            @Override
                            public void run() {
                                mMsgViewFiller.showMain();
                            }

                        }) {

                            @Override
                            protected boolean processData(MmShops t) {
                                CartManager.getInstance().updateFood(t);
                                mOrderItemViewFiller.fillViews();
                                mMsgViewFiller.showMain();
                                return true;
                            }
                        });
*/
        mPayBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                submitOrder();
            }

        });

        // TODO: only alipay
        mPay = new AliPay(this);
        mPayResult = new AliPayResult();

        mCodView = findViewById(R.id.select_pay_cod);
        mCodImage = (ImageView) findViewById(R.id.select_pay_cod_img);

        mAlipayView = findViewById(R.id.select_pay_alipay);
        mAlipayImage = (ImageView) findViewById(R.id.select_pay_alipay_img);

        mCodView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mPayType == PaymentType.COD) {
                    mPayType = null;
                    mCodImage
                            .setImageResource(R.drawable.checkbox_uncheck_normal);
                } else {
                    mPayType = PaymentType.COD;
                    mUseScore.setVisibility(View.GONE);
                    mCodImage
                            .setImageResource(R.drawable.checkbox_checked_normal);
                    mAlipayImage
                            .setImageResource(R.drawable.checkbox_uncheck_normal);
                }
            }

        });

        mAlipayView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mPayType == PaymentType.ALIPAY) {
                    mPayType = null;
                    mUseScore.setVisibility(View.GONE);
                    mAlipayImage
                            .setImageResource(R.drawable.checkbox_uncheck_normal);
                } else {
                    mPayType = PaymentType.ALIPAY;
                    mUseScore.setVisibility(View.VISIBLE);
                    mAlipayImage
                            .setImageResource(R.drawable.checkbox_checked_normal);
                    mCodImage
                            .setImageResource(R.drawable.checkbox_uncheck_normal);
                }
            }

        });
        
        findViewById(R.id.order_use_score_open_dialog).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Dialog dialog = new UseScoreDialog(OrderSettlementActivity.this, mUseScoreRunnable);
                dialog.show();
            }
            
        });
        
        mUseScore = findViewById(R.id.order_use_score);
        mUseScoreInfoLoading = findViewById(R.id.order_use_score_info_loading);
        mUseScoreInfo = findViewById(R.id.order_use_score_info);
        mUseScoreCount = (TextView) mUseScoreInfo.findViewById(R.id.order_use_score_count);
        mUseScoreDiscount = (TextView) mUseScoreInfo.findViewById(R.id.order_use_score_discount);
        mUseScoreNeedPay = (TextView) mUseScoreInfo.findViewById(R.id.order_use_score_need_pay);
        
        CartManager.getInstance().setUsedScore(0); // clear
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(PayResult.INTENT_ACTION_PAY_RESULT);
        registerReceiver(mReceiver, filter);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(EXTRA_MM_SHOP_ID, mMmShopId);
        outState.putBoolean(EXTRA_IS_CROSS_AREA, mIsCrossArea);
        outState.putBoolean(EXTRA_IS_SETTLEMENT_SELECTED, mIsSettlementSelected);
    }
    
    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /**
     * Refresh use score info
     */
    private void refreshUseScoreInfo() {
        int usedScore = CartManager.getInstance().getUsedScore();
        if (usedScore == 0) {
            mUseScoreInfo.setVisibility(View.GONE);
        } else {
            float scoreRate = ConfigManager.getInstance().getScoreExchangeRate();
            if (scoreRate == 0) {
                mUseScoreInfoLoading.setVisibility(View.VISIBLE);
                mUseScoreInfo.setVisibility(View.GONE);
                HttpManager.getInstance().getRestAPIClient().getScoreExchangeRate(new HttpCallback<ResponseIntValue>(new Runnable() {

                    @Override
                    public void run() {
                        Util.sendToast("抱歉，暂不能兑换积分~");
                        CartManager.getInstance().setUsedScore(0);
                        mUseScoreInfoLoading.setVisibility(View.GONE);
                        mUseScoreInfo.setVisibility(View.GONE);
                    }
                    
                }) {

                    @Override
                    protected boolean processData(ResponseIntValue t) {
                        if (t.value == 0) {
                            return false;
                        }
                        
                        ConfigManager.getInstance().setScoreExchangeRate((float)t.value/100);
                        fillScoreInfoView();
                        return true;
                    }
                    
                });
            } else {
                fillScoreInfoView();
            }
        }
    }
    
    private void fillScoreInfoView() {
        calculateScoreDiscount();
        
        mUseScoreInfoLoading.setVisibility(View.GONE);
        mUseScoreInfo.setVisibility(View.VISIBLE);
        mUseScoreCount.setText(String.valueOf(CartManager.getInstance().getUsedScore()));
        mUseScoreDiscount.setText(Util.getMoneyText(mDiscount));
        mUseScoreNeedPay.setText(Util.getMoneyText(mRealPayFee));
    }
    
    private void calculateScoreDiscount() {
        if (CartManager.getInstance().isEmpty()) {
            CartManager.getInstance().setUsedScore(0);
            mDiscount = 0;
            mRealPayFee = 0;
            return;
        }
        int usedScore = CartManager.getInstance().getUsedScore();
        float exchangeRate = ConfigManager.getInstance().getScoreExchangeRate();
        
        mDiscount = usedScore * exchangeRate;
        mRealPayFee = CartManager.getInstance().getTotalFee() - mDiscount;
        if (mRealPayFee <= 0) {
            // recalculate, only need to pay 0.01元
            usedScore = (int) ((CartManager.getInstance().getTotalFee()*100)/(exchangeRate*100));
            mDiscount = CartManager.getInstance().getTotalFee();
            mRealPayFee = 0f;
            CartManager.getInstance().setUsedScore(usedScore);
            Util.sendToast("目前可以使用" +usedScore + "积分~");
        }
    }
    
    /**
     * First submit order information to server, then pay
     */
    private void submitOrder() {
        if (mShippingAddress == null) {
            Util.sendToast(R.string.msg_order_addr_empty);
            return;
        }

        if (mPayType == null) {
            Util.sendToast(R.string.msg_order_pay_type_empty);
            return;
        }

        if (mOrder.orderFoods.size() == 0
                || CartManager.getInstance().isEmpty()) {
            Util.sendToast(R.string.msg_order_food_empty);
            return;
        }
        
        if (mGetShippingFeeInprogress) {
            Util.sendToast("配送费正在计算，暂不能提交~");
            return;
        }

        CartManager.getInstance().setSubmitting(true);

        OrderSubmitFoods submitFoods = new OrderSubmitFoods();

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
                submitFoods.list.add(new OrderSubmitFoodItem(item.food.id,
                        item.count, item.note));
                foodNames.append(item.food.name);
            }
        }

        // Default is Ali
        // TODO: shall be according to pay method type

        mOrder.totalPrice = CartManager.getInstance().getTotalFee();

        final String subject = mmShopNames.toString() + ':'
                + foodNames.toString();
        final String body = foodNames.toString(); 
        final boolean useScore = mPayType != PaymentType.COD &&
                CartManager.getInstance().getUsedScore() != 0;
        final int usedScore = useScore ? CartManager.getInstance().getUsedScore() : 0;
        /*
        if (mPayType != PaymentType.COD) {
            calculateScoreDiscount();
        }
        final String price = mPayType != PaymentType.COD ? String.valueOf(mRealPayFee) : String.valueOf(mOrder.totalPrice);
        */// TODO tmp
                                    // String.valueOf(mOrder.totalPrice);

        if (DEBUG)
            Log.d(TAG, "pay, subject:" + subject + ", body:" + body);
        // mPay.pay(mmShopNames.toString() + ' ' + foodNames.toString(), body,
        // mOrder.totalFee);

        mPayBtn.setEnabled(false);
        mPayBtn.setText(R.string.msg_order_submitting);
        HttpManager
                .getInstance()
                .getRestAPIClient()
                .submitOrder(ConfigManager.getInstance().getCurrentSession(),
                        mPayType.ordinal(),
                        mShippingAddress.addr, mShippingAddress.rname,
                        mShippingAddress.phone, useScore, usedScore, submitFoods,
                        new HttpCallback<OrderSubmitResult>(new Runnable() {

                            @Override
                            public void run() {
                                CartManager.getInstance().setSubmitting(false);
                                // Util.sendToast(R.string.msg_order_submit_fail);
                                mPayBtn.setEnabled(true);
                                mPayBtn.setText(R.string.submit_order);
                            }

                        }) {

                            @Override
                            protected boolean processData(OrderSubmitResult t) {
                                CartManager.getInstance().setSubmitting(false);
                                // clean cart
                                if (!mIsSettlementSelected) {
                                    CartManager.getInstance().clear();
                                } else {
                                    if (mIsCrossArea) {
                                        CartManager.getInstance().clear(mMmShopId);
                                    } else {
                                        CartManager.getInstance().clear(false);
                                    }
                                }
                                if (mPayType == PaymentType.COD) {
                                    // 提交成功, open order detail activity
                                    Util.sendToast(R.string.msg_submit_order_succ);
                                    Util.startActivity(
                                            OrderSettlementActivity.this,
                                            OrderDetailActivity.class, t.value);
                                    finish();
                                } else {
                                    // alipay
                                    if (useScore) {
                                        ConfigManager.getInstance().reduceCurrentScore(usedScore);
                                    }
                                    if (t.pricePay == 0f) {
                                        Util.sendToast(R.string.msg_submit_order_succ);
                                        Util.startActivity(
                                                OrderSettlementActivity.this,
                                                OrderDetailActivity.class, t.value);
                                        finish();
                                        return true;
                                    }
                                    mPayBtn.setText(R.string.msg_order_paying);
                                    showInfoDialog(PayResult.Status.CONFIRMING);

                                    mPay.pay(String.valueOf(t.value), subject,
                                            body, String.valueOf(t.pricePay));
                                    return true;
                                }
                                return true;
                            }

                        });

    }

    private void showInfoDialog(PayResult.Status status) {
        if (mInfoDialog != null && mInfoDialog.isShowing()) {
            mInfoDialog.dismiss();
        }
        mInfoDialog = InformationDialog.fromPayResult(this, status);
        mInfoDialog.show();
    }

    private Runnable mGetHttpAddressListener = new Runnable() {
        @Override
        public void run() {
            Util.showHideView(mShippingAddressZone, mShippingAddressLoading);
            refreshAddress(true, false);
        }
    };
    
    private void refreshShippingFee() {
        /*
         *  mShippingFeeZone = findViewById(R.id.shipping_fee_zone);
        mShippingFeeLoading = mShippingFeeZone.findViewById(R.id.shipping_fee_loading);
        mShippingFeeResult = mShippingFeeZone.findViewById(R.id.shipping_fee_result);
        mShippingFeeAmount = (TextView) mShippingFeeZone.findViewById(R.id.shipping_fee_amount);
        
         private boolean mGetShippingFeeInprogress;
    private float mShippingFee;
         */
        
        if (!mIsCrossArea || mShippingAddress == null) {
            mShippingFeeZone.setVisibility(View.GONE);
            
            return;
        }
        
        mShippingFeeZone.setVisibility(View.VISIBLE);
        
        Util.showHideView(mShippingFeeLoading, mShippingFeeResult);
        
        mGetShippingFeeInprogress = true;
        
        if (!mShippingAddress.cross && TextUtils.isEmpty(mShippingAddress.dname)) {
            mShippingAddress.dname = ConfigManager.getInstance().getDistrictNameByZoneId(mShippingAddress.qid);
        }
        if (DEBUG)
        Log.d(TAG, "getShippingFee, shopId:" + mMmShopId + ", addr:" + mShippingAddress.getFullAddress());
        
        HttpManager.getInstance().getRestAPIClient().getShippingFee(mMmShopId, mShippingAddress.getFullAddress(), new HttpCallback<ResponseIntValue>(new Runnable() {

            @Override
            public void run() {
                mGetShippingFeeInprogress = false;
                Util.showHideView(mShippingFeeResult, mShippingFeeLoading);
                mShippingFee = 10f; // default 10
                mShippingFeeAmount.setText(Util.getMoneyText(mShippingFee));
            }
            
        }) {

            @Override
            protected boolean processData(ResponseIntValue t) {
                mGetShippingFeeInprogress = false;
                Util.showHideView(mShippingFeeResult, mShippingFeeLoading);
                mShippingFee = (float)t.value;
                mShippingFeeAmount.setText(Util.getMoneyText(mShippingFee));
                return true;
            }
            
        });
    }

    private void refreshAddress(boolean useDefault, boolean get) {
        if (useDefault) {
            int crossAreaId = -1;
            if (mIsCrossArea) {
                crossAreaId = ConfigManager.getInstance().getCrossAreaId();
            }
            mShippingAddress = ShippingAddressManager.getInstance()
                    .getDefaultAddress(crossAreaId);
            

        }
        if (mShippingAddress == null) {
            if (get) {
                Util.showHideView(mShippingAddressLoading, mShippingAddressZone);
                ShippingAddressManager.getInstance()
                        .getShippingAddress(
                                ConfigManager.getInstance().getConfigData()
                                        .getZoneId(), mGetHttpAddressListener,
                                mGetHttpAddressListener);

            } else {
                mShippingAddressZone.setOnClickListener(mCreateNewAddrListener);
                Util.showHideView(mNoShippingAddressText, mOrderShippingAddress);
            }
        } else {
            mOrder.shippingAddress = mShippingAddress;
            mOrderAddressPhone.setText(mShippingAddress.phone);
            mOrderAddressDetails.setText(mShippingAddress.getFullAddress());
            mShippingAddressZone.setOnClickListener(mSelectAddrListener);
            Util.showHideView(mOrderShippingAddress, mNoShippingAddressText);
        }
        
        refreshShippingFee();
    }

    private void startSelectAddrActivity() {
        Intent intent = new Intent(this,
                ShippingAddressManagementActivity.class);
        intent.putExtra(
                ShippingAddressManagementActivity.EXTRA_SEL_ADDR_ACTIVITY_TYPE,
                1);
        intent.putExtra(ShippingAddressManagementActivity.EXTRA_SEL_ADDR_ACTIVITY_CROSS_AREA_ID, mIsCrossArea ? ConfigManager.getInstance().getCrossAreaId() : -1);
        startActivityForResult(intent,
                ShippingAddressManagementActivity.REQ_SELECT_ADDR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ShippingAddressManagementActivity.RES_SELECT_ADDR_SUCC) {
            int aid = data.getExtras().getInt(Util.EXTRA_ID);
            if (aid != 0) {
                mShippingAddress = ShippingAddressManager.getInstance()
                        .getShippingAddress(aid);

                refreshAddress(false, false);
            } else {
                refreshAddress(true, false);
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // ?
        if (mOrderItemViewFiller != null) {
            mOrderItemViewFiller.fillViews();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_order_settlement;
    }
    
}
