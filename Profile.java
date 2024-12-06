package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.maintenanceapp.Models.SignUpModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    TextView txtName, txtPhone, txtPassword;
    CircleImageView imageView;

    LinearLayout btnLogout;

    AppCompatButton btnUpdate;

    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtName = findViewById(R.id.profileName);
        txtPhone = findViewById(R.id.profilePhone);
        txtPassword = findViewById(R.id.profilePassword);
        imageView = findViewById(R.id.profile_image);
        btnUpdate = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        String uid = firebaseAuth.getCurrentUser().getUid();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent1 = new Intent(Profile.this, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent1);
            }
        });

        userReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel model = snapshot.getValue(SignUpModel.class);
                    txtName.setText(model.getName());
                    txtPhone.setText(model.getPhone());
                    txtPassword.setText(model.getPassword());
                    if (model.getUri().equals("blank")) {
                        imageView.setImageResource(R.drawable.blankprofile);
                    } else {
                        Picasso.get().load(model.getUri()).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, UpdateProfile.class));
            }
        });
    }
}