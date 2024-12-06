package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Models.OrderModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ServicePCheckRequest extends AppCompatActivity {

    ImageView imageView;
    TextView txtDescription;
    AppCompatButton btnAccept, btnReject, btnDone;

    EditText edtReason;

    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference rejectedReference = FirebaseDatabase.getInstance().getReference("Rejected");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    LinearLayout linearLayout;
    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_pcheck_request);

        imageView = findViewById(R.id.checkRequestImage);
        txtDescription = findViewById(R.id.checkRequestDescription);
        linearLayout = findViewById(R.id.servicePCheckRLayout);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        Intent intent = getIntent();
        String id = intent.getExtras().getString("customerId");


        dialog = new Dialog(ServicePCheckRequest.this);
        dialog.setContentView(R.layout.dialog_resource_file);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                linearLayout.setVisibility(View.VISIBLE);
            }
        });


        orderReference.child(firebaseAuth.getCurrentUser().getUid()).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    OrderModel orderModel = snapshot.getValue(OrderModel.class);
                    if (orderModel.getCustomerId().equals(id)) {
                        if (orderModel.getUri().equals("blank")) {
                            imageView.setImageResource(R.drawable.baseline_insert_photo_24);
                        } else {
                            Picasso.get().load(orderModel.getUri()).into(imageView);
                        }
                        txtDescription.setText(orderModel.getDescription());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderReference.child(firebaseAuth.getCurrentUser().getUid()).child(id).child("status")
                        .setValue("accepted").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ServicePCheckRequest.this, "Order Accepted", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edtReason = dialog.findViewById(R.id.dialogReason);
                btnDone = dialog.findViewById(R.id.dialogDone);
                dialog.show();
                if (dialog.isShowing()) {
                    linearLayout.setVisibility(View.GONE);
                }

                btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String reason = edtReason.getText().toString();
                        if (reason.isEmpty()) {
                            Toast.makeText(ServicePCheckRequest.this, "Enter Reason", Toast.LENGTH_SHORT).show();
                        } else {
                            orderReference.child(firebaseAuth.getCurrentUser().getUid()).child(id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        OrderModel orderModel = snapshot.getValue(OrderModel.class);
                                        orderModel.setStatus("rejected");
                                        orderModel.setDescription(reason);
                                        rejectedReference.child(firebaseAuth.getCurrentUser().getUid()).child(id).setValue(orderModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                orderReference.child(firebaseAuth.getCurrentUser().getUid()).child(id).removeValue();
                                                Toast.makeText(ServicePCheckRequest.this, "Order Rejected", Toast.LENGTH_SHORT).show();
                                                Intent intent1 = new Intent(ServicePCheckRequest.this, RequestList.class);
                                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent1);
                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                });


            }
        });
    }

}