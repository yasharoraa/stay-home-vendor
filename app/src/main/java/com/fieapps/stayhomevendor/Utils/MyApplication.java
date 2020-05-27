package com.fieapps.stayhomevendor.Utils;

import android.app.Application;

import com.fieapps.stayhomevendor.Models.Geocoding.UserLocation;

public class MyApplication extends Application {

    private UserLocation userLocation;

    private boolean isNewOrderPlaced;

    public UserLocation getLocation() {
        return userLocation;
    }

    public void setLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public boolean isNewOrderPlaced() {
        return isNewOrderPlaced;
    }

    public void setNewOrderPlaced(boolean newOrderPlaced) {
        isNewOrderPlaced = newOrderPlaced;
    }
}
