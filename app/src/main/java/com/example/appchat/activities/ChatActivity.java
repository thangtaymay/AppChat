package com.example.appchat.activities;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.appchat.adapters.ChatAdapter;
import com.example.appchat.databinding.ActivityChatBinding;
import com.example.appchat.models.ChatMessage;
import com.example.appchat.models.User;
import com.example.appchat.network.ApiClient;
import com.example.appchat.network.ApiService;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;

    private User receiverUser;

    private List<ChatMessage> chatMessages;

    private ChatAdapter chatAdapter;

    private PreferenceManager preferenceManager;

    private FirebaseFirestore database;

    private String conversionId = null;

    private Boolean isReceiverAvailable = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        setListeners();

        loadReceiverDetails();

        init();

        listenMessages();

    }

    //init() : Phương thức này khởi tạo các thành phần khác nhau cần thiết cho chức năng chat.
    // Nó thiết lập RecyclerView để hiển thị các tin nhắn chat, tạo một ChatAdapter để xử lý dữ liệu tin nhắn,
    // và khởi tạo cơ sở dữ liệu Firebase Firestore để lưu trữ và lấy tin nhắn.
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerview.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }

    //sendMessage() : Phương thức này được gọi khi người dùng gửi một tin nhắn. Nó tạo một HashMap chứa thông tin cần thiết về tin nhắn,
    // chẳng hạn như ID người gửi, ID người nhận, nội dung tin nhắn và thời gian dấu thời gian.
    // Tin nhắn sau đó được thêm vào cơ sở dữ liệu Firestore trong bộ sưu tập "chat". Nếu đây là một cuộc trò chuyện hiện có (conversion),
    // phương thức sẽ cập nhật tin nhắn cuối cùng và thời gian; nếu không, nó sẽ tạo một cuộc trò chuyện mới.
    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());

            addConversion(conversion);
        }
        if (!isReceiverAvailable ){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());


                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);


                sendNotification(body.toString());

            }catch (Exception e){
                showToat(e.getMessage());
            }
        }
        binding.inputMessage.setText(null);
//        timestamp.date(2023, 8, 6);request.time < timestamp.date(2023, 8, 6)
    }


    private void showToat(String messsage){
        Toast.makeText(this, messsage, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                        try {
                                if (response.body() != null){
                                    JSONObject responseJson = new JSONObject(response.body());
                                    JSONArray results = responseJson.getJSONArray("results");
                                    if (responseJson.getInt("failure") == 1){
                                        JSONObject error = (JSONObject) results.get(0);
                                        showToat(error.getString("error"));
                                        return;
                                    }
                                }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        showToat("Notification sent successfully");
                }else {
                    showToat("Error : "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                showToat(t.getMessage());
            }
        });

    }

    // listenAvailabilityOfReceiver() : Phương thức này lắng nghe sự thay đổi trong trạng thái sẵn có của người nhận (đang trực tuyến/ ngoại tuyến).
    // Nó gắn một bộ lắng nghe snapshot vào bộ sưu tập "users" trong Firestore, cụ thể là tài liệu đại diện cho người dùng người nhận.
    // Nếu sẵn có thay đổi về tình trạng sẵn có của người nhận (được biểu thị bằng trường "availability" trong Firestore),
    // giao diện người dùng sẽ được cập nhật để hiển thị xem người nhận có sẵn có hay không.

    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USER).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;

                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
            }
            if (isReceiverAvailable){
                binding.textAvailability.setVisibility(View.VISIBLE);

            }else {
                binding.textAvailability.setVisibility(View.GONE);

            }


        }));
    }

    //Phương thức listenMessages():
    //Phương thức này lắng nghe các tin nhắn chat mới trong Firestore. Nó gắn hai bộ lắng nghe snapshot vào bộ sưu tập "chat":
    // một cho các tin nhắn được gửi bởi người dùng và một cho các tin nhắn được gửi bởi người nhận. Khi có tin nhắn mới được thêm vào,
    // chúng được xử lý và hiển thị trong chat.
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id).addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    // xử lý sự kiện khi có sự thay đổi trong bộ sưu tập "chat" của Firestore
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {//Đây là một biến tham chiếu đến một EventListener của Firestore.
        // EventListener này được sử dụng để lắng nghe sự kiện khi có thay đổi trong bộ sưu tập "chat" của Firestore.
        if (error != null){
            return;//Khi có sự thay đổi trong bộ sưu tập "chat", hàm được thực thi và nhận vào hai tham số value và error. Ta bắt đầu xử lý như sau:
//            Đầu tiên, mã kiểm tra xem có lỗi xảy ra không. Nếu có lỗi, nó sẽ thoát khỏi hàm.
        }
        if (value != null){
            //Nếu không có lỗi, mã kiểm tra xem giá trị value (QuerySnapshot) có tồn tại hay không. Sau đó, nó thực hiện việc xử lý dữ liệu.
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
//Lấy thông tin tin nhắn:
//Dựa vào loại thay đổi của tài liệu (DocumentChange.Type.ADDED), mã tạo một đối tượng ChatMessage và lấy thông tin liên quan từ tài liệu Firestore.
// Các thông tin bao gồm người gửi (senderId), người nhận (receiverId), nội dung tin nhắn (message), thời gian tin nhắn (dateTime) và đối tượng thời gian tin nhắn (dateObject).
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    chatMessages.add(chatMessage);

                }

            }
//            Sắp xếp tin nhắn:
//            Sau khi lấy thông tin tin nhắn, danh sách chatMessages sẽ được sắp xếp dựa trên thời gian tin nhắn. Tin nhắn mới nhất sẽ được đưa lên đầu danh sách.
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0){
                //Cập nhật giao diện người dùng:
                //Nếu đây là lần đầu tiên load dữ liệu (count == 0), chatAdapter sẽ được thông báo để cập nhật giao diện.
                // Nếu không, nó sẽ thông báo cập nhật mục của danh sách chatMessages và di chuyển RecyclerView đến vị trí cuối cùng.
                chatAdapter.notifyDataSetChanged();

            }else {
                chatAdapter.notifyItemChanged(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerview.smoothScrollToPosition(chatMessages.size() - 1);

            }
            binding.chatRecyclerview.setVisibility(View.VISIBLE);
            //Hiển thị RecyclerView:
            //Sau khi đã xử lý xong, RecyclerView sẽ được hiển thị (setVisibility(View.VISIBLE)).

        }
        binding.progressBar.setVisibility(View.GONE);
        //Tắt ProgressBar:
        //ProgressBar (đang được hiển thị khi dữ liệu đang tải) sẽ bị ẩn (setVisibility(View.GONE)).
        if (conversionId == null){
            checkForConversion();
            //Kiểm tra conversionId:
            //Nếu conversionId (ID của cuộc trò chuyện) chưa được xác định, hàm checkForConversion() sẽ được gọi để kiểm tra xem cuộc trò chuyện đã tồn tại hay chưa.
        }

    };

    //Phương thức getBitmapFromEncodedString():
    //Phương thức tiện ích này chuyển đổi một chuỗi mã hóa Base64 thành hình ảnh Bitmap. Mã sử dụng nó để hiển thị hình ảnh của người nhận trong cuộc trò chuyện.
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }
    //Phương thức loadReceiverDetails():
    //Phương thức này tải thông tin về người nhận và hiển thị tên của họ trong giao diện hoạt động chat.
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

    }

    //Phương thức setListeners():
    //Phương thức này thiết lập các bộ lắng nghe nhấp chuột cho các yếu tố giao diện người dùng như nút trở lại và nút gửi.
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    //Phương thức getReadableDateTime():
    //Một phương thức tiện ích để chuyển đổi một đối tượng Date thành một chuỗi thời gian ngày và giờ dễ đọc.
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);

    }

    //Phương thức addConversion():
    //Phương thức này thêm một cuộc trò chuyện mới vào bộ sưu tập "conversations" trong Firestore.
    // Nó lưu trữ thông tin về người gửi, người nhận và tin nhắn cuối cùng được trao đổi trong cuộc trò chuyện.
    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());

    }

    //Phương thức updateConversion():
    //Phương thức này cập nhật một cuộc trò chuyện hiện có trong Firestore với tin nhắn mới nhất và dấu thời gian.
    // Nó được gọi khi một tin nhắn mới được gửi trong cuộc trò chuyện hiện có.
    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
                          documentReference.update(
                                  Constants.KEY_LAST_MESSAGE, message,
                                  Constants.KEY_TIMESTAMP, new Date()
                          );

    }

    //Phương thức checkForConversion():
    //Phương thức này kiểm tra xem có cuộc trò chuyện nào giữa người dùng hiện tại và người nhận hay không.
    // Nó thực hiện điều này bằng cách truy vấn bộ sưu tập "conversations" với cả ID người gửi và ID người nhận. Nếu tìm thấy cuộc trò chuyện, conversionId sẽ được cập nhật.

    private void checkForConversion(){
        if (chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)

            );

        }
    }

    //Phương thức checkForConversionRemotely():
    //Phương thức này truy vấn Firestore để kiểm tra xem có cuộc trò chuyện nào giữa hai người dùng (người gửi và người nhận) hay không.
    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);

    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();

        }
    };

    //Phương thức onResume():
    //Phương thức này được gọi khi hoạt động được khôi phục.
    // Nó gọi listenAvailabilityOfReceiver() để cập nhật trạng thái sẵn có của người nhận khi người dùng trở lại hoạt động chat.
    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

}