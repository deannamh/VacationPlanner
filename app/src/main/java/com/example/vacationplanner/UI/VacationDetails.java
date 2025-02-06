package com.example.vacationplanner.UI;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.example.vacationplanner.database.Repository;
import com.example.vacationplanner.entities.Excursion;
import com.example.vacationplanner.entities.Vacation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VacationDetails extends AppCompatActivity {

    int vacationID;
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

    Repository repository;

    Vacation currentVacation;
    int numExcursions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vacation_details);

        editTitle = findViewById(R.id.titleText);
        editHotelName = findViewById(R.id.hotelText);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);

        //get values from RecyclerView that were sent over in Intent
        vacationID = getIntent().getIntExtra("id", -1);
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
                if (vacationID == -1) { //vacation is not saved yet
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
        repository = new Repository(getApplication());
        final ExcursionAdapter excursionAdapter = new ExcursionAdapter(this);
        recyclerView.setAdapter(excursionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // excursionAdapter.setExcursions(repository.getmAllExcursions()); // the excursionrecyclerview will show all excursions
        // we want the excursionrecyclerview to show the excursion associated with a specific vacation id:
        List<Excursion> filteredExcursions = repository.getAssociatedExcursions(vacationID);
        excursionAdapter.setExcursions(filteredExcursions);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() { //gets excursions associated with vacation id and updates the recyclerview
        super.onResume();
        repository = new Repository(getApplication());
        List<Excursion> filteredExcursions = repository.getAssociatedExcursions(vacationID);
        RecyclerView recyclerView = findViewById(R.id.excursionrecyclerview);
        final ExcursionAdapter excursionAdapter = new ExcursionAdapter(this);
        recyclerView.setAdapter(excursionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        excursionAdapter.setExcursions(filteredExcursions);
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
//                else if (!dateStart.after(currentDate)) { //if the start date is not AFTER current date (can't be the same day or before)
//                    //Toast.makeText(VacationDetails.this, "Please select a start date later than the current date: " + currDate + ".", Toast.LENGTH_LONG).show();
//                    displaySnackbar("Select a start date after the current date: " + currDate + ".");
//                }
                else {
                    //need to check if changing start and end dates affect any associated excursions
                    List<Excursion> associatedExcursions = repository.getAssociatedExcursions(vacationID);
                    int numAssociatedExcursions = 0;

                    for (Excursion e : associatedExcursions) {
                        Date excursionDate = sdf.parse(e.getExcursionDate());
                        assert excursionDate != null;

                        if (excursionDate.before(dateStart) || excursionDate.after(dateEnd)) {  //excursion can't be before vacation starts or after it ends
                            ++numAssociatedExcursions;
                        }
                    }

                    if (numAssociatedExcursions > 0) { //means there are excursions within original vacation dates that would be affected by new dates
                        displaySnackbar("Associated excursion(s) should be deleted or modified first.");
                    }
                    else {
                        Vacation vacation;
                        if (vacationID == -1) { //if vacationID does not exist from intent extra, set the vacationID to 1
                            if (repository.getmAllVacations().isEmpty()) {
                                vacationID = 1;
                            } else { // increment last vacationID
                                vacationID = repository.getmAllVacations().get(repository.getmAllVacations().size() - 1).getVacationID() + 1;
                                vacation = new Vacation(vacationID, editTitle.getText().toString(), editHotelName.getText().toString(), stringStartDate, stringEndDate);
                                repository.insert(vacation);
                                Toast.makeText(VacationDetails.this, "Vacation: " + vacation.getTitle() + " was added.", Toast.LENGTH_LONG).show();
                                //displaySnackbar(vacation.getTitle() + " vacation was added.");
                                this.finish(); //close the screen and go back to the previous screen. need to update VacationList.java next and make it update to show changes(onResume)
                            }
                        } else { //existing vacation was modified by user so we need to update it instead:
                            vacation = new Vacation(vacationID, editTitle.getText().toString(), editHotelName.getText().toString(), stringStartDate, stringEndDate);
                            repository.update(vacation);
                            Toast.makeText(VacationDetails.this, "Vacation: " + vacation.getTitle() + " was updated.", Toast.LENGTH_LONG).show();
                            //displaySnackbar(vacation.getTitle() + " vacation was updated.");
                            this.finish();
                        }
                    }
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        if (item.getItemId() == R.id.vacationdelete) {
            List<Vacation> allVacations = repository.getmAllVacations();

            for (Vacation v : allVacations) {
                if (v.getVacationID() == vacationID) currentVacation = v;
            }

            // need to make sure there are no excursions added to a vacation before deleting it
            numExcursions = 0;
            List<Excursion> allExcursions = repository.getmAllExcursions();

            for (Excursion e : allExcursions) {
                if (e.getVacationID() == vacationID) numExcursions++;
            }

            if (numExcursions == 0) {
                repository.delete(currentVacation);
                Toast.makeText(VacationDetails.this, "Vacation: " + currentVacation.getTitle() + " was deleted.", Toast.LENGTH_LONG).show();
                //displaySnackbar(currentVacation.getTitle() + " vacation was deleted.");
                this.finish();
            } else {
                //Toast.makeText(VacationDetails.this, "Cannot delete a vacation with excursions.", Toast.LENGTH_LONG).show();
                displaySnackbar("Cannot delete a vacation with excursions.");
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

            if (startDate != null) {
                setNotification(startDate.getTime(), "Vacation: " + title + " is starting today!");
            }

            if (endDate != null) {
                setNotification(endDate.getTime(), "Vacation: " + title + " is ending today.");
            }

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

            List<Excursion> associatedExcursions = repository.getAssociatedExcursions(vacationID);
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
}