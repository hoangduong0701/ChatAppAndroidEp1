package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ChatActivity;
import com.example.myapplication.FriendFragment.RequestFriendFragment;
import com.example.myapplication.ProfileActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.FriendModel;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class RequestFriendRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, RequestFriendRecyclerAdapter.RequestFriendModelViewHolder> {
    HashMap<String, Object> myUser;
    Context context;
    String currentUserID = FirebaseUtil.currentUserID();
    public RequestFriendRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }


    public RequestFriendRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestFriendModelViewHolder holder, int position, @NonNull UserModel model) {

        holder.usernameText.setText(model.getUsername());
        holder.phoneText.setText(model.getPhone());
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl().
                addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        Uri uri = task1.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }

                });
        setData();
        holder.addBtn.setOnClickListener(v -> {

            HashMap<String, Object> objFriend = new HashMap<>();
            objFriend.put(FirebaseUtil.KEY_USER_ID, model.getUserId());
            objFriend.put(FirebaseUtil.KEY_USER_NAME, model.getUsername());
            objFriend.put(FirebaseUtil.KEY_PHONE, model.getPhone());
            objFriend.put(FirebaseUtil.KEY_TOKEN, model.getFcmToken());

            FirebaseUtil.requestFriend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, model.getUserId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        deleteRequest(queryDocumentSnapshot.getId());
                    }

                }
            });
            FirebaseUtil.waitFriend(model.getUserId()).whereEqualTo(FirebaseUtil.KEY_USER_ID, currentUserID).get().addOnCompleteListener(task -> {

                if (task.isSuccessful() && task.getResult() !=null){
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        hashMyUser(model.getUserId(), queryDocumentSnapshot.getString(FirebaseUtil.KEY_USER_NAME), queryDocumentSnapshot.getString(FirebaseUtil.KEY_PHONE), queryDocumentSnapshot.getString(FirebaseUtil.KEY_TOKEN));
                        deleteWait(model.getUserId(), queryDocumentSnapshot.getId());
                    }
                }
            });
            FirebaseUtil.friend(currentUserID).add(objFriend).addOnSuccessListener(documentReference -> {
                showToast("Bạn vừa có thêm bạn mới");
            });


        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }


    void hashMyUser(String id, String name, String phone, String token){

        HashMap<String, Object> myUser = new HashMap<>();
        myUser.put(FirebaseUtil.KEY_USER_ID, currentUserID);
        myUser.put(FirebaseUtil.KEY_USER_NAME, name);
        myUser.put(FirebaseUtil.KEY_PHONE, phone);
        myUser.put(FirebaseUtil.KEY_TOKEN, token);
        FirebaseUtil.friend(id).add(myUser).addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
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
    void deleteRequest(String string){

        FirebaseUtil.requestFriend(currentUserID).document(string).delete().addOnSuccessListener(unused -> {

        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
        });
    }
    void deleteWait(String id, String string){
        FirebaseUtil.waitFriend(id).document(string).delete().addOnSuccessListener(unused -> {
        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
        });
    }
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public RequestFriendModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_requestfriend_row, parent, false);
        return new RequestFriendRecyclerAdapter.RequestFriendModelViewHolder(view);
    }

    static class RequestFriendModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;
        ImageButton addBtn;

        public RequestFriendModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_textRq);
            phoneText = itemView.findViewById(R.id.phone_textRq);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            addBtn = itemView.findViewById(R.id.addRequestBtn);

        }


    }
}
