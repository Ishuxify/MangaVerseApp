package com.example.mangaverseapp.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChapterResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("mangaId")
    private String mangaId;

    @SerializedName("chapters")
    private List<Chapter> chapters;  // List of chapter objects

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMangaId() {
        return mangaId;
    }

    public void setMangaId(String mangaId) {
        this.mangaId = mangaId;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    @Override
    public String toString() {
        return "ChapterResponse{" +
                "id='" + id + '\'' +
                ", mangaId='" + mangaId + '\'' +
                ", chapters=" + (chapters != null ? chapters.size() : 0) + " chapters" +
                '}';
    }

    // Inner Chapter class to hold individual chapter details
    public static class Chapter {

        @SerializedName("chapterTitle")
        private String chapterTitle;

        @SerializedName("chapterId")
        private String chapterId;

        @SerializedName("chapterUrl")
        private String chapterUrl;

        // Getters and Setters
        public String getChapterTitle() {
            return chapterTitle;
        }

        public void setChapterTitle(String chapterTitle) {
            this.chapterTitle = chapterTitle;
        }

        public String getChapterId() {
            return chapterId;
        }

        public void setChapterId(String chapterId) {
            this.chapterId = chapterId;
        }

        public String getChapterUrl() {
            return chapterUrl;
        }

        public void setChapterUrl(String chapterUrl) {
            this.chapterUrl = chapterUrl;
        }

        @Override
        public String toString() {
            return "Chapter{" +
                    "chapterTitle='" + chapterTitle + '\'' +
                    ", chapterId='" + chapterId + '\'' +
                    ", chapterUrl='" + chapterUrl + '\'' +
                    '}';
        }
    }
}
