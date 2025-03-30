package com.example.vacationplanner.entities;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import java.util.UUID;


public class Vacation {
    //@DocumentId
    private String vacationID;
    private String userID;
    private String title;
    private String hotelName;
    private String startDate;
    private String endDate;
    private boolean notifyStart;
    private boolean notifyEnd;


    public Vacation(){

    }

    public Vacation(String vacationID, String title, String hotelName, String startDate, String endDate) {
        this.vacationID = vacationID;
        this.title = title;
        this.hotelName = hotelName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Vacation(String vacationID, String userID, String title, String hotelName, String startDate, String endDate, boolean notifyStart, boolean notifyEnd) {
        this.vacationID = vacationID;
        this.userID = userID;
        this.title = title;
        this.hotelName = hotelName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notifyStart = notifyStart;
        this.notifyEnd = notifyEnd;
    }

    public String getVacationID() {
        return vacationID;
    }

    public void setVacationID(String vacationID) {
        this.vacationID = vacationID;
    }

    public String getUserId() {
        return userID;
    }
    public void setUserId(String userId) {
        this.userID = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString(){
        return title;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public boolean isNotifyStart() {
        return notifyStart;
    }

    public void setNotifyStart(boolean notifyStart) {
        this.notifyStart = notifyStart;
    }

    public boolean isNotifyEnd() {
        return notifyEnd;
    }

    public void setNotifyEnd(boolean notifyEnd) {
        this.notifyEnd = notifyEnd;
    }
}
