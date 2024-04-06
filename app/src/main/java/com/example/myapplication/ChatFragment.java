package com.example.myapplication;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.app.Dialog;
import android.content.Context;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import com.example.myapplication.adapter.*;
import com.example.myapplication.model.ChatroomModel;
import com.example.myapplication.test.SwipeHelperLeft;
import com.example.myapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;


public class ChatFragment extends Fragment {
    RecyclerView recyclerView;
    RecentChatRecyclerAdapter adapter;
    Dialog dialog;
    ChatroomModel chatroomModel;

    public ChatFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatroomModel = new ChatroomModel();
        recyclerView = view.findViewById(R.id.recyler_view);
        setupRecyclerView();
        swipeHelperLeft();
        return view;

    }

    void setupRecyclerView(){
        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserID())
                .orderBy("lastMessageTimestamp",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query,ChatroomModel.class).build();

        adapter = new RecentChatRecyclerAdapter(options,getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    void swipeHelperLeft(){
        SwipeHelperLeft swipeHelperleft = new SwipeHelperLeft(getContext(), recyclerView, adapter, chatroomModel) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelperLeft.UnderlayButton(
                        getContext(),
                        "Lưu trữ",
                        R.drawable.luutru,
                        Color.parseColor("#CB35D3"),
                        new SwipeHelperLeft.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                // TODO: onArchive
                                Toast.makeText(getContext(), "Archive", Toast.LENGTH_SHORT).show();
                            }
                        }
                ));

                underlayButtons.add(new SwipeHelperLeft.UnderlayButton(
                        getContext(),
                        "Delete",
                        R.drawable.baseline_delete_24,
                        Color.parseColor("#FE3B30"),

                        new SwipeHelperLeft.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                // TODO: onDelete


                                deletedialog();
                            }
                        }
                ));
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHelperleft);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void deletedialog(){
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_dialog_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.getWindow().setBackgroundDrawable(getDrawable(getContext(), R.drawable.dialog_backgroud));
        }

        Window window =  dialog.getWindow();
        if (window != null) {
            ViewGroup.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = dpToPx(getContext(), 280);
            layoutParams.height = dpToPx(getContext(), 130);
            window.setAttributes((android.view.WindowManager.LayoutParams) layoutParams);
        }

        Button delete = dialog.findViewById(R.id.btn_delete_dialog);
        Button cancel = dialog.findViewById(R.id.btn_cancel);


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String id = chatroomModel.getChatroomId();
                Toast.makeText(getContext(), "Chat:" + id, Toast.LENGTH_SHORT).show();

                FirebaseUtil.deletechat(id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                document.getReference().delete();
                            }
                            FirebaseUtil.deletechatCollection().document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){


                                    }else {
                                        Log.w("TAG", "khong xoa dc.", task.getException());
                                    }
                                }
                            });
                            dialog.dismiss();

                        }else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
 