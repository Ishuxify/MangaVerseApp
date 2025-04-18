package com.example.mangaverseapp.ui.signIn;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mangaverseapp.data.repository.UserRepositoryImpl;
import com.example.mangaverseapp.domain.usecase.LoginUseCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignInViewModel extends AndroidViewModel {
    private LoginUseCase loginUseCase;
    private MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public SignInViewModel(Application application) {
        super(application);
        // Initialize the UseCase with the repository
        loginUseCase = new LoginUseCase(new UserRepositoryImpl(application));
    }

    // Method to handle sign-in functionality
    public void signIn(String email, String password) {
        // Execute the login task in a background thread
        executorService.execute(() -> {
            boolean success = loginUseCase.execute(email, password);
            // Post result back to the main thread
            mainHandler.post(() -> loginResult.setValue(success));
        });
    }

    // Getter to observe the login result
    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up resources when ViewModel is cleared
        executorService.shutdown();
    }
}
