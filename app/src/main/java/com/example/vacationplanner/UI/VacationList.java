package com.example.vacationplanner.UI;

import android.app.AlertDialog;
import android.content.Intent;
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

        // initialize firebase repositories
        vacationRepository = new VacationRepository();
        excursionRepository = new ExcursionRepository();


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
        vacationRepository.getAllVacations().observe(this, vacations -> {
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
        //gets vacations from database again and updates the recyclerview
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
            // Reference to store the actual Firestore vacation ID
            final String[] actualVacationId = {null};

            // Create sample vacation 1
            Vacation vacation1 = new Vacation("temp-id", "Trinidad and Tobago", "Hyatt", "02/01/25", "02/15/25");
            vacation1.setUserId(vacationRepository.getCurrentUserId());

            // Create sample vacation 2
            Vacation vacation2 = new Vacation("temp-id2", "Florida", "Marriott", "04/03/25", "04/10/25");
            vacation2.setUserId(vacationRepository.getCurrentUserId());

            // Add vacation 1 to Firestore and get it's vacationID
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("vacations")
                    .add(vacation1)
                    .addOnSuccessListener(documentReference -> {
                        // Store the Firestore document ID
                        String id = documentReference.getId();
                        actualVacationId[0] = id;

                        // update the vacationID field in the document
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

                        // Add excursions after confirming vacation 1 was saved
                        new Handler().postDelayed(() -> {
                            // create excursions with the Firestore document ID
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
            this.finish();
            // Comment out the previous line and use the next two lines instead for back button to go to VacationDetails instead
            // Intent intent = new Intent(VacationList.this, VacationDetails.class);
            // startActivity(intent);
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