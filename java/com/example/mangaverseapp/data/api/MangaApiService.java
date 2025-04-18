package com.example.mangaverseapp.data.api;

import com.example.mangaverseapp.data.model.MangaResponse;
import com.example.mangaverseapp.data.model.MangaDetailResponse;
import com.example.mangaverseapp.data.model.ChapterResponse;
import com.example.mangaverseapp.data.model.ImageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MangaApiService {

    @GET("manga/fetch")
    Call<MangaResponse> getMangaList(
            @Query("page") int page,
            @Query("genres") String genres,
            @Query("nsfw") boolean nsfw,
            @Query("type") String type
    );

    @GET("manga/{id}")
    Call<MangaDetailResponse> getMangaDetail(@Path("id") String mangaId);

    @GET("manga/chapter")
    Call<ChapterResponse> getMangaChapter(@Query("id") String mangaId);

    @GET("manga/image")
    Call<ImageResponse> getMangaImage(@Query("id") String imageId);
}