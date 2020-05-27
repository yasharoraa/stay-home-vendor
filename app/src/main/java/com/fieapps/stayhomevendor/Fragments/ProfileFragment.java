package com.fieapps.stayhomevendor.Fragments;

import android.content.Context;
import android.content.Intent;
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
import com.fieapps.stayhomevendor.Activities.HomeActivityStore;
import com.fieapps.stayhomevendor.Models.Store.Store;
import com.fieapps.stayhomevendor.R;
import com.fieapps.stayhomevendor.Utils.Constants;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ProfileFragment extends Fragment {

    @BindView(R.id.image_view)
    ImageView imageView;

    @BindView(R.id.text_View)
    TextView textView;

    @BindView(R.id.email_text_view)
    TextView emailTextView;

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
        emailTextView.setOnClickListener(view -> {
            Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+"indstayhome@gmail.com")); // mention an email id here
            startActivity(Intent.createChooser(sendEmail, "Choose an email client from..."));
        });
        return rootView;
    }

    private void getMyStoreProfile(String token) {
        activity.profileViewModel.init(token);
        activity.profileViewModel.getMyProfile().observe(this, store -> {
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

}
