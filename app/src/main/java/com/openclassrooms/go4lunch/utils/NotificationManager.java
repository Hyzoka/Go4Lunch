package com.openclassrooms.go4lunch.utils;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.RestauranHelper;
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.login.LoginActivity;
import com.openclassrooms.go4lunch.model.Restaurant;
import com.openclassrooms.go4lunch.model.User;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class NotificationManager extends FirebaseMessagingService{

    private static final String TAG = "NotificationsService";
    private final int NOTIFICATION_ID = 0;
    private final String NOTIFICATION_TAG = "FIREBASEOC";
    public static final String SHARED_PREFS = "SharedPrefsPerso";
    public static final String NOTIF_PREFS = "notifications";


    private String userId;
    private String restoTodayId;
    private String restoTodayName;
    private String restoTodayAddress;
    private List<String> listUserId = new ArrayList<>();
    private String listNames="";
    private boolean notifOk;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        notifOk = sharedPreferences.getBoolean(NOTIF_PREFS, true);
        userId = UserHelper.getCurrentUserId();

        // We look if the user wants to receive notifications
        checkIfNotifToday();
    }

    private void checkIfNotifToday() {
        Log.d(TAG, "checkIfNotifToday");
        DateFormat forToday = new DateFormat();
        final String today = forToday.getTodayDate();

        // We check that the user has selected a restaurant for today
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String myRestoToday;
                if (user != null) {
                    myRestoToday = user.getRestoToday();
                    String registeredDate = user.getRestoDate();
                        if (!myRestoToday.isEmpty() && registeredDate.equals(today) && notifOk) {
                                showNotification();
                    }
                }
            }
        });
    }

    private void showNotification() {

        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    restoTodayId=user.getRestoToday();
                }

                RestauranHelper.getRestaurant(restoTodayId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Restaurant resto = documentSnapshot.toObject((Restaurant.class));
                        if (resto != null) {
                            restoTodayName = resto.getRestoName();
                            restoTodayAddress = resto.getAddress();
                            // I retrieve the list of colleagues who have chosen this restaurant
                            listUserId = resto.getClientsTodayList();
                        }

                        for (int i=0; i<listUserId.size(); i++) {
                            UserHelper.getUser(listUserId.get(i)).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User user = documentSnapshot.toObject(User.class);
                                    String name = null;
                                    if (user != null) {
                                        name = user.getUsername();
                                    }
                                    listNames+= name +", ";

                                    String restoName = getResources().getString(R.string.notif_message1) + " " + restoTodayName;
                                    String address = restoTodayAddress;
                                    String colleagues = listNames;
                                    if(colleagues.endsWith(", ")) colleagues = colleagues.substring(0, colleagues.length() - 2);

                                    sendVisualNotification(restoName,address, colleagues);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void sendVisualNotification(String restoName, String address, String colleagues) {
        Log.d(TAG, "sendVisualNotification");
        //  Create an Intent that will be shown when user will click on the Notification
        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //  Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine(restoName);
        inboxStyle.addLine(address);
        inboxStyle.addLine(getResources().getString(R.string.notif_message2));
        inboxStyle.addLine(colleagues);

        //  Create a Channel (Android 8)
        String channelId = getString(R.string.default_notification_channel_id);

        //  Build a Notification object
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(restoName)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        //  Add the Notification to the Notification Manager and show it.
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //  Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Firebase Message";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        //  Show notification
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

}
