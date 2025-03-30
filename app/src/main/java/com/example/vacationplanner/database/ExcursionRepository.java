package com.example.vacationplanner.database;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.vacationplanner.entities.Excursion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExcursionRepository {
    private static final String TAG = "ExcursionRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final String COLLECTION_NAME = "excursions";

    public ExcursionRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Get all excursions for a specific vacation
    public LiveData<List<Excursion>> getAssociatedExcursions(String vacationId) {
        MutableLiveData<List<Excursion>> excursionsLiveData = new MutableLiveData<>();

        db.collection(COLLECTION_NAME)
                .whereEqualTo("vacationID", vacationId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting excursions", error);
                        return;
                    }

                    List<Excursion> excursionList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Excursion excursion = document.toObject(Excursion.class);
                            excursionList.add(excursion);
                        }
                    }
                    excursionsLiveData.setValue(excursionList);
                });

        return excursionsLiveData;
    }

    // Insert new excursion
    public void insert(Excursion excursion) {
        Log.d(TAG, "Inserting excursion: " + excursion.getExcursionTitle() +
                " with vacation ID: " + excursion.getVacationID());

        // Add to Firestore
        db.collection(COLLECTION_NAME)
                .add(excursion)
                .addOnSuccessListener(documentReference -> {
                    // Set the Firestore document ID as the excursion ID
                    String id = documentReference.getId();
                    documentReference.update("excursionID", id);
                    Log.d(TAG, "Excursion added with ID: " + id);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding excursion", e);
                });
    }

    // Update existing excursion
    public void update(Excursion excursion) {
        db.collection(COLLECTION_NAME)
                .document(excursion.getExcursionID())
                .set(excursion)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Excursion updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating excursion", e));
    }

    // Delete excursion
    public void delete(Excursion excursion) {
        db.collection(COLLECTION_NAME)
                .document(excursion.getExcursionID())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Excursion deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting excursion", e));
    }

    // Delete all excursions for a vacation
    public void deleteAssociatedExcursions(String vacationId) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("vacationID", vacationId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Log.d(TAG, "All excursions deleted for vacation: " + vacationId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting excursions for vacation", e));
    }
}