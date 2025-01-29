package com.example.vacationplanner.UI;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.R;
import com.example.vacationplanner.database.Repository;
import com.example.vacationplanner.entities.Excursion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

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

    List<Excursion> excursionList = new ArrayList<>();

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
        startDateButton.setText(startDate);
        endDateButton.setText(endDate);

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
                Date date;
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
                Date date;
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
        FloatingActionButton fab = findViewById(R.id.floatingActionButton2);
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
        // the excursionrecyclerview will show all excursions:
        // excursionAdapter.setExcursions(repository.getmAllExcursions());
        // we want the excursionrecyclerview to show the excursion associated with a specific vacation id:
        List<Excursion> allExcursions = repository.getmAllExcursions();
        for (Excursion e: allExcursions){
            if (e.getVacationID() == vacationID){
                excursionList.add(e);
            }
        }
        excursionAdapter.setExcursions(excursionList);
        



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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