package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Models.OrderModel;
import com.example.maintenanceapp.Models.SignUpModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerCheckServiceP extends AppCompatActivity {

    TextView txtName, txtPhone, txtOrderStatus;
    CircleImageView imageView;
    RatingBar ratingBar;
    AppCompatButton btnChat, btnOrder;
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference rejectReference = FirebaseDatabase.getInstance().getReference("Rejected");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String rValue, oValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_check_service_p);

        Intent intent = getIntent();
        String servicePID = intent.getExtras().getString("servicePId");


        imageView = findViewById(R.id.customerCheckServicePImage);
        txtName = findViewById(R.id.customerCheckServicePName);
        txtPhone = findViewById(R.id.customerCheckServicePPhone);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        ratingBar = findViewById(R.id.rating);
        btnChat = findViewById(R.id.btnChat);
        btnOrder = findViewById(R.id.btnOrder);

        userReference.child(servicePID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                    txtName.setText(signUpModel.getName());
                    txtPhone.setText(signUpModel.getPhone());
                    if (signUpModel.getUri().equals("blank")) {
                        imageView.setImageResource(R.drawable.blankprofile);
                    } else {
                        Picasso.get().load(signUpModel.getUri()).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        orderReference.child(servicePID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int divisionNumber = 0;
                float rating = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel orderModel = dataSnapshot.getValue(OrderModel.class);
                        if (orderModel.getStatus().equals("complete")) {
                            divisionNumber++;
                            rating = rating + orderModel.getRating();
                        }
                    }
                    float actualRating = rating / divisionNumber;
//                    Toast.makeText(CustomerCheckServiceP.this, actualRating+"", Toast.LENGTH_SHORT).show();
                    ratingBar.setRating(actualRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        txtOrderStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderReference.child(servicePID).child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            OrderModel orderModel = snapshot.getValue(OrderModel.class);
                            if (orderModel.getStatus().equals("pending") || orderModel.getStatus().equals("accepted")) {
                                ShowDialog(orderModel.getStatus());
                            }
                        } else {
                            rejectReference.child(servicePID).child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        OrderModel orderModel = snapshot.getValue(OrderModel.class);
                                        ShowDialog(orderModel.getStatus());
                                    } else {
                                        Toast.makeText(CustomerCheckServiceP.this, "No Order", Toast.LENGTH_SHORT).show();
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
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(CustomerCheckServiceP.this, ChatActivity.class);
                intent1.putExtra("ServicePKey", servicePID);
                startActivity(intent1);
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(CustomerCheckServiceP.this, OrderPlace.class);
                intent1.putExtra("orderKey", servicePID);
                startActivity(intent1);
            }
        });
    }


    private void ShowDialog(String status) {
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }

}