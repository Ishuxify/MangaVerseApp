package com.example.mangaverseapp.domain.usecase;

import com.example.mangaverseapp.data.model.User;
import com.example.mangaverseapp.data.repository.UserRepository;

public class LoginUseCase {
    private UserRepository userRepository;

    public LoginUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean execute(String email, String password) {
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            userRepository.saveUser(email, password);
            return true;
        } else {
            return user.getPassword().equals(password);
        }
    }

    public boolean isUserLoggedIn() {
        return userRepository.isUserLoggedIn();
    }
}