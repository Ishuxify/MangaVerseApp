package com.example.mangaverseapp.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String email;
    private String password;

    // Constructor to initialize User with email and password
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter and Setter for ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Custom method to check password validity (optional)
    public boolean isValidPassword(String inputPassword) {
        return password != null && password.equals(inputPassword);
    }
}
