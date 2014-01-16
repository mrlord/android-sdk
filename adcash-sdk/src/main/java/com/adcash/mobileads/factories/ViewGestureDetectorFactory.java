package com.adcash.mobileads.factories;

import android.content.Context;
import android.view.View;

import com.adcash.mobileads.AdConfiguration;
import com.adcash.mobileads.ViewGestureDetector;

public class ViewGestureDetectorFactory {
    protected static ViewGestureDetectorFactory instance = new ViewGestureDetectorFactory();

    @Deprecated // for testing
    public static void setInstance(ViewGestureDetectorFactory factory) {
        instance = factory;
    }

    public static ViewGestureDetector create(Context context, View view, AdConfiguration adConfiguration) {
        return instance.internalCreate(context, view, adConfiguration);
    }

    protected ViewGestureDetector internalCreate(Context context, View view, AdConfiguration adConfiguration) {
        return new ViewGestureDetector(context, view, adConfiguration);
    }
}

