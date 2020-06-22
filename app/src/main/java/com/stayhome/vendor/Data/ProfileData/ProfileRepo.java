package com.stayhome.vendor.Data.ProfileData;

import androidx.lifecycle.MutableLiveData;

import com.stayhome.vendor.Models.Store.Store;
import com.stayhome.vendor.WebServices.ApiInterface;
import com.stayhome.vendor.WebServices.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepo {

    private static ProfileRepo profileRepo;

    static ProfileRepo getInstance() {
        if (profileRepo == null){
            profileRepo =  new ProfileRepo();
        }
        return profileRepo;
    }

    private ApiInterface apiInterface;

    private  ProfileRepo() {apiInterface = ServiceGenerator.createService(ApiInterface.class);}

    MutableLiveData<Store> getStore(String token) {
        MutableLiveData<Store> storeData = new MutableLiveData<>();
        apiInterface.getMyStoreProfile(token).enqueue(new Callback<Store>() {
            @Override
            public void onResponse(Call<Store> call, Response<Store> response) {
                if (response.isSuccessful() && response.body()!=null){
                    storeData.setValue(response.body());
                }else{
                    storeData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<Store> call, Throwable t) {
                storeData.setValue(null);
            }
        });
        return storeData;
    }
}
