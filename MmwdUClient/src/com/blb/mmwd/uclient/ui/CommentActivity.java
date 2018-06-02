package com.blb.mmwd.uclient.ui;

import android.app.Dialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.manager.ConfigManager;
import com.blb.mmwd.uclient.manager.HttpManager;
import com.blb.mmwd.uclient.rest.api.HttpCallback;
import com.blb.mmwd.uclient.rest.model.Comment;
import com.blb.mmwd.uclient.rest.model.response.Comments;
import com.blb.mmwd.uclient.ui.adapter.CommentFeedbackListAdapter;
import com.blb.mmwd.uclient.ui.dialog.AddCommentDialog;
import com.blb.mmwd.uclient.ui.filler.MessageViewFiller;
import com.blb.mmwd.uclient.ui.filler.PrevNextViewFiller;
import com.blb.mmwd.uclient.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class CommentActivity extends TopCaptionActivity {

    private ListView mList;
    private BaseAdapter mAdapter;
    private PrevNextViewFiller mPrevNextFiller;
    private MessageViewFiller mMsgFiller;
    private List<CommentFeedbackListAdapter.CommentFeedbackItem> mCommentItems = new ArrayList<CommentFeedbackListAdapter.CommentFeedbackItem>();
    private int mCurrentPage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mList = (ListView) findViewById(R.id.comment_feedback_list);
        mMsgFiller = new MessageViewFiller(findViewById(R.id.error_msg_zone), mList);
        mPrevNextFiller = new PrevNextViewFiller(findViewById(R.id.prev_next_zone), new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mCurrentPage > 0) {
                    refreshView(mCurrentPage - 1);
                }
                
            }
            
        }, new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                refreshView(mCurrentPage + 1);
                
            }
            
        });
        findViewById(R.id.comment_feedback_add_new).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Dialog dialog = new AddCommentDialog(arg0.getContext(), new Runnable() {

                    @Override
                    public void run() {
                        refreshView(0);
                        
                    }
                    
                });
                dialog.show();
            }
            
        });
        
        mAdapter = new CommentFeedbackListAdapter(this, mCommentItems, new Runnable() {

            @Override
            public void run() {
                refreshView(mCurrentPage);
                
            }
            
        });
        mList.setAdapter(mAdapter);
        refreshView(mCurrentPage);
    }
    
    private void refreshView(final int pageNum) {
        mMsgFiller.fill(null);
        HttpManager.getInstance().getRestAPIClient().getCommentList(ConfigManager.getInstance().getCurrentSession(), 
                pageNum, Util.MAX_ITEM_NUM_ONE_PAGE, new HttpCallback<Comments>(new Runnable() {

                    @Override
                    public void run() {
                        mMsgFiller.fill(0, Util.getString(R.string.msg_no_comment), Util.getString(R.string.btn_click_to_refresh), new OnClickListener() {


                            @Override
                            public void onClick(View arg0) {
                                refreshView(pageNum);
                                
                            }
                            
                        }, null, null);
                        
                    }
                    
                }) {

                    @Override
                    protected boolean processData(Comments t) {
                        if (t.list == null || t.list.isEmpty()) {
                            // not the first page
                            if (pageNum > 0) {
                                mMsgFiller.showMain();
                                Util.sendToast(R.string.msg_no_more);
                                return true;
                            }
                            return false;
                        }
                        mCurrentPage = pageNum;
                        mPrevNextFiller.fill(mCurrentPage, t.list.size());
                        mCommentItems.clear();
                        mAdapter.notifyDataSetChanged();
                        for (Comment c : t.list) {
                                                        
                            String readableDate = Util.toReadableDate(c.date1, Util.sDateFormat);
                            
                            mCommentItems.add(new CommentFeedbackListAdapter.CommentFeedbackItem(c.id,
                                    false, c.content, readableDate));
                            if (TextUtils.isEmpty(c.reply)) {
                                mCommentItems.add(new CommentFeedbackListAdapter.CommentFeedbackItem(c.id,
                                        true, Util.getString(R.string.no_feedback_default_text), null));
                            } else {
                                readableDate = Util.toReadableDate(c.date2, Util.sDateFormat);
                               
                                mCommentItems.add(new CommentFeedbackListAdapter.CommentFeedbackItem(c.id,
                                        true, c.reply, readableDate));
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        mMsgFiller.showMain();
                        return true;
                    }
            
        });
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCurrentPage = 0;
        refreshView(mCurrentPage);
    }
    
    @Override
    protected int getLayoutResourceId() {
        // TODO Auto-generated method stub
        return R.layout.activity_comment;
    }

}
