package com.fieapps.stayhomevendor.Models.Store;

import com.fieapps.stayhomevendor.Models.Category;
import com.google.gson.annotations.SerializedName;

public class Store extends BaseStore {

    @SerializedName("cat")
    private Category[] categories;

    public Store(String id,String name, String owner, String address, String image, boolean online, Integer pincode, double distance,Category[] categories) {
        super(id,name, owner, address, image, online, pincode, distance);
        setCategories(categories);
    }

    public void setCategories(Category[] categories) {
        this.categories = categories;
    }

    public Category[] getCategories() {
        return categories;
    }
}
