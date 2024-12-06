package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maintenanceapp.Models.SignUpModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    EditText edtEmail, edtPassword;
    AppCompatButton btnLogin;
    TextView txtSignUp;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    ProgressDialog progressDialog;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtSignUp = findViewById(R.id.txtSignUp);
        edtEmail = findViewById(R.id.emailLogin);
        edtPassword = findViewById(R.id.passwordLogin);
        linearLayout = findViewById(R.id.mainLayout);
        btnLogin = findViewById(R.id.btnLogin);

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUp.class));
            }
        });
        linearLayout.setVisibility(View.VISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if (!(email.matches(emailPattern))) {
                    Toast.makeText(MainActivity.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_resource_file);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    linearLayout.setVisibility(View.GONE);
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            userReference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        SignUpModel model = snapshot.getValue(SignUpModel.class);
                                        if (model.getStatus().equals("active")){
                                            if (model.getRole().equals("admin")) {
                                                startActivity(new Intent(MainActivity.this, AdminDashboard.class));
                                                progressDialog.dismiss();
                                                linearLayout.setVisibility(View.VISIBLE);
                                            } else if (model.getRole().equals("service_provider")) {
                                                edtEmail.setText("");
                                                edtPassword.setText("");
                                                startActivity(new Intent(MainActivity.this, Service_ProviderDashboard.class));
                                                progressDialog.dismiss();
                                                linearLayout.setVisibility(View.VISIBLE);

                                            } else {
                                                edtEmail.setText("");
                                                edtPassword.setText("");
                                                startActivity(new Intent(MainActivity.this, CustomerDashboard.class));
                                                progressDialog.dismiss();
                                                linearLayout.setVisibility(View.VISIBLE);

                                            }
                                        }else {
                                            progressDialog.dismiss();
                                            linearLayout.setVisibility(View.VISIBLE);
                                            edtEmail.setText("");
                                            edtPassword.setText("");
                                            Toast.makeText(MainActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        edtEmail.setText("");
                                        edtPassword.setText("");
                                        progressDialog.dismiss();
                                        linearLayout.setVisibility(View.VISIBLE);
                                        Toast.makeText(MainActivity.this, "No such user Found", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            linearLayout.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }
}