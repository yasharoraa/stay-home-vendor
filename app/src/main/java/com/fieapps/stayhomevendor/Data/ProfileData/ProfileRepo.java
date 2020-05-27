package com.fieapps.stayhomevendor.Data.ProfileData;

import androidx.lifecycle.MutableLiveData;

import com.fieapps.stayhomevendor.Models.Store.Store;
import com.fieapps.stayhomevendor.WebServices.ApiInterface;
import com.fieapps.stayhomevendor.WebServices.ServiceGenerator;

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
