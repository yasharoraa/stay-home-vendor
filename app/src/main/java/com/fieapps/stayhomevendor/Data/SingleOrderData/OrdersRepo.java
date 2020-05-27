package com.fieapps.stayhomevendor.Data.SingleOrderData;

import androidx.lifecycle.MutableLiveData;

import com.fieapps.stayhomevendor.Models.Order.Order;
import com.fieapps.stayhomevendor.WebServices.ApiInterface;
import com.fieapps.stayhomevendor.WebServices.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class OrdersRepo {

    private static OrdersRepo ordersRepo;

    static OrdersRepo getInstance() {
        if (ordersRepo == null){
            ordersRepo = new OrdersRepo();
        }
        return ordersRepo;
    }

    private ApiInterface apiInterface;

    private OrdersRepo() {
        apiInterface = ServiceGenerator.createService(ApiInterface.class);
    }

    MutableLiveData<Order> getOrders(String orderId) {
        MutableLiveData<Order> orderData = new MutableLiveData<>();
        apiInterface.getOrderById(orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body()!=null){
                    orderData.setValue(response.body());
                }else{
                    orderData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                orderData.setValue(null);
            }
        });
        return orderData;
    }
}