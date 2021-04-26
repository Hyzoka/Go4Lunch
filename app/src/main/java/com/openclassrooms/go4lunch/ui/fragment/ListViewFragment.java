package com.openclassrooms.go4lunch.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.ui.DetailRestoActivity;
import com.openclassrooms.go4lunch.view.ListOfRestaurantsAdapter;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

public class ListViewFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private ListOfRestaurantsAdapter adapter;
    private String PLACEIDRESTO = "resto_place_id";

    public ListViewFragment() { }

    public static ListViewFragment newInstance(String param1, String param2) {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_view, container, false);
        mRecyclerView = rootView.findViewById(R.id.fragment_restaurants_recyclerview);

        PlacesClient placesClient = Places.createClient(getActivity());

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG,Place.Field.RATING,Place.Field.PHOTO_METADATAS,Place.Field.ID);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ContextCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResult = placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();
                        Log.i("CURRENT PLACE", String.valueOf(likelyPlaces.getPlaceLikelihoods()));
                        // set up the RecyclerView
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        adapter = new ListOfRestaurantsAdapter(getContext(), likelyPlaces.getPlaceLikelihoods());
                        mRecyclerView.setAdapter(adapter);
                        adapter.setOnItemClickListener(new ListOfRestaurantsAdapter.OnRestaurantItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                launchRestaurantDetail(likelyPlaces.getPlaceLikelihoods().get(position).getPlace().getId());
                            }
                        });
                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        }
        return rootView;
    }


    private void launchRestaurantDetail(String id) {
        Intent WVIntent = new Intent(getContext(), DetailRestoActivity.class);
        WVIntent.putExtra(PLACEIDRESTO, id);
        startActivity(WVIntent);
    }


}