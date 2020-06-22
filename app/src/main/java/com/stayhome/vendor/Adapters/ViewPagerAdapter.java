package com.stayhome.vendor.Adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.stayhome.vendor.Fragments.OrdersFragment;
import com.stayhome.vendor.Fragments.ProfileFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {


    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        Log.i("FRAGMENT", position + "");

        return position == 0 ? new OrdersFragment() : new ProfileFragment();
    }




    @Override
    public int getItemCount() {
        return 2;
    }
}
