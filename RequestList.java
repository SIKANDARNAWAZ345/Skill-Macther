package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.maintenanceapp.Adapter.RequestListAdapter;
import com.example.maintenanceapp.Models.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RequestList extends AppCompatActivity {
    RecyclerView recyclerView;

    RequestListAdapter adapter;
    ArrayList<OrderModel> arrayList = new ArrayList<>();

    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);
        recyclerView = findViewById(R.id.requestListRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(RequestList.this, LinearLayoutManager.VERTICAL, false));
        adapter = new RequestListAdapter(arrayList, RequestList.this);


        orderReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel orderModel = dataSnapshot.getValue(OrderModel.class);
                        arrayList.add(orderModel);
                    }
                }
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}