package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

public class ActorInfo extends AppCompatActivity {

    CircleImageView imageView;
    TextView txtName, txtPhone, txtAddress, txtCity;

    AppCompatButton btnActive, btnInactive;

    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actor_info);
        imageView = findViewById(R.id.infoImage);
        txtName = findViewById(R.id.infoName);
        txtPhone = findViewById(R.id.infoPhone);
        txtAddress = findViewById(R.id.infoAddress);
        txtCity = findViewById(R.id.infoCity);
        btnActive = findViewById(R.id.btnActivate);
        btnInactive = findViewById(R.id.btnDeActivate);

        Intent intent = getIntent();
        String actorId = intent.getExtras().getString("infoKey");

        userReference.child(actorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                    if (signUpModel.getUri().equals("blank")) {
                        imageView.setImageResource(R.drawable.blankprofile);
                    } else {
                        Picasso.get().load(signUpModel.getUri()).into(imageView);
                    }
                    txtName.setText(signUpModel.getName());
                    txtPhone.setText(signUpModel.getPhone());
                    txtAddress.setText(signUpModel.getAddress());
                    txtCity.setText(signUpModel.getCity());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        btnInactive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = "inActive";
                ChangeStatus(value, actorId);
            }
        });

        btnActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = "active";
                ChangeStatus(value, actorId);
            }
        });
    }

    private void ChangeStatus(String value, String id) {

        userReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                if (signUpModel.getStatus().equals(value)) {
                    Toast.makeText(ActorInfo.this, "Already " + value, Toast.LENGTH_SHORT).show();
                } else {
                    signUpModel.setStatus(value);
                    userReference.child(id).setValue(signUpModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ActorInfo.this, "Status Changed", Toast.LENGTH_SHORT).show();
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