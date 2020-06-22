package com.stayhome.vendor.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stayhome.vendor.Activities.HomeActivityStore;
import com.stayhome.vendor.Activities.MainActivity;
import com.stayhome.vendor.Activities.SupportActivity;
import com.stayhome.vendor.Models.Store.Store;
import com.stayhome.vendor.R;
import com.stayhome.vendor.Utils.Constants;
import com.stayhome.vendor.Utils.SafeClickFragment;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ProfileFragment extends SafeClickFragment {

    @BindView(R.id.image_view)
    ImageView imageView;

    @BindView(R.id.text_View)
    TextView textView;

    @BindView(R.id.support_text_view)
    TextView supportTextView;

    @BindView(R.id.about_us_text_view)
    TextView aboutUsTextView;

    @BindView(R.id.sign_out_text)
    TextView signOutTextView;

    private HomeActivityStore activity;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile,container,false);
        activity = (getActivity() instanceof HomeActivityStore) ? (HomeActivityStore) getActivity() : null;
        if (activity == null) return null;
        unbinder = ButterKnife.bind(this, rootView);
        String token = activity.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
        getMyStoreProfile(token);
        supportTextView.setOnClickListener(this);
        aboutUsTextView.setOnClickListener(this);
        signOutTextView.setOnClickListener(this);
        return rootView;
    }

    private void getMyStoreProfile(String token) {
        activity.profileViewModel.init(token);
        activity.profileViewModel.getMyProfile().observe(activity, store -> {
            if (store == null) return;
            setupData(store);
        });
    }
    private void setupData(Store store){
        textView.setText(
                String.format(Locale.getDefault(),
                        "%s\nPhone Number : %s\nOwner : %s\nGSTIN : %s\nAddress : %s\n%s %d",
                        store.getName(), store.getPhone(), store.getOwner(), store.getGstin(), store.getAddress(), store.getCity(), store.getPincode())
        );
        if(store.getImage()==null) return;
        Uri uri = Uri.parse(store.getImage());
        Glide.with(this).load(uri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }

    @Override
    public void onSafeClick(View view) {
        switch (view.getId()) {
            case R.id.support_text_view:
                startActivity(new Intent(activity, SupportActivity.class));
                break;
            case R.id.about_us_text_view:
                String url = "https://www.innovvapp.com/about";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.sign_out_text:
                logout();
                break;

        }
    }
    private void logout() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.TOKEN))
            sharedPreferences.edit().remove(Constants.TOKEN).apply();

        startActivity(new Intent(activity, MainActivity.class));
    }

}
