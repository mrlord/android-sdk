/*
 * Copyright (c) 2010-2013, Adcash OU.
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
import com.adcash.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import com.adcash.mobileads.factories.CustomEventInterstitialFactory;
import com.adcash.mobileads.util.Json;

import java.util.*;

import static com.adcash.mobileads.AdFetcher.AD_CONFIGURATION_KEY;
import static com.adcash.mobileads.AdcashErrorCode.ADAPTER_NOT_FOUND;
import static com.adcash.mobileads.AdcashErrorCode.NETWORK_TIMEOUT;
import static com.adcash.mobileads.AdcashErrorCode.UNSPECIFIED;

public class CustomEventInterstitialAdapter implements CustomEventInterstitialListener {
    public static final int DEFAULT_INTERSTITIAL_TIMEOUT_DELAY = 30000;

    private final AdcashInterstitial mAdcashInterstitial;
    private boolean mInvalidated;
    private CustomEventInterstitialAdapterListener mCustomEventInterstitialAdapterListener;
    private CustomEventInterstitial mCustomEventInterstitial;
    private Context mContext;
    private Map<String, Object> mLocalExtras;
    private Map<String, String> mServerExtras;
    private final Handler mHandler;
    private final Runnable mTimeout;

    public CustomEventInterstitialAdapter(AdcashInterstitial adCashInterstitial, String className, String jsonParams) {
        mHandler = new Handler();
        mAdcashInterstitial = adCashInterstitial;
        mServerExtras = new HashMap<String, String>();
        mLocalExtras = new HashMap<String, Object>();
        mContext = adCashInterstitial.getActivity();
        mTimeout = new Runnable() {
            @Override
            public void run() {
                Log.d("Adcash", "Third-party network timed out.");
                onInterstitialFailed(NETWORK_TIMEOUT);
                invalidate();
            }
        };

        Log.d("Adcash", "Attempting to invoke custom event: " + className);
        try {
            mCustomEventInterstitial = CustomEventInterstitialFactory.create(className);
        } catch (Exception exception) {
            Log.d("Adcash", "Couldn't locate or instantiate custom event: " + className + ".");
            if (mCustomEventInterstitialAdapterListener != null) mCustomEventInterstitialAdapterListener.onCustomEventInterstitialFailed(ADAPTER_NOT_FOUND);
        }
        
        // Attempt to load the JSON extras into mServerExtras.
        try {
            mServerExtras = Json.jsonStringToMap(jsonParams);
        } catch (Exception exception) {
            Log.d("Adcash", "Failed to create Map from JSON: " + jsonParams);
        }
        
        mLocalExtras = adCashInterstitial.getLocalExtras();
        if (adCashInterstitial.getLocation() != null) {
            mLocalExtras.put("location", adCashInterstitial.getLocation());
        }

        AdViewController adViewController = adCashInterstitial.getAdcashInterstitialView().getAdViewController();
        if (adViewController != null) {
            mLocalExtras.put(AD_CONFIGURATION_KEY, adViewController.getAdConfiguration());
        }
    }
    
    void loadInterstitial() {
        if (isInvalidated() || mCustomEventInterstitial == null) {
            return;
        }
        mCustomEventInterstitial.loadInterstitial(mContext, this, mLocalExtras, mServerExtras);

        if (getTimeoutDelayMilliseconds() > 0) {
            mHandler.postDelayed(mTimeout, getTimeoutDelayMilliseconds());
        }
    }
    
    void showInterstitial() {
        if (isInvalidated() || mCustomEventInterstitial == null) return;
        
        mCustomEventInterstitial.showInterstitial();
    }

    void invalidate() {
        if (mCustomEventInterstitial != null) mCustomEventInterstitial.onInvalidate();
        mCustomEventInterstitial = null;
        mContext = null;
        mServerExtras = null;
        mLocalExtras = null;
        mCustomEventInterstitialAdapterListener = null;
        mInvalidated = true;
    }

    boolean isInvalidated() {
        return mInvalidated;
    }

    void setAdapterListener(CustomEventInterstitialAdapterListener listener) {
        mCustomEventInterstitialAdapterListener = listener;
    }

    private void cancelTimeout() {
        mHandler.removeCallbacks(mTimeout);
    }

    private int getTimeoutDelayMilliseconds() {
        if (mAdcashInterstitial == null
                || mAdcashInterstitial.getAdTimeoutDelay() == null
                || mAdcashInterstitial.getAdTimeoutDelay() < 0) {
            return DEFAULT_INTERSTITIAL_TIMEOUT_DELAY;
        }

        return mAdcashInterstitial.getAdTimeoutDelay() * 1000;
    }

    interface CustomEventInterstitialAdapterListener {
        void onCustomEventInterstitialLoaded();
        void onCustomEventInterstitialFailed(AdcashErrorCode errorCode);
        void onCustomEventInterstitialShown();
        void onCustomEventInterstitialClicked();
        void onCustomEventInterstitialDismissed();
    }

    /*
     * CustomEventInterstitial.Listener implementation
     */
    @Override
    public void onInterstitialLoaded() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            cancelTimeout();
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialLoaded();
        }
    }

    @Override
    public void onInterstitialFailed(AdcashErrorCode errorCode) {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            if (errorCode == null) {
                errorCode = UNSPECIFIED;
            }
            cancelTimeout();
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialFailed(errorCode);
        }
    }

    @Override
    public void onInterstitialShown() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialShown();
        }
    }

    @Override
    public void onInterstitialClicked() {
        if (isInvalidated()) {
            return;
        }

        if (mCustomEventInterstitialAdapterListener != null) {
            mCustomEventInterstitialAdapterListener.onCustomEventInterstitialClicked();
        }
    }

    @Override
    public void onLeaveApplication() {
        onInterstitialClicked();
    }

    @Override
    public void onInterstitialDismissed() {
        if (isInvalidated()) return;

        if (mCustomEventInterstitialAdapterListener != null) mCustomEventInterstitialAdapterListener.onCustomEventInterstitialDismissed();
    }

    @Deprecated
    void setCustomEventInterstitial(CustomEventInterstitial interstitial) {
        mCustomEventInterstitial = interstitial;
    }
}
