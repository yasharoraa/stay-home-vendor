package com.stayhome.vendor.Models.Order;

import androidx.annotation.Keep;

import com.stayhome.vendor.Models.Address;
import com.stayhome.vendor.Models.User;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@Keep
public class OrderForStore extends Order {


    @SerializedName("buyer")
    private User buyer;

    public OrderForStore(ArrayList<OrderItem> items, Address address) {
        super(items, address);
    }

    public OrderForStore(String imageUrl, Address address) {
        super(imageUrl, address);
    }

    public User getBuyer() {
        return buyer;
    }
}
