package com.example.alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PopupMenuWallpaperAdapter extends RecyclerView.Adapter<PopupMenuWallpaperAdapter.ViewHolder> {

    private final List<MenuItemData> items;
    private final OnItemClickListener onItemClick;

    public PopupMenuWallpaperAdapter(List<MenuItemData> items, OnItemClickListener onItemClick) {
        this.items = items;
        this.onItemClick = onItemClick;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        public final LinearLayout layout;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.menu_item_text);
            layout = view.findViewById(R.id.menu_item_layout);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MenuItemData item = items.get(position);
        holder.textView.setText(item.title);
        holder.layout.setBackgroundResource(item.backgroundRes);
        holder.itemView.setOnClickListener(v -> onItemClick.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onClick(MenuItemData menuItem);
    }
}
