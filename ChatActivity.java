package com.example.maintenanceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.maintenanceapp.Adapter.ChatAdapter;
import com.example.maintenanceapp.Models.ChatModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    EditText edtMessage;
    View camera;
    FloatingActionButton btnSend;
    ImageView imageView;
    RecyclerView recyclerView;

    ArrayList<ChatModel> arrayList = new ArrayList<>();

    ChatAdapter chatAdapter;

    int Image_Picker_Code = 100;


    Uri Path_Uri;

    Bitmap img;

    DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chats");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    StorageReference storageReference,sr;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edtMessage = findViewById(R.id.edtChat);
        imageView=findViewById(R.id.chatImage);
        camera = findViewById(R.id.camera);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.chatRecyclerView);

        Intent intent = getIntent();
        String receiverId = intent.getExtras().getString("ServicePKey");

        sr= FirebaseStorage.getInstance().getReference("ChatImages");

        String senderId = firebaseAuth.getCurrentUser().getUid();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        chatAdapter = new ChatAdapter(arrayList, ChatActivity.this);

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                int counter = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                    if (senderId.equals(chatModel.getSenderId()) && receiverId.equals(chatModel.getReceiverId()) ||
                            receiverId.equals(chatModel.getSenderId()) && senderId.equals(chatModel.getReceiverId())) {
                        arrayList.add(chatModel);
                        counter++;
                    }
                    recyclerView.setAdapter(chatAdapter);
                    chatAdapter.notifyDataSetChanged();
                    if (counter == 0) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (Path_Uri==null){
            imageView.setVisibility(View.GONE);
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edtMessage.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(ChatActivity.this, "Type a message", Toast.LENGTH_SHORT).show();
                } else if (Path_Uri!=null){
                    progressDialog = new ProgressDialog(ChatActivity.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_resource_file);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    storageReference=sr.child(Path_Uri.getLastPathSegment());
                    storageReference.putFile(Path_Uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    sendMessage(senderId, receiverId, message,uri.toString());
                                    edtMessage.setText("");
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, "Image not Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    sendMessage(senderId, receiverId, message,"blank");
                    edtMessage.setText("");
                }
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent1, Image_Picker_Code);
                Intent intent1 = new Intent(Intent.ACTION_PICK);
                intent1.setType("image/*");
                startActivityForResult(Intent.createChooser(intent1, "Choose Image"), Image_Picker_Code);
            }
        });

    }

    private void sendMessage(String senderId, String receiverId, String message,String uri) {

        ChatModel chatModel = new ChatModel(senderId, receiverId, message, uri);
        chatReference.push().setValue(chatModel);
        imageView.setVisibility(View.GONE);
        Path_Uri=null;
        recyclerView.setVisibility(View.VISIBLE);


        DatabaseReference chatListReference = FirebaseDatabase.getInstance().getReference("ChatList");
        chatListReference.child(senderId).child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatListReference.child(senderId).child(receiverId).child("id").setValue(receiverId);
                    chatListReference.child(receiverId).child(senderId).child("id").setValue(senderId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Picker_Code && resultCode == RESULT_OK) {

//            img = (Bitmap) (data.getExtras().get("data"));
////            imageView.setImageBitmap(img);
            if (data != null) {
                Path_Uri = data.getData();
                imageView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Picasso.get().load(Path_Uri).into(imageView);
                Toast.makeText(this, Path_Uri.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}