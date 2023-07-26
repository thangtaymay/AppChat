package com.example.appchat.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context){
        // Khởi tạo đối tượng SharedPreferences với tên và chế độ riêng tư
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);

    }

    public void putBoolean(String key, Boolean value){
        // Lưu giá trị boolean vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();

    }

    public Boolean getBoolean(String key){
        // Lấy giá trị boolean từ SharedPreferences
        return sharedPreferences.getBoolean(key,false);

    }

    public void putString(String key, String value){
        // Lưu giá trị chuỗi vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();

    }

    public String getString(String key){
        // Lấy giá trị chuỗi từ SharedPreferences
        return  sharedPreferences.getString(key,null);

    }

    public void clear(){
        // Xóa tất cả các giá trị trong SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }

}
