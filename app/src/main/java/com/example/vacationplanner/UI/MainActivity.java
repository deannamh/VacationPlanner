package com.example.vacationplanner.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.vacationplanner.entities.User;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static int numAlert;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    // for sharedpreferences:
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SKJTravelPrefs";
    private static final String USER_ID_KEY = "userId";
    private static final String USER_EMAIL_KEY = "userEmail";
    private static final String USER_ROLE_KEY = "userRole";
    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize firebase auth and firestore:
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //initialize sharedpreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

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
                    Toast.makeText(MainActivity.this, "Please enter your email and password to log in.", Toast.LENGTH_LONG).show();
                    return;
                }

                // sign in with firebase
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, task -> {
                    if (task.isSuccessful()) {
                        //if sign in is successful, get the current user
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserAndEnter(user.getUid(), user.getEmail());
                        }
                    } else {
                        // if user login failed:
                        Toast.makeText(MainActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // register button onClick:
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter email and password to register", Toast.LENGTH_LONG).show();
                    return;
                }

                // Create new user account
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Registration successful, set role to "user"
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // add user to Firestore db with default "user" role
                                    addNewUserToFirestore(user.getUid(), user.getEmail(), "user");
                                }
                            } else {
                                // Registration failed
                                Toast.makeText(MainActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
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
        // Check if user is currently signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // check if user or admin to go to correct screen
            checkUserAndEnter(currentUser.getUid(), currentUser.getEmail());
        }
    }

    // Set up new default user
    private void addNewUserToFirestore(String userId, String email, String role) {
        User user = new User(userId, email);
        // Add user to firestore db:
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created");

                    db.collection("users").document(userId)
                            .update("role", role)
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d(TAG, "User role set to: " + role);

                                //save user info to sharedpreferences using putString() and apply();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(USER_ID_KEY, userId);
                                editor.putString(USER_EMAIL_KEY, email);
                                editor.putString(USER_ROLE_KEY, role);
                                editor.apply();

                                //send admin to admin homepage, send user to user homepage
                                if (role.equals("admin")) {
                                    openAdminScreen();
                                } else {
                                    openUserScreen();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error setting user role", e);
                                Toast.makeText(MainActivity.this, "Could not set user role", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document in Firestore db", e);
                    Toast.makeText(MainActivity.this, "Could not create user", Toast.LENGTH_LONG).show();
                });
    }


    // check user role in firestore db and go to correct screen
    private void checkUserAndEnter(String userId, String email) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role == null) {
                            role = "user"; // Default role is "user" if it is not set
                        }

                        // save user info to sharedpreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(USER_ID_KEY, userId);
                        editor.putString(USER_EMAIL_KEY, email);
                        editor.putString(USER_ROLE_KEY, role);
                        editor.apply();

                        // redirect to correct homepage
                        if (role.equals("admin")) {
                            openAdminScreen();
                        } else {
                            openUserScreen();
                        }

                    } else {
                        // user document doesn't exist so make a new one with default role "user"
                        addNewUserToFirestore(userId, email, "user");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user role", e);
                    Toast.makeText(MainActivity.this, "Error retrieving user information", Toast.LENGTH_LONG).show();
                });
    }

    // open admin homepage AdminSearchActivity
    private void openAdminScreen() {
        Intent intent = new Intent(MainActivity.this, AdminSearchActivity.class);
        startActivity(intent);
        finish();
    }

    // open user homepage UserVacationList
    private void openUserScreen() {
        Intent intent = new Intent(MainActivity.this, UserVacationList.class);
        startActivity(intent);
        finish();
    }

}