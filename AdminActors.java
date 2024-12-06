package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.maintenanceapp.Adapter.ActorAdapter;
import com.example.maintenanceapp.Models.SignUpModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminActors extends AppCompatActivity {

    TextView txtNotFound;
    RecyclerView recyclerView;

    ActorAdapter adapter;

    ArrayList<SignUpModel> arrayList = new ArrayList<>();

    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_actors);

        txtNotFound = findViewById(R.id.actorNotFound);
        recyclerView = findViewById(R.id.actorRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(AdminActors.this, LinearLayoutManager.VERTICAL, false));
        adapter = new ActorAdapter(arrayList, AdminActors.this);
        Intent intent = getIntent();
        String value = intent.getExtras().getString("actorKey");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    int counter = 0;
                    if (value.equals("Customers")) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            SignUpModel signUpModel = dataSnapshot.getValue(SignUpModel.class);
                            if (signUpModel.getRole().equals("customer")) {
                                arrayList.add(signUpModel);
                                counter++;

                            }
                        }
                    } else if (value.equals("Provider")) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            SignUpModel signUpModel = dataSnapshot.getValue(SignUpModel.class);
                            if (signUpModel.getRole().equals("service_provider")) {
                                arrayList.add(signUpModel);
                                counter++;
                            }
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

    }
}