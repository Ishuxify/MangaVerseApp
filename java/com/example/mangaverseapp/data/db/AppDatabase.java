package com.example.mangaverseapp.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mangaverseapp.data.model.User;
import com.example.mangaverseapp.data.model.Manga;

@Database(entities = {User.class, Manga.class}, version = 3, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDAO userDao();
    public abstract MangaDAO mangaDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mangaverse_db"
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigrationOnDowngrade() // For dev only, remove in production
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE manga_table ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Migration from version 2 to 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE manga_table ADD COLUMN subtitle TEXT NOT NULL DEFAULT ''");
        }
    };
}