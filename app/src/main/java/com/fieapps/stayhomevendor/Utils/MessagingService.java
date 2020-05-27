package com.fieapps.stayhomevendor.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.fieapps.stayhomevendor.Activities.ViewOrderActivity;
import com.fieapps.stayhomevendor.Interfaces.OnNotificationImageLoaded;
import com.fieapps.stayhomevendor.R;
import com.fieapps.stayhomevendor.WebServices.ApiInterface;
import com.fieapps.stayhomevendor.WebServices.ServiceGenerator;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingService extends FirebaseMessagingService {

    private final String CHANNEL_ID = "order";

    private final String LATEST_CHANNEL_ID = "order_update_notifications";

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (remoteMessage.getData().containsKey("image")) {
                new sendImageNotification(bitmap -> sendNotification(remoteMessage.getData(), bitmap)).execute(remoteMessage.getData().get("image"));
            } else {
                sendNotification(remoteMessage.getData(), null);
            }

        }

        // Check if message contains a notification payload.


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String token = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.TOKEN, null);
        Call<ResponseBody> call = ServiceGenerator.createService(ApiInterface.class).putFirebaseToken(token, s);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful())
                    Log.i(TAG, "token : " + "post success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void sendNotification(Map<String, String> messageBody, Bitmap bitmap) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        Intent ii = new Intent(getApplicationContext(), ViewOrderActivity.class);
        ii.putExtra(Constants.ORDER_ID, messageBody.get("order"));

        String slug = messageBody.get("slug");

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        String status = messageBody.get("status");
        if (slug == null || status == null)
            return;

        int uniqueCode = Integer.parseInt(status + slug.substring(0, 5));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueCode, ii, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_home);
        mBuilder.setContentTitle(messageBody.get("message"));
        mBuilder.setContentText("OID-" + slug);
        mBuilder.setAutoCancel(true);
        if (bitmap != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap).setSummaryText("OID-" + slug).setBigContentTitle(messageBody.get("message")));
        } else {
            bigText.bigText("OID-" + slug);
            bigText.setBigContentTitle(messageBody.get("message"));
            bigText.setSummaryText("Tap to view");
            mBuilder.setStyle(bigText);
        }


        mBuilder.setPriority(Notification.PRIORITY_MAX);

        NotificationManager mNotificationManager;

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager == null) return;



        Uri soundUri = Uri.parse("android.resource://"
                + getPackageName() + "/" + R.raw.swiftly);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    LATEST_CHANNEL_ID,
                    "Order Update Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri,audioAttributes);

            channel.enableVibration(true);
            mBuilder.setChannelId(LATEST_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(channel);
        }else{
            mBuilder.setSound(soundUri);
            mBuilder.setVibrate(new long[]{100, 200});
        }
        mNotificationManager.notify(uniqueCode,mBuilder.build());
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return super.openFileInput(name);
    }


    private static class sendImageNotification extends AsyncTask<String, Void, Bitmap> {
        private OnNotificationImageLoaded listener;

        sendImageNotification(OnNotificationImageLoaded onNotificationImageLoaded) {
            super();
            this.listener = onNotificationImageLoaded;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                return BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            listener.OnLoad(result);
        }
    }
}
