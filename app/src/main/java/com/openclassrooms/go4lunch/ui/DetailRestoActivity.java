package com.openclassrooms.go4lunch.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.RestauranHelper;
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.model.Restaurant;
import com.openclassrooms.go4lunch.model.User;
import com.openclassrooms.go4lunch.utils.DateFormat;
import com.openclassrooms.go4lunch.utils.Rate;
import com.openclassrooms.go4lunch.view.ListOfClientsAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class DetailRestoActivity  extends AppCompatActivity {

    private String WEB = "resto_web";
    private String PLACEIDRESTO = "resto_place_id";
    private String restoToday;
    private List<String> listRestoLike= new ArrayList<>();
    private String restoAddress;
    private TextView nameTV;
    private TextView addressTV;
    private ImageView photoIV;
    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private LinearLayout toPhone;
    private LinearLayout toWebsite;
    private ImageView likeThisResto;
    private LinearLayout likeThisRestoLinear;
    private FloatingActionButton myRestoTodayBtn;

    private String restoTel;
    private String placeidResto;

    private String userId;
    private String restoName;
    private String lastRestoId;
    private String lastRestoDate;
    private String lastRestoName;
    private String today;

    private final static String TAG = "DETAILRESTOACTIVITY";

    private static final int REQUEST_CALL = 1;

    private RecyclerView recyclerView;
    private Context context;
    private ListOfClientsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_resto);

        context = this;
        DateFormat forToday = new DateFormat();
        today = forToday.getTodayDate();
        userId = UserHelper.getCurrentUserId();
        placeidResto = getIntent().getStringExtra(PLACEIDRESTO);

        recyclerView = findViewById(R.id.fragment_workmates_detailresto_recyclerview);
        setupRecyclerView();

        PlacesClient placesClient = Places.createClient(this);

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG,Place.Field.RATING,Place.Field.PHOTO_METADATAS,Place.Field.WEBSITE_URI,Place.Field.PHONE_NUMBER);
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeidResto, placeFields);

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                Place mResto = response.getPlace();
                restoName = mResto.getName();
                restoAddress = mResto.getAddress();
                nameTV = findViewById(R.id.name_detail);
                nameTV.setText(restoName);
                addressTV = findViewById(R.id.address_detail);
                addressTV.setText(restoAddress);
                star1 =  findViewById(R.id.star1_detail);
                star2 =  findViewById(R.id.star2_detail);
                star3 =  findViewById(R.id.star3_detail);

                if (mResto.getRating() != null){
                double restoRate = mResto.getRating();
                Rate myRate = new Rate(restoRate, star1, star2, star3);
                }
                else {
                    Rate myRate = new Rate(0, star1, star2, star3);
                }

                photoIV =  findViewById(R.id.photo_detail);
                        final List<PhotoMetadata> metadata = mResto.getPhotoMetadatas();
                        if (metadata == null || metadata.isEmpty()) {
                            Log.w(TAG, "No photo metadata.");
                            photoIV.setImageResource(R.drawable.buffet);
                            return;
                        }
                        final PhotoMetadata photoMetadata = metadata.get(0);
                        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                .build();
                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                            Bitmap bitmap = fetchPhotoResponse.getBitmap();
                            if (bitmap != null){
                                photoIV.setImageBitmap(bitmap);}
                            else
                                photoIV.setImageResource(R.drawable.buffet);

                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                final ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + exception.getMessage());
                                final int statusCode = apiException.getStatusCode();
                                photoIV.setImageResource(R.drawable.buffet);
                            }
                        });
                restoTel = mResto.getPhoneNumber();
                toPhone = findViewById(R.id.phone_detail_button);
                toPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makePhoneCall();
                    }
                });

                final String restoWebsite = String.valueOf(mResto.getWebsiteUri());
                toWebsite = findViewById(R.id.website_detail_button);
                toWebsite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (restoWebsite.equals("no-website")) {
                            Toast.makeText(DetailRestoActivity.this, R.string.no_website, Toast.LENGTH_LONG).show();
                        } else {
                            Intent WVIntent = new Intent(DetailRestoActivity.this, WebViewActivity.class);
                            WVIntent.putExtra(WEB, restoWebsite);
                            startActivity(WVIntent);
                        }
                    }
                });
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                    // TODO: Handle error with given status code.
                }
            });
    }
        likeThisRestoLinear =  findViewById(R.id.like_detail_button_linear);
        // update view
        updateLikeView(placeidResto);
        likeThisRestoLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update Firestore
                updateLikeInFirebase(placeidResto);
            }
        });

        myRestoTodayBtn = findViewById(R.id.restoToday_FloatingButton);
        updateTodayView(placeidResto);
        myRestoTodayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRestoTodayInFirebase(placeidResto, restoName);
            }
        });
    }

    private void makePhoneCall(){
        if (restoTel.trim().length()>0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:"+restoTel;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(this, R.string.no_phone_number, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CALL) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this,R.string.no_permission_for_call, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateLikeInFirebase(final String idResto) {
        Log.d(TAG, "updateLikeInFirebase: idresto " +idResto);
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "onSuccess: documentSnapshot exists");
                    listRestoLike = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestoLike();
                    if (listRestoLike != null) {
                        if (listRestoLike.contains(idResto)) {
                            Log.d(TAG, "onSuccess: retirer le resto");
                            listRestoLike.remove(idResto);
                            likeThisResto.setImageResource(R.drawable.ic_action_star_no);
                        } else {
                            Log.d(TAG, "onSuccess: ajouter le resto");
                            listRestoLike.add(idResto);
                            likeThisResto.setImageResource(R.drawable.ic_action_star);
                        }
                    }
                    UserHelper.updateLikedResto(listRestoLike, userId);
                }
            }
        });
    }

    private void updateRestoInUser(String id, String name, String date ) {
        UserHelper.updateTodayResto(id, userId);
        UserHelper.updateTodayRestoName(name, userId);
        UserHelper.updateRestoDate(date, userId);
    }

    private void removeUserInRestaurant(final String id, final String name){
        RestauranHelper.getRestaurant(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);
                    Date dateRestoSheet;
                    if (usersToday != null) {
                        dateRestoSheet = usersToday.getDateCreated();

                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestoSheet);

                        if (dateRegistered.equals(today)) {
                            List<String> listUsersToday = new ArrayList<>();
                            listUsersToday = usersToday.getClientsTodayList();
                            listUsersToday.remove(userId);
                            RestauranHelper.updateClientsTodayList(listUsersToday, id);
                        } else {
                            RestauranHelper.createRestaurant(id, name, restoAddress);
                        }
                    }
                }
            }
        });
    }

    private void addUserInRestaurant(final String id, final String name) {
        RestauranHelper.getRestaurant(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);

                    Date dateRestoSheet;
                    if (usersToday != null) {
                        dateRestoSheet = usersToday.getDateCreated();
                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestoSheet);
                        if (dateRegistered.equals(today)) {
                            List<String> listUsersToday = new ArrayList<>();
                            listUsersToday = usersToday.getClientsTodayList();
                            listUsersToday.add(userId);
                            RestauranHelper.updateClientsTodayList(listUsersToday, id);
                        }else {
                            RestauranHelper.createRestaurant(id, name, restoAddress);
                            updateUserTodayInFirebase(userId, id);
                        }
                    }
                } else {
                    RestauranHelper.createRestaurant(id, name, restoAddress);
                    updateUserTodayInFirebase(userId, id);
                }
            }
        });
    }

    private void updateUserTodayInFirebase(String myId, String myRestoId) {
        List<String> listUsersToday = new ArrayList<>();
        listUsersToday.add(myId);
        RestauranHelper.updateClientsTodayList(listUsersToday, myRestoId);
    }

    private void  updateRestoTodayInFirebase(final String restoChoiceId, final String restoChoiceName) {
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User myRestoToday = documentSnapshot.toObject(User.class);
                    if (myRestoToday != null) {
                        lastRestoId = myRestoToday.getRestoToday();
                        lastRestoDate = myRestoToday.getRestoDate();
                        lastRestoName = myRestoToday.getRestoTodayName();

                        if (lastRestoId != null && lastRestoId.length() > 0 && lastRestoDate.equals(today)) {
                            if (lastRestoId.equals(restoChoiceId)) {
                                myRestoTodayBtn.setImageResource(R.drawable.ic_validation_no);
                                updateRestoInUser("", "", today);
                                // This user is also removed from Restaurant from the guest list
                                removeUserInRestaurant(restoChoiceId, restoChoiceName);
                            } else {
                                // It was not this one so we replace it with the new choice in User
                                myRestoTodayBtn.setImageResource(R.drawable.ic_validation);
                                updateRestoInUser(restoChoiceId, restoChoiceName, today);
                                // We delete the user from the list of guests of his former restaurant chosen
                                removeUserInRestaurant(lastRestoId, lastRestoName);
                                // and we add the user in the list of guests of the new restaurant
                                addUserInRestaurant(restoChoiceId, restoChoiceName);
                            }
                        } else {
                            // No restaurant was registered, so we save this one in User
                            updateRestoInUser(restoChoiceId, restoChoiceName, today);
                            myRestoTodayBtn.setImageResource(R.drawable.ic_validation);
                            // and we add this guest to the restaurant list
                            addUserInRestaurant(restoChoiceId, restoChoiceName);
                        }
                    }
                }
            }
        });
    }

    private void updateLikeView(String id) {
        final String idLike=id;
        likeThisResto =  findViewById(R.id.like_detail_button);
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                listRestoLike = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestoLike();
                if(listRestoLike!=null) {
                    if (listRestoLike.contains(idLike)) {
                        likeThisResto.setImageResource(R.drawable.ic_action_star);
                    } else {
                        likeThisResto.setImageResource(R.drawable.ic_action_star_no);
                    }
                } else {
                    likeThisResto.setImageResource(R.drawable.ic_action_star_no);
                }
            }
        });
    }

    private void updateTodayView(String id) {
        final String idToday = id;

        // Default values
        myRestoTodayBtn.setImageResource(R.drawable.ic_validation_no);

        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                restoToday = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestoToday();
                lastRestoDate = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestoDate();

                if (restoToday != null && restoToday.length()>0&&lastRestoDate.equals(today)) { // We check that there is a restaurant registered and that it was registered today
                    if (restoToday.equals(idToday)) {
                        myRestoTodayBtn.setImageResource(R.drawable.ic_validation);
                    }
                }
            }
        });
    }

    private void setupRecyclerView() {
        RestauranHelper.getRestaurant(placeidResto).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);

                    Date dateRestoSheet;
                    if (usersToday != null) {
                        dateRestoSheet = usersToday.getDateCreated();
                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestoSheet);

                        if (dateRegistered.equals(today)) {
                            List<String> listId = usersToday.getClientsTodayList();

                            if (listId != null) {
                                adapter = new ListOfClientsAdapter(listId, Glide.with(recyclerView), listId.size());
                                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                }
            }
        });
    }
}