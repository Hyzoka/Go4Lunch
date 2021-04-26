package com.openclassrooms.go4lunch.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.RestauranHelper;
import com.openclassrooms.go4lunch.model.Restaurant;
import com.openclassrooms.go4lunch.utils.DateFormat;
import com.openclassrooms.go4lunch.utils.GpsTracker;
import com.openclassrooms.go4lunch.utils.Rate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ListOfRestaurantsAdapter extends RecyclerView.Adapter<ListOfRestaurantsAdapter.ViewHolder> {

    private static final String TAG = "OPENINGHOURS";
    private String today;
    private Context context;
    private  List<PlaceLikelihood> list;
    private RequestManager glide;
    private boolean textOK = false;
    private GpsTracker gpsTracker;
    private OnRestaurantItemClickListener mListener;

    public ListOfRestaurantsAdapter(Context context,  List<PlaceLikelihood> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_view_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlaceLikelihood place = list.get(position);

        holder.restoName.setText(place.getPlace().getName());

        holder.restoAddress.setText(place.getPlace().getAddress());

        // Opening hours
        holder.restoOpenHour.setTextColor(holder.restoOpenHour.getResources().getColor(R.color.colorMyGrey));
        if(place.getPlace().getOpeningHours()!= null) {
            // default value that will be overwritten with today's schedules if the restaurant is open today
            isRestaurantOpen(place.getPlace().getOpeningHours(), holder);
            textOK = false;
        } else {
            holder.restoOpenHour.setText(R.string.no_hours);
        }

        // //Distance
       float distance;
       float results[] = new float[10];
       double restoLat = Objects.requireNonNull(place.getPlace().getLatLng()).latitude;
       double restoLng = Objects.requireNonNull(place.getPlace().getLatLng()).longitude;
       double myLatitude = getLocation().getLatitude();
       double myLongitude = getLocation().getLongitude();
       Location.distanceBetween(myLatitude, myLongitude, restoLat, restoLng,results);
       distance = results[0];
       String dist =  Math.round(distance)+"m";
        holder.restoDistance.setText(dist);

        // Number of interested colleagues
        // Set to 0 by default
        holder.restoLovers.setText("0");
        DateFormat forToday = new DateFormat();
        today = forToday.getTodayDate();
        RestauranHelper.getRestaurant(place.getPlace().getId()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant resto = documentSnapshot.toObject(Restaurant.class);

                    // Date check
                    Date dateRestoSheet;
                    if (resto != null) {
                        dateRestoSheet = resto.getDateCreated();
                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestoSheet);
                        if (dateRegistered.equals(today)) {
                            // Number of interested colleagues
                            List<String> listUsers = resto.getClientsTodayList();
                            String textnb = String.valueOf(listUsers.size());
                            holder.restoLovers.setText(textnb);
                        }
                    }
                }
            }
        });

        // Assign the number of stars
        if (place.getPlace().getRating()!= null) {
            Double rate = place.getPlace().getRating();
            Rate myRate = new Rate(rate, holder.star1, holder.star2, holder.star3);
        } else {
            Rate myRate = new Rate(0, holder.star1, holder.star2, holder.star3);
        }

        // Images
        PlacesClient placesClient = Places.createClient(context);
        final List<PhotoMetadata> metadata = place.getPlace().getPhotoMetadatas();
        if (metadata == null || metadata.isEmpty()) {
            Log.w(TAG, "No photo metadata.");
            holder.restoPicture.setImageResource(R.drawable.buffet);
            return;
        }
        final PhotoMetadata photoMetadata = metadata.get(0);
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            if (bitmap != null){
                holder.restoPicture.setImageBitmap(bitmap);}
            else
                holder.restoPicture.setImageResource(R.drawable.buffet);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                holder.restoPicture.setImageResource(R.drawable.buffet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView restoName, restoAddress, restoOpenHour,restoDistance,restoLovers;
            ImageView restoPicture, star1, star2, star3;

        public ViewHolder(View itemView) {
            super(itemView);

            restoName = itemView.findViewById(R.id.restaurant_name);
            restoAddress = itemView.findViewById(R.id.restaurant_address);
            restoOpenHour = itemView.findViewById(R.id.restaurant_openinghours);
            restoDistance = itemView.findViewById(R.id.restaurant_proximity);
            restoLovers = itemView.findViewById(R.id.restaurant_lovers_nb);
            restoPicture = itemView.findViewById(R.id.restaurant_photo);
            star1 =  itemView.findViewById(R.id.restaurant_star1);
            star2 =  itemView.findViewById(R.id.restaurant_star2);
            star3 =  itemView.findViewById(R.id.restaurant_star3);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (mListener!=null){
                        mListener.onItemClick(position);
                    }
                }
            });
        }
    }
    public interface OnRestaurantItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnRestaurantItemClickListener listener) {
        this.mListener = listener;
    }



    private GpsTracker getLocation(){
        gpsTracker = new GpsTracker(context);
        if(gpsTracker.canGetLocation()){
        }else{
            gpsTracker.showSettingsAlert();
        }
        return gpsTracker;
    }

    private void isRestaurantOpen(OpeningHours restaurantDetail,ViewHolder holder) {
        Calendar calendar = Calendar.getInstance();
        holder.restoOpenHour.setTextColor( holder.restoOpenHour.getResources().getColor(R.color.colorMyGrey));
        holder.restoOpenHour.setText( holder.restoOpenHour.getResources().getString(R.string.closed_today));

        for(Period period: restaurantDetail.getPeriods()){
            if(period.getClose() == null) {
                holder.restoOpenHour.setText( holder.restoOpenHour.getResources().getString(R.string.always_open));
            } else {
                String text;
                String textTime;
                    //textOK allows you to manage cases where there are several opening hours for the same day
                    DateFormat hour = new DateFormat();
                    switch (getOpeningHour(period)) {
                        case 1:
                            holder.restoOpenHour.setTextColor(   holder.restoOpenHour.getResources().getColor(R.color.colorPrimary));
                            text =    holder.restoOpenHour.getResources().getString(R.string.open_at);
                            textTime = hour.getHoursFormat(String.valueOf(period.getOpen().getTime()));
                            text+=textTime;
                            holder.restoOpenHour.setText(text);

                            break;
                        case 2:
                            holder.restoOpenHour.setTextColor(   holder.restoOpenHour.getResources().getColor(R.color.colorMyGreen));
                            text =    holder.restoOpenHour.getResources().getString(R.string.open_until);

                            textTime = hour.getHoursFormat(String.valueOf(period.getClose().getTime()));
                            text+=textTime;
                            holder.restoOpenHour.setText(text);

                            break;
                        case 3:
                            holder.restoOpenHour.setTextColor(   holder.restoOpenHour.getResources().getColor(R.color.colorMyGrey));
                            holder.restoOpenHour.setText(   holder.restoOpenHour.getResources().getString(R.string.closed));
                    }
            }
        }
    }

    // Method that get opening hours from GooglePlaces
    private int getOpeningHour(Period period){

        Calendar calendar = Calendar.getInstance();
        int currentHour = 1200;
        if (calendar.get(Calendar.MINUTE)<10) {
            currentHour = Integer.parseInt("" + calendar.get(Calendar.HOUR_OF_DAY) + "0" +calendar.get(Calendar.MINUTE));
        } else {
            currentHour = Integer.parseInt("" + calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE));
        }
        int closureHour = Integer.parseInt(String.valueOf(period.getClose().getTime()));
        int openHour = Integer.parseInt(String.valueOf(period.getOpen().getTime()));

        Log.d(TAG, "getOpeningHour: currenthour " +currentHour);
        if (currentHour<openHour) {
            textOK = true; // We are earlier than the first schedule so do not go compare with the second
            return 1;
        }
        else if (currentHour>openHour&&currentHour<closureHour) {
            textOK = true; // We are in the first time slot so do not go compare with the second
            return 2;
        }
        else return 3;
    }
}