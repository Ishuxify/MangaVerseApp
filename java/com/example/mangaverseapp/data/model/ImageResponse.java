package com.example.mangaverseapp.data.model;

import com.google.gson.annotations.SerializedName;

public class ImageResponse {

    @SerializedName("imageUrl")
    private String imageUrl;  // URL of the image

    // Getter and Setter
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "ImageResponse{" +
                "imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
