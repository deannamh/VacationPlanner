package com.example.vacationplanner.UI;

import android.app.DatePickerDialog;
import android.content.Intent;
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

    Repository repository;

    Vacation currentVacation;
    int numExcursions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vacation_details);

        editTitle = findViewById(R.id.titleText);
        editHotelName= findViewById(R.id.hotelText);
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

        // format date to set button text with current date if it's a null or empty string (like when adding a new vacation)
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String currentDate = sdf.format(new Date());

        if (startDate == null || startDate.isEmpty()){
            startDate = currentDate;
        }
        if (endDate == null || endDate.isEmpty()){
            endDate = currentDate;
        }
        startDateButton.setText(startDate);
        endDateButton.setText(endDate);


        // when user picks date from datepicker dialog box, set Calendar object to that date and update the start button label
        dpStartDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarStartDate.set(Calendar.YEAR, year);
                calendarStartDate.set(Calendar.MONTH, month);
                calendarStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateButtonLabelStart();
            }
        };

        dpEndDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarEndDate.set(Calendar.YEAR, year);
                calendarEndDate.set(Calendar.MONTH, month);
                calendarEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateButtonLabelEnd();
            }
        };

        // when user clicks the startDateButton, we want to get the text from the button, parse it into calendar object and make a datepicker dialog box pop up
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = startDateButton.getText().toString(); // getting text from the button

                if (info.isEmpty()){
                    calendarStartDate.setTime(new Date()); // if the button text is empty, set it to the current date
                }
                else {
                    try{
                        calendarStartDate.setTime(sdf.parse(info)); // parsing info to get a date object and set the time of our calender object to that info
                    }
                    catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }

                new DatePickerDialog(VacationDetails.this, dpStartDate, calendarStartDate.get(Calendar.YEAR),
                        calendarStartDate.get(Calendar.MONTH), calendarStartDate.get(Calendar.DAY_OF_MONTH)).show(); // make a date picker dialog box pop up
            }
        });

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = endDateButton.getText().toString(); // getting text from the button

                if (info.isEmpty()){
                    calendarEndDate.setTime(new Date()); // if the button text is empty, set it to the current date
                }
                else {
                    try{
                        calendarEndDate.setTime(sdf.parse(info)); // parsing info to get a date object and set the time of our calender object to that info
                    }
                    catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }

                new DatePickerDialog(VacationDetails.this, dpEndDate, calendarEndDate.get(Calendar.YEAR),
                        calendarEndDate.get(Calendar.MONTH), calendarEndDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        // Floating action button that takes user to Excursion Details page to add excursions
        FloatingActionButton fab = findViewById(R.id.fabAddExcursion);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VacationDetails.this, ExcursionDetails.class);
                startActivity(intent);
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
        List<Excursion> filteredExcursions = new ArrayList<>();
        List<Excursion> alLExcursions = repository.getmAllExcursions();

        for (Excursion e: alLExcursions){
            if (e.getVacationID() == vacationID){
                filteredExcursions.add(e);
            }
        }
        excursionAdapter.setExcursions(filteredExcursions);
        



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // inflate menu
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_vacation_details, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        // if user selects 'save vacation' menu option
        if (item.getItemId() == R.id.vacationsave){
            String myFormat = "MM/dd/yy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            String stringStartDate = sdf.format(calendarStartDate.getTime());
            String stringEndDate = sdf.format(calendarEndDate.getTime());

            // convert stringStartDate and stringEndDate back to Date objects so we can compare dates
            try {
                Date dateStart = sdf.parse(stringStartDate);
                Date dateEnd = sdf.parse(stringEndDate);
                assert dateEnd != null;

                if (!dateEnd.after(dateStart)){ //if the date is not AFTER start date (can't be the same day or before)
                    Toast.makeText(VacationDetails.this, "Please select an end date later than the start date.", Toast.LENGTH_LONG).show();
                }
                else {
                    Vacation vacation;
                    if(vacationID == -1){ //if vacationID does not exist from intent extra, set the vacationID to 1
                        if(repository.getmAllVacations().isEmpty()) {
                            vacationID = 1;
                        }
                        else { // increment last vacationID
                            vacationID = repository.getmAllVacations().get(repository.getmAllVacations().size() - 1).getVacationID() + 1;
                            vacation = new Vacation(vacationID, editTitle.getText().toString(), editHotelName.getText().toString(), stringStartDate, stringEndDate);
                            repository.insert(vacation);
                            this.finish(); //close the screen and go back to the previous screen. need to update VacationList.java next and make it update to show changes(onResume)
                        }
                    }
                    else { //existing vacation was modified by user so we need to update it instead:
                        vacation = new Vacation(vacationID, editTitle.getText().toString(), editHotelName.getText().toString(), stringStartDate, stringEndDate);
                        repository.update(vacation);
                        this.finish();
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        if (item.getItemId() ==  R.id.vacationdelete){
            List<Vacation> allVacations = repository.getmAllVacations();

            for (Vacation v: allVacations){
                if(v.getVacationID() == vacationID) currentVacation = v;
            }

            numExcursions = 0;
            List<Excursion> allExcursions = repository.getmAllExcursions();

            for (Excursion e: allExcursions){
                if (e.getVacationID() == vacationID) numExcursions++;
            }

            if(numExcursions == 0){
                repository.delete(currentVacation);
                Toast.makeText(VacationDetails.this, currentVacation.getTitle() + " was deleted.", Toast.LENGTH_LONG).show();
                VacationDetails.this.finish();
            }
            else {
               Toast.makeText(VacationDetails.this, "Cannot delete a vacation with excursions.", Toast.LENGTH_LONG).show();
            }

        }

        if (item.getItemId() == android.R.id.home) { // for a back button
            this.finish();
            return true;
        }

        return true;
    }

    // this method uses SimpleDateFormat to format the calendar objects for startDateButton
    private void updateButtonLabelStart() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        startDateButton.setText(sdf.format(calendarStartDate.getTime()));
    }

    //method for formatting calendar object for endDateButton
    private void updateButtonLabelEnd() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        endDateButton.setText(sdf.format(calendarEndDate.getTime()));
    }
}