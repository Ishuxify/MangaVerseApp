package com.example.mangaverseapp.data.repository;

import com.example.mangaverseapp.data.model.User;

public interface UserRepository {
    void saveUser(String email, String password);
    User getUserByEmail(String email);
    boolean isUserLoggedIn();

    boolean isEmailRegistered(String email);
}