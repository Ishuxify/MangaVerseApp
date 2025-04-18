package com.example.mangaverseapp.data.repository;

import com.example.mangaverseapp.data.model.Manga;
import java.util.List;

public interface MangaDataCallback {
    void onDataLoaded(List<Manga> mangaList);
    void onSuccess(List<Manga> mangaList);
    void onError(String error);
}
