package com.example.lab3components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 第 5 题的 Adapter：ViewHolder 复用 + 稳定 id。
 * 点击只挂在“名称”这一个控件上，用的是 getBindingAdapterPosition()，
 * 这样即使列表刚插入过数据，点到的也一定是屏幕上那一条，不会串行。
 */
public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.Holder> {

    public static class Animal {
        final String name;
        final int photo;
        final long id;

        Animal(String name, int photo, long id) {
            this.name = name;
            this.photo = photo;
            this.id = id;
        }
    }

    public interface OnNameClick {
        void onNameClick(int position);
    }

    private final List<Animal> data;
    private final OnNameClick listener;

    AnimalAdapter(List<Animal> data, OnNameClick listener) {
        this.data = data;
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).id;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycler, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Animal item = data.get(position);
        holder.photo.setImageResource(item.photo);
        holder.name.setText(item.name);
        holder.badge.setText("#" + (position + 1));
        holder.name.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onNameClick(pos);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView photo;
        final TextView name;
        final TextView badge;

        Holder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.ivPhoto);
            name = itemView.findViewById(R.id.tvName);
            badge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
