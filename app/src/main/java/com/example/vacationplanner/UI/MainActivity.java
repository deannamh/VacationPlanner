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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
                                // Check user role and navigate accordingly
                                checkUserAndEnter();
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
                                // Registration success - set user role to default "user"
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Set up user in Firestore with default "user" role
                                    addNewUser(user);
                                }
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

    // Set up new default user
    private void addNewUser(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("role", "user"); // default role for new users
        userData.put("userId", user.getUid());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Registration complete!", Toast.LENGTH_SHORT).show();
                    // send users to user screen
                    openUserScreen();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                });
    }

    // Check user role in Firestore db and go to correct screen
    private void checkUserAndEnter() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("admin".equals(role)) {
                            openAdminScreen();
                        } else {
                            openUserScreen();
                        }
                    } else {
                        // If user document doesn't exist yet, create it with default role
                        addNewUser(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error checking user role", Toast.LENGTH_SHORT).show();
                    // Default to user interface if there's an error
                    openUserScreen();
                });
    }

    // Navigate to the admin interface (VacationList)
    private void openAdminScreen() {
        Intent intent = new Intent(MainActivity.this, VacationList.class);
        startActivity(intent);
        finish();
    }

    // Navigate to the user interface (UserVacationList)
    private void openUserScreen() {
        Intent intent = new Intent(MainActivity.this, UserVacationList.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // check if user or admin to go to correct screen
            checkUserAndEnter();
        }
    }
}