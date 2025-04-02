package com.example.vacationplanner.UI;

import android.app.AlarmManager;
import android.app.AlertDialog;
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

import com.example.vacationplanner.R;
import com.example.vacationplanner.database.ExcursionRepository;
import com.example.vacationplanner.database.VacationRepository;
import com.example.vacationplanner.entities.Excursion;
import com.example.vacationplanner.entities.Vacation;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ExcursionDetails extends AppCompatActivity {
    String excursionID; //changed from int to string
    String excursionTitle;
    String excursionDate;
    String vacationID; // changed from int to string

    EditText editExcursionTitle;

    Button dateButton;
    DatePickerDialog.OnDateSetListener dpDate;
    final Calendar calendarDate = Calendar.getInstance();

    //Repository repository = new Repository(getApplication());
    VacationRepository vacationRepository;
    ExcursionRepository excursionRepository;

    Vacation currentVacation;
    //Excursion currentExcursion;
    Date startDate;
    Date endDate;
    String vacationStartDate;
    String vacationEndDate;
    final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_details);

        // toolbar set up as the action bar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize firebase repositories since we are no longer using Room database
        vacationRepository = new VacationRepository();
        excursionRepository = new ExcursionRepository();

        editExcursionTitle = findViewById(R.id.excursionTitle);
        dateButton = findViewById(R.id.excursionDateButton);

        excursionID = getIntent().getStringExtra("id"); // changed from getIntExtra
        excursionTitle = getIntent().getStringExtra("title");
        excursionDate = getIntent().getStringExtra("date");
        vacationID = getIntent().getStringExtra("vacationID"); // changed from getIntExtra("vacationID", -1)

        editExcursionTitle.setText(excursionTitle);

        String currentDate = sdf.format(new Date());

        if (excursionDate == null || excursionDate.isEmpty()){
            excursionDate = currentDate;
        }
        dateButton.setText(excursionDate);

        // need to get the current vacation from Room database to check the start and end date to make sure the excursion is during that time
//        List<Vacation> allVacations = repository.getmAllVacations();
//        for (Vacation v : allVacations){
//            if (v.getVacationID() == vacationID) currentVacation = v;
//        }
//
//        //String for dates
//        vacationStartDate = currentVacation.getStartDate();
//        vacationEndDate = currentVacation.getEndDate();
//
//        // Convert String date to Date objects using sdf
//        try {
//            startDate = sdf.parse(vacationStartDate); //Date object
//            endDate = sdf.parse(vacationEndDate); // Date object
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }

        // this will get the current vacation from Firebase to check the start and end date to make sure the excursion is during that time
        vacationRepository.getVacationById(vacationID).observe(this, vacation -> {
            if (vacation != null) {
                currentVacation = vacation;

                // String for dates
                vacationStartDate = currentVacation.getStartDate();
                vacationEndDate = currentVacation.getEndDate();

                // Convert String date to Date objects using sdf
                try {
                    startDate = sdf.parse(vacationStartDate);
                    endDate = sdf.parse(vacationEndDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });


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
                    //Toast.makeText(ExcursionDetails.this, "Selected date must be during vacation dates. Choose a date between " + vacationStartDate + " and " + vacationEndDate + ".", Toast.LENGTH_LONG).show();
                    //use snackbar since toast is being cut off:
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Selected date must be during vacation dates. Choose a date between " + vacationStartDate + " and " + vacationEndDate + ".", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setBackgroundTint(Color.parseColor("#96EDE9"));
                    snackbar.setTextColor(Color.parseColor("#0D1E5F"));
                    snackbar.setActionTextColor(Color.parseColor("#ED3358"));
                    snackbar.setTextMaxLines(5);
                    snackbar.setAction("Dismiss", v -> {}).show();

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

            // compare date objects (date, startDate, endDate)
            try {
                Date date = sdf.parse(stringExcursionDate);
                assert date != null;

                if (date.before(startDate) || date.after(endDate)){ //if the date is not during the associated vacation:
                    //Toast.makeText(ExcursionDetails.this, "Choose a date between " + vacationStartDate + " and " + vacationEndDate + ".", Toast.LENGTH_LONG).show();
                    //use snackbar since toast is being cut off:
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Selected date must be during vacation dates. Choose a date between " + vacationStartDate + " and " + vacationEndDate + ".", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setBackgroundTint(Color.parseColor("#96EDE9"));
                    snackbar.setTextColor(Color.parseColor("#0D1E5F"));
                    snackbar.setActionTextColor(Color.parseColor("#ED3358"));
                    snackbar.setTextMaxLines(5);
                    snackbar.setAction("Dismiss", v -> {}).show();
                }
                else {
                    // Create excursion if it doesn't exist:
                    if(excursionID == null || excursionID.isEmpty()){
                        // New excursion - generate new ID with UUID
                        excursionID = UUID.randomUUID().toString();
                        Excursion excursion = new Excursion(excursionID, editExcursionTitle.getText().toString(), stringExcursionDate, vacationID, false);
                        excursionRepository.insert(excursion);
                        Toast.makeText(ExcursionDetails.this, "Excursion: " + excursion.getExcursionTitle() + " was added.", Toast.LENGTH_LONG).show();
                        this.finish();
                    }
                    else {
                        // existing excursion was modified by user so we need to update it instead:
                        Excursion excursion = new Excursion(excursionID, editExcursionTitle.getText().toString(), stringExcursionDate, vacationID, false);
                        excursionRepository.update(excursion);
                        Toast.makeText(ExcursionDetails.this, "Excursion: " + excursion.getExcursionTitle() +
                                " was updated.", Toast.LENGTH_LONG).show();
                        this.finish();
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        if (item.getItemId() ==  R.id.excursiondelete){
            if (excursionID != null && !excursionID.isEmpty()) { // if excursionID is not null and excursionID is not emmpty:
                // create a temp excursion object to delete
                Excursion excursionToDelete = new Excursion();
                // set excursionID and excursionID to the temp excursion
                excursionToDelete.setExcursionID(excursionID);
                excursionToDelete.setExcursionTitle(excursionTitle);
                excursionRepository.delete(excursionToDelete);

                Toast.makeText(ExcursionDetails.this, "Excursion: " + excursionTitle + " was deleted.", Toast.LENGTH_LONG).show();
                ExcursionDetails.this.finish();
            }
        }

        if (item.getItemId() == R.id.excursionalert){
            String dateFromScreen = dateButton.getText().toString();

            Date excursionDate = null;

            try {
                excursionDate = sdf.parse(dateFromScreen);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            Long trigger = excursionDate.getTime();
            Intent intent = new Intent(ExcursionDetails.this, MyReceiver.class);
            intent.putExtra("key", "Excursion: " + excursionTitle + " is starting today!");
            PendingIntent sender = PendingIntent.getBroadcast(ExcursionDetails.this, ++MainActivity.numAlert, intent, PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, sender);

            Toast.makeText(ExcursionDetails.this, "Excursion alert set.", Toast.LENGTH_LONG).show();
            return true;
        }

        if (item.getItemId() == R.id.logoutExcursionDetails) {
            confirmLogout();
            return true;
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