package com.stayhome.vendor.Models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class Category{

    @SerializedName("_id")
    private String id;

    @SerializedName("cat_id")
    private String categoryId;

    @SerializedName("image")
    private String image;

    @SerializedName("image_small")
    private String smallImage;

    @SerializedName("name")
    private String name;

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
