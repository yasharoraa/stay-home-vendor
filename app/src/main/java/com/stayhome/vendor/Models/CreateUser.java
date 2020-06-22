package com.stayhome.vendor.Models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class CreateUser {

    @SerializedName("user")
    User user;


    public User getUser() {
        return user;
    }

    public CreateUser(String phone, String password) {
        this.user = new User(phone,password);
    }

    public class User {
        @SerializedName("phone")
        private String phone;

        @SerializedName("password")
        private String password;

        User(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }

        public String getPhone() {
            return phone;
        }

        public String getPassword() {
            return password;
        }
    }
}
