package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.maintenanceapp.Adapter.RejectedOrdersAdapter;
import com.example.maintenanceapp.Models.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Accepted_or_Complete extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView txtNotFound;
    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    ArrayList<OrderModel> arrayList = new ArrayList<>();
    RejectedOrdersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_or_complete);
        recyclerView = findViewById(R.id.orderRecycler);
        txtNotFound = findViewById(R.id.txtNotInProcess);

        recyclerView.setLayoutManager(new LinearLayoutManager(Accepted_or_Complete.this, LinearLayoutManager.VERTICAL, false));
        adapter = new RejectedOrdersAdapter(arrayList, Accepted_or_Complete.this);

        Intent intent = getIntent();
        String value = intent.getExtras().getString("requestKey");

        orderReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel orderModel = dataSnapshot.getValue(OrderModel.class);
                        if (orderModel.getStatus().equals(value)) {
                            arrayList.add(orderModel);
                            count++;
                        }
                    }
                    if (count == 0) {
                        txtNotFound.setVisibility(View.VISIBLE);
                        txtNotFound.setText("No "+value+" task found");
                    }
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                else {
                    txtNotFound.setVisibility(View.VISIBLE);
                    txtNotFound.setText("No "+value+" task found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}