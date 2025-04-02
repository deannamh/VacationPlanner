package com.example.vacationplanner.UI;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.R;
import com.example.vacationplanner.database.VacationRepository;
import com.google.firebase.auth.FirebaseAuth;


public class UserVacationList extends AppCompatActivity {
    private VacationRepository vacationRepository;
    private UserVacationAdapter userVacationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_vacation_list);

        // toolbar set up as the action bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize firebase repository
        vacationRepository = new VacationRepository();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // to display the list of vacations on the RecyclerView:
        RecyclerView recyclerView = findViewById(R.id.userVacationRecyclerView);

        // Call UserVacationAdapter and set it on the RecyclerView
        userVacationAdapter = new UserVacationAdapter(this);
        recyclerView.setAdapter(userVacationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // put list of vacations on RecyclerView
        loadVacations();
    }

    // method to load vacations from firebase on to RecyclerView:
    private void loadVacations() {
        vacationRepository.getAllVacations().observe(this, vacations -> {
            userVacationAdapter.setVacations(vacations);
        });
    }

    // Creates menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_vacation_list, menu);
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume(); // Firebase LiveData will automatically update when changes are made in the recyclerview in loadVacations method
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.logoutUserVacationList) {
            confirmLogout();
            return true;
        }

        if (item.getItemId() == android.R.id.home) { // for a back button to home page
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // sign out from Firebase
                    FirebaseAuth.getInstance().signOut();

                    // go back to login screen
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
