package com.example.vacationplanner.database;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.vacationplanner.entities.Vacation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class VacationRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final String COLLECTION_NAME = "vacations";
    private static final String TAG = "VacationRepository";

    public VacationRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // method to get current user ID
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Get all vacations
    public LiveData<List<Vacation>> getAllVacations() {
        MutableLiveData<List<Vacation>> vacationsLiveData = new MutableLiveData<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            vacationsLiveData.setValue(new ArrayList<>());
            return vacationsLiveData;
        }

        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting vacations", error);
                        return;
                    }

                    List<Vacation> vacationList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Vacation vacation = document.toObject(Vacation.class);
                            vacationList.add(vacation);
                        }
                    }
                    vacationsLiveData.setValue(vacationList);
                });
        return vacationsLiveData;
    }

    public LiveData<List<Vacation>> getAllVacationsForUser(String userId) {
        MutableLiveData<List<Vacation>> vacationsLiveData = new MutableLiveData<>();

        if (userId == null) {
            vacationsLiveData.setValue(new ArrayList<>());
            return vacationsLiveData;
        }

        // similar to getAllVacations except here we use the userId parameter instead of current user
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting vacations for user: " + userId, error);
                        return;
                    }

                    List<Vacation> vacationList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Vacation vacation = document.toObject(Vacation.class);
                            vacationList.add(vacation);
                        }
                    }
                    vacationsLiveData.setValue(vacationList);
                });
        return vacationsLiveData;
    }

    // insert new vacation
    public void insert(Vacation vacation) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return;
        }

        // set user ID
        vacation.setUserId(userId);

        // add to Firestore
        db.collection(COLLECTION_NAME)
                .add(vacation)
                .addOnSuccessListener(documentReference -> {
                    // set  firestore document ID as the vacation ID so there isn't a mismatch when adding excursions later
                    String id = documentReference.getId();
                    documentReference.update("vacationID", id);

                    // update vacation object with new ID
                    vacation.setVacationID(id);

                    Log.d(TAG, "Vacation added with ID: " + id);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error adding vacation", e));
    }

    // update existing vacation
    public void update(Vacation vacation) {
        String userId = getCurrentUserId();
        if (userId == null || !userId.equals(vacation.getUserId())) {
            return;
        }

        Log.d(TAG, "Attempting to update vacation with ID: " + vacation.getVacationID());
        Log.d(TAG, "Vacation title= " + vacation.getTitle() +
                ", userId= " + vacation.getUserId());

        db.collection(COLLECTION_NAME)
                .document(vacation.getVacationID())
                .set(vacation)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Vacation updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating vacation", e);
                });
    }

    // delete vacation
    public void delete(Vacation vacation) {
        String userId = getCurrentUserId();
        Log.d(TAG, "Current user ID: " + userId);
        Log.d(TAG, "Vacation user ID: " + vacation.getUserId());
        Log.d(TAG, "Attempting to delete vacation with ID: " + vacation.getVacationID());

        if (userId == null) {
            Log.e(TAG, "Delete failed: User not logged in");
            return;
        }

        if (!userId.equals(vacation.getUserId())) {
            Log.e(TAG, "Delete failed: User ID mismatch - current: " + userId + ", vacation: " + vacation.getUserId());
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(vacation.getVacationID())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Vacation deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting vacation", e));
    }

    // get vacation by ID
    public LiveData<Vacation> getVacationById(String vacationId) {
        MutableLiveData<Vacation> vacationLiveData = new MutableLiveData<>();

        db.collection(COLLECTION_NAME)
                .document(vacationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Vacation vacation = documentSnapshot.toObject(Vacation.class);
                        vacationLiveData.setValue(vacation);
                    } else {
                        vacationLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting vacation", e);
                    vacationLiveData.setValue(null);
                });

        return vacationLiveData;
    }

    // helper method to delete a vacation by vacationID
    public void deleteById(String vacationID) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Log.d(TAG, "Attempting to delete vacation by ID: " + vacationID);

        // get vacation by id to check it is for the right user
        db.collection(COLLECTION_NAME)
                .document(vacationID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String docUserId = documentSnapshot.getString("userId");

                        if (userId.equals(docUserId) || docUserId == null) {
                            // vacation belongs to user or it has no user ID so we can delete it
                            documentSnapshot.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Vacation deleted successfully"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting vacation", e));
                        } else {
                            Log.e(TAG, "Delete failed: User ID mismatch");
                        }
                    } else {
                        Log.e(TAG, "Delete failed: Vacation not found");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving vacation", e));
    }
}