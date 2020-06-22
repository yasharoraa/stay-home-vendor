package com.stayhome.vendor.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stayhome.vendor.Adapters.CatSelectGridAdapter;
import com.stayhome.vendor.Data.CategoryData.CategoryViewModel;
import com.stayhome.vendor.Interfaces.OnCategoryItemSelected;
import com.stayhome.vendor.Models.Category;
import com.stayhome.vendor.Models.Geocoding.UserLocation;
import com.stayhome.vendor.Models.Store.CreateStore;
import com.stayhome.vendor.R;
import com.stayhome.vendor.Utils.Constants;
import com.stayhome.vendor.Utils.MyApplication;
import com.stayhome.vendor.Utils.PlaceDetectingActivity;
import com.stayhome.vendor.WebServices.ApiInterface;
import com.stayhome.vendor.WebServices.ServiceGenerator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FillDetailsActivity extends PlaceDetectingActivity implements OnCategoryItemSelected {

    private Unbinder unbinder;

    private int i;

    @BindView(R.id.name_edit_text)
    TextInputEditText nameEditText;

    @BindView(R.id.address_edit_text)
    EditText addressEditText;

    @BindView(R.id.button_update_details)
    Button buttonUpdateDetails;

    @BindView(R.id.owner_name_layout)
    TextInputLayout ownerNameLayout;

    @BindView(R.id.gstin_layout)
    TextInputLayout gstinLayout;

    @BindView(R.id.formattedAddressTextView)
    TextView formattedAddressTextView;

    @BindView(R.id.gstin_edit_text)
    EditText gstinEditText;

    @BindView(R.id.recycler_view)
    RecyclerView gridView;

    @BindView(R.id.title_text_view)
    TextView categoryTitle;

    @BindView(R.id.scroll_view)
    NestedScrollView scrollView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.owner_name_edit_text)
    EditText ownerNameEditText;

    @BindView(R.id.checkbox_confirm)
    CheckBox checkBox;

    @BindView(R.id.checkbox_terms)
    CheckBox terms;

    @BindView(R.id.terms)
    TextView termsTextView;

    @BindView(R.id.privacy)
    TextView privacyTextView;


    private CategoryViewModel categoryViewModel;
    private List<Category> selectedCategories;
    private CatSelectGridAdapter categoryGridAdapter;

    private UserLocation userLocation;

    private final String SAVED_SHOP_NAME = "shop_name";
    private final String SAVED_ADDRESS = "saved_address";
    private final String SAVED_OWNER_NAME = "owner_name";
    private final String SAVED_GSTIN = "saved_gstin";
    private final String SELECTED_CATEGORIES = "selected_cat";

    private boolean doubleBackToExitPressedOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_details);
        ButterKnife.bind(this);
        if (selectedCategories == null)
            selectedCategories = new ArrayList<>();

        buttonUpdateDetails.setOnClickListener(view -> setStoreDetails());

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
        gridView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        int margin = Math.round(getResources().getDimension(R.dimen.defult_item_layout_margin));
        gridView.setPadding(margin,margin,margin,margin);

        getCategoryData();
        categoryTitle.setText("Select Category");

        categoryTitle.setTextColor(ContextCompat.getColor(this,R.color.black800));
        gstinEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE){
                scrollView.post(() -> scrollView.fullScroll(NestedScrollView.FOCUS_DOWN));
            }
            return false;
        });

        if (savedInstanceState != null){
            if (((MyApplication)getApplication()).getLocation()!=null && ((MyApplication)getApplication()).getLocation().getCity()!=null){
                userLocation = ((MyApplication)getApplication()).getLocation();
                formattedAddressTextView.setText(userLocation.getFormattedAddress());
                String[] texts = userLocation.getFormattedAddress().split(",");
                if (texts.length<2) return;
                addressEditText.setText(String.format("%s, %s", texts[0], texts[1]));
            }else{
                super.getLastLocation(true);
            }
            nameEditText.setText(savedInstanceState.getString(SAVED_SHOP_NAME));
            addressEditText.setText(savedInstanceState.getString(SAVED_ADDRESS));
            ownerNameEditText.setText(savedInstanceState.getString(SAVED_OWNER_NAME));
            gstinEditText.setText(savedInstanceState.getString(SAVED_GSTIN));
        }else{
            getLastLocation(true);
        }

        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        privacyTextView.setMovementMethod(LinkMovementMethod.getInstance());

        termsTextView.setOnClickListener(view -> openLink("https://www.adalbadal.com/terms.html"));

        privacyTextView.setOnClickListener(view -> openLink("https://www.adalbadal.com/privacy_policy.html"));


    }

    private void openLink(String link) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        startActivity(i);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_SHOP_NAME, Objects.requireNonNull(nameEditText.getText()).toString());
        outState.putString(SAVED_ADDRESS,addressEditText.getText().toString());
        outState.putString(SAVED_OWNER_NAME,ownerNameEditText.getText().toString());
        outState.putString(SAVED_GSTIN,gstinEditText.getText().toString());
    }

    @Override
    public void OnPlaceDetected(UserLocation userLocation, String error) {
//        .toggleProgress(false,"");
        ((MyApplication)getApplication()).setLocation(userLocation);
        this.userLocation = userLocation;
        formattedAddressTextView.setText(userLocation.getFormattedAddress());
        String[] texts = userLocation.getFormattedAddress().split(",");
        if (texts.length<2) return;
        addressEditText.setText(String.format("%s, %s", texts[0], texts[1]));

    }

    private void setStoreDetails() {
        CreateStore store = getStore();
        if (store == null) return;
        String token = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
        if (token == null) return;
        toggleProgress(true);
        ServiceGenerator.createService(ApiInterface.class).updateMyStoreProfile(token.trim(), store).enqueue(new Callback<CreateStore>() {
            @Override
            public void onResponse(Call<CreateStore> call, Response<CreateStore> response) {
                if (response.isSuccessful()) {
                    makeToast("Details updated successfully");
                    startActivity(new Intent(FillDetailsActivity.this,UploadImageActivity.class));
//                    .change(Constants.MainEnum.UPLOAD_IMAGE_STORE);
                } else if (response.code() == 401){
                    Constants.logout(FillDetailsActivity.this);
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        JSONObject errJson = jsonObject.getJSONObject(jsonObject.keys().next());
                        String key = errJson.getString(errJson.keys().next());
                        makeToast(key);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        makeToast("Details not updated. Please try again !");
                    }
                   toggleProgress(false);
                }

            }

            @Override
            public void onFailure(Call<CreateStore> call, Throwable t) {
               toggleProgress(false);
                makeToast("Details not updated. Please try again !");
            }
        });

    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    private CreateStore getStore() {



        nameEditText.setError(null);
        addressEditText.setError(null);
        ownerNameEditText.setError(null);
        gstinLayout.setError(null);

        boolean cancel = false;
        View focusView = null;

        String name = nameEditText.getText() != null ? nameEditText.getText().toString().trim() : "";
        String address = addressEditText.getText() != null ? addressEditText.getText().toString().trim() : "";
        String ownerName = ownerNameEditText.getText() != null ? ownerNameEditText.getText().toString().trim() : "";
        String Gstin = gstinEditText.getText() != null ? gstinEditText.getText().toString().trim() : "";

        if (name.isEmpty() || name.length() < 3) {
            nameEditText.setError("Please enter a valid name");
            focusView = nameEditText;
            cancel = true;
        } else if (address.isEmpty() || address.length() < 6) {
            addressEditText.setError("Please enter a valid address");
            focusView = nameEditText;
            cancel = true;
        } else if (ownerName.isEmpty() || ownerName.length() < 3) {
            ownerNameEditText.setError("Please enter a valid name");
            focusView = ownerNameEditText;
            cancel = true;
        } else if (!isValidGstin(Gstin)) {
            gstinEditText.setError("Please enter a valid gstin");
            focusView = gstinEditText;
            cancel = true;
        }

        if (userLocation==null){
                makeToast("Please wait while we fetch your location");
                return null;
        }

        String placeId = userLocation.getPlaceId();

        double[] coordinates = userLocation.getCoordinates();

        if (cancel) {
            focusView.requestFocus();
            return null;
        }

        if (selectedCategories.isEmpty()){
            makeToast("Select category to continue.");
            return null;
        }

        String[] categories = new String[selectedCategories.size()];

        for (int i=0; i<selectedCategories.size(); i++){
            categories[i] = selectedCategories.get(i).getId();
        }

        if (userLocation.getPincode()== null || userLocation.getCity()== null || placeId== null || coordinates == null) {
            makeToast("Location Error, Please try again later.");
            return null;
        }

        if (!checkBox.isChecked()){
            makeToast("Check confirm checkbox to continue.");
            return null;
        }

        if (!terms.isChecked()){
            makeToast("Check privacy policy and terms of use checkbox to continue.");
            return null;
        }

        return new CreateStore(name,ownerName,Gstin,address,Integer.parseInt(userLocation.getPincode()),userLocation.getCity(),placeId,coordinates,categories);
    }

    private static boolean isValidGstin(String input) {
        Pattern pattern;
        Matcher matcher;
        String PATTERN = "\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[A-Z\\d]{1}[Z]{1}[A-Z\\d]{1}";
        pattern = Pattern.compile(PATTERN);
        matcher = pattern.matcher(input);
        return matcher.matches();
    }


    private void getCategoryData() {
        Log.i("DATA", "STRATED");
        categoryViewModel.init();
        categoryViewModel.getCategoriesRepo().observe(this, categories -> {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            gridView.setLayoutManager(gridLayoutManager);
            int margin = Math.round(getResources().getDimension(R.dimen.defult_item_layout_margin));
            gridView.addItemDecoration(new Constants.SpacesItemDecoration(margin / 2));
            categoryGridAdapter = new CatSelectGridAdapter(this, categories, selectedCategories);
            gridView.setAdapter(categoryGridAdapter);
        });
    }


    @Override
    public void onCategoryItemSelected(Category category) {
        boolean value = (selectedCategories.contains(category)) ? selectedCategories.remove(category) : selectedCategories.add(category);
        categoryGridAdapter.notifyDataSetChanged();
    }

    private void toggleProgress(boolean show){
        if (scrollView==null || buttonUpdateDetails==null || progressBar ==null)
            return;

        scrollView.setVisibility(show?View.GONE:View.VISIBLE);
        buttonUpdateDetails.setVisibility(show?View.GONE:View.VISIBLE);
        progressBar.setVisibility(show?View.VISIBLE:View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            this.finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }


}
