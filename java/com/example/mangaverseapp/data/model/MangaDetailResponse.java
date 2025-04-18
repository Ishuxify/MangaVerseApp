package com.example.mangaverseapp.data.model;

import com.google.gson.annotations.SerializedName;

public class MangaDetailResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("description")
    private String description;

    @SerializedName("image")
    private String imageUrl;  // URL of the manga image

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "MangaDetailResponse{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
