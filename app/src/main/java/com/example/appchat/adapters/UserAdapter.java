package com.example.appchat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.ItemContainerUserBinding;
import com.example.appchat.listeners.UserListener;
import com.example.appchat.models.User;

import java.util.List;

//UserAdapter được sử dụng để hiển thị danh sách người dùng trong một RecyclerView
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

//    Một danh sách các đối tượng User đại diện cho danh sách người dùng cần hiển thị.
    private final List<User> users;

    private final UserListener userListener;


//    Constructor của adapter, nhận danh sách người dùng và khởi tạo adapter với nó.
    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

//    Phương thức được gọi khi adapter cần tạo một ViewHolder mới cho một item trong danh sách.
//    Nó khởi tạo và trả về một UserViewHolder mới được liên kết với một mục ItemContainerUserBinding.
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(itemContainerUserBinding);
    }

//    Phương thức này được gọi để hiển thị dữ liệu của người dùng tại vị trí cụ thể trong danh sách.
//    Nó gọi phương thức setUserData của UserViewHolder để đặt dữ liệu người dùng vào các thành phần giao diện của mục.
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

//    Phương thức này trả về số lượng người dùng trong danh sách.
    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{


    //    Lớp lồng bên trong UserAdapter, đại diện cho một ViewHolder cho mục người dùng.Nó kế thừa từ lớp RecyclerView.
//    ViewHolder và bao gồm một đối tượng ItemContainerUserBinding để truy cập các thành phần giao diện trong mục.
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;

        }

//        Phương thức này được sử dụng để đặt dữ liệu người dùng vào các thành phần giao diện của mục.
//        Nó lấy thông tin từ đối tượng User được truyền vào và gán giá trị tương ứng cho các thành phần trong ItemContainerUserBinding
        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }

    }

//    Phương thức này chuyển đổi một chuỗi hình ảnh đã mã hóa (dưới dạng Base64) thành một đối tượng Bitmap.
//    Nó sử dụng lớp Base64 để giải mã chuỗi và sau đó sử dụng BitmapFactory để tạo đối tượng Bitmap từ dữ liệu đã giải mã.
    private Bitmap getUserImage(String endcodedImage){
        byte[] bytes = Base64.decode(endcodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }

}
