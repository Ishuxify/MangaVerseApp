package com.example.mangaverseapp.data.repository;

import android.content.Context;
import com.example.mangaverseapp.data.db.AppDatabase;
import com.example.mangaverseapp.data.db.UserDAO;
import com.example.mangaverseapp.data.model.User;

public class UserRepositoryImpl implements UserRepository {
    private UserDAO userDao;

    public UserRepositoryImpl(Context context) {
        userDao = AppDatabase.getInstance(context).userDao();
    }

    @Override
    public void saveUser(String email, String password) {
        User user = new User(email, password);
        userDao.insert(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    @Override
    public boolean isUserLoggedIn() {
        return userDao.getUserCount() > 0;
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return userDao.isEmailRegistered(email);
    }
}
