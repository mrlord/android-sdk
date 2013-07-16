package com.adcash.mobileads.factories;

import android.content.Context;
import com.adcash.mobileads.AdViewController;
import com.adcash.mobileads.AdcashView;

public class AdViewControllerFactory {
    protected static AdViewControllerFactory instance = new AdViewControllerFactory();

    @Deprecated // for testing
    public static void setInstance(AdViewControllerFactory factory) {
        instance = factory;
    }

    public static AdViewController create(Context context, AdcashView adCashView) {
        return instance.internalCreate(context, adCashView);
    }

    protected AdViewController internalCreate(Context context, AdcashView adCashView) {
        return new AdViewController(context, adCashView);
    }
}
