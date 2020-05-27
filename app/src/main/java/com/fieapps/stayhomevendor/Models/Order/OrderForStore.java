package com.fieapps.stayhomevendor.Models.Order;

import androidx.annotation.Keep;

import com.fieapps.stayhomevendor.Models.Address;
import com.fieapps.stayhomevendor.Models.User;
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
