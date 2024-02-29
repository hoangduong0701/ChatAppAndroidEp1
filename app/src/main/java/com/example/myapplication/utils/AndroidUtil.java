package com.example.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class AndroidUtil {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void passUserModelAsIntent(Intent intent, UserModel model) {
        intent.putExtra("username", model.getUsername());
        intent.putExtra("phone", model.getPhone());
        intent.putExtra("userId", model.getUserId());
        intent.putExtra("fcmToken", model.getFcmToken());

    }

    public static UserModel getUserModelFromIntent(Intent intent) {
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
    public static void setProfilePictrue(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl != null && imageView != null) {
            // Hãy kiểm tra xem context có khác null không (chưa chắc là nguyên nhân, nhưng là một biện pháp phòng tránh)
            if (context != null) {
                Glide.with(context)
                        .load(imageUrl)
                        .into(imageView);
            } else {
                // Xử lý trường hợp context là null, có thể hiển thị một log hoặc thực hiện hành động khác tùy thuộc vào yêu cầu của bạn.
                Log.e("AndroidUtil", "Context is null");
            }
        } else {
            // Xử lý trường hợp imageUrl hoặc imageView là null
            Log.e("AndroidUtil", "ImageUrl or ImageView is null");
        }
    }

    // ...

}