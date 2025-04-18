package com.example.mangaverseapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mangaverseapp.R;
import com.example.mangaverseapp.data.model.Manga;

public class MangaDetailFragment extends Fragment {

    private static final String ARG_MANGA = "manga";

    // Factory method to create a new instance of this fragment
    public static MangaDetailFragment newInstance(Manga manga) {
        MangaDetailFragment fragment = new MangaDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MANGA, manga);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manga_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find Views
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView subtitleTextView = view.findViewById(R.id.subtitleTextView);
        TextView descriptionTextView = view.findViewById(R.id.descriptionTextView);
        ImageView posterImageView = view.findViewById(R.id.image_view_poster);

        if (getArguments() != null) {
            // Retrieve Manga object from arguments
            Manga manga = getArguments().getParcelable(ARG_MANGA);

            if (manga != null) {
                // Set data to views
                titleTextView.setText(manga.getTitle());

                // Check for subtitle and description, set default if null or empty
                subtitleTextView.setText(!TextUtils.isEmpty(manga.getSubtitle()) ? manga.getSubtitle() : "No subtitle available");
                descriptionTextView.setText(!TextUtils.isEmpty(manga.getDescription()) ? manga.getDescription() : "No description available");

                // Load image dynamically using Glide
                String imageUrl = manga.getImageUrl();
                if (!TextUtils.isEmpty(imageUrl)) {
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .into(posterImageView);
                } else {
                    // Load fallback image if URL is empty or null
                    Glide.with(requireContext())
                            .load("https://upload.wikimedia.org/wikipedia/commons/6/65/No-Image-Placeholder.svg")
                            .into(posterImageView);
                }
            } else {
                Log.e("MangaDetailFragment", "Manga object is null");
            }
        } else {
            Log.e("MangaDetailFragment", "Arguments are null");
        }
    }
}
