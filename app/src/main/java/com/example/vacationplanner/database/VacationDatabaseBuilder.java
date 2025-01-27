package com.example.vacationplanner.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.vacationplanner.dao.ExcursionDAO;
import com.example.vacationplanner.dao.VacationDAO;
import com.example.vacationplanner.entities.Excursion;
import com.example.vacationplanner.entities.Vacation;

@Database(entities = {Vacation.class, Excursion.class}, version = 1, exportSchema = false)
public abstract class VacationDatabaseBuilder extends RoomDatabase {
    public abstract VacationDAO vacationDAO();
    public abstract ExcursionDAO excursionDAO();

    private static volatile  VacationDatabaseBuilder INSTANCE;

    // builds asynchronous database once you put something in that will trigger building it
    static VacationDatabaseBuilder getDatabase(final Context context){
        if (INSTANCE == null){ // build db if instance is null:
            synchronized (VacationDatabaseBuilder.class){
                if (INSTANCE == null){ // build instance if null:
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), VacationDatabaseBuilder.class, "MyVacationDatabase.db")
                            .fallbackToDestructiveMigration()
                            //.allowMainThreadQueries() including this line would create synchronous database instead
                            .build();
                }
            }
        }
        return INSTANCE;

    }
}
