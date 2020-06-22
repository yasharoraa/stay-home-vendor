package com.stayhome.vendor.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.stayhome.vendor.Models.CreateUser;
import com.stayhome.vendor.Models.Store.CreateStore;
import com.stayhome.vendor.R;
import com.stayhome.vendor.Utils.Constants;
import com.stayhome.vendor.Utils.OtpEditText;
import com.stayhome.vendor.WebServices.ApiInterface;
import com.stayhome.vendor.WebServices.ServiceGenerator;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpDialogActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.et_otp)
    OtpEditText otpEditText;

    @BindView(R.id.error_text_view)
    TextView errorTextView;

    @BindView(R.id.resend_button)
    Button resendButton;

    @BindView(R.id.change_number_button)
    Button changeNumberButton;

    @BindView(R.id.main_content)
    View mainContent;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private String tempId;

    private String phone;

    private String pass;

    private final String SAVED_PROCESSING = "saved_processing";

    private final String SAVED_TEXT = "saved_text";

    public boolean processing;

    private final String TAG = this.getClass().getSimpleName();

    public Call call1;
    public Call<ResponseBody> call2;

    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_dialog);
        this.setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
        if (savedInstanceState == null){
            tempId = getIntent().getStringExtra(Constants.TEMP_ID);
            phone = getIntent().getStringExtra(Constants.PHONE_NUMBER);
            pass = getIntent().getStringExtra(Constants.PASSWORD);
        }else{
            tempId = savedInstanceState.getString(Constants.TEMP_ID);
            phone = savedInstanceState.getString(Constants.PHONE_NUMBER);
            pass = savedInstanceState.getString(Constants.PASSWORD);
            processing = savedInstanceState.getBoolean(SAVED_PROCESSING);
            if (processing)
                toggleProgress(true);

            String otp = savedInstanceState.getString(SAVED_TEXT);
            if (otp != null)
                otpEditText.setText(otp);
        }
        if (tempId == null || phone == null || pass == null) return;
        changeNumberButton.setOnClickListener(this);
        resendButton.setOnClickListener(this);
        otpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 6) {
                    Constants.hideKeyboard(OtpDialogActivity.this);
                    createUser(Integer.parseInt(editable.toString()));
                }
            }
        });

        otpEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (otpEditText.getText() != null && otpEditText.getText().length() == 6) {
                createUser(Integer.parseInt(otpEditText.toString()));
            }
            return false;
        });

        countDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (resendButton != null)
                    resendButton.setText("Resend in " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                if (resendButton!=null){
                    resendButton.setEnabled(true);
                    resendButton.setText("RESEND");
                }
            }

        };
        countDownTimer.start();
    }

    // 0 -> kooch ni hoga
    // 1 -> FillDetailIntent
    // 2 -> VeificationWaitingIntent
    // 3 -> Home Activity Store

    private void storeCheck(CreateStore store) {
        processing = false;
        if (store.getName() == null || store.getOwner() == null || store.getAddress() == null || store.getGstin() == null || store.getPincode() == null
                || store.getCoordinates() == null || store.getCategories() == null) {
           setIntentResult(1);
        } else if (!store.isAccepted()) {
            setIntentResult(2);
        } else {
           setIntentResult(3);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_PROCESSING, processing);
        outState.putString(Constants.TEMP_ID, tempId);
        outState.putString(Constants.PHONE_NUMBER, phone);
        outState.putString(Constants.PASSWORD, pass);
        if (otpEditText.getText() != null)
            outState.putString(SAVED_TEXT, otpEditText.getText().toString());
    }

    private void toggleProgress(boolean progress){
        if (mainContent!=null) mainContent.setVisibility(progress?View.GONE:View.VISIBLE);
        if (progressBar!=null) progressBar.setVisibility(progress?View.VISIBLE:View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer!=null)
            countDownTimer.cancel();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.TOKEN))
            sharedPreferences.edit().remove(Constants.TOKEN).apply();

        setIntentResult(0);
    }

    private void loginWithFirebase(String jwt, CreateStore store) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        createLogoutTryAgainDialog(jwt,store);
                        return;
                    }
                    String token = task.getResult().getToken();
                    if (store.getFirebase() != null && store.getFirebase().equals(token)) {
                        storeCheck(store);
                        processing = false;
                        return;
                    }
                    // Get new Instance ID token
                    call2 = ServiceGenerator.createService(ApiInterface.class).putFirebaseToken(jwt, token);
                    call2.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                storeCheck(store);
                            } else if (response.code() == 401) {
                                Constants.logout(OtpDialogActivity.this);
                            } else {
                                createLogoutTryAgainDialog(jwt,store);
                                processing = false;
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            createTryAgainExitDialog(jwt,store);
                            processing = false;
                        }
                    });
                });
    }
    private void createLogoutTryAgainDialog(String jwt, CreateStore store) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User validation failed !");
        builder.setCancelable(false);
        builder.setPositiveButton("Try Again", (dialogInterface, i) -> loginWithFirebase(jwt,store));
        builder.setNegativeButton("Logout", (dialogInterface, i) -> logout());
        builder.create().show();
    }

    private void createTryAgainExitDialog(String jwt, CreateStore store) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Please check your network connnection and try again !");
        builder.setPositiveButton("Try Again", (dialogInterface, i) -> loginWithFirebase(jwt,store));
        builder.setNegativeButton("Exit", (dialogInterface, i) -> this.finishAffinity());
        builder.create().show();
    }
    private void createUser(int OTP) {
        setError(null);
        processing = true;
        toggleProgress(true);
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        call1 = loginStore(apiInterface.verifyNumber(tempId, OTP));
    }

    private void setError(String text) {
        if (errorTextView != null) errorTextView.setText(text == null ? "" : text);
    }

    private Call<CreateStore> loginStore(Call<CreateStore> call) {
        call.enqueue(new Callback<CreateStore>() {
            @Override
            public void onResponse(@NotNull Call<CreateStore> call, @NotNull Response<CreateStore> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getToken() != null) {
                        CreateStore store = response.body();
                        loginWithFirebase(putValues(store.getToken()), store);
                    } else {
                        setError("Empty response from server");
                        processing = false;
                        toggleProgress(false);
                    }
                } else {
                    setupError(response.errorBody());
                }

            }

            @Override
            public void onFailure(Call<CreateStore> call, Throwable t) {
                setError("Please check your internet connectivity and try again !");
                Log.i(TAG, t.getMessage() + t.getCause());
                toggleProgress(false);
                processing = false;
            }
        });
        return call;
    }

    private String putValues(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String value = "Bearer " + token.trim();
        sharedPreferences.edit().putString(Constants.TOKEN, value).apply();
        return value;
    }

    private void setupError(ResponseBody errorBody){
        if (errorBody != null) {
            try {
                JSONObject jsonObject = new JSONObject(errorBody.string());
                JSONObject errJson = jsonObject.getJSONObject(jsonObject.keys().next());
                String key = errJson.getString(errJson.keys().next());
                setError(key);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                setError("Unknown Response ! Please try again later");
            }
        }
        toggleProgress(false);
        processing = false;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.resend_button :
                resendOtp();
                resendButton.setEnabled(false);
                break;
            case R.id.change_number_button:
                setIntentResult(0);
                break;
        }
    }

    private void resendOtp() {
        CreateUser user = new CreateUser(phone, pass);
        setError(null);
        processing = true;
        toggleProgress(true);
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        call1 = postStore(apiInterface.createStoreProfile(user));

    }

    private Call<JsonObject> postStore(Call<JsonObject> call) {
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() !=null) {
                    tempId = response.body().get("id").getAsString();
                    processing = false;
                    toggleProgress(false);
                }else{
                    setupError(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                setError("Please check your internet connectivity and try again !");
                Log.i(TAG, t.getMessage() + t.getCause());
                toggleProgress(false);
                processing = false;
            }
        });
        return call;
    }

    private void setIntentResult(int result){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",result);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (processing) {
            Constants.showCancelDialog(this, "Do you want to abort this process?", () -> {
                if (call1 != null) call1.cancel();
                if (call2 != null) call2.cancel();
                this.onBackPressed();
            });
            return;
        }
        super.onBackPressed();
    }

}
