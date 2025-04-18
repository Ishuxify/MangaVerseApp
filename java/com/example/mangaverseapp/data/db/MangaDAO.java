package com.example.mangaverseapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mangaverseapp.data.model.Manga;

import java.util.List;

@Dao
public interface MangaDAO {

    // Insert a list of mangas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Manga> mangaList);

    // Insert a single manga
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Manga manga);

    // Get all mangas
    @Query("SELECT * FROM manga_table")
    LiveData<List<Manga>> getAllMangas();

    // Get a manga by API id
    @Query("SELECT * FROM manga_table WHERE id = :id")
    LiveData<Manga> getMangaById(String id);

    // Get all mangas synchronously (for repository use)
    @Query("SELECT * FROM manga_table")
    List<Manga> getAllMangasSync();

    // Delete all mangas
    @Query("DELETE FROM manga_table")
    void deleteAll();

    // Optional: Filter by status
    @Query("SELECT * FROM manga_table WHERE status = :status")
    LiveData<List<Manga>> getMangasByStatus(String status);
}