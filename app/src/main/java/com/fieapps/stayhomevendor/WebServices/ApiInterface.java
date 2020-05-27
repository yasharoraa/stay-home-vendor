package com.fieapps.stayhomevendor.WebServices;

import androidx.annotation.Keep;

import com.fieapps.stayhomevendor.Models.Address;
import com.fieapps.stayhomevendor.Models.Category;
import com.fieapps.stayhomevendor.Models.CreateUser;
import com.fieapps.stayhomevendor.Models.Order.Order;
import com.fieapps.stayhomevendor.Models.Order.OrderForStore;
import com.fieapps.stayhomevendor.Models.Store.CreateStore;
import com.fieapps.stayhomevendor.Models.Store.Store;
import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

@Keep
public interface ApiInterface {

    String AUTH = "Authorization";
    String OFFSET = "offset";

    @GET("orders/store")
    Call<List<OrderForStore>> getStoreOrders(@Header(AUTH) String token, @Query(OFFSET) int offset);


    @GET("orders/store/{id}")
    Call<OrderForStore> getStoreOrderById(@Header(AUTH) String token, @Path(value = "id") String id);

    @GET("stores/profile")
    Call<Store> getMyStoreProfile(@Header(AUTH) String token);

    @PUT("stores")
    Call<CreateStore> updateMyStoreProfile(@Header(AUTH) String token, @Body CreateStore store);

    @POST("temp/store")
    Call<JsonObject> createStoreProfile(@Body CreateUser user);

    @POST("stores/{temp_id}")
    Call<CreateStore> verifyNumber(@Path(value = "temp_id",encoded = true) String tempId,@Query("otp") int otp);

    @POST("stores/login")
    Call<CreateStore> loginStore(@Body CreateUser createUser);

    @GET("categories")
    Call<List<Category>> getCategories();

    @GET("stores/nearby")
    Call<List<Store>> getNearByStoresByCustomPin(@Query("pincode") String pincode);

    @Multipart
    @POST("upload")
    Call<JsonObject> postImage(@Header(AUTH) String token, @Part MultipartBody.Part image, @Query("sub") String sub);

    @GET("address")
    Call<List<Address>> getAddress(@Header(AUTH) String token);

    @GET("orders/store")
    Call<List<Order>> getMyOrders(@Header(AUTH) String token,
                                  @Query("offset") Integer offset,
                                  @Query("limit") Integer limit,
                                  @Query("lat") double latitude,
                                  @Query("lon") double longitude);

    @GET("orders/store/{order_id}")
    Call<Order> getOrderById(@Path(value = "order_id", encoded = true) String orderId);

    @PUT("orders/cancel/{order_id}")
    Call<Order> cancelOrder(@Header(AUTH) String token, @Path(value = "order_id", encoded = true) String orderId);

    @PUT("orders/update/{order_id}")
    Call<Order> updateOrder(@Header(AUTH) String token, @Path(value = "order_id", encoded = true) String orderId,@Query("code") int code);

    @GET("stores/amionline")
    Call<JsonObject> amIOnline(@Header(AUTH) String token);

    @PUT("stores/online")
    Call<JsonObject> setOnline(@Header(AUTH) String token,@Query("online")int online);

    @GET("stores")
    Call<CreateStore> getMyStore(@Header(AUTH) String token);

    @PUT("stores/firebase")
    Call<ResponseBody> putFirebaseToken(@Header(AUTH) String token,@Query("firebase") String firebaseToken);

    @GET("version/store")
    Call<String> getMinimumVersion();

    @GET("geocoding")
    Call<JsonObject> getMyKey(@Header(AUTH) String token);
}
