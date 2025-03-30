package com.example.vacationplanner.UI;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class VacationDetails extends AppCompatActivity {

    String vacationID;
    String title;
    String hotelName;
    String startDate;
    String endDate;
    EditText editTitle;
    EditText editHotelName;

    //for DatePicker
    Button startDateButton;
    Button endDateButton;
    DatePickerDialog.OnDateSetListener dpStartDate; //dp for datepicker
    DatePickerDialog.OnDateSetListener dpEndDate;
    final Calendar calendarStartDate = Calendar.getInstance();
    final Calendar calendarEndDate = Calendar.getInstance();
    final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);

    // for Room Database:
    //Repository repository;
    //Vacation currentVacation;
    //int numExcursions;

    // for Firebase:
    VacationRepository vacationRepository;
    ExcursionRepository excursionRepository;

    Vacation currentVacation;
    List<Excursion> associatedExcursions = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_details);

        // initialize firebase repositories:
        vacationRepository = new VacationRepository();
        excursionRepository = new ExcursionRepository();

        editTitle = findViewById(R.id.titleText);
        editHotelName = findViewById(R.id.hotelText);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);

        //get values from RecyclerView that were sent over in Intent
        vacationID = getIntent().getStringExtra("id");
        Log.d("VacationDetails", "Received vacation ID: " + vacationID);

        title = getIntent().getStringExtra("title");
        hotelName = getIntent().getStringExtra("hotel");
        startDate = getIntent().getStringExtra("startdate");
        endDate = getIntent().getStringExtra("enddate");

        editTitle.setText(title);
        editHotelName.setText(hotelName);

        if (startDate != null) {
            try {
                Date dateStart = sdf.parse(startDate);
                calendarStartDate.setTime(dateStart);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (endDate != null){
            try{
                Date dateEnd = sdf.parse(endDate);
                calendarEndDate.setTime(dateEnd);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        // when user clicks the startDateButton, we want to get the text from the button, parse it into calendar object and make a datepicker dialog box pop up
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = startDateButton.getText().toString(); // getting text from the button

                if (info.equals("")) info = startDate;

                try {
                    calendarStartDate.setTime(sdf.parse(info)); // parsing info to get a date object and set the time of our calender object to that info
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                new DatePickerDialog(VacationDetails.this, dpStartDate, calendarStartDate.get(Calendar.YEAR),
                        calendarStartDate.get(Calendar.MONTH), calendarStartDate.get(Calendar.DAY_OF_MONTH)).show(); // make a date picker dialog box pop up
            }
        });

        // when user picks date from datepicker dialog box, set Calendar object to that date and update the start button label.
        // this also ensures the correct date format will be used since user selects a date from a calendar instead of entering it manually
        dpStartDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarStartDate.set(Calendar.YEAR, year);
                calendarStartDate.set(Calendar.MONTH, month);
                calendarStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateButtonLabelStart();
            }
        };

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = endDateButton.getText().toString(); // getting text from the button

                if (info.equals("")) info = endDate;

                try {
                    calendarEndDate.setTime(sdf.parse(info)); // parsing info to get a date object and set the time of our calender object to that info
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                new DatePickerDialog(VacationDetails.this, dpEndDate, calendarEndDate.get(Calendar.YEAR),
                        calendarEndDate.get(Calendar.MONTH), calendarEndDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        dpEndDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarEndDate.set(Calendar.YEAR, year);
                calendarEndDate.set(Calendar.MONTH, month);
                calendarEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateButtonLabelEnd();
            }
        };


        // Floating action button that takes user to Excursion Details page to add excursions
        FloatingActionButton fab = findViewById(R.id.fabAddExcursion);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vacationID == null || vacationID.isEmpty()) { //vacation is not saved yet
                    Toast.makeText(VacationDetails.this, "Save vacation to add excursions", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(VacationDetails.this, ExcursionDetails.class);
                    //we need to send (putExtra) the vacationID to excursiondetails so we can ensure the excursion date is during the associated vacation.
                    //sending the ID instead of the dates lets us check the database for the dates (more recent dates in case of changes by user)
                    intent.putExtra("vacationID", vacationID);
                    startActivity(intent);
                }
            }
        });

        // populate excursionrecyclerview on vacation details activity associated with a vacationID
        RecyclerView recyclerView = findViewById(R.id.excursionrecyclerview);
        final ExcursionAdapter excursionAdapter = new ExcursionAdapter(this);
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
            RecyclerView recyclerView = findViewById(R.id.excursionrecyclerview);
            final ExcursionAdapter excursionAdapter = new ExcursionAdapter(this);
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

        updateButtonLabelEnd();
        updateButtonLabelStart();
    }

    // inflate menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vacation_details, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // if user selects 'save vacation' menu option
        if (item.getItemId() == R.id.vacationsave) {
            String stringStartDate = sdf.format(calendarStartDate.getTime());
            String stringEndDate = sdf.format(calendarEndDate.getTime());
            // convert stringStartDate and stringEndDate back to Date objects so we can compare dates
            try {
                Date dateStart = sdf.parse(stringStartDate);
                Date dateEnd = sdf.parse(stringEndDate);

                Date currentDate = new Date();
                String currDate = sdf.format(currentDate);

                assert dateEnd != null;
                assert dateStart != null;

                // we are validating that the chosen end date is after the start date
                //if the end date is not AFTER start date (can't be the same day or before)
                if (!dateEnd.after(dateStart)) {
                    //Toast.makeText(VacationDetails.this, "Select an end date after the start date: " + startDate + ".", Toast.LENGTH_LONG).show();
                    displaySnackbar("Select an end date after the start date: " + stringStartDate + ".");
                }

                else {
                    //need to check if changing start and end dates affect any associated excursions
                    int numOutOfRangeExcursions = 0;

                    for (Excursion e : associatedExcursions) {
                        Date excursionDate = sdf.parse(e.getExcursionDate());
                        assert excursionDate != null;

                        if (excursionDate.before(dateStart) || excursionDate.after(dateEnd)) {  //excursion can't be before vacation starts or after it ends
                            ++numOutOfRangeExcursions;
                        }
                    }

                    if (numOutOfRangeExcursions > 0) { //means there are excursions within original vacation dates that would be affected by new dates
                        displaySnackbar("Associated excursion(s) should be deleted or modified first.");
                    }
                    else {
                        Vacation vacation;
                        if (vacationID == null || vacationID.isEmpty()) { //if vacationID does not exist from intent extra
                            vacationID = UUID.randomUUID().toString();
                            Log.d("VacationDetails", "Creating new vacation with ID: " + vacationID);

                            vacation = new Vacation(vacationID, editTitle.getText().toString(), editHotelName.getText().toString(), stringStartDate, stringEndDate);

                            // set userId field to logged in user's id
                            vacation.setUserId(vacationRepository.getCurrentUserId());

                            // insert into firebase
                            vacationRepository.insert(vacation);
                            Toast.makeText(VacationDetails.this, "Vacation: " + vacation.getTitle() + " was added.", Toast.LENGTH_LONG).show();
                            this.finish(); //close the screen and go back to the previous screen. need to update VacationList.java next and make it update to show changes(onResume)
                        }
                        else { // existing vacation was modified by user so we need to update it instead:
                            Log.d("VacationDetails", "Updating existing vacation with ID: " + vacationID);
                            vacation = new Vacation(vacationID, editTitle.getText().toString(), editHotelName.getText().toString(), stringStartDate, stringEndDate);
                            // need to save current userid:
                            if (currentVacation != null) {
                                vacation.setUserId(currentVacation.getUserId());
                            } else {
                                vacation.setUserId(vacationRepository.getCurrentUserId());
                            }

                            vacationRepository.update(vacation);
                            Toast.makeText(VacationDetails.this, "Vacation: " + vacation.getTitle() + " was updated.", Toast.LENGTH_LONG).show();
                            this.finish();
                        }
                    }
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        if (item.getItemId() == R.id.vacationdelete) {
            if (vacationID != null && !vacationID.isEmpty()) {
                // check if there are associated excursions for this vacation before deleting:
                if (associatedExcursions.isEmpty()) {
                    // delete by vacationID using helper method
                    vacationRepository.deleteById(vacationID);
                    Toast.makeText(VacationDetails.this, "Vacation: " + title + " was deleted.",
                            Toast.LENGTH_LONG).show();
                    this.finish();
                } else {
                    displaySnackbar("Cannot delete a vacation with excursions.");
                }
            }
        }



        if (item.getItemId() == R.id.vacationalert){
            String startDateFromScreen = startDateButton.getText().toString();
            String endDateFromScreen = endDateButton.getText().toString();

            Date startDate = null;
            Date endDate = null;

            try {
                startDate = sdf.parse(startDateFromScreen);
                endDate = sdf.parse(endDateFromScreen);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

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
            Toast.makeText(VacationDetails.this, "Vacation alert set.", Toast.LENGTH_LONG).show();

        }

        if (item.getItemId() == R.id.vacationshare){
            Intent sentIntent = new Intent();
            sentIntent.setAction(Intent.ACTION_SEND);

            //need to get all the vacation details to share (vacation title, hotel, start and end dates, associated excursions)
            StringBuilder vacationDetails = new StringBuilder();

            vacationDetails.append("Vacation title: " + editTitle.getText().toString() + "\n");
            vacationDetails.append("Hotel: " + editHotelName.getText().toString() + "\n");
            vacationDetails.append("Start date: " + startDateButton.getText().toString() + "\n");
            vacationDetails.append("End date: " + endDateButton.getText().toString() + "\n");

            for (Excursion e: associatedExcursions){
                vacationDetails.append("Excursion title: " + e.getExcursionTitle() + " , Excursion Date: " + e.getExcursionDate() + "\n");
            }

            sentIntent.putExtra(Intent.EXTRA_TEXT, vacationDetails.toString());
            sentIntent.putExtra(Intent.EXTRA_TITLE, "Shared Vacation Details");
            sentIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sentIntent, null);
            startActivity(shareIntent);
            return true;
        }

        if (item.getItemId() == R.id.logoutVacationDetails) {
            confirmLogout();
            return true;
        }

        if (item.getItemId() == android.R.id.home) { // for a back button
            this.finish();
            return true;
        }

        return true;
    }

    // this method uses SimpleDateFormat to format the calendar objects for startDateButton
    private void updateButtonLabelStart() {
        startDateButton.setText(sdf.format(calendarStartDate.getTime()));
    }

    //method for formatting calendar object for endDateButton
    private void updateButtonLabelEnd() {
        endDateButton.setText(sdf.format(calendarEndDate.getTime()));
    }

    private void displaySnackbar(String alertString) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), alertString, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(Color.parseColor("#96EDE9"));
        snackbar.setTextColor(Color.parseColor("#0D1E5F"));
        snackbar.setActionTextColor(Color.parseColor("#ED3358"));
        snackbar.setTextMaxLines(5);
        snackbar.show();
    }

    private void setNotification(Long trigger, String alertMessage ){
        Intent intent = new Intent(VacationDetails.this, MyReceiver.class);
        intent.putExtra("key", alertMessage);
        PendingIntent sender = PendingIntent.getBroadcast(VacationDetails.this, ++MainActivity.numAlert, intent, PendingIntent.FLAG_IMMUTABLE);
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