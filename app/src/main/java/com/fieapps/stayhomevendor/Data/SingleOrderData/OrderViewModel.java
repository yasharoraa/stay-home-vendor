package com.fieapps.stayhomevendor.Data.SingleOrderData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fieapps.stayhomevendor.Models.Order.Order;

public class OrderViewModel extends ViewModel {

    private MutableLiveData<Order> orderLiveData;

    private OrdersRepo ordersRepo;

    private final String TAG = this.getClass().getSimpleName();

    public void init(String orderId){
        if (orderLiveData !=null && orderLiveData.getValue()!=null)
            return;

        ordersRepo = OrdersRepo.getInstance();
        orderLiveData = ordersRepo.getOrders(orderId);
    }

    public LiveData<Order> getOrdersRepo() {
        return orderLiveData;
    }

}
