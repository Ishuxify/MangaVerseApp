package com.example.mangaverseapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mangaverseapp.R;
import com.example.mangaverseapp.data.model.Manga;

import java.util.ArrayList;
import java.util.List;

public class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.MangaViewHolder> {

    private List<Manga> mangaList = new ArrayList<>();
    private final Context context;
    private boolean isLoading = false;
    private OnItemClickListener listener;

    public MangaAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMangaList(List<Manga> newList) {
        if (newList != null) {
            mangaList = new ArrayList<>(newList);
            notifyDataSetChanged();
        }
    }

    public void addMangaList(List<Manga> newList) {
        if (newList != null) {
            int start = mangaList.size();
            mangaList.addAll(newList);
            notifyItemRangeInserted(start, newList.size());
        }
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    @NonNull
    @Override
    public MangaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.manga_list_item, parent, false);
        return new MangaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MangaViewHolder holder, int position) {
        Manga manga = mangaList.get(position);
        holder.title.setText(manga.getTitle());
        holder.subtitle.setText(manga.getSubtitle());
        holder.description.setText(manga.getDescription());

        // Load image directly from thumb URL
        Glide.with(context)
                .load(manga.getImageUrl())
                .placeholder(R.drawable.google)
                .error(R.drawable.ios)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Error loading image: " + (e != null ? e.getMessage() : "Unknown error"));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(manga);
            }
        });

        // Pagination trigger with debouncing
        if (position == mangaList.size() - 1 && !isLoading) {
            isLoading = true;
            if (listener != null) {
                listener.onLoadMore();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mangaList.size();
    }

    static class MangaViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, subtitle, description;

        MangaViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.mangaImage);
            title = itemView.findViewById(R.id.mangaTitle);
            subtitle = itemView.findViewById(R.id.mangaSubtitle);
            description = itemView.findViewById(R.id.mangaDescription);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Manga manga);
        void onLoadMore();
    }
}