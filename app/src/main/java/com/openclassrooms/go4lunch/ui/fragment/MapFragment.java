package com.openclassrooms.go4lunch.ui.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.api.RestauranHelper;
import com.openclassrooms.go4lunch.model.Restaurant;
import com.openclassrooms.go4lunch.ui.DetailRestoActivity;
import com.openclassrooms.go4lunch.utils.DateFormat;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.utils.GpsTracker;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private View mView;
    private String PLACEIDRESTO = "resto_place_id";

    private final static String TAG = "MapsFragment";
    private static final float DEFAULT_ZOOM = 16f;

    private ImageView mGps;
    private String today;
    private GpsTracker gpsTracker;
    private Marker myMarker;
    private GoogleMap mMap;

    public MapFragment() {}

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_maps, container, false);
        mGps = mView.findViewById(R.id.ic_gps);
        DateFormat forToday = new DateFormat();
        today = forToday.getTodayDate();
        return mView;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initMap();
    }

    private void initMap() {
        MapView mMapView;
        mMapView = mView.findViewById(R.id.map);

        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), DEFAULT_ZOOM));

        PlacesClient placesClient = Places.createClient(getActivity());

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
        if (ContextCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();
                        for (int i = 0; i < likelyPlaces.getPlaceLikelihoods().size(); i++) {
                            PlaceLikelihood place = likelyPlaces.getPlaceLikelihoods().get(i);
                            String restoName = place.getPlace().getName();
                            String restoPlaceId = place.getPlace().getId();

                            final MarkerOptions markerOptions = new MarkerOptions();

                            RestauranHelper.getRestaurant(place.getPlace().getId()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()){
                                        Restaurant resto = documentSnapshot.toObject(Restaurant.class);
                                        Date dateRestoSheet;
                                        if (resto != null) {
                                            dateRestoSheet = resto.getDateCreated();
                                            DateFormat myDate = new DateFormat();
                                            String dateRegistered = myDate.getRegisteredDate(dateRestoSheet);

                                            if (dateRegistered.equals(today)) {
                                                int nbreUsers = resto.getClientsTodayList().size();
                                                if (nbreUsers > 0) {
                                                    markerOptions.position(place.getPlace().getLatLng())
                                                            .title(restoName)
                                                            .snippet(restoPlaceId)
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                                    myMarker = mMap.addMarker(markerOptions);
                                                    myMarker.setTag(0);
                                                }
                                            }
                                        }
                                    }
                                }
                            });

                            markerOptions.position(place.getPlace().getLatLng())
                                    .title(restoName)
                                    .snippet(restoPlaceId)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            myMarker = mMap.addMarker(markerOptions);
                            myMarker.setTag(0);

                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                   launchRestaurantDetail(marker.getSnippet());
                                }
                            });
                        }

                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        }
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            Objects.requireNonNull(getContext()), R.raw.style_map));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        init();
    }

    private GpsTracker getLocation(){
        gpsTracker = new GpsTracker(getContext());
        if(gpsTracker.canGetLocation()){
        }else{
            gpsTracker.showSettingsAlert();
        }
        return gpsTracker;
    }

    private void init() {
        // click on gps
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), DEFAULT_ZOOM));
            }
        });
    }

    //--------------------------------------------------------------------------------------------------------------------
    //manages the click on the info bubble
    //--------------------------------------------------------------------------------------------------------------------
    private void launchRestaurantDetail(String id) {
        Intent WVIntent = new Intent(getContext(), DetailRestoActivity.class);
        WVIntent.putExtra(PLACEIDRESTO, id);
        startActivity(WVIntent);
    }
}