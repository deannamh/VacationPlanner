package com.example.vacationplanner.UI;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.R;
import com.example.vacationplanner.database.ExcursionRepository;
import com.example.vacationplanner.database.VacationRepository;
import com.example.vacationplanner.entities.Excursion;
import com.example.vacationplanner.entities.Vacation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class VacationList extends AppCompatActivity {
    private VacationRepository vacationRepository;
    private ExcursionRepository excursionRepository;
    private VacationAdapter vacationAdapter;

    private static final String USER_ROLE_KEY = "userRole";
    private static final String SELECTED_USER_ID = "selectedUserId";
    private static final String SELECTED_USER_EMAIL = "selectedUserEmail";
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_list);

        // toolbar set up as the action bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);;

        // initialize firebase repositories
        vacationRepository = new VacationRepository();
        excursionRepository = new ExcursionRepository();

        // get user role from SharedPreferences so we can make sure the admin app pages are shown to the admin and the user pages are shown to the user
        SharedPreferences sharedPreferences = getSharedPreferences("SKJTravelPrefs", MODE_PRIVATE);
        userRole = sharedPreferences.getString(USER_ROLE_KEY, "user");

        // check if admin selected a user
        if (userRole.equals("admin")) {
            String selectedUserId = sharedPreferences.getString(SELECTED_USER_ID, "");
            String selectedUserEmail = sharedPreferences.getString(SELECTED_USER_EMAIL, "");

            if (!selectedUserId.isEmpty()) {
                // if the admin selected a user id, show that user's email in the toolbar title
                if (toolbar != null) {
                    toolbar.setTitle("Vacations for: " + selectedUserEmail);
                }
            } else {
                // if the admin did not select a user, go back to the admin home page
                Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AdminSearchActivity.class);
                startActivity(intent);
                finish();
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // floating action button on VacationList page to add vacations(goes to vacation details page)
        FloatingActionButton fab = findViewById(R.id.fabAddVacation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VacationList.this, VacationDetails.class);
                startActivity(intent);
            }
        });

        // to display the list of vacations on the RecyclerView:
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Call VacationAdapter and set it on the RecyclerView
        vacationAdapter = new VacationAdapter(this);
        recyclerView.setAdapter(vacationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // put list of vacations on RecyclerView
        loadVacations();
    }

    // method to load vacations from firebase on to RecyclerView:
    private void loadVacations() {
        // get the current user id based on role
        String currUserId = vacationRepository.getCurrentUserId();

        if (userRole != null && userRole.equals("admin")) {
            SharedPreferences sharedPreferences = getSharedPreferences("SKTravelPrefs", MODE_PRIVATE);
            String selectedUserId = sharedPreferences.getString(SELECTED_USER_ID, "");
            if (!selectedUserId.isEmpty()) {
                currUserId = selectedUserId;
            }
        }

        // use current user ID to get vacations
        vacationRepository.getAllVacationsForUser(currUserId).observe(this, vacations -> {
            vacationAdapter.setVacations(vacations);
        });
    }

    // Creates menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vacation_list, menu);
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume(); // Firebase LiveData will automatically update when changes are made in the recyclerview in loadVacations method
        //gets vacations from room database again and updates the recyclerview
//        super.onResume();
//        List<Vacation> allVacations = repository.getmAllVacations();
//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
//        final VacationAdapter vacationAdapter = new VacationAdapter(this);
//        recyclerView.setAdapter(vacationAdapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        vacationAdapter.setVacations(allVacations);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sample) {
            // store the actual firestore vacation id
            final String[] actualVacationId = {null};

            // Create sample vacations
            Vacation vacation1 = new Vacation("temp-id", "Trinidad and Tobago", "Hyatt", "02/01/25", "02/15/25");
            vacation1.setUserId(vacationRepository.getCurrentUserId());
            Vacation vacation2 = new Vacation("temp-id2", "Florida", "Marriott", "04/03/25", "04/10/25");
            vacation2.setUserId(vacationRepository.getCurrentUserId());

            //firestore uses asynchronos db operations
            // Add vacation 1 to firestore and get its vacation id
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("vacations")
                    .add(vacation1)
                    .addOnSuccessListener(documentReference -> {
                        //save firestore document iid
                        String id = documentReference.getId();
                        actualVacationId[0] = id;

                        // update the vacationID field in the firestore document so there isn't a mismatch since creating a vacation generates a different id than firestore's document id
                        documentReference.update("vacationID", id);
                        Log.d("SampleData", "Vacation 1 added with actual Firestore ID: " + id);

                        // add vacation 2
                        db.collection("vacations")
                                .add(vacation2)
                                .addOnSuccessListener(docRef2 -> {
                                    String id2 = docRef2.getId();
                                    docRef2.update("vacationID", id2);
                                    Log.d("SampleData", "Vacation 2 added with actual Firestore ID: " + id2);
                                });

                        // now add excursions after confirming vacationw were added by code above
                        new Handler().postDelayed(() -> {
                            // create excursions with the firestore document id
                            Excursion excursion1 = new Excursion(UUID.randomUUID().toString(),
                                    "Snorkeling", "02/05/25", actualVacationId[0], false);
                            Log.d("SampleData", "Adding excursion 'Snorkeling' for  vacation ID: " + actualVacationId[0]);
                            excursionRepository.insert(excursion1);

                            Excursion excursion2 = new Excursion(UUID.randomUUID().toString(),
                                    "Boat Tour", "02/07/25", actualVacationId[0], false);
                            Log.d("SampleData", "Adding excursion 'Boat Tour' for vacation ID: " + actualVacationId[0]);
                            excursionRepository.insert(excursion2);

                            Toast.makeText(VacationList.this, "Sample data added", Toast.LENGTH_SHORT).show();
                        }, 1000);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SampleData", "Error adding vacation", e);
                        Toast.makeText(VacationList.this, "Error adding sample data", Toast.LENGTH_SHORT).show();
                    });

            return true;
        }

        if (item.getItemId() == R.id.logoutVacationList) {
            confirmLogout();
            return true;
        }

        if (item.getItemId() == android.R.id.home) { // for a back button to home page
            if (userRole != null && userRole.equals("admin")) {
                // back button takes admin to admin home page AdminSearchActivity
                Intent intent = new Intent(VacationList.this, AdminSearchActivity.class);
                startActivity(intent);
            } else {
                this.finish();
            }
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

                    // go bcak to login screen
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}