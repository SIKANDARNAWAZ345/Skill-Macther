package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.example.maintenanceapp.Adapter.ShowChatUserAdapter;
import com.example.maintenanceapp.Models.ChatModel;
import com.example.maintenanceapp.Models.SignUpModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ServicePChatUsers extends AppCompatActivity {

    RecyclerView recyclerView;


    ArrayList<SignUpModel> arrayList = new ArrayList<>();

    ShowChatUserAdapter adapter;


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference chatListReference = FirebaseDatabase.getInstance().getReference("ChatList");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_pchat_users);
        recyclerView = findViewById(R.id.servicePChatUserRecycler);


        recyclerView.setLayoutManager(new LinearLayoutManager(ServicePChatUsers.this, LinearLayoutManager.VERTICAL, false));
        adapter = new ShowChatUserAdapter(arrayList, ServicePChatUsers.this);


        chatListReference.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String receiverId = dataSnapshot.child("id").getValue(String.class);
                        assert receiverId != null;
                        userReference.child(receiverId).addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                                    assert signUpModel != null;
                                    if (!(signUpModel.getUid().equals(firebaseAuth.getCurrentUser().getUid()))) {
                                        arrayList.add(signUpModel);
                                        recyclerView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                } else {
                    Toast.makeText(ServicePChatUsers.this, "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}