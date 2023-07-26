package com.example.appchat.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.appchat.adapters.RecentConversationAdapter;
import com.example.appchat.databinding.ActivityMainBinding;
import com.example.appchat.listeners.ConversionListener;
import com.example.appchat.models.ChatMessage;
import com.example.appchat.models.User;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    private List<ChatMessage> conversations;

    private RecentConversationAdapter recentConversationAdapter;

    private FirebaseFirestore database;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("eee","onCreate_MainActivity" );

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        init();
        // Tải chi tiết người dùng
        loadUserDetails();

        getToken();
        // xét sự kiện click
        setListeners();

        listenConversations();
    }


    private void init(){
        conversations = new ArrayList<>();

        recentConversationAdapter = new RecentConversationAdapter(conversations, this);

        binding.conversationsRecyclerView.setAdapter(recentConversationAdapter);

        database = FirebaseFirestore.getInstance();


    }

    private void setListeners() {


        Log.e("eee","setListeners_MainActivity" );

        binding.imageSignOut.setOnClickListener(v -> signOut());

        binding.fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
                Log.e("eee","fabNewChat_onClick_MainActivity" );
            }
        });

    }

    private void loadUserDetails() {
        Log.e("eee","loadUserDetails()_onClick_MainActivity" );
        // Hiển thị tên người dùng từ PreferenceManager
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        // Lấy chuỗi Base64 của hình ảnh từ PreferenceManager

        // Giải mã và tạo đối tượng Bitmap từ chuỗi Base64
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        // Đặt hình ảnh vào ImageView
        binding.imageProfile.setImageBitmap(bitmap);

    }

    private void showToast(String message) {
        Log.e("eee","showToast_onClick_MainActivity" );
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId= documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                    }else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId= documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }

                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    conversations.add(chatMessage);

                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i ++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){

                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }


                    }
                }
            }

            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConversationAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);


        }
    });


    private void getToken() {
        Log.e("eee","getToken()_onClick_MainActivity" );
        // Lấy token từ Firebase Messaging
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {

        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);


        Log.e("eee"," updateToken_onClick_MainActivity" );
        // Lấy đối tượng FirebaseFirestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // Lấy tham chiếu tới tài liệu người dùng
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(preferenceManager.getString(Constants.KEY_USER_ID));
        // Cập nhật token trong tài liệu người dùng
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
//                .addOnSuccessListener(unused -> showToast("Token update sucessfuly"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut() {
        Log.e("eee"," signOut()_onClick_MainActivity" );
        showToast("Signing Out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());

        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();

        }).addOnFailureListener(e -> showToast("Unable to sign out "));


    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

}