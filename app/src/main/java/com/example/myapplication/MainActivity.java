package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.myapplication.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageButton searchButton;

    ChatFragment chatFragment;
    ProfileFragment profileFragment;
    phonebook phonebook;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();
        phonebook = new phonebook();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchButton = findViewById(R.id.main_search_btn);

        searchButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));

        });

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

        getFCMToken();

    }
    void getFCMToken(){

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            String token = task.getResult();
            Log.i("My token", token);

            FirebaseUtil.currentUserDetails().update("fcmToken", token);
        });
    }

}