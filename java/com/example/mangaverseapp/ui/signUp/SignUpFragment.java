package com.example.mangaverseapp.ui.signUp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.mangaverseapp.R;
import com.example.mangaverseapp.data.db.AppDatabase;
import com.example.mangaverseapp.data.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpFragment extends Fragment {

    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton signUpButton;
    TextView loginText;

    private AppDatabase appDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Initialize UI elements
        fullNameEditText = view.findViewById(R.id.et_signUp_fullname);
        emailEditText = view.findViewById(R.id.et_signUp_email);
        passwordEditText = view.findViewById(R.id.et_signUp_password);
        confirmPasswordEditText = view.findViewById(R.id.et_signUp_confirmPassword);
        signUpButton = view.findViewById(R.id.btn_signUp);
        TextInputLayout emailInputLayout = view.findViewById(R.id.emailInputLayout_signup);
        loginText = view.findViewById(R.id.login);

        loginText.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signInFragment);
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String email = editable.toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setError("Invalid email format");
                } else {
                    emailInputLayout.setError(null); // No error
                }
            }
        });

        // Initialize Room Database
        appDatabase = AppDatabase.getInstance(getContext());

        signUpButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email already exists
            new Thread(() -> {
                User existingUser = appDatabase.userDao().getUserByEmail(email);
                if (existingUser != null) {
                    // Email already exists
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Email is already registered", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Create User object and insert into Room Database
                    User newUser = new User(email, password);
                    appDatabase.userDao().insert(newUser);

                    // Once user is saved, navigate to the Sign In Fragment
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Sign up successful. Please Log in.", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signInFragment);
                    });
                }
            }).start();
        });

        return view;
    }
}
