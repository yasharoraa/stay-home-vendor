package com.stayhome.vendor.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stayhome.vendor.BuildConfig;
import com.stayhome.vendor.Models.CreateUser;
import com.stayhome.vendor.Models.ResetPassword;
import com.stayhome.vendor.Models.Store.CreateStore;
import com.stayhome.vendor.R;
import com.stayhome.vendor.Utils.Constants;
import com.stayhome.vendor.WebServices.ApiInterface;
import com.stayhome.vendor.WebServices.ServiceGenerator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.phone_number_edit_text)
    EditText phoneNumberEditText;

    @BindView(R.id.password_edit_text)
    EditText passwordEditText;

    @BindView(R.id.confirm_password_edit_text)
    EditText confirmPasswordEditText;

    @BindView(R.id.button_sign_in)
    Button signInButton;

    @BindView(R.id.button_create_account)
    Button signUpButton;

    @BindView(R.id.confirm_password_text_input)
    TextInputLayout confirmLayout;

    @BindView(R.id.layout_login)
    View loginLayout;

    @BindView(R.id.sign_in)
    TextView signInToggle;

    @BindView(R.id.error_text)
    TextView errorText;

    @BindView(R.id.action_title)
    TextView actionTitle;

    private boolean SignUp = false;

    private boolean progress;

    private String progressText;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.progress_text)
    TextView textView;

    private final int OTP_REQUEST_CODE = 556;

    private final String SAVED_IS_SIGN_UP = "is_sign_up";

    private final String SAVED_PROGRESS = "saved_progress";

    private final String SAVED_PROGRESS_TEXT = "saved_progress_text";

    private final String SAVED_PHONE = "saved_phone";

    private final String SAVED_PASSWORD = "saved_password";

    private final String SAVED_CONFIRM_PASSWORD = "saved_confirm_password";

    private final String SAVED_VALIDATING = "saved_validating";

    private final String TAG = this.getClass().getSimpleName();

    private Call call1;

    private Call<ResponseBody> call2;

    private boolean validating;

    private final int RESET_PASSWORD_REQUEST_CODE = 236;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Constants.setFullScreen(this);
        ButterKnife.bind(this);
        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        signInToggle.setOnClickListener(this);

        if (savedInstanceState != null) {
            SignUp = savedInstanceState.getBoolean(SAVED_IS_SIGN_UP);
            progress = savedInstanceState.getBoolean(SAVED_PROGRESS);
            if (progress)
                toggleProgress(true, savedInstanceState.getString(SAVED_PROGRESS_TEXT));

            phoneNumberEditText.setText(savedInstanceState.getString(SAVED_PHONE));
            passwordEditText.setText(savedInstanceState.getString(SAVED_PASSWORD));
            confirmPasswordEditText.setText(savedInstanceState.getString(SAVED_CONFIRM_PASSWORD));
            validating = savedInstanceState.getBoolean(SAVED_VALIDATING);
        } else {
                checkVersion();
        }

        setSignUp(SignUp);

    }

    private void setSignUp(boolean isSignUp) {
        confirmLayout.setVisibility(SignUp ? View.VISIBLE : View.GONE);
        signInButton.setVisibility(SignUp ? View.GONE : View.VISIBLE);
        actionTitle.setText(getString(isSignUp ? R.string.already_have_a_account : R.string.forgot_password));
        signInToggle.setText(getString(isSignUp ? R.string.login : R.string.reset));
    }

    private void checkVersion(){
        toggleProgress(true, "Validating version");
        ServiceGenerator.createService(ApiInterface.class).getMinimumVersion().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body()!=null){
                    if (BuildConfig.VERSION_CODE<Integer.parseInt(response.body())){
                        createUpdateRequiredDialog();
                    }else{
                        if (!checkFirst()){
                            toggleProgress(true, "Validating user");
                        }else{
                            toggleProgress(false,null);
                        }
                    }
                }else{
                    createTryAgainExitDialog();
                    Log.i(TAG,response.code() + "");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                createTryAgainExitDialog();
                Log.i(TAG,t.getMessage() + t.getMessage());
            }
        });
    }
    private void createUpdateRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Required!");
        builder.setMessage("App update required to continue using StayHome.");
        builder.setCancelable(false);
        builder.setPositiveButton("Update", (dialogInterface, i) -> {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });
        builder.create().show();
    }


    private void createLogoutTryAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User validation failed !");
        builder.setCancelable(false);
        builder.setPositiveButton("Try Again", (dialogInterface, i) -> checkVersion());
        builder.setNegativeButton("Logout", (dialogInterface, i) -> logout());
        builder.create().show();
    }

    private void createTryAgainExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Please check your network connnection and try again !");
        builder.setPositiveButton("Try Again", (dialogInterface, i) -> checkVersion());
        builder.setNegativeButton("Exit", (dialogInterface, i) -> this.finishAffinity());
        builder.create().show();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.TOKEN))
            sharedPreferences.edit().remove(Constants.TOKEN).apply();

        toggleProgress(false, null);
    }

    private boolean checkFirst() {
        String token = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
        Log.i(TAG, token + "");
        if (token != null) {
            validating = true;
            ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
            call1 = apiInterface.getMyStore(token);
            call1.enqueue(new Callback<CreateStore>() {
                @Override
                public void onResponse(@NotNull Call<CreateStore> call, @NotNull Response<CreateStore> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CreateStore store = response.body();
                        loginWithFirebase(token, store);
                    } else if (response.code() == 401) {
                        Log.i(TAG, "LOOP Startred here");
                        logout();
                    } else {
                        Log.i(TAG, "call 1 issue" + response.code());
                        createLogoutTryAgainDialog();
                        validating = false;
                    }
                }

                @Override
                public void onFailure(Call<CreateStore> call, Throwable t) {
                    createTryAgainExitDialog();
                    validating = false;
                }
            });
        }
        return token == null;
    }


    private void storeCheck(CreateStore store) {
        validating = false;
        if (store.getName() == null || store.getOwner() == null || store.getAddress() == null || store.getGstin() == null || store.getPincode() == null
                || store.getCoordinates() == null || store.getCategories() == null) {
            Intent intent = new Intent(MainActivity.this, FillDetailsActivity.class);
            startActivity(intent);
        } else if (!store.isAccepted()) {
            Intent intent = new Intent(MainActivity.this, VerificationWaitingActivity.class);
            startActivity(intent);
        } else {
            startActivity(new Intent(MainActivity.this, HomeActivityStore.class));
        }
    }

    private void loginWithFirebase(String jwt, CreateStore store) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        createLogoutTryAgainDialog();
                        return;
                    }
                    String token = task.getResult().getToken();
                    if (store.getFirebase() != null && store.getFirebase().equals(token)) {
                        storeCheck(store);
                        validating = false;
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
                                logout();
                            } else {
                                createLogoutTryAgainDialog();
                                validating = false;
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            createTryAgainExitDialog();
                            validating = false;
                        }
                    });
                });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_IS_SIGN_UP, SignUp);
        outState.putBoolean(SAVED_PROGRESS, progress);
        outState.putString(SAVED_PROGRESS_TEXT, progressText);
        outState.putString(SAVED_PHONE, phoneNumberEditText.getText().toString());
        outState.putString(SAVED_PASSWORD, passwordEditText.getText().toString());
        outState.putString(SAVED_CONFIRM_PASSWORD, confirmPasswordEditText.getText().toString());
        outState.putBoolean(SAVED_VALIDATING, validating);
    }

    public void toggleProgress(boolean isProgress, String text) {
        MainActivity.this.progress = isProgress;
        MainActivity.this.progressText = text;
        if (loginLayout != null) loginLayout.setVisibility(isProgress ? View.GONE : View.VISIBLE);
        if (progressBar != null) progressBar.setVisibility(isProgress ? View.VISIBLE : View.GONE);
        if (textView != null) textView.setText(text);
    }

    private Bundle getBundle(String key, int value) {
        Bundle bundle = new Bundle();
        bundle.putInt(key, value);
        return bundle;
    }

    @Override
    public void onBackPressed() {
        if (validating) {
            Constants.showCancelDialog(this, "Do you want to abort this process?", () -> {
                if (call1 != null) call1.cancel();
                if (call2 != null) call2.cancel();
                this.finishAffinity();
            });
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_create_account:
                toggleSignUpVisibility(true);
                break;
            case R.id.sign_in:
                if (SignUp)
                    toggleSignUpVisibility(false);
                else
                    showDialog();
                break;
            case R.id.button_sign_in:
                createUser(Login.LOG_IN_STORE);
                break;
        }
    }

    private void toggleSignUpVisibility(boolean isSignUp) {
        if (SignUp && isSignUp) {
            createUser(Login.SIGN_UP_STORE);
            return;
        }
        SignUp = isSignUp;
        setSignUp(isSignUp);
    }

    private void createUser(Login type) {
        CreateUser user = getUserDetails(type.equals(Login.SIGN_UP_STORE));
        if (user == null) return;
        setError(null);
        toggleProgress(true, "Logging in");
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        if (type.equals(Login.LOG_IN_STORE)) {
            call1 = loginStore(apiInterface.loginStore(user));
        } else {
            call1 = postStore(apiInterface.createStoreProfile(user), user);
        }
        call1 = type.equals(Login.SIGN_UP_STORE) ? apiInterface.createStoreProfile(user) :
                apiInterface.loginStore(user);
        validating = true;
    }

    private Call<JsonObject> postStore(Call<JsonObject> call, CreateUser user) {
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(MainActivity.this, OtpDialogActivity.class);
                    intent.putExtra(Constants.TEMP_ID, response.body().get("id").getAsString());
                    intent.putExtra(Constants.PHONE_NUMBER, user.getUser().getPhone());
                    intent.putExtra(Constants.PASSWORD, user.getUser().getPassword());
                    startActivityForResult(intent, OTP_REQUEST_CODE);
                } else {
                    setupError(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                setError("Please check your internet connectivity and try again !");
                Log.i(TAG, t.getMessage() + t.getCause());
                toggleProgress(false, null);
                validating = false;
            }
        });
        return call;
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
                        validating = false;
                        toggleProgress(false, null);
                    }
                } else {
                    setupError(response.errorBody());
                }

            }

            @Override
            public void onFailure(Call<CreateStore> call, Throwable t) {
                setError("Please check your internet connectivity and try again !");
                Log.i(TAG, t.getMessage() + t.getCause());
                toggleProgress(false, null);
                validating = false;
            }
        });
        return call;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 0 -> kooch ni hoga
        // 1 -> FillDetailIntent
        // 2 -> VeificationWaitingIntent
        // 3 -> Home Activity Store

        if (requestCode == OTP_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            int i = data.getIntExtra("result", 0);
            if (i == 1) {
                startActivity(new Intent(MainActivity.this, FillDetailsActivity.class));
            } else if (i == 2) {
                startActivity(new Intent(MainActivity.this, VerificationWaitingActivity.class));
            } else if (i == 3) {
                startActivity(new Intent(MainActivity.this, HomeActivityStore.class));
            }else{
                validating = false;
                toggleProgress(false, null);
            }
        } else if (requestCode == RESET_PASSWORD_REQUEST_CODE && resultCode == Activity.RESULT_OK && data!=null){
            resetPassword(data.getParcelableExtra(Constants.RESET_PASSWORD));
        }else{
            validating = false;
            toggleProgress(false, null);
        }
    }

    private void setupError(ResponseBody errorBody) {
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
        toggleProgress(false, null);
        validating = false;
    }

    private void resetPassword(ResetPassword resetPassword){
        validating = true;
        toggleProgress(true, "Setting new password");
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<ResponseBody> call =  apiInterface.resetPassword(resetPassword);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Password successfully changed.",Toast.LENGTH_SHORT).show();
                    setError("Password successfully changed.");
                    validating = false;
                    toggleProgress(false,null);
                } else {
                    setupError(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                validating = false;
                setError(getString(R.string.check_connection));
                toggleProgress(false, null);
            }
        });
        call1 = call;
    }



    private CreateUser getUserDetails(boolean isSignUp) {
        phoneNumberEditText.setError(null);
        passwordEditText.setError(null);

        boolean cancel = false;

        View focusView = null;

        String number = phoneNumberEditText.getText() != null ? phoneNumberEditText.getText().toString().trim() : "";

        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

        String conFirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString() : "";

        if (!isValid(number, false)) {
            phoneNumberEditText.setError("Enter a 10 digit valid number");
            focusView = phoneNumberEditText;
            cancel = true;
        } else if (!isValid(password, true)) {
            passwordEditText.setError("Enter a valid 6-20 character password with at least one digit and letter",null);
            focusView = passwordEditText;
            cancel = true;
        } else if (isSignUp && !conFirmPassword.equals(password)) {
            confirmPasswordEditText.setError("Passwords do not match",null);
            focusView = confirmPasswordEditText;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
            Log.i("User : ", "CANCEL");
            return null;
        }
        Log.i("User : ", "! CANCEL");
        return new CreateUser(number, password);
    }


    private static boolean isValid(String input, boolean isPassword) {
        Pattern pattern;
        Matcher matcher;
        String PATTERN = isPassword ? ("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,20}$") : ("^[0-9]{10}$");
        pattern = Pattern.compile(PATTERN);
        matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private void setError(String text) {
        if (errorText != null) errorText.setText(text == null ? "" : text);
    }


    private enum Login {
        LOG_IN_STORE,
        SIGN_UP_STORE
    }

    private String putValues(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String value = "Bearer " + token.trim();
        sharedPreferences.edit().putString(Constants.TOKEN, value).apply();
        return value;
    }

    private void forgotPassword(String phone) {
        validating = true;
        toggleProgress(true, "Sending verification code");
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<JsonObject> call = (Call<JsonObject>) apiInterface.forgotPassword(phone);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
                    intent.putExtra(Constants.PHONE_NUMBER,phone);
                    intent.putExtra(Constants.RESET_ID,response.body().get("id").getAsString());
                    startActivityForResult(intent, RESET_PASSWORD_REQUEST_CODE);
                } else {
                    setupError(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                validating = false;
                setError(getString(R.string.check_connection));
                toggleProgress(false, null);
            }
        });

        call1 = call;

    }
    public void showDialog() {
        final Dialog dialog = new Dialog(this, R.style.AppTheme_AddressDialog);
        dialog.setContentView(R.layout.reset_password_dialog);

        EditText text = dialog.findViewById(R.id.phone_number_edit_text);
        FloatingActionButton floatingActionButton = dialog.findViewById(R.id.button_continue);
        floatingActionButton.setOnClickListener(v -> {
            if (text.getText().length() == 10) {
                forgotPassword(text.getText().toString());
                dialog.dismiss();
            } else {
                text.setError("Enter a valid phone number to continue.");
            }
        });
        dialog.show();
    }
}
