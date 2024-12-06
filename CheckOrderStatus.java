package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Models.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CheckOrderStatus extends AppCompatActivity {

    ImageView imageView;
    TextView txtDescription, txtStatus;
    AppCompatButton btnComplete;
    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference rejectReference = FirebaseDatabase.getInstance().getReference("Rejected");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    RatingBar ratingBar;
    AppCompatButton btnCancel, btnDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_order_status);
        imageView = findViewById(R.id.checkStatusImage);
        txtStatus = findViewById(R.id.checkStatus);
        txtDescription = findViewById(R.id.checkStatusDescription);
        btnComplete = findViewById(R.id.btnComplete);
        Intent intent = getIntent();
        String serviceId = intent.getExtras().getString("checkServicePKey");

        Dialog dialog = new Dialog(CheckOrderStatus.this);
        dialog.setContentView(R.layout.rating_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        orderReference.child(serviceId).child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    OrderModel orderModel = snapshot.getValue(OrderModel.class);
                    if (orderModel.getUri().equals("blank")) {
                        imageView.setImageResource(R.drawable.baseline_insert_photo_24);
                    } else {
                        Picasso.get().load(orderModel.getUri()).into(imageView);
                    }
                    txtDescription.setText(orderModel.getDescription());
                    txtStatus.setText(orderModel.getStatus());
                } else {
                    rejectReference.child(serviceId).child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                OrderModel orderModel = snapshot.getValue(OrderModel.class);
                                if (orderModel.getUri().equals("blank")) {
                                    imageView.setImageResource(R.drawable.baseline_insert_photo_24);
                                } else {
                                    Picasso.get().load(orderModel.getUri()).into(imageView);
                                }
                                txtDescription.setText(orderModel.getDescription());
                                txtStatus.setText(orderModel.getStatus());
                            } else {
                                Toast.makeText(CheckOrderStatus.this, "No Order Found", Toast.LENGTH_SHORT).show();
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

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();
                ratingBar = dialog.findViewById(R.id.dialogRating);

                btnCancel = dialog.findViewById(R.id.ratingCancel);
                btnDone = dialog.findViewById(R.id.ratingDone);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        CompleteOrder(serviceId,0);
                    }
                });
                btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        float rating = ratingBar.getRating();
                        CompleteOrder(serviceId,rating);
                    }
                });

            }
        });
    }

    private void CompleteOrder(String serviceId,float rating) {
        orderReference.child(serviceId).child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    OrderModel orderModel = snapshot.getValue(OrderModel.class);
                    if (orderModel.getStatus().equals("accepted")) {
                        orderModel.setStatus("complete");
                        orderModel.setRating(rating);
                        orderReference.child(serviceId).child(firebaseAuth.getCurrentUser().getUid()).setValue(orderModel);
                    } else {
                        Toast.makeText(CheckOrderStatus.this, "Not Accepted Yet", Toast.LENGTH_SHORT).show();
                    }
                    if (orderModel.getStatus().equals("complete")){
                        btnComplete.setVisibility(View.GONE);
                    }
                } else {
//                    btnComplete.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}