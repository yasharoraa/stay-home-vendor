package com.fieapps.stayhomevendor.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.fieapps.stayhomevendor.Activities.HomeActivityStore;
import com.fieapps.stayhomevendor.Activities.ViewOrderActivity;
import com.fieapps.stayhomevendor.Adapters.OrdersAdapter;
import com.fieapps.stayhomevendor.Interfaces.ActivityFragmentCommunication;
import com.fieapps.stayhomevendor.Interfaces.OrderClickListener;
import com.fieapps.stayhomevendor.R;
import com.fieapps.stayhomevendor.Utils.Constants;
import com.fieapps.stayhomevendor.Utils.MyApplication;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class OrdersFragment extends Fragment implements ActivityFragmentCommunication, OrderClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.progress_blocks)
    ShimmerFrameLayout shimmerLayout;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;



    private Unbinder unbinder;
    private HomeActivityStore activity;
    private OrdersAdapter ordersAdapter;
    private LinearLayoutManager layoutManager;
    private String token;
    private int offset;
    private boolean clearFirst;

    private final String SAVED_TOKEN = "saved_token";
    private final String SAVED_OFFSET = "saved_offset";
    private final String SAVED_CLEAR_FIRST = "saved_clear_first";

    private final String TAG = this.getClass().getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_orders, container, false);
        activity = (getActivity() instanceof HomeActivityStore) ? (HomeActivityStore) getActivity() : null;
        if (activity == null) return null;
        activity.setActivityListener(this);
        unbinder = ButterKnife.bind(this, rootView);
        if (savedInstanceState == null) {
            token = activity.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
//            token = Constants.token;
        } else {
            token = savedInstanceState.getString(SAVED_TOKEN);
            offset = savedInstanceState.getInt(SAVED_OFFSET);
            clearFirst = savedInstanceState.getBoolean(SAVED_CLEAR_FIRST);
        }

        setUpRecyclerView();

        if (((MyApplication) activity.getApplication()).getLocation() != null)
            initData(offset, clearFirst);

        swipeContainer.setOnRefreshListener(() -> {
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            initData(0, true);
        });

        return rootView;
    }


    private void setUpRecyclerView() {
        if (ordersAdapter == null) {
            ordersAdapter = new OrdersAdapter(OrdersFragment.this);
            ordersAdapter.setHasStableIds(true);
        }


        if (layoutManager == null)
            layoutManager = new LinearLayoutManager(activity);


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(ordersAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnScrollListener(scrollListener());
        recyclerView.setPadding(0, 0, 0, Math.round(getResources().getDimension(R.dimen.recycler_view_padding)));
        recyclerView.setClipToPadding(false);
        int margin = Math.round(getResources().getDimension(R.dimen.defult_item_layout_margin_half));
        recyclerView.addItemDecoration(new Constants.OrderSpacing(margin / 2));

    }

    private void initData(int offset, boolean clearFirst) {
        this.offset = offset;
        this.clearFirst = clearFirst;
        activity.showProgress(true);
        if (swipeContainer.isRefreshing())
            swipeContainer.setRefreshing(false);
        activity.orderViewModel.init(token,
                offset, 10, clearFirst, ((MyApplication) activity.getApplication()).getLocation().getCoordinates());
        observeData(clearFirst);
    }

    private void showError(boolean show) {
        activity.showError(show);
        if (recyclerView != null)
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void observeData(boolean clear) {
        if (clear) ordersAdapter.clear();
        activity.orderViewModel.getOrdersRepo().observe(this, orders -> {
            if (orders == null) {
                showError(true);
                return;
            }
            if (offset > 0) {
                showProgress(false, 1);
            } else {
                showProgress(false, 0);
            }

            if (clearFirst) {
                clearFirst = false;
            }
            ordersAdapter.addAll(orders);
            if (offset > 0) {
                activity.orderViewModel.postValue(ordersAdapter.getList());
            }
        });
    }

    private RecyclerView.OnScrollListener scrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
//                if (layoutManager.findLastCompletelyVisibleItemPosition() == storeAdapter.getList().size()-1) {
                    if (ordersAdapter.getItemCount() >= offset) {
                        offset = offset + 10;
                        initData(offset, false);
                    }

                }
            }
        };
    }

    @Override
    public void reloadFragment() {
        initData(offset, clearFirst);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_TOKEN, token);
        outState.putInt(SAVED_OFFSET, offset);
        outState.putBoolean(SAVED_CLEAR_FIRST, clearFirst);
    }

    @Override
    public void showProgress(boolean show) {
        if (offset > 0) {
            showProgress(true, 1);
        } else {
            showProgress(true, 0);
        }
    }

    @Override
    public void onLocationDetected() {
        initData(offset, clearFirst);
    }

    private void showProgress(boolean show, int type) {

        if (type == 0) {
            if (shimmerLayout == null || recyclerView == null)
                return;
            shimmerLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            if (show) {
                shimmerLayout.startShimmer();
            } else {
                shimmerLayout.stopShimmer();
            }
        } else {
            if (progressBar != null)
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }

    @Override
    public void onClick(String orderId) {
        Intent intent = new Intent(activity, ViewOrderActivity.class);
        intent.putExtra(Constants.ORDER_ID, orderId);
        startActivity(intent);
    }



}
