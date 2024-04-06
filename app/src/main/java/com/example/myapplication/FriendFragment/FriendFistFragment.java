package com.example.myapplication.FriendFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.adapter.FriendFistRecyclerAdapter;
import com.example.myapplication.adapter.RequestFriendRecyclerAdapter;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;


public class FriendFistFragment extends Fragment {
    RecyclerView recyclerView;
    UserModel userModel;
    FriendFistRecyclerAdapter adapter;

    public FriendFistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend_fist, container, false);
        recyclerView = view.findViewById(R.id.friend_fist_recyclerview);
        userModel = new UserModel();
        setUpRecyclerView();
        return view;
    }
    void setUpRecyclerView(){
        Query query = FirebaseUtil.friend(FirebaseUtil.currentUserID());
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query,UserModel.class).build();
        adapter = new FriendFistRecyclerAdapter(options,getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }
    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }
}