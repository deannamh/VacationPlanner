package com.example.vacationplanner.UI;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vacationplanner.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationplanner.entities.User;
import com.example.vacationplanner.entities.Vacation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdminSearchActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private UserAdapter userAdapter;

    // for searching by email
    private List<User> userList;
    private EditText searchEmailEditText;
    private Button searchByEmailButton;
    private RecyclerView userListRecyclerView;
    private TextView noUsersTextView;

    // for search results showing all vacations for selected date range
    private Button selectStartDateButton;
    private Button selectEndDateButton;
    private Button searchDateRangeButton;
    private List<Vacation> vacationList;
    private VacationAdapter vacationAdapter;
    private RecyclerView vacationListRecyclerView;
    private TextView vacationListFound;

    // for datepicker
    DatePickerDialog.OnDateSetListener dpStartDate;
    DatePickerDialog.OnDateSetListener dpEndDate;
    final Calendar calendarStartDate = Calendar.getInstance();
    final Calendar calendarEndDate = Calendar.getInstance();
    final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);


    // for sharedpreferecnes
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SKJTravelPrefs";
    private static final String SELECTED_USER_ID = "selectedUserId";
    private static final String SELECTED_USER_EMAIL = "selectedUserEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_searchpage);
        db = FirebaseFirestore.getInstance();

        // set up SharedPreferences (android api that can be used for retrieving key-value pairs from small collection of data)
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // toolbar set up as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // for searching users by email
        searchEmailEditText = findViewById(R.id.searchByEmailEditText);
        searchByEmailButton = findViewById(R.id.searchByEmailButton);
        userListRecyclerView = findViewById(R.id.userListRecyclerView);
        noUsersTextView = findViewById(R.id.noUsersTextView);

        // for searching vacations by date range
        selectStartDateButton = findViewById(R.id.selectStartDateButton);
        selectEndDateButton = findViewById(R.id.selectEndDateButton);
        searchDateRangeButton = findViewById(R.id.searchDateRange);
        vacationListRecyclerView = findViewById(R.id.vacationListRecyclerView);
        vacationListFound = findViewById(R.id.vacationListFound);

        // for RecyclerView to show user found by email
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this);
        userAdapter.setUsers(userList);
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListRecyclerView.setAdapter(userAdapter);

        // for RecyclerView to show vacations found by date range
        vacationList = new ArrayList<>();
        vacationAdapter = new VacationAdapter(this);
        vacationAdapter.setVacations(vacationList);
        if (vacationListRecyclerView != null) {
            vacationListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            vacationListRecyclerView.setAdapter(vacationAdapter);
            vacationListRecyclerView.setVisibility(View.GONE); // we don't need to see this right away
        }

        // when admin clicks the selectStartDateButton, we want to get text from the button, parse it into calendar object and show datepicker dialog
        selectStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = selectStartDateButton.getText().toString(); // getting text from the button

                if (info.equals("Select start date")) {
                    // Use current date if no date is selected
                    calendarStartDate.setTime(new Date());
                } else {
                    try {
                        calendarStartDate.setTime(sdf.parse(info)); // parsing info to get a date object and set the time of our calender object to that info
                    } catch (ParseException e) {
                        calendarStartDate.setTime(new Date()); // Use current date if parsing fails
                    }
                }

                new DatePickerDialog(AdminSearchActivity.this, dpStartDate, calendarStartDate.get(Calendar.YEAR),
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

        // when user clicks the selectEndDateButton, we want to get text from the button, parse it into calendar object and show datepicker dialog
        selectEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = selectEndDateButton.getText().toString(); // getting text from the button

                if (info.equals("Select end date")) {
                    // Use current date if no date is selected
                    calendarEndDate.setTime(new Date());
                } else {
                    try {
                        calendarEndDate.setTime(sdf.parse(info)); // parsing info to get a date object and set the time of our calender object to that info
                    } catch (ParseException e) {
                        calendarEndDate.setTime(new Date()); // Use current date if parsing fails
                    }
                }

                new DatePickerDialog(AdminSearchActivity.this, dpEndDate, calendarEndDate.get(Calendar.YEAR),
                        calendarEndDate.get(Calendar.MONTH), calendarEndDate.get(Calendar.DAY_OF_MONTH)).show(); // make a date picker dialog box pop up
            }
        });

        // when user picks date from datepicker dialog box, set Calendar object to that date and update the end button label.
        dpEndDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarEndDate.set(Calendar.YEAR, year);
                calendarEndDate.set(Calendar.MONTH, month);
                calendarEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateButtonLabelEnd();
            }
        };


        // searchByEmailButton onclicklistener
        searchByEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // show user list when searching by email and hide the vacation list
                userListRecyclerView.setVisibility(View.VISIBLE);
                if (vacationListRecyclerView != null) {
                    vacationListRecyclerView.setVisibility(View.GONE);
                }

                String email = searchEmailEditText.getText().toString().trim().toLowerCase(); // convert to lowercase since firebase stores emails in lowercase

                if (email.isEmpty()) {
                    Toast.makeText(AdminSearchActivity.this, "Please enter an email address to search", Toast.LENGTH_SHORT).show();
                    return;
                }

                userList.clear();
                userAdapter.notifyDataSetChanged();
                noUsersTextView.setVisibility(View.GONE);

                // Search for users in Firestore
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String userId = document.getId();
                                    String userEmail = document.getString("email");
                                    User user = new User(userId, userEmail);
                                    userList.add(user);
                                }

                                userAdapter.notifyDataSetChanged();

                                if (userList.isEmpty()) {
                                    noUsersTextView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.e("AdminSearchActivity",  "Error: " + Objects.requireNonNull(task.getException()).getMessage());
                                Toast.makeText(AdminSearchActivity.this, "Error searching for users", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // searchDateRangeButton onclicklistener
        searchDateRangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide user list and show vacation list
                userListRecyclerView.setVisibility(View.GONE);
                if (vacationListRecyclerView != null) {
                    // show textview heading "Vacations Found: "  and show the vacationList recyclerview
                    vacationListFound.setVisibility(View.VISIBLE);
                    vacationListRecyclerView.setVisibility(View.VISIBLE);
                }
                // get date strings from the button
                String startDateString = selectStartDateButton.getText().toString();
                String endDateString = selectEndDateButton.getText().toString();

                // Check if dates have been selected
                if (startDateString.equals("Select start date") || endDateString.equals("Select end date")) {
                    Toast.makeText(AdminSearchActivity.this, "Please select both a start date and end date", Toast.LENGTH_SHORT).show();
                    return;
                }

                // validate the end date is after the start date
                try {
                    Date startDate = sdf.parse(startDateString);
                    Date endDate = sdf.parse(endDateString);

                    if (endDate.before(startDate)) {
                        Toast.makeText(AdminSearchActivity.this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    Toast.makeText(AdminSearchActivity.this, "Error parsing dates", Toast.LENGTH_SHORT).show();
                    return;
                }

                // clear previous vacationlist results
                vacationList.clear();
                vacationAdapter.notifyDataSetChanged();

                // search for vacations in firestore db within the date range
                db.collection("vacations")
                        .whereGreaterThanOrEqualTo("startDate", startDateString)
                        .whereLessThanOrEqualTo("startDate", endDateString)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Vacation vacation = document.toObject(Vacation.class);
                                    // set vacation id to vacation id from firestore document
                                    vacation.setVacationID(document.getId());
                                    vacationList.add(vacation);
                                }

                                vacationAdapter.notifyDataSetChanged();

                                if (vacationList.isEmpty()) {
                                    Toast.makeText(AdminSearchActivity.this, "No vacations found for selected date range", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("AdminSearchActivity", "Error: " + Objects.requireNonNull(task.getException()).getMessage());
                                Toast.makeText(AdminSearchActivity.this, "Error searching for vacations", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

// this method uses SimpleDateFormat to format the calendar objects for startDateButton
private void updateButtonLabelStart() {
    selectStartDateButton.setText(sdf.format(calendarStartDate.getTime()));
}

//method for formatting calendar object for endDateButton
private void updateButtonLabelEnd() {
    selectEndDateButton.setText(sdf.format(calendarEndDate.getTime()));
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.admin_logout) {
            // sign out from firebase auth
            FirebaseAuth.getInstance().signOut();

            // remove selected user from sharedpreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(SELECTED_USER_ID);
            editor.remove(SELECTED_USER_EMAIL);
            editor.apply();

            // go back to login screen
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}