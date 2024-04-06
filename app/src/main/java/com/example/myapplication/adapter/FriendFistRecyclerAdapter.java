package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ChatActivity;
import com.example.myapplication.ProfileActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.FriendModel;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FriendFistRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, FriendFistRecyclerAdapter.FriendFistModelViewHolder> {
    Context context;

    public FriendFistRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendFistModelViewHolder holder, int position, @NonNull UserModel model) {

        holder.usernameTxt.setText(model.getUsername());
        holder.phoneTxt.setText(model.getPhone());
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl().
                addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        Uri uri = task1.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }

                });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public FriendFistModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recycler_friend_fist_row, parent, false);
        return new FriendFistRecyclerAdapter.FriendFistModelViewHolder(view);
    }

    static class  FriendFistModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameTxt, phoneTxt;
        ImageView profilePic;

        public FriendFistModelViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            usernameTxt = itemView.findViewById(R.id.user_name_textFf);
            phoneTxt = itemView.findViewById(R.id.phone_textFf);
        }

    }
}
