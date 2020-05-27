package com.fieapps.stayhomevendor.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.fieapps.stayhomevendor.Interfaces.ImageUploadCallback;
import com.fieapps.stayhomevendor.R;
import com.fieapps.stayhomevendor.Utils.AsyncTasks;
import com.fieapps.stayhomevendor.Utils.Constants;
import com.roger.catloadinglibrary.CatLoadingView;
import com.takusemba.cropme.CropLayout;
import com.takusemba.cropme.OnCropListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

import static com.fieapps.stayhomevendor.Utils.Constants.getRealPathFromURI;

public class UploadImageActivity extends AppCompatActivity {

    @BindView(R.id.crop_view)
    CropLayout cropLayout;

    @BindView(R.id.select_image)
    Button selectImageButton;

    @BindView(R.id.button_upload_image)
    Button uploadImageButton;

    @BindView(R.id.button_skip)
    TextView buttonSkip;

    private final String SAVED_IMAGE_UR = "saved_image_uri";

    private final String PROGRESS_TAG = "p_tag";

    private String ImageUri;

    private final String TAG = this.getClass().getSimpleName();

    private AsyncTasks.ImageConvertClass imageConvertClass;

    private boolean LOADING_PROCESS_RUNNING;

    private Call uploadCall;

    private static final int CAMERA_REQUEST = 1888;

    private boolean doubleBackToExitPressedOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        ButterKnife.bind(this);
        cropLayout.addOnCropListener(cropListener());
        if (savedInstanceState != null) {
            ImageUri = savedInstanceState.getString(SAVED_IMAGE_UR);

            if (ImageUri != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(ImageUri);
                setImage(bitmap);
            }
        }

        selectImageButton.setOnClickListener(view -> chooser().create().show());

        uploadImageButton.setOnClickListener(view -> {
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT){
                makeToast("Please rotate screen to portrait for better experience.");
                return;
            }
            if (cropLayout.isOffFrame()){
                makeToast("Crop image inside frame to continue.");
            }
            cropLayout.crop();
        });
        buttonSkip.setOnClickListener(view -> {
            Intent intent = new Intent(UploadImageActivity.this, VerificationWaitingActivity.class);
            startActivity(intent);
        });

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
            outState.putString(SAVED_IMAGE_UR, ImageUri);

    }

    private OnCropListener cropListener() {
        return new OnCropListener() {
            @Override
            public void onSuccess(@NotNull Bitmap bitmap) {
                Log.i(TAG,"SUCCESS");
                String token = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
                if (token == null) return;
                Log.i(TAG,"TOKEN_NOT_NULL");
                LOADING_PROCESS_RUNNING = true;
                uploadImage(getCacheDir(), token, new ImageUploadCallback() {
                    @Override
                    public void onSuccess(String url) {
                        hideDialog(url);
                        makeToast("Image Uploaded successfully");

                    }

                    @Override
                    public void onCancel() {
                        hideDialog(null);
                        makeToast("Image Processing Error");
                    }

                    @Override
                    public void onError(String error) {
                        hideDialog(null);
                        makeToast("Error :" + error);
                    }

                    @Override
                    public void OnCallStart(Call call) {
                        uploadCall = call;
                    }
                }, bitmap);
                CatLoadingView mView = new CatLoadingView();
                mView.setCancelable(false);
                mView.show(getSupportFragmentManager(), PROGRESS_TAG);
            }

            @Override
            public void onFailure(@NotNull Exception e) {
                Log.i(TAG,"Failure");
            }
        };
    }

    private void hideDialog(String url) {
        LOADING_PROCESS_RUNNING = false;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(PROGRESS_TAG);
        if (fragment != null) {
            DialogFragment dialogFragment = (DialogFragment) fragment;
            dialogFragment.dismiss();
        }
        if (url == null) return;
        Intent intent = new Intent(this, VerificationWaitingActivity.class);
        startActivity(intent);
    }

    private void uploadImage(File cache, String token, ImageUploadCallback callback, Bitmap bitmap) {
        imageConvertClass = new AsyncTasks.ImageConvertClass(cache, token, "stores", callback);
        imageConvertClass.execute(bitmap);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            chooseImageFromCamera();
        }
        if (requestCode==1&&grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Constants.selectPicture(this);
            //resume tasks needing this permission
        }
    }

    private void setImage(Bitmap bitmap) {
        cropLayout.setBitmap(bitmap);
    }

    private void setImageUri(Uri uri) {
        cropLayout.setUri(uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.PICK_IMAGE && resultCode == RESULT_OK
                && null != data) {

            Uri selectedImage = data.getData();
            String imageEncoded = getRealPathFromURI(this, selectedImage);
            ImageUri = imageEncoded;
            Bitmap bitmap = BitmapFactory.decodeFile(imageEncoded);
            setImage(bitmap);
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
//            if (data.getExtras()==null)
//                return;

            File file = new File(getApplicationContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "stayhome.jpg");
            Uri uri =  FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", file);
            setImageUri(uri);
        }
    }

    private AlertDialog.Builder chooser() {
        return new AlertDialog.Builder(this)
                .setTitle("Select image from")
                .setItems(getResources().getStringArray(R.array.dialog_array), (dialog, which) -> {
                    if (which == 1) {
                        /*chooseImage();*/
                        if (Constants.isStoragePermissionGranted(UploadImageActivity.this))
                            Constants.selectPicture(UploadImageActivity.this);
                    } else {
                        chooseImageFromCamera();
                    }
                });
    }

    private void chooseImageFromCamera(){
        if (Constants.isCameraPermissionGranted(UploadImageActivity.this)){
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(getApplicationContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "stayhome.jpg");
            Uri uri =  FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", file);
//            Uri uri = Uri.fromFile(file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(cameraIntent,CAMERA_REQUEST);
        }
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void showCancelDialog(Activity activity, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (title != null) builder.setTitle(title);

        builder.setPositiveButton("Abort", (dialogInterface, i) -> {
            if (imageConvertClass != null)
                imageConvertClass.cancel(true);

            if (uploadCall != null)
                uploadCall.cancel();

            super.onBackPressed();
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (LOADING_PROCESS_RUNNING) {
            showCancelDialog(UploadImageActivity.this, "Do you want to abort uploading process?");
            return;
        }else{
            if (doubleBackToExitPressedOnce) {
                this.finishAffinity();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
        }
        super.onBackPressed();
    }
}
