package com.example.alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ColorThemeMenuAdapter extends RecyclerView.Adapter<ColorThemeMenuAdapter.ViewHolder> {

    private final List<ColorThemeMenuItem> menuItems;
    private final OnItemClickListener onItemClick;

    public ColorThemeMenuAdapter(List<ColorThemeMenuItem> menuItems, OnItemClickListener onItemClick) {
        this.menuItems = menuItems;
        this.onItemClick = onItemClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_theme_menu_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ColorThemeMenuItem menuItem = menuItems.get(position);
        holder.mainColorView.setBackgroundResource(menuItem.mainColorRes);
        holder.secondColorView.setBackgroundResource(menuItem.secondColorRes);
        holder.itemView.setOnClickListener(v -> onItemClick.onClick(menuItem));
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mainColorView;
        public final View secondColorView;

        public ViewHolder(View itemView) {
            super(itemView);
            mainColorView = itemView.findViewById(R.id.menu_theme_main_icon);
            secondColorView = itemView.findViewById(R.id.menu_theme_second_icon);
        }
    }

    public interface OnItemClickListener {
        void onClick(ColorThemeMenuItem menuItem);
    }
}
