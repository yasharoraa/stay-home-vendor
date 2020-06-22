package com.stayhome.vendor.Data.OrderData;

import androidx.lifecycle.MutableLiveData;


import com.stayhome.vendor.Models.Order.Order;
import com.stayhome.vendor.WebServices.ApiInterface;
import com.stayhome.vendor.WebServices.ServiceGenerator;

import java.util.List;

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

    MutableLiveData<List<Order>> getOrders(String token, int offset, int limit,double[] coordinates) {
        MutableLiveData<List<Order>> ordersData = new MutableLiveData<>();
        apiInterface.getMyOrders(token,offset,limit,coordinates[0],coordinates[1]).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful()) {
                    ordersData.setValue(response.body());
                } else {
                    ordersData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                ordersData.setValue(null);
            }
        });
        return ordersData;
    }
}