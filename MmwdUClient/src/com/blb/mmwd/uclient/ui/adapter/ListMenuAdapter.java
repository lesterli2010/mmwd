package com.blb.mmwd.uclient.ui.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import com.blb.mmwd.uclient.R;
import com.blb.mmwd.uclient.ui.adapter.SimpleSelectionListAdapter.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListMenuAdapter extends BaseAdapter {
    private List<ListMenu> mMenuItems;
    private LayoutInflater mInflater;
    private static class ViewHolder {
        ImageView mIcon;
        TextView mName;
        // ImageView mSelectedImage;
    }
    public static class ListMenu {
        public int iconResId;
        public String name;
        public OnClickListener onClickListener;
        
        public ListMenu(int iconResId, String name, OnClickListener onClickListener) {
            this.iconResId = iconResId;
            this.name = name;
            this.onClickListener = onClickListener;
        }
    }

    public ListMenuAdapter(Context context, List<ListMenu> menuItems) {
        mMenuItems = menuItems;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mMenuItems.size();
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
            view = mInflater.inflate(R.layout.item_mine_menu, parent, false);
            holder.mIcon = (ImageView) view.findViewById(R.id.mine_menu_icon);
            holder.mName = (TextView) view.findViewById(R.id.mine_menu_name);
            // holder.mSelectedImage = (ImageView)
            // view.findViewById(R.id.select_item_selected_image);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        ListMenu menu = mMenuItems.get(position);
        if (menu.iconResId != 0) {
        holder.mIcon.setImageResource(menu.iconResId);
        holder.mIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }
        holder.mName.setText(menu.name);
        view.setOnClickListener(menu.onClickListener);
        return view;
    }

}
