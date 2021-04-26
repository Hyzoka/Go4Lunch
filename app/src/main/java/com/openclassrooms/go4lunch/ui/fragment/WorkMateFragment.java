package com.openclassrooms.go4lunch.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.model.User;
import com.openclassrooms.go4lunch.ui.DetailRestoActivity;
import com.openclassrooms.go4lunch.view.ListOfWorkmatesAdapter;

public class WorkMateFragment extends Fragment {

    private ListOfWorkmatesAdapter adapter;
    private RecyclerView recyclerView;
    private String PLACEIDRESTO = "resto_place_id";

    public WorkMateFragment() {}

    public static WorkMateFragment newInstance(String param1, String param2) {
        WorkMateFragment fragment = new WorkMateFragment();
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

        View view;
        view =  inflater.inflate(R.layout.fragment_work_mate, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_work_mate);

        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {

        Query allUsers= UserHelper.getAllUsers();

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(allUsers, User.class)
                .build();

        adapter = new ListOfWorkmatesAdapter(options, Glide.with(recyclerView));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ListOfWorkmatesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    String restoId;
                    if (user != null) {
                        restoId = user.getRestoToday();
                        if (restoId.length() > 1) {
                            Intent WVIntent = new Intent(getContext(), DetailRestoActivity.class);
                            WVIntent.putExtra(PLACEIDRESTO, restoId);
                            startActivity(WVIntent);
                        }else
                            Toast.makeText(getContext(), R.string.no_lunch, Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
