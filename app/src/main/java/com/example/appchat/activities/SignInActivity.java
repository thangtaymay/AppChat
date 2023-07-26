package com.example.appchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.databinding.ActivitySignInBinding;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

    }

    private void setListeners(){
        // Xử lý sự kiện khi nhấp vào "Create New Account"
        binding.tvCreateNewAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        // Xử lý sự kiện khi nhấp vào "Sign In"
        binding.buttonSignIn.setOnClickListener(v -> {
            // Kiểm tra tính hợp lệ của thông tin đăng nhập
           if (isValidSignInDetails()){
               // Thực hiện đăng nhập
               signIn();
           }
        });

//        binding.buttonSignIn.setOnClickListener(v -> addDataToFirestore());

    }

//    private void addDataToFirestore(){
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("first_name", "Chirag");
//        data.put("last_name","Kachhadiya");
//        database.collection("users").add(data).addOnSuccessListener(documentReference -> {
//            Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show();
//        }).addOnFailureListener(exception -> {
//            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//
//    }

    private void signIn(){
        // Thực hiện đăng nhập

        // Hiển thị hiệu ứng tải
        loading(true);
        // Khởi tạo đối tượng FirebaseFirestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // kết nối
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_EMAIL, binding.edtInputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.edtInputPass.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {

                       // Lấy DocumentSnapshot đầu tiên từ kết quả truy vấn
                       DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                       // Lưu trạng thái đăng nhập vào PreferenceManager
                       preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                       preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                       preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                       preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                       // Chuyển màn đến MainActivity
                       Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);

                   }else {
                       // Đăng nhập thất bại
                       loading(false);
                       showToat("Unable to sign in");
                   }

                });


    }

    private void loading(Boolean isLoading){
        if (isLoading){
            // ẩn nút đăng nhập
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            // hiển thị thanh tiến trình
            binding.progressBar.setVisibility(View.VISIBLE);

        }else {
            // hiển thị nút đăng nhập
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            // ẩn thanh tiến trình
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToat(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails() {
        if (binding.edtInputEmail.getText().toString().trim().isEmpty()) {
            showToat("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtInputEmail.getText().toString()).matches()) {
            showToat("enter valid email");
            return false;
        }else if (binding.edtInputPass.getText().toString().trim().isEmpty()) {
            showToat("Enter password");
            return false;
        }else {
            return true;
        }

    }




}