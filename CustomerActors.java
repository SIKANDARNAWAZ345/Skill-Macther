package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Adapter.CustomerActorAdapter;
import com.example.maintenanceapp.Models.DistanceListModel;
import com.example.maintenanceapp.Models.SignUpModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class CustomerActors extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView txtNotFound;

    AppCompatButton btnNearMe;

    CustomerActorAdapter adapter, nearAdapter;
    ArrayList<SignUpModel> arrayList = new ArrayList<>();
    ArrayList<DistanceListModel> distanceList = new ArrayList<>();

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");

    boolean nearBy = false;
    double lat, lng;
    float distance;
    DistanceListModel distanceListModel = new DistanceListModel();

    ArrayList<SignUpModel> myList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_actors);
        recyclerView = findViewById(R.id.customerActorRecyclerView);
        txtNotFound = findViewById(R.id.customerActorNotFound);
        btnNearMe = findViewById(R.id.btnNearMe);
        Intent intent = getIntent();
        String value = intent.getExtras().getString("actorKey");

        recyclerView.setLayoutManager(new LinearLayoutManager(CustomerActors.this, LinearLayoutManager.VERTICAL, false));
        adapter = new CustomerActorAdapter(arrayList, CustomerActors.this, distanceList);

        nearAdapter = new CustomerActorAdapter(myList, CustomerActors.this, distanceList);
        recyclerView.setAdapter(adapter);
        recyclerView.setAdapter(nearAdapter);
        userReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                    lat = signUpModel.getLat();
                    lng = signUpModel.getLng();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;
//                distanceList.clear();
                if (snapshot.exists()) {
                    arrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SignUpModel model = dataSnapshot.getValue(SignUpModel.class);
                        if (model.getProfession().equals(value)) {
                            float[] result = new float[1];
                            Location.distanceBetween(lat, lng, model.getLat(), model.getLng(), result);
                            distance = result[0] / 1000;
                            float round = Math.round(distance);
                            DistanceListModel distanceListModel = new DistanceListModel(model.getUid(), round);
                            distanceList.add(distanceListModel);
                            arrayList.add(model);
                            counter++;
                        }
                    }
                    if (counter == 0) {
                        txtNotFound.setVisibility(View.VISIBLE);
                    }

                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                } else {
                    txtNotFound.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnNearMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList.clear();
                SearchNearBy();


            }
        });

    }

    private void SearchNearBy() {

        //sorting DistanceList in descending order;
        for (int i = 0; i < distanceList.size(); i++) {
            for (int j = i + 1; j < distanceList.size(); j++) {
                if (distanceList.get(i).getDistance() > distanceList.get(j).getDistance()) {
                    // Swap elements
                    distanceListModel = distanceList.get(i);
                    distanceList.set(i, distanceList.get(j));
                    distanceList.set(j, distanceListModel);
                }
            }
        }
        // after sorting it will get uid from DistanceList and show user in distance wise;

        for (int i = 0; i < distanceList.size(); i++) {
            userReference.child(distanceList.get(i).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                        myList.add(signUpModel);
                    }
                    recyclerView.setAdapter(nearAdapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CustomerActors.this, "Problem", Toast.LENGTH_SHORT).show();
                }
            });
        }

        myList.clear();

        //nearAdapter.notifyDataSetChanged();
    }

}