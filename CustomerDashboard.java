package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Models.SignUpModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerDashboard extends AppCompatActivity {
    TextView txtName, txtEmail;
    CircleImageView imageView;

    CardView crdMechanic,crdPlumber,crdPainter,crdElectrician,crdCheckOrder;

    SupportMapFragment fragment;
    LatLng latLng;
    FusedLocationProviderClient client;

    double lat, lng;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);
        crdMechanic=findViewById(R.id.customerMechanic);
        crdPlumber=findViewById(R.id.customerPlumber);
        crdPainter=findViewById(R.id.customerPainter);
        crdElectrician=findViewById(R.id.customerElectrician);
        crdCheckOrder=findViewById(R.id.customerOrders);
        txtName = findViewById(R.id.customerName);
        txtEmail = findViewById(R.id.customerEmail);
        imageView = findViewById(R.id.customerProfile);


        fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.customerGoogle_map);

        client = LocationServices.getFusedLocationProviderClient(CustomerDashboard.this);


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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerDashboard.this,Profile.class));
            }
        });

        Dexter.withContext(CustomerDashboard.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        CurrentLocation();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

        crdMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CustomerDashboard.this,CustomerActors.class);
                intent.putExtra("actorKey","Car-Mechanic");
                startActivity(intent);
            }
        });
        crdPlumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CustomerDashboard.this,CustomerActors.class);
                intent.putExtra("actorKey","Plumber");
                startActivity(intent);
            }
        });
        crdPainter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CustomerDashboard.this,CustomerActors.class);
                intent.putExtra("actorKey","Painter");
                startActivity(intent);
            }
        });
        crdElectrician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CustomerDashboard.this,CustomerActors.class);
                intent.putExtra("actorKey","Electrician");
                startActivity(intent);
            }
        });

        crdCheckOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerDashboard.this,CustomerCheckOrder.class));
            }
        });

    }
    private void CurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    fragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            Toast.makeText(CustomerDashboard.this, lat + "" + lng + "", Toast.LENGTH_SHORT).show();
                            UpdateLocation(lat, lng);
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location");
                            googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        }
                    });
                }
            }
        });
    }


    private void UpdateLocation(double lat, double lng) {

        userReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                    signUpModel.setLat(lat);
                    signUpModel.setLng(lng);
                    userReference.child(firebaseAuth.getCurrentUser().getUid()).setValue(signUpModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}