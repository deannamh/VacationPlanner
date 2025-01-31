package com.example.vacationplanner.UI;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vacationplanner.R;
import com.example.vacationplanner.database.Repository;
import com.example.vacationplanner.entities.Excursion;
import com.example.vacationplanner.entities.Vacation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcursionDetails extends AppCompatActivity {
    int excursionID;
    String excursionTitle;
    String excursionDate;
    int vacationID;

    EditText editExcursionTitle;

    Button dateButton;
    DatePickerDialog.OnDateSetListener dpDate;
    final Calendar calendarDate = Calendar.getInstance();

    Repository repository = new Repository(getApplication());

    Vacation currentVacation;
    Excursion currentExcursion;
    Date startDate;
    Date endDate;
    String vacationStartDate;
    String vacationEndDate;
    final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_excursion_details);

        editExcursionTitle = findViewById(R.id.excursionTitle);
        dateButton = findViewById(R.id.excursionDateButton);

        excursionID = getIntent().getIntExtra("excursionID", -1);
        excursionTitle = getIntent().getStringExtra("title");
        excursionDate = getIntent().getStringExtra("date");
        vacationID = getIntent().getIntExtra("vacationID", -1);

        editExcursionTitle.setText(excursionTitle);

        String currentDate = sdf.format(new Date());

        if (excursionDate == null || excursionDate.isEmpty()){
            excursionDate = currentDate;
        }
        dateButton.setText(excursionDate);

        // need to get the current vacation to check the start and end date to make sure the excursion is during that time
        List<Vacation> allVacations = repository.getmAllVacations();
        for (Vacation v : allVacations){
            if (v.getVacationID() == vacationID) currentVacation = v;
        }

        //String for dates
        vacationStartDate = currentVacation.getStartDate();
        vacationEndDate = currentVacation.getEndDate();

        // Convert String date to Date objects using sdf
        try {
            startDate = sdf.parse(vacationStartDate); //Date object
            endDate = sdf.parse(vacationEndDate); // Date object
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        // when user picks date from datepicker dialog box, set Calendar object to that date and update the start button label
        // Validate that the chosen date is during the vacation
        dpDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarDate.set(Calendar.YEAR, year);
                calendarDate.set(Calendar.MONTH, month);
                calendarDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                Date dateSelected = calendarDate.getTime();

                if (dateSelected.before(startDate) || dateSelected.after(endDate)){
                    Toast.makeText(ExcursionDetails.this, "Selected date must be during vacation dates. Choose a date between " + vacationStartDate + " and " + vacationEndDate + ".", Toast.LENGTH_LONG).show();
                }
                else {
                    updateButtonLabel();
                }
            }
        };

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = dateButton.getText().toString(); // getting text from the button

                if (info.isEmpty()){
                    calendarDate.setTime(new Date()); // if the button text is empty, set it to the current date
                }
                else {
                    try{
                        calendarDate.setTime(sdf.parse(info)); // parsing text from button to get a date object and set the time of our calender object to that info
                    }
                    catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }

                new DatePickerDialog(ExcursionDetails.this, dpDate, calendarDate.get(Calendar.YEAR),
                        calendarDate.get(Calendar.MONTH), calendarDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_excursion_details, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        // if user selects 'save excursion' menu option
        if (item.getItemId() == R.id.excursionsave){
            String stringExcursionDate = sdf.format(calendarDate.getTime());

            // convert excursionDate, vacationStartDate, vacationEndDate back to Date objects so we can compare dates
            try {
                Date date = sdf.parse(stringExcursionDate);
                assert date != null;

                if (date.before(startDate) || date.after(endDate)){ //if the date is not during the associated vacation:
                    Toast.makeText(ExcursionDetails.this, "Selected date must be during vacation dates. Choose a date between " + vacationStartDate + " and " + vacationEndDate + ".", Toast.LENGTH_LONG).show();
                }
                else {
                    Excursion excursion;
                    if(excursionID == -1){ //if excursionID does not exist from intent extra, set it to 1
                        if(repository.getmAllExcursions().isEmpty()) {
                            excursionID = 1;
                        }
                        else { // increment last excursionID
                            excursionID = repository.getmAllExcursions().get(repository.getmAllExcursions().size() - 1).getExcursionID() + 1;
                            excursion = new Excursion(excursionID, editExcursionTitle.getText().toString(), stringExcursionDate, vacationID);
                            repository.insert(excursion);
                            this.finish(); //close the screen and go back to the previous screen. need to update VacationList.java next and make it update to show changes(onResume)
                        }
                    }
                    else { //existing excursion was modified by user so we need to update it instead:
                        excursion = new Excursion(excursionID, editExcursionTitle.getText().toString(), stringExcursionDate, vacationID);
                        repository.update(excursion);
                        this.finish();
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        if (item.getItemId() ==  R.id.excursiondelete){
            List<Excursion> allExcursions = repository.getmAllExcursions();

            for (Excursion e: allExcursions){
                if(e.getExcursionID() == excursionID) currentExcursion = e;
            }

            repository.delete(currentExcursion);
            Toast.makeText(ExcursionDetails.this, currentExcursion.getExcursionTitle() + " was deleted.", Toast.LENGTH_LONG).show();
            ExcursionDetails.this.finish();
        }

        if (item.getItemId() == android.R.id.home) { // for a back button
            this.finish();
            return true;
        }

        return true;
    }

    private void updateButtonLabel() {
        dateButton.setText(sdf.format(calendarDate.getTime()));
    }
}