package com.blb.mmwd.uclient.ui.filler;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.ui.adapter.ContentListAdapter.FoodCartListener;
import com.blb.mmwd.uclient.util.Util;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FoodSelectionViewFiller {
    private final static boolean DEBUG = true;
    private final static String TAG = "FoodSelectionViewFiller";
    private Button mIncBtn;
    private Button mDecBtn;
    private EditText mAmountInputText;
    private TextView mFoodRest;
    private FoodCartListener mFoodCartListener;

    public FoodSelectionViewFiller(View view, TextView foodRest,
            FoodCartListener foodCartListener) {
        mIncBtn = (Button) view.findViewById(R.id.submit_info_amount_inc);
        mDecBtn = (Button) view.findViewById(R.id.submit_info_amount_dec);
        mAmountInputText = (EditText) view
                .findViewById(R.id.submit_info_amount_edit);
        mFoodRest = foodRest;
        mFoodCartListener = foodCartListener;
    }

    public FoodSelectionViewFiller(View view, FoodCartListener foodCartListener) {
        this(view, null, foodCartListener);
    }

    public void fill(final Food food) {
        int cartCount = CartManager.getInstance().getFoodCount(food.mmid,
                food.id);

        //Log.d(TAG, "fill food:" + food);
        if (cartCount > food.rest) {
            if (DEBUG)
                Log.d(TAG, "not enouth food: cartCount:" + cartCount
                        + ", rest:" + food.rest);
            cartCount = food.rest;
            /*
             * the rest is not enough , take all of them
             */

            CartManager.getInstance().updateFood(
                    new OrderFoodItem(cartCount, null, food));
        }

        mDecBtn.setEnabled(true);
        mIncBtn.setEnabled(true);

        if (cartCount <= 0) {
            mDecBtn.setEnabled(false);
        }

        if (cartCount >= food.rest) {
            mIncBtn.setEnabled(false);
        }

        int myRest = food.rest - cartCount;
        if (mFoodRest != null) {
            mFoodRest.setText(String.valueOf(myRest));
        }

        mAmountInputText.setText(String.valueOf(cartCount));

        mAmountInputText.addTextChangedListener(new TextWatcher() {
            private int mPrevAmount;;

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
                try {
                    mPrevAmount = Integer.parseInt(arg0.toString());
                } catch (Exception e) {
                    mPrevAmount = 0;
                }
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {

                int currentAmount = 0;
                try {
                    currentAmount = Integer.parseInt(arg0.toString());
                } catch (Exception e) {
                    currentAmount = 0;
                }
                // if (DEBUG)
                // Log.d(TAG, "mPrevAmount:" + mPrevAmount + ", currentAmount:"
                // + currentAmount);
                if (mPrevAmount == currentAmount
                        || currentAmount == CartManager.getInstance()
                                .getFoodCount(food.mmid, food.id)) {
                    return;
                }

                if (currentAmount <= food.rest && mFoodCartListener != null) {
                    mFoodCartListener.updateFood(new OrderFoodItem(
                            currentAmount, null, food));

                    int myRest = food.rest - currentAmount;
                    if (mFoodRest != null) {
                        mFoodRest.setText(String.valueOf(myRest));
                    }

                    mDecBtn.setEnabled(true);
                    mIncBtn.setEnabled(true);

                    if (currentAmount <= 0) {
                        mDecBtn.setEnabled(false);
                    }

                    if (currentAmount >= food.rest) {
                        mIncBtn.setEnabled(false);
                    }

                } else {
                    Util.sendToast(R.string.msg_order_no_food);
                }
            }

        });

        mIncBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                handleFoodIncDec(food, true);
            }
        });

        mDecBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                handleFoodIncDec(food, false);

            }

        });
    }

    /**
     * 
     * @param inc
     *            : indicate whether add or delete food
     */
    private void handleFoodIncDec(final Food food, final boolean inc) {

        // First get current number
        int current;
        try {
            current = Integer.parseInt(mAmountInputText.getText().toString());
        } catch (Exception e) {
            current = 0;
        }

        if (inc) {
            if (current >= food.rest) {
                return;
            }
            current++;
            if (current >= food.rest) {
                mIncBtn.setEnabled(false);
            }
            if (!mDecBtn.isEnabled()) {
                mDecBtn.setEnabled(true);
            }
        } else {
            if (current <= 0) {
                return;
            }
            current--;
            if (current <= 0) {
                mDecBtn.setEnabled(false);
            }
            if (!mIncBtn.isEnabled()) {
                mIncBtn.setEnabled(true);
            }
        }
        mAmountInputText.setText(String.valueOf(current));
    }
}
