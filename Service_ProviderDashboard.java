package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Models.OrderModel;
import com.example.maintenanceapp.Models.SignUpModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Service_ProviderDashboard extends AppCompatActivity {
    TextView txtName, txtEmail, txtRequestCount;
    CircleImageView imageView;
    CardView crdCompleteTask, crdRequest, crdMessages, crdInProcess;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_dashboard);
        txtName = findViewById(R.id.servicePName);
        txtEmail = findViewById(R.id.servicePEmail);
        txtRequestCount = findViewById(R.id.txtRequestCount);
        imageView = findViewById(R.id.servicePProfile);
        crdCompleteTask = findViewById(R.id.servicePCompleteTask);
        crdRequest = findViewById(R.id.servicePRequests);
        crdMessages = findViewById(R.id.servicePMessages);
        crdInProcess = findViewById(R.id.servicePInProcess);

        userReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                    txtName.setText(signUpModel.getName());
                    txtEmail.setText(signUpModel.getEmail());
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

        orderReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = 0;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        OrderModel model = dataSnapshot.getValue(OrderModel.class);
                        if (model.getStatus().equals("pending")) {
                            count++;
                        }
                    }
                    txtRequestCount.setText(count + "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Service_ProviderDashboard.this, Profile.class));
            }
        });

        crdRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = txtRequestCount.getText().toString();
                int counter = Integer.parseInt(value);
                if (counter == 0) {
                    Toast.makeText(Service_ProviderDashboard.this, "No Request Available", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Service_ProviderDashboard.this, RequestList.class));
                }
            }
        });

        crdMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Service_ProviderDashboard.this, ServicePChatUsers.class));
            }
        });

        crdInProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Service_ProviderDashboard.this, Accepted_or_Complete.class);
                intent.putExtra("requestKey","accepted");
                startActivity(intent);

            }
        });
        crdCompleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Service_ProviderDashboard.this, Accepted_or_Complete.class);
                intent.putExtra("requestKey","complete");
                startActivity(intent);

            }
        });

    }


}