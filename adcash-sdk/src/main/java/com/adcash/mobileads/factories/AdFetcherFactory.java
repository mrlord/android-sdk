package com.adcash.mobileads.factories;

import com.adcash.mobileads.AdFetcher;
import com.adcash.mobileads.AdViewController;

public class AdFetcherFactory {
    protected static AdFetcherFactory instance = new AdFetcherFactory();

    public static void setInstance(AdFetcherFactory factory) {
        instance = factory;
    }

    public static AdFetcher create(AdViewController adViewController, String userAgent) {
        return instance.internalCreate(adViewController, userAgent);
    }

    protected AdFetcher internalCreate(AdViewController adViewController, String userAgent) {
        return new AdFetcher(adViewController, userAgent);
    }
}
