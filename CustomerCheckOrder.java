package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Adapter.CheckOrderAdapter;
import com.example.maintenanceapp.Models.OrderModel;
import com.example.maintenanceapp.Models.SignUpModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CustomerCheckOrder extends AppCompatActivity {

    RecyclerView recyclerView;

    CheckOrderAdapter adapter;

    ArrayList<SignUpModel> arrayList = new ArrayList<>();
    TextView txtNotFound;

    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference rejectReference = FirebaseDatabase.getInstance().getReference("Rejected");

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_check_order);
        recyclerView = findViewById(R.id.customerOrderRecyclerView);
        txtNotFound = findViewById(R.id.customerOrderNotFound);


        recyclerView.setLayoutManager(new LinearLayoutManager(CustomerCheckOrder.this, LinearLayoutManager.VERTICAL, false));
        adapter = new CheckOrderAdapter(arrayList, CustomerCheckOrder.this);

        orderReference.addValueEventListener(new ValueEventListener() {
            int count = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        for (DataSnapshot secondSnapshot : dataSnapshot.getChildren()) {
                            OrderModel orderModel = secondSnapshot.getValue(OrderModel.class);
                            if (firebaseAuth.getCurrentUser().getUid().equals(orderModel.getCustomerId())) {
                                ShowUser(orderModel.getServicePId());
                                count++;
                            }
                        }
                    }
                    if (count == 0) {
                        rejectReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    for (DataSnapshot secondSnapshot : dataSnapshot.getChildren()) {
                                        OrderModel orderModel = secondSnapshot.getValue(OrderModel.class);
                                        if (firebaseAuth.getCurrentUser().getUid().equals(orderModel.getCustomerId())) {
                                            ShowUser(orderModel.getServicePId());
                                            count++;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    rejectReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                for (DataSnapshot secondSnapshot : dataSnapshot.getChildren()) {
                                    OrderModel orderModel = secondSnapshot.getValue(OrderModel.class);
                                    if (firebaseAuth.getCurrentUser().getUid().equals(orderModel.getCustomerId())) {
                                        ShowUser(orderModel.getServicePId());
                                        count++;
                                    }
                                }
                            }
                            if (count == 0) {
                                Toast.makeText(CustomerCheckOrder.this, "No Order Found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ShowUser(String servicePId) {

        userReference.child(servicePId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                    arrayList.add(signUpModel);
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