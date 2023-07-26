package com.example.appchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.appchat.R;
import com.example.appchat.adapters.UserAdapter;
import com.example.appchat.databinding.ActivityUsersBinding;
import com.example.appchat.listeners.UserListener;
import com.example.appchat.models.User;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {


    private ActivityUsersBinding binding;

    private PreferenceManager preferenceManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();

        getUsers();

        Log.e("eee","onCreate_UsersActivity");

    }


    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        Log.e("eee","setListeners_UsersActivity");

    }



    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER).get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUsreId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (currentUsreId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }

                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);

                        }

                        if (users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);


                        } else {
                                showErrorMessage();
                                }

                    }else {
                        showErrorMessage();
                    }

                });

    }

    private void showErrorMessage(){

        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
        Log.e("eee","showErrorMessage_UsersActivity");
    }


    private void loading(Boolean isLoading) {
        Log.e("eee","loading_UsersActivity");
        if (isLoading) {
            // hiển thị thanh tiến trình
            binding.progressBar.setVisibility(View.VISIBLE);

        } else {
            // ẩn thanh tiến trình
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onUserClicked(User user) {

        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();


    }




}