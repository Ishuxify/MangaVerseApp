package com.example.mangaverseapp.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mangaverseapp.data.api.ApiClient;
import com.example.mangaverseapp.data.api.MangaApiService;
import com.example.mangaverseapp.data.db.AppDatabase;
import com.example.mangaverseapp.data.model.Manga;
import com.example.mangaverseapp.data.model.MangaResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MangaRepositoryImpl implements MangaRepository {

    private final AppDatabase database;
    private final MangaApiService apiService;
    private final Executor executor;

    public MangaRepositoryImpl(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.apiService = ApiClient.getApiClient().create(MangaApiService.class);
        this.executor = Executors.newFixedThreadPool(2); // Shared executor
    }

    @Override
    public void fetchMangaFromApi(int page, int pageSize, String genres, boolean nsfw, String type, MangaDataCallback callback) {
        apiService.getMangaList(page, genres, nsfw, type).enqueue(new Callback<MangaResponse>() {
            @Override
            public void onResponse(Call<MangaResponse> call, Response<MangaResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    List<Manga> mangaList = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                    insertMangaList(mangaList); // Store in Room
                    callback.onDataLoaded(mangaList);
                } else {
                    callback.onError("Error fetching data: " + (response.message() != null ? response.message() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(Call<MangaResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public LiveData<List<Manga>> getAllManga() {
        return database.mangaDao().getAllMangas();
    }

    @Override
    public void insertMangaList(List<Manga> mangaList) {
        if (mangaList != null) {
            executor.execute(() -> {
                try {
                    database.mangaDao().insertAll(mangaList);
                } catch (Exception e) {
                    // Log error (could use a logger library in production)
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void clearAllManga() {
        executor.execute(() -> {
            try {
                database.mangaDao().deleteAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}