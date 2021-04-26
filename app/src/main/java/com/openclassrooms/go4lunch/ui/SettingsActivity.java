package com.openclassrooms.go4lunch.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.login.LoginActivity;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class SettingsActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "SharedPrefsPerso";
    public static final String NOTIF_PREFS = "notifications";
    private Switch notif;
    private SharedPreferences.Editor editor;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        notif = findViewById(R.id.switch_notifications);
        submit = (Button) findViewById(R.id.submitButton);
        notif.setChecked(sharedPreferences.getBoolean(NOTIF_PREFS, true));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String statusSwitch1, statusSwitch2;
                if (notif.isChecked()){
                    statusSwitch1 = notif.getTextOn().toString();
                notif.setChecked(sharedPreferences.getBoolean(NOTIF_PREFS, true));
                }
                else{
                    statusSwitch1 = notif.getTextOff().toString();
                    notif.setChecked(sharedPreferences.getBoolean(NOTIF_PREFS, false));

                }
                editor = sharedPreferences.edit();
                Toast.makeText(getApplicationContext(),  statusSwitch1, Toast.LENGTH_SHORT).show(); // display the current state for switch's
                finish();

            }
        });

    }
}
