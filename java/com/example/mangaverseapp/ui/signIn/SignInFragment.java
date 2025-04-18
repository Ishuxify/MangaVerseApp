package com.example.mangaverseapp.ui.signIn;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mangaverseapp.R;
import com.google.android.material.textfield.TextInputLayout;

public class SignInFragment extends Fragment {
    private SignInViewModel viewModel;

    private static final String PREF_NAME = "mangaverse_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(SignInViewModel.class);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            view.post(() -> navigateToHome(view));
        }

        // Get references to UI elements
        EditText emailEditText = view.findViewById(R.id.editTextEmail);
        TextInputLayout emailLayout = view.findViewById(R.id.emailInputLayout);
        EditText passwordEditText = view.findViewById(R.id.editTextPassword);
        Button signInButton = view.findViewById(R.id.buttonSignIn);
        TextView signUpTextView = view.findViewById(R.id.SignUp);

        // Email validation logic
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String email = editable.toString();
                if (!isValidEmail(email)) {
                    emailLayout.setError("Invalid email format");
                } else {
                    emailLayout.setError(null); // No error
                }
            }
        });

        // Navigate to Sign Up screen when "Sign Up" text is clicked
        signUpTextView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_signInFragment_to_signUpFragment);
        });

        // Sign In Button click listener
        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter Email and Password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check password length
            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Perform sign-in using ViewModel
            viewModel.signIn(email, password);

            // Observe login result
            viewModel.getLoginResult().observe(getViewLifecycleOwner(), success -> {
                if (success) {
                    // Login Successful, save user login status and navigate to Home screen
                    saveUserLoginStatus(view.getContext(), true);
                    view.post(() -> {
                        navigateToHome(view);
                        Toast.makeText(view.getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    });
                } else{
                    // Invalid credentials
                    Toast.makeText(view.getContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();

                }
            });
        });

        return view;
    }

    // Method to check if user is logged in
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Method to save user login status
    private static void saveUserLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // Method to navigate to home screen
    private void navigateToHome(View view) {
        NavController navController = Navigation.findNavController(view);
        if (navController.getCurrentDestination().getId() != R.id.homeFragment) {
            navController.navigate(R.id.action_signInFragment_to_homeFragment);
        }
    }


    // Email validation method
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
