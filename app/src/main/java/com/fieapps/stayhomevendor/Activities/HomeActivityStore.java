package com.fieapps.stayhomevendor.Activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.fieapps.stayhomevendor.Adapters.ViewPagerAdapter;
import com.fieapps.stayhomevendor.Data.OrderData.OrderViewModel;
import com.fieapps.stayhomevendor.Data.ProfileData.ProfileViewModel;
import com.fieapps.stayhomevendor.Interfaces.ActivityFragmentCommunication;
import com.fieapps.stayhomevendor.Models.Geocoding.UserLocation;
import com.fieapps.stayhomevendor.R;
import com.fieapps.stayhomevendor.Utils.Constants;
import com.fieapps.stayhomevendor.Utils.LocationDetectingActivity;
import com.fieapps.stayhomevendor.Utils.MyApplication;
import com.fieapps.stayhomevendor.WebServices.ApiInterface;
import com.fieapps.stayhomevendor.WebServices.ServiceGenerator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivityStore extends LocationDetectingActivity {

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.switch_button)
    SwitchButton switchButton;

    @BindView(R.id.view_pager)
    ViewPager2 viewPager;

    @BindView(R.id.error_layout)
    View errorView;

    @BindView(R.id.error_button)
    Button errorButton;

    private int selectedFragment;

    public OrderViewModel orderViewModel;
    public ProfileViewModel profileViewModel;

    private final String TAG = this.getClass().getSimpleName();
    ActivityFragmentCommunication activityListener;

    private final String IS_PROCESSING = "is_processing";
    private final String SAVED_TOKEN = "saved_token";
    private final String SAVED_ONLINE = "saved_online";


    private boolean isProcessing;
    private String token;
    private boolean online;

    ViewPagerAdapter viewPagerAdapter;

    private boolean doubleBackToExitPressedOnce;


    @Override
    public void onLocationDetected(Location mLocation) {
        ((MyApplication) getApplication()).setLocation(new UserLocation(new double[]{mLocation.getLatitude(), mLocation.getLongitude()},
                null, null, null, null));
        if (activityListener != null)
            activityListener.onLocationDetected();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_store);
        ButterKnife.bind(this);
        if (((MyApplication) getApplication()).getLocation() == null)
            super.getLastLocation(false);

        orderViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        errorButton.setOnClickListener(view -> {
            if (activityListener != null)
                activityListener.reloadFragment();
        });

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setIcon(position == 0 ? R.drawable.ic_assignment_black_24dp : R.drawable.ic_store_black_24dp);
            tab.setText(position == 0 ? "Orders" : "Profile");
        }).attach();


        // TODO: 7/5/20 ADD SAVE INSTANCE STATE FULL IMPLEMENATION
        if (savedInstanceState != null) {
            token = savedInstanceState.getString(SAVED_TOKEN);
            isProcessing = savedInstanceState.getBoolean(IS_PROCESSING);
            online = savedInstanceState.getBoolean(SAVED_ONLINE);
            switchButton.setChecked(online);
            if (isProcessing){
                switchButton.setEnabled(false);
            }else {
                switchButton.setOnCheckedChangeListener(checkListener());
            }
        } else {
            token = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
            if (token != null) {
                onlineState(token, null);
            } else {

            }
        }

    }



    private SwitchButton.OnCheckedChangeListener checkListener() {
        return (view, isChecked) -> {
            if (isProcessing){
                makeToast("Please wait while we process your request");
            }else{
                onlineState(token,isChecked ? 1 : 0);
            }
        };
    }

    private void onlineState(String token, Integer online) {
        if (isProcessing)
            return;

        switchButton.setEnabled(false);
        isProcessing = true;
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<JsonObject> call = online != null ? apiInterface.setOnline(token, online) : apiInterface.amIOnline(token);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (switchButton != null){
                        switchButton.setOnCheckedChangeListener(null);
                        switchButton.setEnabled(true);
                        HomeActivityStore.this.online = (response.body().has("online") && response.body().get("online") != null) && response.body().get("online").getAsBoolean();
                        switchButton.setChecked(HomeActivityStore.this.online);
                        switchButton.setOnCheckedChangeListener(checkListener());
                    }
                }else if (response.code() == 401){
                    Constants.logout(HomeActivityStore.this);
                } else {
                    makeToast("Online update failed");
                    switchButton.setEnabled(true);
                    switchButton.setOnCheckedChangeListener(checkListener());
                }
                isProcessing = false;
            }



            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                makeToast("Online update failed");
                isProcessing = false;
                switchButton.setEnabled(true);
                switchButton.setOnCheckedChangeListener(checkListener());
            }
        });
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    public void setActivityListener(ActivityFragmentCommunication activityListener) {
        this.activityListener = activityListener;
    }

    public void showError(boolean show) {
        errorView.setVisibility(show ? View.VISIBLE : View.GONE);
        viewPager.setVisibility(show ? View.GONE : View.VISIBLE);

    }

    public void showProgress(boolean show) {
        showError(false);
        activityListener.showProgress(show);
//        if (show){
//            shine.startAnimation(Constants.initLoadingAnimation(img,shine));
//        }else {
//            shine.clearAnimation();
//        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PROCESSING,isProcessing);
        outState.putString(SAVED_TOKEN,token);
        outState.putBoolean(SAVED_ONLINE,online);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                finishAndRemoveTask();
//            }else{
//                this.finishAffinity();
//            }
            this.finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


}
