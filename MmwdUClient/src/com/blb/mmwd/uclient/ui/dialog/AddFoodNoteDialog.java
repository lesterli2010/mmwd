package com.blb.mmwd.uclient.ui.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.CartManager;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.OrderFoodItem;
import com.blb.mmwd.uclient.rest.model.User;
import com.blb.mmwd.uclient.rest.model.response.Food;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.util.Util;

public class AddFoodNoteDialog extends MmwdDialog {
    private EditText mComment;
    private Food mFood;
    private int mFoodCount;
    
    public AddFoodNoteDialog(Context context, String note, int foodCount, Food food, Runnable positiveListener) {
        super(context, Util.getString(R.string.dialog_title_add_food_note), Util.getString(R.string.dialog_save), null, positiveListener);
       // super.mPositiveInprogStr = Util.getString(R.string.dialog_send_inprog);
        mComment = (EditText) findViewById(R.id.comment_input_text);
        if (!TextUtils.isEmpty(note)) {
            mTitleView.setText(Util.getString(R.string.dialog_title_modify_food_note));
            mComment.setText(note);
        } else {
            mComment.setHint(Util.getString(R.string.add_food_note_hint, food.mmName));
        }
        mFood = food;
        mFoodCount = foodCount;
    }
   

    @Override
    protected void onOkClicked() {
        String comment = mComment.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Util.sendToast(R.string.msg_food_note_error);
            return;
        }
        
        CartManager.getInstance().updateFood(new OrderFoodItem(mFoodCount, comment, mFood));
    
        Util.hideSoftKeyboard(getOwnerActivity());
        dismissWithPostiveAction();
    }


    @Override
    protected int getViewResourceId() {
        // TODO Auto-generated method stub
        return R.layout.dialog_add_food_note;
    }

}
