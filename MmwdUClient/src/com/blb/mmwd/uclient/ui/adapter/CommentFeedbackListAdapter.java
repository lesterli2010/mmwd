package com.blb.mmwd.uclient.ui.adapter;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.manager.ShippingAddressManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.response.ResponseHead;
import com.blb.mmwd.uclient.ui.dialog.ConfirmationDialog;
import com.blb.mmwd.uclient.ui.dialog.InformationDialog;
import com.blb.mmwd.uclient.util.Util;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class CommentFeedbackListAdapter extends BaseAdapter {
    private List<CommentFeedbackItem> mItems;
    private LayoutInflater mInflater;
    private WeakReference<Context> mContext;
    private Runnable mDelListener;
    private class ViewHolder implements
    OnClickListener {
        int commentId;
        public PopupWindow editMenuWindow;
        ImageView personImg;
        TextView personName;
        TextView content;
        TextView time;
        View editBtn;
        // ImageView mSelectedImage;
        
        @Override
        public void onClick(View v) {
            if (editMenuWindow == null) {
                View menuView = mInflater.inflate(
                        R.layout.menu_edit_del_address, null);

                editMenuWindow = new PopupWindow(menuView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                
                editMenuWindow.setOutsideTouchable(true);
                editMenuWindow.setFocusable(true);
                editMenuWindow
                        .setBackgroundDrawable(mContext
                                .get()
                                .getResources()
                                .getDrawable(
                                        R.drawable.abc_ab_stacked_transparent_light_holo));
                menuView.findViewById(R.id.tv_edit).setVisibility(View.GONE); // not support edit
                menuView.findViewById(R.id.tv_del).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                // TODO Auto-generated method stub
                                editMenuWindow.dismiss();
                                Dialog dialog = new ConfirmationDialog(mContext.get(), 
                                        Util.getString(R.string.dialog_confirm_del_comm_title), 
                                        Util.getString(R.string.dialog_confirm_del_comm_content),
                                        null, new Runnable() {

                                            @Override
                                            public void run() {
                                                final Dialog infoDiag = new InformationDialog(mContext.get(), R.string.dialog_del_inprog);
                                                infoDiag.show();
                                                
                                                HttpManager.getInstance().getRestAPIClient().delComment(ConfigManager.getInstance().getCurrentSession(),
                                                        commentId, new HttpCallback<ResponseHead>(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                infoDiag.dismiss();
                                                                //Util.sendToast("");
                                                                
                                                            }
                                                            
                                                        }) {

                                                            @Override
                                                            protected boolean processData(
                                                                    ResponseHead t) {
                                                                Util.sendToast("意见反馈已经删除~");
                                                                if (mDelListener != null) {
                                                                    mDelListener.run();
                                                                }
                                                                infoDiag.dismiss();
                                                                return true;
                                                            }
                                                    
                                                });
                                                
                                            }
                                    
                                });
                                dialog.show();
                            }
                            
                        });
            }
            if (editMenuWindow.isShowing()) {
                editMenuWindow.dismiss();
            } else {
               // mEditMenuWindow.showAtLocation(v,  Gravity.RIGHT | Gravity.BOTTOM, 10,0); 
                editMenuWindow.showAsDropDown(v, -20, 0);
            }
        }
    }

    public static class CommentFeedbackItem {
        int id;
        boolean isFeedback;
        String content;
        String date;
        
        public CommentFeedbackItem(int id, boolean isFeedback, String content, String date) {
            this.id = id;
            this.isFeedback = isFeedback;
            this.content = content;
            this.date = date;
        }
    }
    
    public CommentFeedbackListAdapter(Context context, List<CommentFeedbackItem> commentItems, Runnable listener) {
        mItems = commentItems;
        mInflater = LayoutInflater.from(context);
        mContext = new WeakReference<Context>(context);
        mDelListener = listener;
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_comment_feedback, parent, false);
            holder.personImg = (ImageView) view.findViewById(R.id.comment_feedback_person_icon);
            holder.personName = (TextView) view.findViewById(R.id.comment_feedback_person_name);
            holder.time = (TextView) view.findViewById(R.id.comment_feedback_time);
            holder.content = (TextView) view.findViewById(R.id.comment_feedback_content);
            holder.editBtn = view.findViewById(R.id.ll_toolbar);
            // holder.mSelectedImage = (ImageView)
            // view.findViewById(R.id.select_item_selected_image);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final CommentFeedbackItem item = mItems.get(position);
        holder.commentId = item.id;
        if (item.isFeedback) {
            holder.personName.setText(mContext.get().getString(R.string.service_people_txt));
            holder.personName.setTextColor(mContext.get().getResources()
                    .getColor(R.color.text_blue));
            holder.personImg.setImageResource(R.drawable.ic_mine_menu_comment);
            holder.editBtn.setVisibility(View.GONE);
            holder.editBtn.setOnClickListener(null);
        } else {
            holder.personName.setText(mContext.get().getString(R.string.mine_txt));
            holder.personName.setTextColor(mContext.get().getResources()
                    .getColor(R.color.main_color));
            holder.personImg.setImageResource(R.drawable.ic_mine);
            holder.editBtn.setVisibility(View.VISIBLE);
            holder.editBtn.setOnClickListener(holder);
        }
        
        holder.content.setText(item.content);
        if(item.date != null) {
            holder.time.setText(item.date);
            holder.time.setVisibility(View.VISIBLE);
        } else {
            holder.time.setVisibility(View.GONE);
        }
        return view;
    }

}
