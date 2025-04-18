package com.example.mangaverseapp.data.repository;

import androidx.lifecycle.LiveData;

import com.example.mangaverseapp.data.model.Manga;

import java.util.List;

public interface MangaRepository {
    void insertMangaList(List<Manga> mangaList);
    LiveData<List<Manga>> getAllManga();
    void clearAllManga();
    void fetchMangaFromApi(int page, int pageSize, String genres, boolean nsfw, String type, MangaDataCallback callback);

    interface MangaDataCallback {
        void onDataLoaded(List<Manga> mangaList);
        void onError(String error);
    }
}