package com.openclassrooms.go4lunch.view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.User;
import com.openclassrooms.go4lunch.utils.DateFormat;

public class ListOfWorkmatesAdapter  extends FirestoreRecyclerAdapter<User, ListOfWorkmatesAdapter.UserHolder> {

    private RequestManager glide;
    private Context context;
    private OnItemClickListener mListener;


    public ListOfWorkmatesAdapter(@NonNull FirestoreRecyclerOptions<User> options, RequestManager glide) {
        super(options);
        this.glide = glide;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserHolder userHolder, int i, @NonNull User user) {
        DateFormat forToday = new DateFormat();
        String today = forToday.getTodayDate();
        String registeredDate = user.getRestoDate();

        // Default values
        String text;
        text = user.getUsername() + context.getString(R.string.not_decided);
        userHolder.textUser.setTypeface(null, Typeface.ITALIC);
        userHolder.textUser.setTextColor(context.getResources().getColor(R.color.colorMyGrey));

        // Specifications if a restaurant was chosen for today
        if (user.getRestoTodayName()!= null && !user.getRestoTodayName().isEmpty()) {
            if(registeredDate.equals(today)){
                text = user.getUsername() + context.getString(R.string.decided) + user.getRestoTodayName();
                userHolder.textUser.setTypeface(null, Typeface.NORMAL);
                userHolder.textUser.setTextColor(context.getResources().getColor(R.color.black));
            }
        }
        userHolder.textUser.setText(text);

        // Images
        if (user.getUrlPicture() != null && !user.getUrlPicture().isEmpty()){
            String urlPhoto = user.getUrlPicture();
            glide.load(urlPhoto)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userHolder.imageUser);
        } else {
            userHolder.imageUser.setImageResource(R.drawable.baseline_people_24);
        }
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_mate_item, parent, false);
        context = parent.getContext();
        return new UserHolder(view);
    }

    class UserHolder extends RecyclerView.ViewHolder {
        TextView textUser;
        ImageView imageUser;

        UserHolder(@NonNull View itemView) {
            super(itemView);
            textUser = itemView.findViewById(R.id.workmates_TextView);
            imageUser = itemView.findViewById(R.id.workmates_ImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (mListener!=null){
                        mListener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

}