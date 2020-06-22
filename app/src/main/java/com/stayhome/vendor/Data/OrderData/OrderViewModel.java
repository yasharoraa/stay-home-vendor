package com.stayhome.vendor.Data.OrderData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.stayhome.vendor.Models.Order.Order;

import java.util.List;

public class OrderViewModel extends ViewModel {

    private MutableLiveData<List<Order>> orderLiveData;

    private OrdersRepo ordersRepo;

    private int initialOffset;

    private final String TAG = this.getClass().getSimpleName();

    public void init(String token,int offset,int limit,boolean clear,double[] coordinates){
        if (orderLiveData !=null && orderLiveData.getValue()!=null && initialOffset == offset && !clear)
            return;

        initialOffset = offset;

        ordersRepo = OrdersRepo.getInstance();
        orderLiveData = ordersRepo.getOrders(token,offset,limit,coordinates);
    }

    public LiveData<List<Order>> getOrdersRepo() {
        return orderLiveData;
    }

    public void postValue(List<Order> orders){
        orderLiveData = new MutableLiveData<>();
        orderLiveData.postValue(orders);
    }
}
