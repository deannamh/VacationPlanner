package com.example.vacationplanner.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vacationplanner.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminSearchActivity extends AppCompatActivity {
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView userListRecyclerView;
    private TextView noUsersTextView;

    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private List<User> userList;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SKTravelPrefs";
    private static final String SELECTED_USER_ID = "selectedUserId";
    private static final String SELECTED_USER_EMAIL = "selectedUserEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_searchpage);
        db = FirebaseFirestore.getInstance();

        // set up SharedPreferences (android api that can be used for retrieving key-value pairs from small collection of data)
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        userListRecyclerView = findViewById(R.id.userListRecyclerView);
        noUsersTextView = findViewById(R.id.noUsersTextView);

        // for RecyclerView
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this);
        userAdapter.setUsers(userList);
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListRecyclerView.setAdapter(userAdapter);

        // search button onclicklistener using searchUsers() method
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUsers();
            }
        });

        // can shorten above to lambda expression:
        // searchButton.setOnClickListener(v -> searchUsers());
    }

    private void searchUsers() {
        String email = searchEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address to search", Toast.LENGTH_SHORT).show();
            return;
        }

        userList.clear();
        userAdapter.notifyDataSetChanged();
        noUsersTextView.setVisibility(View.GONE);

        // Search for users in Firestore
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            String userEmail = document.getString("email");
                            User user = new User(userId, userEmail);
                            userList.add(user);
                        }

                        userAdapter.notifyDataSetChanged();

                        if (userList.isEmpty()) {
                            noUsersTextView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(AdminSearchActivity.this, "Error searching for users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.admin_logout) {
            // sign out from Firebase Auth
            FirebaseAuth.getInstance().signOut();

            // remove selected user from SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(SELECTED_USER_ID);
            editor.remove(SELECTED_USER_EMAIL);
            editor.apply();

            // go back to login screen
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}