/*
 * Copyright (c) 2010-2013, Adcash Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.adcash.mobileads;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.adcash.mobileads.CustomEventBanner.CustomEventBannerListener;
import com.adcash.mobileads.factories.CustomEventBannerFactory;
import com.adcash.mobileads.util.Json;

import java.util.*;

import static com.adcash.mobileads.AdFetcher.AD_CONFIGURATION_KEY;
import static com.adcash.mobileads.AdcashErrorCode.ADAPTER_NOT_FOUND;
import static com.adcash.mobileads.AdcashErrorCode.NETWORK_TIMEOUT;
import static com.adcash.mobileads.AdcashErrorCode.UNSPECIFIED;

public class CustomEventBannerAdapter implements CustomEventBannerListener {
    public static final int DEFAULT_BANNER_TIMEOUT_DELAY = 10000;
    private boolean mInvalidated;
    private AdcashView mAdcashView;
    private Context mContext;
    private CustomEventBanner mCustomEventBanner;
    private Map<String, Object> mLocalExtras;
    private Map<String, String> mServerExtras;

    private final Handler mHandler;
    private final Runnable mTimeout;
    private boolean mStoredAutorefresh;

    public CustomEventBannerAdapter(AdcashView adCashView, String className, String classData) {
        mHandler = new Handler();
        mAdcashView = adCashView;
        mContext = adCashView.getContext();
        mLocalExtras = new HashMap<String, Object>();
        mServerExtras = new HashMap<String, String>();
        mTimeout = new Runnable() {
            @Override
            public void run() {
                Log.d("Adcash", "Third-party network timed out.");
                onBannerFailed(NETWORK_TIMEOUT);
                invalidate();
            }
        };

        Log.d("Adcash", "Attempting to invoke custom event: " + className);
        try {
            mCustomEventBanner = CustomEventBannerFactory.create(className);
        } catch (Exception exception) {
            Log.d("Adcash", "Couldn't locate or instantiate custom event: " + className + ".");
            mAdcashView.loadFailUrl(ADAPTER_NOT_FOUND);
            return;
        }

        // Attempt to load the JSON extras into mServerExtras.
        try {
            mServerExtras = Json.jsonStringToMap(classData);
        } catch (Exception exception) {
            Log.d("Adcash", "Failed to create Map from JSON: " + classData + exception.toString());
        }

        mLocalExtras = mAdcashView.getLocalExtras();
        if (mAdcashView.getLocation() != null) {
            mLocalExtras.put("location", mAdcashView.getLocation());
        }
        if (mAdcashView.getAdViewController() != null) {
            mLocalExtras.put(AD_CONFIGURATION_KEY, mAdcashView.getAdViewController().getAdConfiguration());
        }
    }

    void loadAd() {
        if (isInvalidated() || mCustomEventBanner == null) {
            return;
        }
        mCustomEventBanner.loadBanner(mContext, this, mLocalExtras, mServerExtras);

        if (getTimeoutDelayMilliseconds() > 0) {
            mHandler.postDelayed(mTimeout, getTimeoutDelayMilliseconds());
        }
    }

    void invalidate() {
        if (mCustomEventBanner != null) mCustomEventBanner.onInvalidate();
        mContext = null;
        mCustomEventBanner = null;
        mLocalExtras = null;
        mServerExtras = null;
        mInvalidated = true;
    }

    boolean isInvalidated() {
        return mInvalidated;
    }

    private void cancelTimeout() {
        mHandler.removeCallbacks(mTimeout);
    }

    private int getTimeoutDelayMilliseconds() {
        if (mAdcashView == null
                || mAdcashView.getAdTimeoutDelay() == null
                || mAdcashView.getAdTimeoutDelay() < 0) {
            return DEFAULT_BANNER_TIMEOUT_DELAY;
        }

        return mAdcashView.getAdTimeoutDelay() * 1000;
    }

    /*
     * CustomEventBanner.Listener implementation
     */
    @Override
    public void onBannerLoaded(View bannerView) {
        if (isInvalidated()) return;
        
        if (mAdcashView != null) {
            cancelTimeout();
            mAdcashView.nativeAdLoaded();
            mAdcashView.setAdContentView(bannerView);
            if (!(bannerView instanceof HtmlBannerWebView)) {
                mAdcashView.trackNativeImpression();
            }
        }
    }

    @Override
    public void onBannerFailed(AdcashErrorCode errorCode) {
        if (isInvalidated()) return;
        
        if (mAdcashView != null) {
            if (errorCode == null) {
                errorCode = UNSPECIFIED;
            }
            cancelTimeout();
            mAdcashView.loadFailUrl(errorCode);
        }
    }

    @Override
    public void onBannerExpanded() {
        if (isInvalidated()) return;

        mStoredAutorefresh = mAdcashView.getAutorefreshEnabled();
        mAdcashView.setAutorefreshEnabled(false);
        mAdcashView.adPresentedOverlay();
    }

    @Override
    public void onBannerCollapsed() {
        if (isInvalidated()) return;

        mAdcashView.setAutorefreshEnabled(mStoredAutorefresh);
        mAdcashView.adClosed();
    }

    @Override
    public void onBannerClicked() {
        if (isInvalidated()) return;
        
        if (mAdcashView != null) mAdcashView.registerClick();
    }
    
    @Override
    public void onLeaveApplication() {
        onBannerClicked();
    }
}
