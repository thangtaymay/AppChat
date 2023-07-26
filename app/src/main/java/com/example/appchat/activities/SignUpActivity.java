package com.example.appchat.activities;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.appchat.R;
import com.example.appchat.databinding.ActivitySignUpBinding;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private PreferenceManager preferenceManager;
    private String encodedImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
    }

    private void setListeners(){
        binding.tvSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDeltails()){
                signUp();

            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });

    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // signup thực hiện quá trình đăng ký
    private void signUp(){
        // Hiển thị hiệu ứng tải
        loading(true);
        // Khởi tạo đối tượng FirebaseFirestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // Tạo đối tượng HashMap chứa thông tin người dùng
        HashMap<String,Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.edtInputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.edtInputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.edtInputPass.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);

        // Thêm người dùng vào collection trong Firebase Firestore
        database.collection(Constants.KEY_COLLECTION_USER).add(user).addOnSuccessListener(documentReference -> {
            // Xử lý thành công
            loading(false);
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
            preferenceManager.putString(Constants.KEY_NAME, binding.edtInputName.getText().toString());
            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }).addOnFailureListener(exception -> {
            // Xử lý lỗi
            loading(false);
            showToast(exception.getMessage());

        });

    }

    private String encodeImage(Bitmap bitmap){
        // Tính toán kích thước hình ảnh xem trước dựa trên kích thước mong muốn
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        // Tạo hình ảnh xem trước với kích thước mong muốn
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        // Chuẩn bị để nén hình ảnh thành byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        // Chuyển đổi byte array thành chuỗi base64
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);

    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
        if (result.getResultCode() == RESULT_OK){
            if (result.getData() != null){
                // Lấy Uri của hình ảnh được chọn
                Uri imageUri = result.getData().getData();
                try {
                    // Mở đầu vào luồng dữ liệu từ Uri
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    // Giải mã và tạo đối tượng Bitmap từ luồng dữ liệu
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    // Đặt hình ảnh vào ImageView trong giao diện
                    binding.imageProfile.setImageBitmap(bitmap);
                    // Ẩn văn bản "Thêm hình ảnh"
                    binding.textAddimage.setVisibility(View.GONE);
                    // Mã hóa hình ảnh thành chuỗi Base64
                    encodedImage = encodeImage(bitmap);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    });


    private boolean isValidSignUpDeltails(){
        if (encodedImage == null){
            showToast("Select profile image");
            return false;
        } else if (binding.edtInputName.getText().toString().trim().isEmpty()) {
            // Xử lý khi trường tên rỗng sau khi loại bỏ khoảng trắng
            showToast("Enter name");
            return false;
        } else if (binding.edtInputEmail.getText().toString().trim().isEmpty()) {
            // Xử lý khi trường email rỗng sau khi loại bỏ khoảng trắng
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtInputEmail.getText().toString()).matches()) {
            // Xử lý khi giá trị nhập vào edtInputEmail không phù hợp với định dạng chuẩn email
            showToast("Enter valid image");
            return false;
        } else if (binding.edtInputPass.getText().toString().trim().isEmpty()) {
            // Xử lý khi trường mật khẩu trống
            showToast("Enter password");
            return false;
        } else if (binding.edtInputConfirmPass.getText().toString().trim().isEmpty()) {
            // Xử lý khi trường xác nhận mật khẩu trống
            showToast("Confirm you password");
            return false;

        } else if (!binding.edtInputPass.getText().toString().equals(binding.edtInputConfirmPass.getText().toString())) {
            // Xử lý khi mật khẩu và xác nhận mật khẩu không khớp
            showToast("Password and Confirpassword must be same");
            return false;
        }else {
            return true;
        }

    }

    private void loading(Boolean isLoading){
        if (isLoading){
            // ẩn nút đăng ký
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            // hiển thị thanh tiến trình
            binding.progressBar.setVisibility(View.VISIBLE);

        }else {
            // hiển thị nút đăng ký
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            // ẩn thanh tiến trình
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }



}