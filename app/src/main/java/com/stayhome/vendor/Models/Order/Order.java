package com.stayhome.vendor.Models.Order;

import androidx.annotation.Keep;

import com.stayhome.vendor.Models.Address;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@Keep
public class Order {

    @SerializedName("_id")
    private String id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("items")
    private ArrayList<OrderItem> items;

    @SerializedName("time")
    private int time;

    @SerializedName("status")
    private int status;

    @SerializedName("address")
    private Address address;

    @SerializedName("slip")
    private String imageUrl;

    @SerializedName("createdAt")
    private String placeDate;

    @SerializedName("distance")
    private double distance;

    public double getDistance() {
        return distance;
    }

    public String getId() {
        return id;
    }

    public ArrayList<OrderItem> getItems() {
        return items;
    }

    public int getTime() {
        return time;
    }

    public int getStatus() {
        return status;
    }

    public Address getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSlug() {
        return slug;
    }

    public String getPlaceDate() {
        return placeDate;
    }



    Order(ArrayList<OrderItem> items, Address address) {
        this.items = items;
        this.address = address;
    }

    Order(String imageUrl, Address address) {
        this.imageUrl = imageUrl;
        this.address = address;
    }
}
