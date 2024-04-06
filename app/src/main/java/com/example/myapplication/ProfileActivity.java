package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    UserModel otherUser;
    ImageView backBtn, avatarImg;
    Button messageBtn, addFriendBtn, deleteFriendBtn, deleteRequestBtn, acceptRequestBtn;
    TextView userNameTxt;
    String currentUserID = FirebaseUtil.currentUserID();
    HashMap<String, Object> myUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        backBtn = findViewById(R.id.imageBack);
        messageBtn = findViewById(R.id.messageProfileBtn);
        addFriendBtn = findViewById(R.id.addFriendProfileBtn);
        deleteFriendBtn = findViewById(R.id.deleteFriendProfileBtn);
        userNameTxt = findViewById(R.id.usernameTextView);
        avatarImg = findViewById(R.id.profilePhotoImageView);
        deleteRequestBtn = findViewById(R.id.delete_rqBtn);
        acceptRequestBtn = findViewById(R.id.acceptRequestBtn);

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());

        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl().
                addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        Uri uri = task1.getResult();
                        AndroidUtil.setProfilePic(this, uri, avatarImg);
                    }

                });
        userNameTxt.setText(otherUser.getUsername());

        messageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, otherUser);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        setListen();


    }
    private void setListen(){
        setData();
        FirebaseUtil.friend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, otherUser.getUserId()).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && !task1.getResult().isEmpty()) {

                        addFriendBtn.setVisibility(View.GONE);
                        deleteRequestBtn.setVisibility(View.GONE);
                        acceptRequestBtn.setVisibility(View.GONE);
                        deleteFriendBtn.setVisibility(View.VISIBLE);

                    }
            addFriendBtn.setOnClickListener(v -> {
                HashMap<String, Object> userFriend = new HashMap<>();
                userFriend.put(FirebaseUtil.KEY_USER_ID, otherUser.getUserId());
                userFriend.put(FirebaseUtil.KEY_USER_NAME, otherUser.getUsername());
                userFriend.put(FirebaseUtil.KEY_PHONE, otherUser.getPhone());
                userFriend.put(FirebaseUtil.KEY_TOKEN, otherUser.getFcmToken());

                FirebaseUtil.waitFriend(currentUserID).add(userFriend).addOnSuccessListener(documentReference -> {

                }).addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });

                FirebaseUtil.requestFriend(otherUser.getUserId()).add(myUser).addOnSuccessListener(documentReference -> {
                    showToast("Đã gửi lời kết bạn mời đến "+ otherUser.getUsername());
                }).addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });
                addFriendBtn.setVisibility(View.GONE);
                //messageBtn.setVisibility(View.GONE);

                deleteRequestBtn.setVisibility(View.VISIBLE);
                deleteFriendBtn.setVisibility(View.GONE);

                acceptRequestBtn.setVisibility(View.GONE);




            });
            acceptRequestBtn.setOnClickListener(v -> {
                HashMap<String, Object> userFriend = new HashMap<>();
                userFriend.put(FirebaseUtil.KEY_USER_ID, otherUser.getUserId());
                userFriend.put(FirebaseUtil.KEY_USER_NAME, otherUser.getUsername());
                userFriend.put(FirebaseUtil.KEY_PHONE, otherUser.getPhone());
                userFriend.put(FirebaseUtil.KEY_TOKEN, otherUser.getFcmToken());
                FirebaseUtil.requestFriend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, otherUser.getUserId()).get().addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                            deleteRequest(queryDocumentSnapshot.getId());
                        }
                    }
                });
                FirebaseUtil.waitFriend(otherUser.getUserId()).whereEqualTo(FirebaseUtil.KEY_USER_ID, currentUserID).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            hashMyUser(otherUser.getUserId(), queryDocumentSnapshot.getString(FirebaseUtil.KEY_USER_NAME), queryDocumentSnapshot.getString(FirebaseUtil.KEY_PHONE), queryDocumentSnapshot.getString(FirebaseUtil.KEY_TOKEN));
                            deleteWait(otherUser.getUserId(), queryDocumentSnapshot.getId());
                        }
                    }
                });
                FirebaseUtil.friend(currentUserID).add(userFriend).addOnSuccessListener(documentReference ->{
                    showToast("Bạn vừa có thêm một người bạn mới");
                }).addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });

                addFriendBtn.setVisibility(View.GONE);
                acceptRequestBtn.setVisibility(View.GONE);
                deleteRequestBtn.setVisibility(View.GONE);
                deleteFriendBtn.setVisibility(View.VISIBLE);

            });
            deleteFriendBtn.setOnClickListener(v -> {
                HashMap<String, Object> userFriend = new HashMap<>();
                userFriend.put(FirebaseUtil.KEY_USER_ID, otherUser.getUserId());
                userFriend.put(FirebaseUtil.KEY_USER_NAME, otherUser.getUsername());
                userFriend.put(FirebaseUtil.KEY_PHONE, otherUser.getPhone());
                userFriend.put(FirebaseUtil.KEY_TOKEN, otherUser.getFcmToken());

                FirebaseUtil.friend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, otherUser.getUserId()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() !=null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            deleteFriend(currentUserID, queryDocumentSnapshot.getId());
                        }
                    }
                });

                FirebaseUtil.friend(otherUser.getUserId()).whereEqualTo(FirebaseUtil.KEY_USER_ID, currentUserID).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            deleteFriend(otherUser.getUserId(), queryDocumentSnapshot.getId());
                            showToast("Bạn vừa hủy kết bạn với "+ otherUser.getUsername());
                        }
                    }
                });
                addFriendBtn.setVisibility(View.VISIBLE);
                deleteFriendBtn.setVisibility(View.GONE);
                deleteRequestBtn.setVisibility(View.GONE);
                acceptRequestBtn.setVisibility(View.GONE);

            });

            //huy loi moi ket ban di
            deleteRequestBtn.setOnClickListener(v -> {
                HashMap<String, Object> userFriend = new HashMap<>();
                userFriend.put(FirebaseUtil.KEY_USER_ID, otherUser.getUserId());
                userFriend.put(FirebaseUtil.KEY_USER_NAME, otherUser.getUsername());
                userFriend.put(FirebaseUtil.KEY_PHONE, otherUser.getPhone());
                userFriend.put(FirebaseUtil.KEY_TOKEN, otherUser.getFcmToken());
                FirebaseUtil.waitFriend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, otherUser.getUserId()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            deleteWait(currentUserID, queryDocumentSnapshot.getId());
                        }
                    }
                });
                FirebaseUtil.requestFriend(otherUser.getUserId()).whereEqualTo(FirebaseUtil.KEY_USER_ID, currentUserID).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            deleteRequest(otherUser.getUserId(), queryDocumentSnapshot.getId());
                        }
                    }
                });

                addFriendBtn.setVisibility(View.VISIBLE);
                deleteFriendBtn.setVisibility(View.GONE);
                deleteRequestBtn.setVisibility(View.GONE);
                acceptRequestBtn.setVisibility(View.GONE);
            });

        });

    }
    private void deleteRequest(String string){
        FirebaseUtil.requestFriend(currentUserID).document(string).delete().addOnSuccessListener(documentReference ->{

        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
        });

    }
    private void deleteRequest(String id, String string) {
        FirebaseUtil.requestFriend(id).document(string).delete().addOnSuccessListener(documentReference -> {

        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
        });
    }

    private void deleteWait(String id, String string) {
        FirebaseUtil.waitFriend(id).document(string).delete().addOnSuccessListener(documentReference -> {

        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
        });

    }
    private void deleteFriend(String id, String string){
        FirebaseUtil.friend(id).document(string).delete().addOnSuccessListener(documentReference -> {

        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
        });

    }
    private void hashMyUser(String id, String name, String phone, String token) {
        HashMap<String, Object> myUser = new HashMap<>();
        myUser.put(FirebaseUtil.KEY_USER_ID, currentUserID);
        myUser.put(FirebaseUtil.KEY_USER_NAME, name);
        myUser.put(FirebaseUtil.KEY_PHONE, phone);
        myUser.put(FirebaseUtil.KEY_TOKEN, token);

        FirebaseUtil.friend(id).add(myUser).addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });

    }

    void setData(){
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    myUser = new HashMap<>();
                    myUser.put(FirebaseUtil.KEY_USER_ID, document.getString(FirebaseUtil.KEY_USER_ID));
                    myUser.put(FirebaseUtil.KEY_USER_NAME, document.getString(FirebaseUtil.KEY_USER_NAME));
                    myUser.put(FirebaseUtil.KEY_PHONE, document.getString(FirebaseUtil.KEY_PHONE));
                    myUser.put(FirebaseUtil.KEY_TOKEN, document.getString(FirebaseUtil.KEY_TOKEN));


                } else {
                    showToast("Error");

                }
            }
        });
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}