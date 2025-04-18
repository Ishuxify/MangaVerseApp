package com.example.mangaverseapp.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.mangaverseapp.data.model.User;

@Dao
public interface UserDAO {
    // Insert a new user into the database
    @Insert
    void insert(User user);

    // Query to get a User by email (used for login)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // Query to check if the user count is greater than 0
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    // Additional query: To check if a user exists with a specific email
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean isEmailRegistered(String email);
}
