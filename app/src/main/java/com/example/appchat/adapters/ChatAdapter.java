package com.example.appchat.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchat.databinding.ItemContainerReceivedMessageBinding;
import com.example.appchat.databinding.ItemContainerSentMessageBinding;
import com.example.appchat.databinding.ItemContainerUserBinding;
import com.example.appchat.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List< ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderId;

    // hằng số dùng để xác định kiểu giao diện của ViewHolder trong RecyclerView
    public static final int VIEW_TYPE_SENT = 1; // sẽ được sử dụng khi tin nhắn là của người gửi
    public static final int VIEW_TYPE_RECEIVED = 2; //sẽ được sử dụng khi tin nhắn là của người nhận.

//    khởi tạo hàm contracter
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages; //Danh sách các tin nhắn (ChatMessage) sẽ được hiển thị trong RecyclerView.
        this.receiverProfileImage = receiverProfileImage; //Hình ảnh bitmap của người nhận tin nhắn, sẽ hiển thị trong trường hợp tin nhắn được nhận.
        this.senderId = senderId; //: Một chuỗi đại diện cho ID của người gửi tin nhắn (có thể là mã duy nhất của người dùng).
        // Dùng để xác định xem mỗi tin nhắn thuộc về người gửi nào.
    }

//    Được gọi khi RecyclerView cần tạo một ViewHolder mới để hiển thị các item trong danh sách.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //parent: ViewGroup chứa ViewHolder mới được tạo ra.
        //viewType: Kiểu của ViewHolder, sẽ được sử dụng để xác định xem ViewHolder nào sẽ được tạo (gửi hay nhận tin nhắn)
        //Returns: ViewHolder mới tạo ra.

        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(ItemContainerSentMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));

        }else {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));

        }

    }
//    onBindViewHolder: Được gọi khi RecyclerView muốn hiển thị dữ liệu (chatMessages) trong ViewHolder tại vị trí (position) cụ thể.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        holder: ViewHolder cần được hiển thị dữ liệu.
//        position: Vị trí của item trong danh sách chatMessages.

        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));

        }else {
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position), receiverProfileImage);
        }

    }

    @Override
    public int getItemCount() {
//        getItemCount: Được gọi để trả về tổng số lượng item trong danh sách chatMessages.
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
//        getItemViewType: Được gọi để xác định kiểu của ViewHolder (gửi hay nhận tin nhắn) dựa trên vị trí (position) của item trong danh sách chatMessages.
//        position: Vị trí của item trong danh sách chatMessages.
//        Returns: Kiểu của ViewHolder tại vị trí cụ thể.
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

//         SentMessageViewHolder: Đây là các lớp con của RecyclerView.ViewHolder,đại diện cho một kiểu ViewHolder tương ứng với tin nhắn gửi
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
//            setData để hiển thị dữ liệu tin nhắn tương ứng vào ViewHolder
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

//        ReceivedMessageViewHolder, cũng hiển thị hình ảnh của người nhận tin nhắn.

        private final ItemContainerReceivedMessageBinding binding;

//         ReceivedMessageViewHolder :  Đây là các lớp con của RecyclerView.ViewHolder,đại diện cho một kiểu ViewHolder tương ứng với tin nhắn nhận
        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());

            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
//            setData để hiển thị dữ liệu tin nhắn tương ứng vào ViewHolder
            binding.textMessage.setText(chatMessage.message);

            binding.textDateTime.setText(chatMessage.dateTime);

            binding.imageProfile.setImageBitmap(receiverProfileImage);

        }
    }
}
