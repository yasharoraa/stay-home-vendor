package com.fieapps.stayhomevendor.Data.ProfileData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fieapps.stayhomevendor.Models.Store.Store;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<Store> profileLiveData;

    private ProfileRepo profileRepo;

    public void init(String token){
        if (profileLiveData != null && profileLiveData.getValue()!=null)
            return;

        profileRepo = ProfileRepo.getInstance();
        profileLiveData = profileRepo.getStore(token);
    }

    public LiveData<Store> getMyProfile() { return profileLiveData; }
}
