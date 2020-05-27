package com.fieapps.stayhomevendor.Models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.AbstractMap;

@Keep
public class ResError {

    @SerializedName("errors")
    private AbstractMap.SimpleEntry<Object,String> map;

    public AbstractMap.SimpleEntry<Object,String> getMap() {
        return map;
    }
}
