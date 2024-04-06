package com.example.myapplication.utils;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FirebaseUtil {
    public static String currentUserID(){
        return FirebaseAuth.getInstance().getUid();
    }
    public static boolean isLoggedIn(){
        if(currentUserID()!=null){
            return true;
        }
        return false;
    }
    public static DocumentReference currentUserDetails(){

        return FirebaseFirestore.getInstance().collection("user").document(currentUserID());
    }
    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("user");
    }


    @NonNull
    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1,String userId2){
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;
        }else{
            return userId2+"_"+userId1;
        }
    }

    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds.get(0).equals(FirebaseUtil.currentUserID())){
            return allUserCollectionReference().document(userIds.get(1));
        }else{
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }
    public static String DateToString(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    public static StorageReference  getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(FirebaseUtil.currentUserID());
    }

    public static StorageReference getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }
    public static CollectionReference deletechatCollection(){
        return  FirebaseFirestore.getInstance().collection("chatrooms");
    }
    public static CollectionReference deletechat(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId).collection("chats");
    }
    public static CollectionReference waitFriend(String userId){
        return FirebaseFirestore.getInstance().collection("user").document(userId).collection("wait_friends");

    }
    public static CollectionReference requestFriend(String userId){
        return FirebaseFirestore.getInstance().collection("user").document(userId).collection("request_friends");

    }
    public  static CollectionReference friend(String userId){
        return FirebaseFirestore.getInstance().collection("user").document(userId).collection("friends");
    }
    public static CollectionReference checkWaitFriend(String userId){
        return FirebaseFirestore.getInstance().collection("user").document(userId).collection("wait_friends");
    }
    public static CollectionReference checkRequestFriend(String userId){
        return FirebaseFirestore.getInstance().collection("user").document(userId).collection("request_friends");

    }
    public static CollectionReference conversion(){
        return FirebaseFirestore.getInstance().collection("conversion");
    }





    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "username";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_TOKEN = "fcmToken";

}
