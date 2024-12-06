package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.example.maintenanceapp.Models.SignUpModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {


    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        if (connected == true) {

        } else {
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseAuth.getCurrentUser() != null) {
                    CheckAccessLevel(firebaseAuth.getCurrentUser().getUid());
                } else {
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                }

            }
        }, 3000);
    }

    private void CheckAccessLevel(String uid) {

        userReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel model = snapshot.getValue(SignUpModel.class);
                    if (model.getStatus().equals("active")) {
                        if (model.getRole().equals("admin")) {
                            startActivity(new Intent(SplashScreen.this, AdminDashboard.class));
                        } else if (model.getRole().equals("service_provider")) {
                            startActivity(new Intent(SplashScreen.this, Service_ProviderDashboard.class));

                        } else {
                            startActivity(new Intent(SplashScreen.this, CustomerDashboard.class));

                        }
                    } else {
                        Toast.makeText(SplashScreen.this, "Access denied", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else {
                    Toast.makeText(SplashScreen.this, "No such user Found", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}