package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ChatActivity;
import com.example.myapplication.ProfileActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    HashMap<String, Object> myUser;
    Context context;
    String nameText = "(Tôi)";
    String currentUserID = FirebaseUtil.currentUserID();
    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {

        holder.usernameText.setText(model.getUsername());
        holder.phoneText.setText(model.getPhone());
        if (model.getUserId().equals(currentUserID)){
            holder.usernameText.setText(model.getUsername()+nameText);
            holder.addFriendBtn.setVisibility(View.GONE);

        }
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl().
                addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        Uri uri = task1.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }

                });
        holder.itemView.setOnClickListener(view -> {
            //chat activity
//            Intent intent = new Intent(context, ChatActivity.class);
//            AndroidUtil.passUserModelAsIntent(intent, model);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
            Intent intent = new Intent(context, ProfileActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        });
        setData();
        FirebaseUtil.friend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, model.getUserId()).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful() && ! task1.getResult().isEmpty()){

                holder.addFriendBtn.setVisibility(View.GONE);
                holder.friendBtn.setVisibility(View.VISIBLE);
               // holder.waitFriendBtn.setVisibility(View.GONE);

            }else {
                FirebaseUtil.checkWaitFriend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, model.getUserId()).get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && !task2.getResult().isEmpty()){

                        holder.addFriendBtn.setVisibility(View.GONE);
//                        holder.requestFriendBtn.setVisibility(View.GONE);
//                        holder.waitFriendBtn.setVisibility(View.GONE);
                    }else {
                        FirebaseUtil.checkRequestFriend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, model.getUserId()).get().addOnCompleteListener(task3 -> {
                            if (task3.isSuccessful() && ! task3.getResult().isEmpty()){
                                holder.addFriendBtn.setVisibility(View.GONE);
//                                holder.requestFriendBtn.setVisibility(View.GONE);
//                                holder.waitFriendBtn.setVisibility(View.GONE);
                            }else {
                                holder.addFriendBtn.setVisibility(View.VISIBLE);
//                                holder.requestFriendBtn.setVisibility(View.GONE);
//                                holder.waitFriendBtn.setVisibility(View.GONE);



                                holder.addFriendBtn.setOnClickListener(v -> {


                                    HashMap<String, Object> arrFriend = new HashMap<>();
                                    arrFriend.put(FirebaseUtil.KEY_USER_ID, model.getUserId());
                                    arrFriend.put(FirebaseUtil.KEY_USER_NAME, model.getUsername());
                                    arrFriend.put(FirebaseUtil.KEY_PHONE, model.getPhone());
                                    arrFriend.put(FirebaseUtil.KEY_TOKEN, model.getFcmToken());


                                    FirebaseUtil.waitFriend(currentUserID).add(arrFriend).addOnSuccessListener(documentReference -> {
                                    }).addOnFailureListener(e -> {
                                        showToast(e.getMessage());
                                    });
                                    FirebaseUtil.requestFriend(model.getUserId()).add(myUser).addOnSuccessListener(documentReference -> {
                                        showToast("Đã gửi lời mời kết bạn đến " + model.getUsername());
                                    }).addOnFailureListener(e -> {
                                        showToast(e.getMessage());
                                    });

                                holder.addFriendBtn.setVisibility(View.GONE);
//                                holder.requestFriendBtn.setVisibility(View.GONE);
//                                holder.waitFriendBtn.setVisibility(View.VISIBLE);

                            });

                        }
                    });
                }
            });
        }
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
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
        @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.recycler_search_user_row, parent, false);

        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView phoneText, friendBtn;
        ImageView profilePic;
        Button addFriendBtn;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            addFriendBtn = itemView.findViewById(R.id.btn_addFriend);
            friendBtn = itemView.findViewById(R.id.btn_Friend);

        }
    }
}
