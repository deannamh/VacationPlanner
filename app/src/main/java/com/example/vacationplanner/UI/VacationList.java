package com.example.vacationplanner.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class VacationList extends AppCompatActivity {
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vacation_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // floating action button on VacationList page to add vacations(vacation details)
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VacationList.this, VacationDetails.class);
                startActivity(intent);
            }
        });

        System.out.println(getIntent().getStringExtra("test"));

        }

    // Creates menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_vacation_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.sample){
            // Toast.makeText(VacationList.this, "put in sample data", Toast.LENGTH_LONG).show();
            repository = new Repository(getApplication());
            Vacation vacation = new Vacation(0, "Trinidad and Tobago", "Hyatt", "02/01/25", "02/15/25");
            repository.insert(vacation);
            vacation = new Vacation(0, "Florida", "Marriott", "04/03/25", "04/10/25");
            repository.insert(vacation);
            Excursion excursion = new Excursion(0, "Snorkeling", "02/05/25", 1);
            repository.insert(excursion);
            excursion = new Excursion(0, "Boat Tour", "02/07/25", 1);
            repository.insert(excursion);

            return true;
        }
        if(item.getItemId()==android.R.id.home){ // for a back button to home page
            this.finish();
            // Comment out the previous line and use the next two lines instead for back button to go to VacationDetails instead
            // Intent intent = new Intent(VacationList.this, VacationDetails.class);
            // startActivity(intent);
            return true;
        }
        return true;
    }
}