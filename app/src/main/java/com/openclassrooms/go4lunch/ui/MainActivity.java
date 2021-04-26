package com.openclassrooms.go4lunch.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.BuildConfig;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.login.LoginActivity;
import com.openclassrooms.go4lunch.model.User;
import com.openclassrooms.go4lunch.ui.fragment.ListViewFragment;
import com.openclassrooms.go4lunch.ui.fragment.MapFragment;
import com.openclassrooms.go4lunch.ui.fragment.WorkMateFragment;
import com.openclassrooms.go4lunch.utils.GpsTracker;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private boolean mLocationPermissionGranted = false;
    private Context mContext;
    private String PLACEIDRESTO = "resto_place_id";
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 13;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private TextView nameTextView,emailTextView;
    private ImageView photoImageView;
    private GpsTracker gpsTracker;

    final MapFragment fragmentMap = new MapFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        layoutLinks();
        updateUIWhenCreating();
        loadFragment(new MapFragment());

        BottomNavigationView bottomView = findViewById(R.id.bottom_navigation);
        bottomView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mContext = this;

        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.PLACE_API);
        getLocationPermission();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.action_map:
                    loadFragment(fragmentMap);
                    return true;
                case R.id.action_list:
                    fragment = new ListViewFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.action_workmate:
                    fragment = new WorkMateFragment();
                    loadFragment(fragment);
                    return true;
            }

            return false;
        }
    };

    protected void layoutLinks() {
        nameTextView = navigationView.getHeaderView(0).findViewById(R.id.ND_name_textView);
        emailTextView = navigationView.getHeaderView(0).findViewById(R.id.ND_email_textView);
        photoImageView = navigationView.getHeaderView(0).findViewById(R.id.ND_photo_imageView);
    }

    //  Update UI when activity is creating
    private void updateUIWhenCreating(){
        if (getCurrentUser() != null) {
            //Get picture URL from Firebase
            if (getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(photoImageView);
            }
        }
            String email = TextUtils.isEmpty(UserHelper.getCurrentUserEmail()) ? getString(R.string.info_no_email_found) : UserHelper.getCurrentUserEmail();
            String username = TextUtils.isEmpty(UserHelper.getCurrentUserName()) ? getString(R.string.info_no_username_found) : UserHelper.getCurrentUserName();
        //Update views with data
            this.nameTextView.setText(username);
            this.emailTextView.setText(email);
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_main_frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.detach(fragment);
        transaction.attach(fragment);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mylunch) {
            startDetailActivity();

        } else if (id == R.id.nav_settings) {
            startSettingsActivity();

        }
        else if (id == R.id.nav_logout) {
            signOutUserFromFirebase();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startDetailActivity(){
        String userId=  UserHelper.getCurrentUserId();
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User myUser = documentSnapshot.toObject(User.class);
                String lunch;
                if (myUser != null) {
                    lunch = myUser.getRestoToday();
                    if (lunch.equals("")) {
                        Toast.makeText(mContext, R.string.no_lunch, Toast.LENGTH_LONG).show();
                    } else {
                        Intent WVIntent = new Intent(mContext, DetailRestoActivity.class);
                        WVIntent.putExtra(PLACEIDRESTO, lunch);
                        startActivity(WVIntent);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar menu
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    // Configure the click on each item of the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_activity_main_search:

                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
                // Define the region
                 RectangularBounds bounds = RectangularBounds.newInstance(
                         new LatLng(getLocation().getLatitude()-0.01, getLocation().getLongitude()-0.01),
                         new LatLng(getLocation().getLatitude()+0.01, getLocation().getLongitude()+0.01));
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationBias(bounds)
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                 Intent intent = new Intent(this, DetailRestoActivity.class);
                 intent.putExtra(PLACEIDRESTO, place.getId());
                 startActivity(intent);
                Log.i("PLACE", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("STATUS", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void startSettingsActivity() {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------------------------------------------------
    // Verify permissions
    //----------------------------------------------------------------------------------------------------------------------------
    public void getLocationPermission() {
        FusedLocationProviderClient mFusedLocationProviderClient;
        //getLocationPermission: getting location permissions
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // onRequestPermissionsResult: called
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            // onRequestPermissionsResult: permissions failed
                            return;
                        } else {
                            // onRequestPermissionsResult: Permissions granted
                            mLocationPermissionGranted = true;
                            getLocationPermission();
                        }
                    }
                }
            }
        }
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this);
        UserHelper.deleteUser(getCurrentUser().getUid());
        startLoginActivity();
    }

    private void startLoginActivity() {
        getCurrentUser().delete();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("logOut",true);
        startActivity(intent);
    }

    public GpsTracker getLocation(){
        gpsTracker = new GpsTracker(this);
        if(gpsTracker.canGetLocation()){
        }else{
            gpsTracker.showSettingsAlert();
        }
        return gpsTracker;
    }

        @Nullable
        protected FirebaseUser getCurrentUser(){
            return FirebaseAuth.getInstance().getCurrentUser(); }

}