package com.adcash.mobileads.factories;

import com.adcash.mobileads.CustomEventBannerAdapter;
import com.adcash.mobileads.AdcashView;

public class CustomEventBannerAdapterFactory {
    private static CustomEventBannerAdapterFactory instance = new CustomEventBannerAdapterFactory();

    @Deprecated // for testing
    public static void setInstance(CustomEventBannerAdapterFactory factory) {
        instance = factory;
    }

    public static CustomEventBannerAdapter create(AdcashView adCashView, String className, String classData) {
        return instance.internalCreate(adCashView, className, classData);
    }

    protected CustomEventBannerAdapter internalCreate(AdcashView adCashView, String className, String classData) {
        return new CustomEventBannerAdapter(adCashView, className, classData);
    }
}
