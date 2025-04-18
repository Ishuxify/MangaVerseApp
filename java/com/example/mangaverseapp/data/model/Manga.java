package com.example.mangaverseapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Entity(tableName = "manga_table")
public class Manga implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int localId;

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("thumb") // Updated to match API
    private String image;

    @SerializedName("summary") // Updated to match API
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("sub_title") // Updated to match API
    private String subtitle;

    // Default constructor required by Room
    public Manga() {
    }

    // Constructor with updated fields
    public Manga(String id, String title, String image, String description, String status, String subtitle) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.description = description;
        this.status = status;
        this.subtitle = subtitle;
    }

    // Getters and Setters
    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageUrl() {
        return image != null && !image.isEmpty()
                ? image
                : "https://via.placeholder.com/150"; // Generic placeholder URL
    }

    public String getDescription() {
        return description != null ? description : "No description available";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubtitle() {
        return subtitle != null && !subtitle.isEmpty() ? subtitle : "No subtitle available";
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public String toString() {
        return "Manga{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", subtitle='" + subtitle + '\'' +
                '}';
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeInt(localId);
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(image);
        parcel.writeString(description);
        parcel.writeString(status);
        parcel.writeString(subtitle);
    }

    public static final Creator<Manga> CREATOR = new Creator<Manga>() {
        @Override
        public Manga createFromParcel(Parcel in) {
            return new Manga(in);
        }

        @Override
        public Manga[] newArray(int size) {
            return new Manga[size];
        }
    };

    protected Manga(Parcel in) {
        localId = in.readInt();
        id = in.readString();
        title = in.readString();
        image = in.readString();
        description = in.readString();
        status = in.readString();
        subtitle = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Manga manga = (Manga) o;
        return id.equals(manga.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}