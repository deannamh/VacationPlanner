package com.example.vacationplanner.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vacationplanner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    public static int numAlert;

    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize firebase auth:
        mAuth = FirebaseAuth.getInstance();

        // UI elements:
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // login button onClick:
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your email and password to log in.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // sign in with firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // if user logs in successfully, go to to VacationList
                                Intent intent = new Intent(MainActivity.this, VacationList.class);
                                startActivity(intent);
                            } else {
                                // if user login failed:
                                Toast.makeText(MainActivity.this, "Login failed: " +
                                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        //register button onClick:
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter email and password to register.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user account
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Registration success
                                Toast.makeText(MainActivity.this, "Registration complete!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, VacationList.class);
                                startActivity(intent);
                            } else {
                                // Registration failed
                                Toast.makeText(MainActivity.this, "Registration failed: " +
                                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // if user is already signed in, go to VacationList
            Intent intent = new Intent(MainActivity.this, VacationList.class);
            startActivity(intent);
            finish();
        }
    }
}