package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.maintenanceapp.Models.OrderModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.nio.file.Path;
import java.util.List;

public class OrderPlace extends AppCompatActivity {

    ImageView imageView;
    EditText edtDescription;
    AppCompatButton btnPlaceOrder;
    int Image_Picker_Code = 1;
    Uri Path_uri;
    StorageReference storageReference, sr;

    DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference rejectReference = FirebaseDatabase.getInstance().getReference("Rejected");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_place);
        imageView = findViewById(R.id.placeOrderImage);
        edtDescription = findViewById(R.id.edtDescription);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        Intent intent = getIntent();
        String servicePID = intent.getExtras().getString("orderKey");

        sr = FirebaseStorage.getInstance().getReference("ChatImages");

        String uid = firebaseAuth.getCurrentUser().getUid();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseImage();
            }
        });
        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = edtDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(OrderPlace.this, "Describe Your Issue", Toast.LENGTH_SHORT).show();
                } else {
                    rejectReference.child(servicePID).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                rejectReference.child(servicePID).child(uid).removeValue();
                                PlaceOrder(servicePID, uid, description);
                            } else {
                                PlaceOrder(servicePID, uid, description);
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

    private void PlaceOrder(String servicePID, String uid, String description) {
        orderReference.child(servicePID).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(OrderPlace.this, "You have already Place Order", Toast.LENGTH_SHORT).show();


                } else {
                    progressDialog = new ProgressDialog(OrderPlace.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_resource_file);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    if (Path_uri != null) {
                        storageReference = sr.child(Path_uri.getLastPathSegment());
                        storageReference.putFile(Path_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        SaveData(servicePID, uid, description, uri.toString());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(OrderPlace.this, "Image not Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        SaveData(servicePID, uid, description, "blank");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SaveData(String servicePID, String uid, String description, String image) {
        OrderModel orderModel = new OrderModel(servicePID, uid, "pending", description, image, 0);
        orderReference.child(servicePID).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                orderReference.child(servicePID).child(uid).setValue(orderModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(OrderPlace.this, "Request delivered to Service Provider", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OrderPlace.this, CustomerDashboard.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ChooseImage() {
        Dexter.withActivity(OrderPlace.this).withPermissions(android.Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Choose Image"), Image_Picker_Code);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Picker_Code && resultCode == RESULT_OK) {
            if (data != null) {
                Path_uri = data.getData();
                Picasso.get().load(Path_uri).into(imageView);
            }
        }
    }
}