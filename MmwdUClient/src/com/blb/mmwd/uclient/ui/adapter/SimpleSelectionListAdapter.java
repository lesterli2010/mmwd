package com.blb.mmwd.uclient.ui.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.rest.model.ConfigItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SimpleSelectionListAdapter extends BaseAdapter {
    private static final String TAG = "SimpleSelectionListAdapter";
    public static class ViewHolder {
        public int mId;
        TextView mTextView;
        // ImageView mSelectedImage;
    }

    private LayoutInflater mInflater;
    private List<ConfigItem> mAttrIdNames;
    private int mConfigedId;
    private WeakReference<Context> mContext;
    public SimpleSelectionListAdapter(Context context, List<ConfigItem> AttrIdNames,
            int configedId) {
        mInflater = LayoutInflater.from(context);
        mContext = new WeakReference<Context>(context);
        mAttrIdNames = AttrIdNames;
        mConfigedId = configedId;
    }

    public void setData(List<ConfigItem> data) {
        mAttrIdNames = data;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mAttrIdNames != null ? mAttrIdNames.size() : 0;
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
            view = mInflater.inflate(R.layout.item_selection_list, parent, false);
            holder.mTextView = (TextView) view.findViewById(R.id.select_item_name);
            // holder.mSelectedImage = (ImageView)
            // view.findViewById(R.id.select_item_selected_image);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.mId = mAttrIdNames.get(position).id;
        holder.mTextView.setText(mAttrIdNames.get(position).name);
        
        Log.d(TAG, "mId:" + holder.mId + ",configedId:" + mConfigedId + ",position:" + position);
        if ((holder.mId == mConfigedId) || (mConfigedId == -1 && position == 0)) {
            holder.mTextView.setTextColor(mContext.get().getResources()
                    .getColor(R.color.main_color));
        } else {
            holder.mTextView.setTextColor(mContext.get().getResources().getColorStateList(R.color.text_color_selector));
        }
        // if (position == mSelectedPos) {
        // holder.mSelectedImage.setVisibility(View.VISIBLE);
        // } else {
        // holder.mSelectedImage.setVisibility(View.GONE);
        // }
        return view;
    }

}
