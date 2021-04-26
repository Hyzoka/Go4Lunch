package com.openclassrooms.go4lunch.api;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.openclassrooms.go4lunch.model.User;

import java.util.List;
import java.util.Objects;

public class UserHelper {
    private static final String COLLECTION_NAME = "users";
    private static final String TAG = "USERHELPER";

    // --- COLLECTION REFERENCE ---
    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }


    // --- CREATE ---
    public static Task<Void> createUser(String uid, String username, String userEmail, String urlPicture) {
        User userToCreate = new User(uid, username, userEmail, urlPicture);
        Log.d(TAG, "createUser: " + username + " - " + userEmail);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---
    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    // --- GET CURRENT USER ID ---
    public static String getCurrentUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    // --- GET CURRENT USER NAME ---
    public static String getCurrentUserName() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
    }

    // --- GET CURRENT USER EMAIL ---
    public static String getCurrentUserEmail() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
    }
    // --- GET CURRENT USER URL PICTURE ---
    public static String getCurrentUserUrlPicture() {
        return FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
    }

    // --- UPDATE NAME---
    public static Task<Void> updateUsername(String username, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("username", username);
    }

    // --- UPDATE TODAY'S RESTO---
    public static Task<Void> updateTodayResto(String restoToday, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("restoToday", restoToday);
    }

    // --- UPDATE TODAY'S RESTO---
    public static Task<Void> updateTodayRestoName(String restoTodayName, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("restoTodayName", restoTodayName);
    }

    // --- UPDATE DATE'S RESTO---
    public static Task<Void> updateRestoDate(String restoDate, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("restoDate", restoDate);
    }

    // --- UPDATE LIKED RESTO---
    public static Task<Void> updateLikedResto(List<String> restoLike, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("restoLike", restoLike);
    }

    // --- DELETE ---
    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }

    // -- GET ALL USERS --
    public static Query getAllUsers(){
        return UserHelper.getUsersCollection();
    }
}