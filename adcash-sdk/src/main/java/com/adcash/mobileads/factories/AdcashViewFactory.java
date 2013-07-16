package com.adcash.mobileads.factories;

import android.content.Context;
import com.adcash.mobileads.AdcashView;

public class AdcashViewFactory {
    private static AdcashViewFactory instance = new AdcashViewFactory();

    @Deprecated // for testing
    public static void setInstance(AdcashViewFactory factory) {
        instance = factory;
    }

    public static AdcashView create(Context context) {
        return instance.internalCreate(context);
    }

    protected AdcashView internalCreate(Context context) {
        return new AdcashView(context);
    }
}
