package com.fieapps.stayhomevendor.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.fieapps.stayhomevendor.Data.SingleOrderData.OrderViewModel;
import com.fieapps.stayhomevendor.Models.Order.Order;
import com.fieapps.stayhomevendor.Models.Order.OrderItem;
import com.fieapps.stayhomevendor.R;
import com.fieapps.stayhomevendor.Utils.Constants;
import com.fieapps.stayhomevendor.Utils.SafeClickActivity;
import com.fieapps.stayhomevendor.WebServices.ApiInterface;
import com.fieapps.stayhomevendor.WebServices.ServiceGenerator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewOrderActivity extends SafeClickActivity {

    @BindView(R.id.view_order_text)
    TextView viewOrderTextView;

    @BindView(R.id.address_text_view)
    TextView addressTextView;

    @BindView(R.id.id_text_view)
    TextView orderIdTextView;

    @BindView(R.id.phone_number_text_view)
    TextView phoneTextView;

    @BindView(R.id.location_address_text_view)
    TextView locationTextView;

    @BindView(R.id.bottom_sheet_layout)
    View bottomSheetLayout;

    @BindView(R.id.text_bottom_sheet_1)
    TextView bottomSheetTextViewOne;

    @BindView(R.id.text_bottom_sheet_2)
    TextView bottomSheetTextViewTwo;

    @BindView(R.id.full_content_layout)
    View contentLayout;

    @BindView(R.id.shimmer_layout)
    ShimmerFrameLayout shimmerLayout;

    @BindView(R.id.error_layout)
    View errorLayout;

    @BindView(R.id.error_button)
    Button errorButton;

    @BindView(R.id.dir_text_view)
    TextView dirTextView;

    private BottomSheetBehavior bottomSheetBehavior;

    private OrderViewModel orderViewModel;

    private String orderId;

    private boolean progress;

    private boolean error;

    private final String IS_ERROR_SHOWN = "is_error_shown";

    private final String IS_PROGRESS_SHOWN = "is_progress_shown";

    private final String IS_PROCESSING = "is_processing";

    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<OrderItem> itemList;

    private String imageUrl;

    private double[] coordinates;

    private String formattedAddress;

    private int status;

    private boolean isProcessing;

    private Call<Order> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);
        ButterKnife.bind(this);
        orderViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setHideable(true);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            orderId = intent.getStringExtra(Constants.ORDER_ID);
            if (orderId == null) return;
        } else {
            orderId = savedInstanceState.getString(Constants.ORDER_ID);
            if (orderId == null) return;

            error = savedInstanceState.getBoolean(IS_ERROR_SHOWN);
            if (error) {
                toggleError(true);
                return;
            }

            progress = savedInstanceState.getBoolean(IS_ERROR_SHOWN);
            if (progress) {
                toggleProgress(true);
                return;
            }

            isProcessing = savedInstanceState.getBoolean(IS_PROCESSING);
            if (isProcessing) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }

        initData(orderId);
        viewOrderTextView.setOnClickListener(this);
        phoneTextView.setOnClickListener(this);
        dirTextView.setOnClickListener(this);
        locationTextView.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ORDER_ID, orderId);
        outState.putBoolean(IS_PROGRESS_SHOWN, progress);
        outState.putBoolean(IS_ERROR_SHOWN, error);
        outState.putBoolean(IS_PROCESSING, isProcessing);
    }

    private void initData(String orderId) {
        toggleError(false);
        toggleProgress(true);
        orderViewModel.init(orderId);
        observeData();
    }

    private void observeData() {
        orderViewModel.getOrdersRepo().observe(this, order -> {
            toggleProgress(false);
            if (order == null) {
                toggleError(true);
                return;
            }
            setupData(order);
        });
    }

    private void setupData(Order order) {
        if (addressTextView == null || viewOrderTextView == null || orderIdTextView == null || phoneTextView == null || locationTextView == null || bottomSheetLayout == null)
            return;

        this.itemList = order.getItems();
        this.imageUrl = order.getImageUrl();
        this.status = order.getStatus();
        this.coordinates = order.getAddress().getLocation();
        this.formattedAddress = order.getAddress().getLocationAddress();
        addressTextView.setText(String.format("%s\n%s", order.getAddress().getFlatAddress(), order.getAddress().getHowToReach()));
        orderIdTextView.setText(String.format("OID-%s", order.getSlug()));
        locationTextView.setText(order.getAddress().getLocationAddress());
        phoneTextView.setText(String.format("+91 %s", order.getAddress().getNumber()));

        switch (order.getStatus()) {
            case 101:
                //Order Placed
                setValues(R.drawable.ic_check_circle_black_24dp,
                        R.color.add_green, "Accept Order",
                        R.drawable.ic_cancel_black_24dp,
                        R.color.jalapino_red, "Cancel Order",
                        true);
                bottomSheetTextViewOne.setOnClickListener(this);
                bottomSheetTextViewTwo.setOnClickListener(this);
                break;
            case 201:
                //Order Accepted
                setValues(R.drawable.ic_local_shipping_black_24dp,
                        R.color.add_green, "Set Status - Out For Delivery",
                        R.drawable.ic_check_circle_black_24dp,
                        R.color.add_green, "Set Status - Order Delivered",
                        true);
                bottomSheetTextViewOne.setOnClickListener(this);
                bottomSheetTextViewTwo.setOnClickListener(this);
                break;
            case 300:
                //cancelled by user
                setValues(R.drawable.ic_cancel_black_24dp,
                        R.color.jalapino_red, "Cancelled by user",
                        0, 0, null,
                        false);
                break;
            case 301:
                //order cancelled by seller
                setValues(R.drawable.ic_cancel_black_24dp,
                        R.color.jalapino_red, "Order cancelled",
                        0, 0, null,
                        false);
                bottomSheetTextViewOne.setOnClickListener(null);
                break;
            case 202:
                //out for delivery
                setValues(R.drawable.ic_check_circle_black_24dp,
                        R.color.add_green, "Set Status - Order Delivered",
                        0, 0, null,
                        false);
                bottomSheetTextViewOne.setOnClickListener(this);
                break;
            case 204:
                //delivered
                setValues(R.drawable.ic_check_circle_black_24dp,
                        R.color.add_green, "Order Delivered / Complete",
                        0, 0, null,
                        false);
                bottomSheetTextViewOne.setOnClickListener(null);
                break;

        }

    }

    private void setValues(int drawableOne, int colorOne, String textOne, int drawableTwo, int colorTwo, String textTwo, boolean secondVisible) {
        Drawable drawable1 = ContextCompat.getDrawable(this, drawableOne);
        assert drawable1 != null;
        drawable1.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, colorOne), PorterDuff.Mode.SRC_IN));
        bottomSheetTextViewOne.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);
        bottomSheetTextViewOne.setText(textOne);
        if (!secondVisible) {
            bottomSheetTextViewTwo.setVisibility(View.GONE);
            return;
        }
        Drawable drawable2 = ContextCompat.getDrawable(this, drawableTwo);
        assert drawable2 != null;
        drawable2.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, colorTwo), PorterDuff.Mode.SRC_IN));
        bottomSheetTextViewTwo.setCompoundDrawablesWithIntrinsicBounds(drawable2, null, null, null);
        bottomSheetTextViewTwo.setText(textTwo);
    }

    private void toggleProgress(boolean show) {
        this.progress = show;
        if (shimmerLayout == null || contentLayout == null || bottomSheetLayout == null)
            return;
        shimmerLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            shimmerLayout.startShimmer();
        } else {
            shimmerLayout.stopShimmer();
        }
    }

    private void toggleError(boolean show) {
        this.error = show;
        if (errorLayout == null || contentLayout == null || bottomSheetLayout == null)
            return;

        errorLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }


    @Override
    public void onSafeClick(View view) {
        switch (view.getId()) {
            case R.id.view_order_text:
                if (imageUrl != null) {
                    Intent intent = new Intent(this, ViewImageActivity.class);
                    intent.putExtra(Constants.IMAGE_URL, imageUrl);
                    startActivity(intent);
                } else if (itemList != null) {
                    Intent intent = new Intent(ViewOrderActivity.this, OrderListViewActivity.class);
                    intent.putExtra(Constants.ORDER_LIST, itemList);
                    startActivity(intent);
                }
                //yet to implement
                break;
            case R.id.text_bottom_sheet_1:
                topTextClick();
                break;
            case R.id.text_bottom_sheet_2:
                bottomTextClick();
                break;
            case R.id.dir_text_view:
                if (coordinates != null && coordinates.length == 2)
                    openMaps(Uri.parse("geo:0,0?q=" + coordinates[0] + "," + coordinates[1]));
                break;
            case R.id.location_address_text_view:
                if (formattedAddress!=null)
                    openMaps(Uri.parse("geo:0,0?q="+formattedAddress));
                break;
        }
    }

    private void openMaps(Uri uri) {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void topTextClick() {
        switch (status) {
            case 101:
                setStatus(201);
                break;
            case 201:
                setStatus(202);
                break;
            case 202:
                setStatus(204);
                break;
        }
    }

    private void bottomTextClick() {
        switch (status) {
            case 101:
                showCancelDialog();
                break;
            case 201:
                setStatus(204);
                break;
        }
    }

    private void setStatus(int status) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        isProcessing = true;
        String token = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
        call = ServiceGenerator.createService(ApiInterface.class).updateOrder(token, orderId, status);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupData(response.body());
                    makeToast("Order update successful");
                } else {
                    makeToast("Order update Failed");
                }
                isProcessing = false;
                if (bottomSheetBehavior != null)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                makeToast("Order update Failed");
                isProcessing = false;
                if (bottomSheetBehavior != null)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });


    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to cancel this order ? ");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> setStatus(301));
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    @Override
    public void onBackPressed() {

        if (isProcessing) {
            Constants.showCancelDialog(this, "Do you want to abort this process?", () -> {
                if (call != null)
                    call.cancel();

                super.onBackPressed();
            });
        } else {
            super.onBackPressed();
        }
    }
}
