package com.example.vacationplanner.UI;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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

import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserVacationDetails extends AppCompatActivity {

    String vacationID;
    String title;
    String hotelName;
    String startDate;
    String endDate;

    TextView editTitle;
    TextView editHotelName;
    TextView editStartDate;
    TextView editEndDate;

    final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);


    // for Firebase:
    VacationRepository vacationRepository;
    ExcursionRepository excursionRepository;

    Vacation currentVacation;
    List<Excursion> associatedExcursions = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_vacation_details);

        // toolbar set up as the action bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize firebase repositories:
        vacationRepository = new VacationRepository();
        excursionRepository = new ExcursionRepository();

        editTitle = findViewById(R.id.userVacationTitleText);
        editHotelName = findViewById(R.id.userVacationHotelText);
        editStartDate = findViewById(R.id.userVacationStartDateText);
        editEndDate = findViewById(R.id.userVacationEndDateText);


        //get values from RecyclerView that were sent over in Intent in adapter
        vacationID = getIntent().getStringExtra("id");
        Log.d("VacationDetails", "Received vacation ID: " + vacationID);

        title = getIntent().getStringExtra("title");
        hotelName = getIntent().getStringExtra("hotel");
        startDate = getIntent().getStringExtra("startdate");
        endDate = getIntent().getStringExtra("enddate");

        // set textview text
        editTitle.setText(title);
        editHotelName.setText(hotelName);
        editStartDate.setText(startDate);
        editEndDate.setText(endDate);

        // populate userexcursionrecyclerview on user vacation details activity associated with a vacationID
        RecyclerView recyclerView = findViewById(R.id.userExcursionRecyclerView);
        final ExcursionAdapter excursionAdapter = new ExcursionAdapter(this, true);
        recyclerView.setAdapter(excursionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // we want the excursionrecyclerview to show the excursion associated with a specific vacation id:
        if (vacationID != null && !vacationID.isEmpty()) {
            excursionRepository.getAssociatedExcursions(vacationID).observe(this, excursions -> {
                associatedExcursions = excursions;
                excursionAdapter.setExcursions(excursions);
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() { //gets excursions associated with vacation id and updates the recyclerview
        super.onResume();

        if (vacationID != null && !vacationID.isEmpty()) {
            RecyclerView recyclerView = findViewById(R.id.userExcursionRecyclerView);
            final ExcursionAdapter excursionAdapter = new ExcursionAdapter(this, true);
            recyclerView.setAdapter(excursionAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            excursionRepository.getAssociatedExcursions(vacationID).observe(this, excursions -> {
                associatedExcursions = excursions;
                Log.d("VacationDetails", "Loaded " + excursions.size() + " excursions for vacation ID: " + vacationID);

                // Debug the contents
                for (Excursion e : excursions) {
                    Log.d("VacationDetails", "Excursion: " + e.getExcursionTitle() + ", Date: " + e.getExcursionDate());
                }

                excursionAdapter.setExcursions(excursions);
            });
        }
    }

    // inflate menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_vacation_details, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.userVacationAlert){
            try {
                Date startDate = sdf.parse(this.startDate);
                Date endDate = sdf.parse(this.endDate);


                // we need to compare the date portion of getTime() to make sure the alerts go off on the right date, regardless of the time
                // so set the time portion of the date to 00:00:00:00 (midnight)
                Calendar myCalendar = Calendar.getInstance();
                myCalendar.set(Calendar.HOUR_OF_DAY, 0);
                myCalendar.set(Calendar.MINUTE, 0);
                myCalendar.set(Calendar.SECOND, 0);
                myCalendar.set(Calendar.MILLISECOND, 0);
                Date currentDate = myCalendar.getTime();

                //we want to make sure the startDate on the screen is not before the current date (needs to be today or later) to trigger the notification
                if (startDate != null && !startDate.before(currentDate)) {
                    setNotification(startDate.getTime(), "Vacation: " + title + " is starting today!");
                }
                // we want the endDate on the screen to be today or later to trigger the notification
                if (endDate != null && !endDate.before(currentDate)) {
                    setNotification(endDate.getTime(), "Vacation: " + title + " is ending today.");
                }
                Toast.makeText(UserVacationDetails.this, "Vacation alert set.", Toast.LENGTH_LONG).show();

            } catch (ParseException e) {
                Log.e("UserVacationDetails", "Error parsing dates", e);
                Toast.makeText(this, "Error setting alert", Toast.LENGTH_SHORT).show();
//                throw new RuntimeException(e); // removed this line because it'll crash the app if there's an error (runtime error)
            }
            return true;
        }

        if (item.getItemId() == R.id.userExcursionAlert) {
            if (associatedExcursions.isEmpty()) {
                Toast.makeText(this, "There are no excursions to set up alerts", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Create a list of excursion names
            String[] excursionNames = new String[associatedExcursions.size()];
            for (int i = 0; i < associatedExcursions.size(); i++) {
                excursionNames[i] = associatedExcursions.get(i).getExcursionTitle();
            }

            // Show dialog to select which excursion
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Excursion")
                    .setItems(excursionNames, (dialog, which) -> {
                        Excursion selectedExcursion = associatedExcursions.get(which);
                        try {
                            Date excursionDate = sdf.parse(selectedExcursion.getExcursionDate());
                            if (excursionDate != null) {
                                setNotification(excursionDate.getTime(),
                                        "Excursion: " + selectedExcursion.getExcursionTitle() + " is starting today!");
                                Toast.makeText(this, "Excursion alert set", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e) {
                            Log.e("UserVacationDetails", "Error parsing excursion date", e);
                            Toast.makeText(this, "Error setting alert", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
            return true;
        }


        if (item.getItemId() == R.id.userShareTrip) {
            Intent sentIntent = new Intent();
            sentIntent.setAction(Intent.ACTION_SEND);

            // Build vacation details string
            StringBuilder vacationDetails = new StringBuilder();
            vacationDetails.append("Vacation title: " + editTitle.getText().toString() + "\n");
            vacationDetails.append("Hotel: " + editHotelName.getText().toString() + "\n");
            vacationDetails.append("Start date: " + editStartDate.getText().toString() + "\n");
            vacationDetails.append("End date: " + editEndDate.getText().toString() + "\n");

            // Add excursion details
            for (Excursion e: associatedExcursions) {
                vacationDetails.append("Excursion title: " + e.getExcursionTitle() + ", Excursion Date: " + e.getExcursionDate() + "\n");
            }

            sentIntent.putExtra(Intent.EXTRA_TEXT, vacationDetails.toString());
            sentIntent.putExtra(Intent.EXTRA_TITLE, "My Vacation Details");
            sentIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sentIntent, null);
            startActivity(shareIntent);
            return true;
        }

        if (item.getItemId() == R.id.logoutUserVacationDetails) {
            confirmLogout();
            return true;
        }

        if (item.getItemId() == android.R.id.home) { // for a back button
            this.finish();
            return true;
        }

        return true;
    }

    private void setNotification(Long trigger, String alertMessage ){
        Intent intent = new Intent(UserVacationDetails.this, MyReceiver.class);
        intent.putExtra("key", alertMessage);
        PendingIntent sender = PendingIntent.getBroadcast(UserVacationDetails.this, ++MainActivity.numAlert, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, sender);
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
