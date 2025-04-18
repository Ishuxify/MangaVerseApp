package com.example.mangaverseapp;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.mangaverseapp.data.repository.UserRepositoryImpl;
import com.example.mangaverseapp.domain.usecase.LoginUseCase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        // Set up the status bar for newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // Setup Toolbar (optional)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();  // Hide the ActionBar if not needed
        }

        // Initialize LoginUseCase
        LoginUseCase loginUseCase = new LoginUseCase(new UserRepositoryImpl(this));

        // Setup NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Setup ActionBar with NavController
            NavigationUI.setupActionBarWithNavController(this, navController);

            // Setup Bottom Navigation with NavController
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Add destination changed listener after NavController is initialized
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                BottomNavigationView bottomNavView = findViewById(R.id.bottom_navigation);

                // Fragments where bottom nav should be hidden
                if (destination.getId() == R.id.signInFragment || destination.getId() == R.id.signUpFragment) {
                    bottomNavView.setVisibility(View.GONE);
                } else {
                    bottomNavView.setVisibility(View.VISIBLE);
                }
            });

            // Check login status and navigate accordingly
            executorService.execute(() -> {
                boolean isLoggedIn = loginUseCase.isUserLoggedIn();

                mainHandler.post(() -> {
                    int currentDestId = navController.getCurrentDestination() != null
                            ? navController.getCurrentDestination().getId()
                            : -1;

                    if (isLoggedIn && currentDestId != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment);
                    } else if (!isLoggedIn && currentDestId != R.id.signInFragment) {
                        navController.navigate(R.id.signInFragment);
                    }
                });
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (navController != null && navController.getCurrentDestination() != null) {
            // Handle the back press for the navigation stack
            if (!navController.popBackStack()) {
                super.onBackPressed();  // Finish the activity if no fragments left to pop
            }
        } else {
            super.onBackPressed();  // Default back behavior if navController is null
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
