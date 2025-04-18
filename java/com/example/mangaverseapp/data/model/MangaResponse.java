package com.example.mangaverseapp.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MangaResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private List<Manga> data;

    // Default constructor for GSON
    public MangaResponse() {
        this.code = 0;
        this.data = new ArrayList<>();
    }

    // Constructor for manual creation (optional)
    public MangaResponse(int code, List<Manga> data) {
        this.code = code;
        this.data = data != null ? new ArrayList<>(data) : new ArrayList<>();
    }

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Manga> getData() {
        return data != null ? Collections.unmodifiableList(data) : new ArrayList<>();
    }

    public void setData(List<Manga> data) {
        this.data = data != null ? new ArrayList<>(data) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "MangaResponse{" +
                "code=" + code +
                ", data=" + (data != null ? data.size() : 0) + " manga items" +
                '}';
    }
}