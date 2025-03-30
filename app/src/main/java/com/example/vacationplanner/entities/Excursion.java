package com.example.vacationplanner.entities;

import com.google.firebase.firestore.DocumentId;

import java.util.UUID;

public class Excursion {
    private String excursionID;
    private String excursionTitle;
    private String excursionDate;
    private String vacationID; // Changed from int to String for Firestore compatibility
    private boolean notify;

    public Excursion() {
    }

    public Excursion(String excursionID, String excursionTitle, String excursionDate, String vacationID) {
        this.excursionID = excursionID;
        this.excursionTitle = excursionTitle;
        this.excursionDate = excursionDate;
        this.vacationID = vacationID;
        this.notify = false;
    }

    // Full constructor
    public Excursion(String excursionID, String excursionTitle, String excursionDate, String vacationID, boolean notify) {
        this.excursionID = excursionID;
        this.excursionTitle = excursionTitle;
        this.excursionDate = excursionDate;
        this.vacationID = vacationID;
        this.notify = notify;
    }

    public String getExcursionID() {
        return excursionID;
    }

    public void setExcursionID(String excursionID) {
        this.excursionID = excursionID;
    }

    public String getExcursionTitle() {
        return excursionTitle;
    }

    public void setExcursionTitle(String excursionTitle) {
        this.excursionTitle = excursionTitle;
    }

    public String getExcursionDate() {
        return excursionDate;
    }

    public void setExcursionDate(String excursionDate) {
        this.excursionDate = excursionDate;
    }

    public String getVacationID() {
        return vacationID;
    }

    public void setVacationID(String vacationID) {
        this.vacationID = vacationID;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}