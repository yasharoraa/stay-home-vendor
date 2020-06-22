package com.stayhome.vendor.Utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.stayhome.vendor.Models.Geocoding.Reverse;
import com.stayhome.vendor.Models.Geocoding.UserLocation;
import com.stayhome.vendor.WebServices.ApiInterface;
import com.stayhome.vendor.WebServices.GeoCodingApi;
import com.stayhome.vendor.WebServices.ServiceGenerator;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.stayhome.vendor.Utils.Constants.CITY;
import static com.stayhome.vendor.Utils.Constants.PIN_CODE;
import static com.stayhome.vendor.Utils.Constants.findComponent;

public abstract class PlaceDetectingActivity extends LocationDetectingActivity {

    private final String TAG  = this.getClass().getSimpleName();

    @Override
    public void onLocationDetected(Location mLocation) {
        getPlace(mLocation);
    }

    private void getPlace(Location location){
        ServiceGenerator.createService(ApiInterface.class).getMyKey(getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null))
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body()!=null && response.body().get("key") !=null){
                            ServiceGenerator.createGeoService(GeoCodingApi.class).
                                    executeReverseGeoCoding(location.getLatitude() + "," + location.getLongitude(), response.body().get("key").getAsString())
                                    .enqueue(new Callback<Reverse>() {
                                        @Override
                                        public void onResponse(Call<Reverse> call, Response<Reverse> response) {
                                            if (response.isSuccessful() && response.body()!=null) {
                                                Reverse reverse = response.body();
                                                String pincode =
                                                        (reverse.getResults().getAddressComponents().get(reverse.getResults().getAddressComponents().size() - 1).getTypes().contains(PIN_CODE)) ?
                                                                reverse.getResults().getAddressComponents().get(reverse.getResults().getAddressComponents().size() - 1).getLongName() :
                                                                findComponent(reverse.getResults().getAddressComponents(), PIN_CODE);

                                                String city = (reverse.getResults().getAddressComponents().get(reverse.getResults().getAddressComponents().size() - 3).getTypes().contains(CITY)) ?
                                                        reverse.getResults().getAddressComponents().get(reverse.getResults().getAddressComponents().size() - 3).getLongName() :
                                                        findComponent(reverse.getResults().getAddressComponents(), CITY);

                                                OnPlaceDetected(new UserLocation(new double[] {location.getLatitude(),location.getLongitude()},
                                                        reverse.getResults().getFormattedAddress(),
                                                        reverse.getResults().getPlaceId(),
                                                        city,
                                                        pincode), null);
                                            }else{
                                                createTryAgainExitDialog(location,"Error fetching address !");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Reverse> call, Throwable t) {
                                            Log.i("LOCATION", "RESPONSE" + t.getMessage() + t.getCause());
                                            createTryAgainExitDialog(location,"Please check your network connection and try again !");
                                        }
                                    });
                        } else {
                            createTryAgainExitDialog(location,"Error fetching address !");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        createTryAgainExitDialog(location,"Please check your network connection and try again !");
                        Log.i(TAG,t.getMessage() + t.getCause());
                    }
                });

    }

    private void createTryAgainExitDialog(Location location, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setPositiveButton("Try Again", (dialogInterface, i) -> getPlace(location));
        builder.setNegativeButton("Exit", (dialogInterface, i) -> this.finishAffinity());
        builder.create().show();
    }


    public abstract void OnPlaceDetected(UserLocation userLocation, String error);
}
