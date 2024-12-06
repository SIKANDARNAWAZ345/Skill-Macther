package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.health.PackageHealthStats;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUp extends AppCompatActivity {

    LinearLayout linearLayout;

    EditText edtName, edtPhone, edtAddress, edtCity, edtEmail, edtPassword, edtCnic;
    CircleImageView imageView;

    Spinner spinner;

    AppCompatButton btnSignUp;

    int Image_Picker_Code;
    Uri Path_Uri;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    StorageReference storageReference, sr;

    ProgressDialog progressDialog;

    RadioButton radioService, radioCustomer;
    RadioGroup radioGroup;

    String role;
    int check = 0;
    String value = "";

    SupportMapFragment fragment;
    LatLng latLng;
    FusedLocationProviderClient client;
    double lat, lng;

    ArrayList<String> spinnerList = new ArrayList<>();

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String phonePattern = "^(\\+92|0)3\\d{9}$";
    String cnicPattern = "\\d{5}[-|]\\d{7}[-|]\\d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        imageView = findViewById(R.id.signUpImage);
        edtName = findViewById(R.id.signUpName);
        edtPhone = findViewById(R.id.signUpPhone);
        edtAddress = findViewById(R.id.signUpAddress);
        edtCity = findViewById(R.id.signUpCity);
        edtEmail = findViewById(R.id.signUpEmail);
        edtPassword = findViewById(R.id.signUpPassword);
        edtCnic = findViewById(R.id.signUpCnic);
        linearLayout = findViewById(R.id.signUpLayout);
        spinner = findViewById(R.id.spinner);
        radioService = findViewById(R.id.radioService);
        radioCustomer = findViewById(R.id.radioCustomer);
        radioGroup = findViewById(R.id.radioGroup);
        btnSignUp = findViewById(R.id.btnSignUp);

        fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        client = LocationServices.getFusedLocationProviderClient(this);

        sr = FirebaseStorage.getInstance().getReference("Images");

        spinnerList.add("Select profession");
        spinnerList.add("Car-Mechanic");
        spinnerList.add("Plumber");
        spinnerList.add("Painter");
        spinnerList.add("Electrician");


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioService) {
                    spinner.setVisibility(View.VISIBLE);
                    check = 1;
                } else if (checkedId == R.id.radioCustomer) {
                    spinner.setVisibility(View.GONE);
                    check = 0;
                }
            }
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(SignUp.this, android.R.layout.simple_spinner_dropdown_item, spinnerList);

        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    check = 1;
                    value = "";
                }
                if (position > 0) {
                    value = spinnerList.get(position);
                    check = 0;
                    Toast.makeText(SignUp.this, value, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Dexter.withContext(SignUp.this)
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

        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                Dexter.withActivity(SignUp.this)
                        .withPermissions(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
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
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();
                String address = edtAddress.getText().toString();
                String city = edtCity.getText().toString();
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                String cnic = edtCnic.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter Name", Toast.LENGTH_SHORT).show();
                } else if (phone.isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter Phone", Toast.LENGTH_SHORT).show();
                } else if (!(phone.matches(phonePattern))) {
                    Toast.makeText(SignUp.this, "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
                } else if (address.isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter Address", Toast.LENGTH_SHORT).show();
                } else if (city.isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter City Name", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if ((!email.matches(emailPattern))) {
                    Toast.makeText(SignUp.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else if (cnic.isEmpty()) {
                    Toast.makeText(SignUp.this, "Enter CNIC", Toast.LENGTH_SHORT).show();
                } else if ((!cnic.matches(cnicPattern))) {
                    Toast.makeText(SignUp.this, "Enter Valid CNIC", Toast.LENGTH_SHORT).show();
                } else if (!(radioService.isChecked() | radioCustomer.isChecked())) {
                    Toast.makeText(SignUp.this, "Select at least one Character", Toast.LENGTH_SHORT).show();
                } else if (check == 1 && value.isEmpty()) {
                    Toast.makeText(SignUp.this, "Select Profession", Toast.LENGTH_SHORT).show();
                } else {
//                    CurrentLocation();
                    if (radioService.isChecked()) {
                        role = "service_provider";

                    } else if (radioCustomer.isChecked()) {
                        role = "customer";
                    }
                    linearLayout.setVisibility(View.GONE);
                    progressDialog = new ProgressDialog(SignUp.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_resource_file);
                    Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if (Path_Uri != null) {
                                storageReference = sr.child(Objects.requireNonNull(Path_Uri.getLastPathSegment()));
                                storageReference.putFile(Path_Uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                SaveData(name, phone, address, city, email, password, cnic, uri.toString(), value, lat, lng);

                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUp.this, "Image not Uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                SaveData(name, phone, address, city, email, password, cnic, "blank", value, lat, lng);
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            linearLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(SignUp.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
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
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location");
                            googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        }
                    });
                }
            }
        });
    }

    private void SaveData(String name, String phone, String address, String city, String email, String password, String cnic, String uri, String val, double latitude, double longitude) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        SignUpModel model = new SignUpModel(name, phone, cnic, address, city, email, password, uid, uri, role, val, "active", latitude, longitude);
        userReference.child(uid).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SignUp.this, "SignUp Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUp.this, MainActivity.class));
                Path_Uri = null;
                edtName.setText("");
                edtPhone.setText("");
                edtAddress.setText("");
                edtCity.setText("");
                edtEmail.setText("");
                edtPassword.setText("");
                edtCnic.setText("");
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUp.this, "something went wrong", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                linearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Picker_Code && resultCode == RESULT_OK) {
            assert data != null;
            if (data.getData() != null) {
                Path_Uri = data.getData();
                Picasso.get().load(Path_Uri).into(imageView);
            }
        }
    }
}