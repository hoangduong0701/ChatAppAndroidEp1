package com.example.myapplication.FriendFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.adapter.FriendFragmentAdapter;
import com.google.android.material.tabs.TabLayout;

public class FriendFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager viewPager;



    public FriendFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout.setupWithViewPager(viewPager);
        FriendFragmentAdapter adapter = new FriendFragmentAdapter(getChildFragmentManager(), FriendFragmentAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new FriendFistFragment(),"Bạn bè");
        adapter.addFragment(new RequestFriendFragment(), "Lời mời");
        viewPager.setAdapter(adapter);
        return view;

    }
}