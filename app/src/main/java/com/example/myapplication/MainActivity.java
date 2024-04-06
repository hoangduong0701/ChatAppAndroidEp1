package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.FriendFragment.FriendFragment;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import BotAi.BotActivity;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageButton searchButton;
    ImageButton menuBtn;
    DrawerLayout drawerLayout;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;
    FriendFragment phonebook;
    NavigationView navigationView;

    UserModel currentUserModel;
    TextView textView;
    ImageView imageView;
    UserModel userModel;
    ProgressBar progressBar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();
        phonebook = new FriendFragment();
        searchButton = findViewById(R.id.main_search_btn);
        menuBtn = findViewById(R.id.main_menu_btn);
        drawerLayout =findViewById(R.id.drawrlayout);
        navigationView = findViewById(R.id.navigationview);




        searchButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));

        });
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);

            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (drawerLayout != null){
                    drawerLayout.closeDrawers();
                }
                if (item.getItemId() == R.id.navChat){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();

                }
                if (item.getItemId() == R.id.navChatGpt){
                    Intent intent = new Intent(MainActivity.this, BotActivity.class);
                    startActivity(intent);

                }
                if (item.getItemId() == R.id.navAbout){
//                    Intent intent = new Intent(MainActivity.this, com.example.myapplication.test.MainActivity.class);
//                    startActivity(intent);

                }
                return true;

            }

        });



        bottomNavigationView();
        getDataHeaderNav();
        getFCMToken();

    }



    void getDataHeaderNav(){
        navigationView = findViewById(R.id.navigationview);
        View headerView = navigationView.getHeaderView(0);
        imageView = headerView.findViewById(R.id.imageViewUser);
        textView = headerView.findViewById(R.id.nameUser);
        progressBar = headerView.findViewById(R.id.profile_progress_bar);
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            userModel = task.getResult().toObject(UserModel.class);
            textView.setText(userModel.getUsername());


        });
        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl().
                addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Uri uri = task.getResult();
                        AndroidUtil.setProfilePic(MainActivity.this, uri, imageView);
                    }
                });

    }
    void setInProgress(boolean inProgress){

        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);

        }else {
            progressBar.setVisibility(View.GONE);

        }
    }
    void bottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_chat){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();

                }
                if (item.getItemId() == R.id.menu_phonebook){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, phonebook).commit();

                }

                if (item.getItemId() == R.id.menu_profile){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();

                }

                return true;

            }
        });
        bottomNavigationView.setSelectedItemId(R.id.menu_chat);

    }



    void getFCMToken(){

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            String token = task.getResult();
            Log.i("My token", token);
            FirebaseUtil.currentUserDetails().update("fcmToken", token);
        });
    }

}