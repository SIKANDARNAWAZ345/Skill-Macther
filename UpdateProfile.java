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
import android.widget.Toast;

import com.example.maintenanceapp.Models.SignUpModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class  UpdateProfile extends AppCompatActivity {
    CircleImageView imageView;
    EditText edtName, edtPhone, edtPassword;
    AppCompatButton btnUpdate;

    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    int IMAGE_PICKER_CODE;
    Uri PATH_URI;

    String phonePattern = "^(\\+92|0)3\\d{9}$";

    StorageReference storageReference, sr;
    ProgressDialog progressDialog;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        imageView = findViewById(R.id.updateDoneprofile_image);
        edtName = findViewById(R.id.updateDoneProfileName);
        edtPhone = findViewById(R.id.updateDoneProfilePhone);
        edtPassword = findViewById(R.id.updateDoneProfilePassword);
        btnUpdate = findViewById(R.id.btnUpdateDone);

        sr = FirebaseStorage.getInstance().getReference("Images");


        userReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                    if (signUpModel.getUri().equals("blank")) {
                        imageView.setImageResource(R.drawable.blankprofile);
                    } else {
                        Picasso.get().load(signUpModel.getUri()).into(imageView);
                    }
                    edtName.setText(signUpModel.getName());
                    edtPhone.setText(signUpModel.getPhone());
                    edtPassword.setText(signUpModel.getPassword());
                    url = signUpModel.getUri();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseImage();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();
                String password = edtPassword.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(UpdateProfile.this, "Enter Name", Toast.LENGTH_SHORT).show();
                } else if (phone.isEmpty()) {
                    Toast.makeText(UpdateProfile.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                } else if (!phone.matches(phonePattern)) {
                    Toast.makeText(UpdateProfile.this, "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(UpdateProfile.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog = new ProgressDialog(UpdateProfile.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_resource_file);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    if (PATH_URI != null) {
                        storageReference = sr.child(PATH_URI.getLastPathSegment());
                        storageReference.putFile(PATH_URI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        SaveData(name, phone, password, uri.toString());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UpdateProfile.this, "Image Not Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        SaveData(name, phone, password, url);
                    }
                }
            }
        });
    }

    private void SaveData(String name, String phone, String password, String uri) {
        userReference.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SignUpModel signUpModel = snapshot.getValue(SignUpModel.class);
                signUpModel.setUri(uri);
                signUpModel.setName(name);
                signUpModel.setPhone(phone);
                signUpModel.setPassword(password);
                firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateProfile.this, "Password", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(UpdateProfile.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                userReference.child(firebaseAuth.getCurrentUser().getUid()).setValue(signUpModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UpdateProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        if (signUpModel.getRole().equals("admin")) {
                            Intent intent = new Intent(UpdateProfile.this, AdminDashboard.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (signUpModel.getRole().equals("service_provider")) {
                            Intent intent = new Intent(UpdateProfile.this, Service_ProviderDashboard.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(UpdateProfile.this, CustomerDashboard.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfile.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ChooseImage() {
        Dexter.withActivity(UpdateProfile.this)
                .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Choose Image"), IMAGE_PICKER_CODE);
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
        if (requestCode == IMAGE_PICKER_CODE && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                PATH_URI = data.getData();
                Picasso.get().load(PATH_URI).into(imageView);
            }
        }
    }
}