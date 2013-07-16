package com.adcash.mobileads.factories;

import com.adcash.mobileads.CustomEventInterstitialAdapter;
import com.adcash.mobileads.AdcashInterstitial;

public class CustomEventInterstitialAdapterFactory {
    private static CustomEventInterstitialAdapterFactory instance = new CustomEventInterstitialAdapterFactory();

    @Deprecated // for testing
    public static void setInstance(CustomEventInterstitialAdapterFactory factory) {
        instance = factory;
    }

    public static CustomEventInterstitialAdapter create(AdcashInterstitial adCashInterstitial, String className, String classData) {
        return instance.internalCreate(adCashInterstitial, className, classData);
    }

    protected CustomEventInterstitialAdapter internalCreate(AdcashInterstitial adCashInterstitial, String className, String classData) {
        return new CustomEventInterstitialAdapter(adCashInterstitial, className, classData);
    }
}
