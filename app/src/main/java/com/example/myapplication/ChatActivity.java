package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.adapter.ChatRecyclerAdapter;
import com.example.myapplication.adapter.SearchUserRecyclerAdapter;
import com.example.myapplication.model.ChatMessageModel;
import com.example.myapplication.model.ChatroomModel;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    EditText messageInput;
    ImageButton sendMessageBtn, backBtn;
    UserModel currentUserModel;
    TextView otherUsername;
    TextView addFriendBtn, waitFriendBtn, requestFriendBtn;
    RecyclerView recyclerView;
    ImageView imageView, friendStatus;
    ImageButton camera_btn, micro_btn;
    String currentUserID = FirebaseUtil.currentUserID();
    HashMap<String, Object> myUser;
    private String encoded_Image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserID(),otherUser.getUserId());
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);
        camera_btn = findViewById(R.id.camera_btn);
        micro_btn = findViewById(R.id.micro_btn);
        addFriendBtn = findViewById(R.id.friend_add);
        waitFriendBtn = findViewById(R.id.friend_wait);
        requestFriendBtn = findViewById(R.id.friend_request);
        friendStatus = findViewById(R.id.friend_status);
        messageInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);// cho phep xuong nhiefu dong
        addFriend();
        callVideo();
        initVoiceButton();
        initVideoButton();

        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl().
                addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        Uri uri = task1.getResult();
                        AndroidUtil.setProfilePic(this, uri, imageView);
                    }

                });

        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });
        otherUsername.setText(otherUser.getUsername());
        sendMessageBtn.setOnClickListener(view -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message);

        });
        camera_btn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    getOrCreateChatroomModel();
    setupChatRecyclerView();
    edittextChanged();

    }
    private void addFriend(){
        setData();
        FirebaseUtil.friend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, otherUser.getUserId()).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful() && ! task1.getResult().isEmpty()){
                friendStatus.setVisibility(View.GONE);
                addFriendBtn.setVisibility(View.GONE);
                requestFriendBtn.setVisibility(View.GONE);
                waitFriendBtn.setVisibility(View.GONE);

            }else {
                FirebaseUtil.checkWaitFriend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, otherUser.getUserId()).get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && !task2.getResult().isEmpty()){
                        friendStatus.setVisibility(View.VISIBLE);
                        addFriendBtn.setVisibility(View.GONE);
                        requestFriendBtn.setVisibility(View.GONE);
                        waitFriendBtn.setVisibility(View.VISIBLE);
                    }else {
                        FirebaseUtil.checkRequestFriend(currentUserID).whereEqualTo(FirebaseUtil.KEY_USER_ID, otherUser.getUserId()).get().addOnCompleteListener(task3 -> {
                            if (task3.isSuccessful() && ! task3.getResult().isEmpty()){
                                friendStatus.setVisibility(View.VISIBLE);
                                addFriendBtn.setVisibility(View.GONE);
                                requestFriendBtn.setVisibility(View.VISIBLE);
                                waitFriendBtn.setVisibility(View.GONE);
                            }else {
                                friendStatus.setVisibility(View.VISIBLE);
                                addFriendBtn.setVisibility(View.VISIBLE);
                                requestFriendBtn.setVisibility(View.GONE);
                                waitFriendBtn.setVisibility(View.GONE);


                                addFriendBtn.setOnClickListener(v -> {
                                    HashMap<String, Object> arrFriend = new HashMap<>();
                                    arrFriend.put(FirebaseUtil.KEY_USER_ID, otherUser.getUserId());
                                    arrFriend.put(FirebaseUtil.KEY_USER_NAME, otherUser.getUsername());
                                    arrFriend.put(FirebaseUtil.KEY_PHONE, otherUser.getPhone());
                                    arrFriend.put(FirebaseUtil.KEY_TOKEN, otherUser.getFcmToken());

                                    FirebaseUtil.waitFriend(currentUserID).add(arrFriend).addOnSuccessListener(documentReference -> {
                                    }).addOnFailureListener(e -> {
                                        showToast(e.getMessage());
                                    });
                                    FirebaseUtil.requestFriend(otherUser.getUserId()).add(myUser).addOnSuccessListener(documentReference -> {
                                        showToast("Đã gửi lời mời kết bạn đến " + otherUser.getUsername());
                                    }).addOnFailureListener(e -> {
                                        showToast(e.getMessage());
                                    });
                                    friendStatus.setVisibility(View.VISIBLE);
                                    addFriendBtn.setVisibility(View.GONE);
                                    requestFriendBtn.setVisibility(View.GONE);
                                    waitFriendBtn.setVisibility(View.VISIBLE);
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
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    private void initVideoButton() {
        ZegoSendCallInvitationButton newVideoCall = findViewById(R.id.video_call_btn);
        newVideoCall.setIsVideoCall(true);
        newVideoCall.setOnClickListener(v -> {

            String targetUserID = otherUser.getUserId();
            String[] split = targetUserID.split(",");
            List<ZegoUIKitUser> users = new ArrayList<>();
            for (String userID : split) {
                String userName = otherUser.getUsername();
                users.add(new ZegoUIKitUser(userID, userName));
            }
            newVideoCall.setInvitees(users);
        });
    }
    private void initVoiceButton() {
        ZegoSendCallInvitationButton newVoiceCall = findViewById(R.id.call_btn);
        newVoiceCall.setIsVideoCall(false);
        newVoiceCall.setOnClickListener(v -> {

            String targetUserID = otherUser.getUserId();
            String[] split = targetUserID.split(",");
            List<ZegoUIKitUser> users = new ArrayList<>();
            for (String userID : split) {
                String userName = otherUser.getUsername();
                users.add(new ZegoUIKitUser(userID, userName));
            }
            newVoiceCall.setInvitees(users);
        });
    }
    void setupChatRecyclerView(){
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });

    }
    void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel==null){
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserID(),otherUser.getUserId()),
                            Timestamp.now(),
                            "",""


                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

                }
            }
        });
    }
    void sendMessageToUser(String message){
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserID());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserID(), Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    messageInput.setText("");
                    sendNotification(message);
                }

            }
        });
    }

    private void sendImageMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("timestamp", FirebaseUtil.timestampToString(Timestamp.now()));
        message.put("message", encoded_Image);
        message.put("senderId", currentUserID);
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(message);

    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            encoded_Image = encodeImage(bitmap);
                            sendImageMessage();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

    );
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 1000;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitMap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitMap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    void sendNotification(String message){

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try{
                    JSONObject jsonObject  = new JSONObject();

                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title",currentUser.getUsername());
                    notificationObj.put("body",message);

                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId",currentUser.getUserId());

                    jsonObject.put("notification",notificationObj);
                    jsonObject.put("data",dataObj);
                    jsonObject.put("to",otherUser.getFcmToken());

                    callApi(jsonObject);


                }catch (Exception e){

                }

            }
        });

    }
    void callVideo(){
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {

            currentUserModel = task.getResult().toObject(UserModel.class);
            assert currentUserModel != null;
            String userName = currentUserModel.getUsername();
            String userId = currentUserModel.getUserId();

            signIn(userId, userName);
        });
    }
    private void signIn(String userID, String userName) {
        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
            return;
        }
        long appID = 661423068;
        String appSign = "28a07604bbf4d439696b812fb8ef50244a63c292cdbc73f33f93eeb6770abf51";
        initCallInviteService(appID, appSign, userID, userName);
    }
    public void initCallInviteService(long appID, String appSign, String userID, String userName) {

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallInvitationService.init(getApplication(), appID, appSign, userID, userName,
                callInvitationConfig);

    }
    void callApi(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAASqJTAdQ:APA91bGSYqemCbqkUD2hSM7HFMNRdDl-HN0EDxcrXKaCjNQJx5CL5qKXCZRYCNbItzQTVbOHhJHbhqMK3w74jfXfkqYNtHhzGiSbYfB39wZ_CQVDCeSdX4O-sXQiU9WZADotgzKhjUMK")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });

    }
    void edittextChanged(){
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageInput.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, 0); // 0 là ID của view không tồn tại
                    messageInput.setLayoutParams(params);
                    camera_btn.setVisibility(View.GONE);
                    camera_btn.setVisibility(View.GONE);
                } else {

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageInput.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                    messageInput.setLayoutParams(params);
                    camera_btn.setVisibility(View.VISIBLE);
                    micro_btn.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUIKitPrebuiltCallInvitationService.unInit();
    }
}