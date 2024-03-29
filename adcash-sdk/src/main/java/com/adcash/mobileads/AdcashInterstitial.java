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

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.adcash.mobileads.AdcashView.LocationAwareness;
import com.adcash.mobileads.factories.CustomEventInterstitialAdapterFactory;

import java.util.*;

import static com.adcash.mobileads.AdcashErrorCode.ADAPTER_NOT_FOUND;
import static com.adcash.mobileads.util.ResponseHeader.CUSTOM_EVENT_DATA;
import static com.adcash.mobileads.util.ResponseHeader.CUSTOM_EVENT_NAME;

public class AdcashInterstitial implements CustomEventInterstitialAdapter.CustomEventInterstitialAdapterListener {

    private enum InterstitialState {
        CUSTOM_EVENT_AD_READY,
        NOT_READY;

        boolean isReady() {
            return this != InterstitialState.NOT_READY;
        }
    }

    private AdcashInterstitialView mInterstitialView;
    private CustomEventInterstitialAdapter mCustomEventInterstitialAdapter;
    private InterstitialAdListener mInterstitialAdListener;
    private Activity mActivity;
    private String mAdUnitId;
    private InterstitialState mCurrentInterstitialState;
    private boolean mIsDestroyed;

    public interface InterstitialAdListener {
        public void onInterstitialLoaded(AdcashInterstitial interstitial);
        public void onInterstitialFailed(AdcashInterstitial interstitial, AdcashErrorCode errorCode);
        public void onInterstitialShown(AdcashInterstitial interstitial);
        public void onInterstitialClicked(AdcashInterstitial interstitial);
        public void onInterstitialDismissed(AdcashInterstitial interstitial);
    }

    private AdcashInterstitialListener mListener;

    @Deprecated
    public interface AdcashInterstitialListener {
        public void OnInterstitialLoaded();
        public void OnInterstitialFailed();
    }

    public AdcashInterstitial(Activity activity, String id) {
        mActivity = activity;
        mAdUnitId = id;

        mInterstitialView = new AdcashInterstitialView(mActivity);
        mInterstitialView.setAdUnitId(mAdUnitId);

        mCurrentInterstitialState = InterstitialState.NOT_READY;

    }

    public void load() {
        resetCurrentInterstitial();
        mInterstitialView.loadAd();
    }

    public void forceRefresh() {
        resetCurrentInterstitial();
        mInterstitialView.forceRefresh();
    }

    private void resetCurrentInterstitial() {
        mCurrentInterstitialState = InterstitialState.NOT_READY;

        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }

        mIsDestroyed = false;
    }

    public boolean isReady() {
        return mCurrentInterstitialState.isReady();
    }

    boolean isDestroyed() {
        return mIsDestroyed;
    }

    public boolean show() {
        switch (mCurrentInterstitialState) {
            case CUSTOM_EVENT_AD_READY:
                showCustomEventInterstitial();
                return true;
        }
        return false;
    }

    private void showCustomEventInterstitial() {
        if (mCustomEventInterstitialAdapter != null) mCustomEventInterstitialAdapter.showInterstitial();
    }

    Integer getAdTimeoutDelay() {
        return mInterstitialView.getAdTimeoutDelay();
    }

    AdcashInterstitialView getAdcashInterstitialView() {
        return mInterstitialView;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setKeywords(String keywords) {
        mInterstitialView.setKeywords(keywords);
    }

    public String getKeywords() {
        return mInterstitialView.getKeywords();
    }

    public void setFacebookSupported(boolean enabled) {
        mInterstitialView.setFacebookSupported(enabled);
    }

    public boolean isFacebookSupported() {
        return mInterstitialView.isFacebookSupported();
    }

    public Activity getActivity() {
        return mActivity;
    }

    public Location getLocation() {
        return mInterstitialView.getLocation();
    }

    public void destroy() {
        mIsDestroyed = true;

        if (mCustomEventInterstitialAdapter != null) {
            mCustomEventInterstitialAdapter.invalidate();
            mCustomEventInterstitialAdapter = null;
        }

        mInterstitialView.setBannerAdListener(null);
        mInterstitialView.destroy();
    }

    public void setInterstitialAdListener(InterstitialAdListener listener) {
        mInterstitialAdListener = listener;
    }

    public InterstitialAdListener getInterstitialAdListener() {
        return mInterstitialAdListener;
    }

    public void setLocationAwareness(LocationAwareness awareness) {
        mInterstitialView.setLocationAwareness(awareness);
    }

    public LocationAwareness getLocationAwareness() {
        return mInterstitialView.getLocationAwareness();
    }

    public void setLocationPrecision(int precision) {
        mInterstitialView.setLocationPrecision(precision);
    }

    public int getLocationPrecision() {
        return mInterstitialView.getLocationPrecision();
    }

    public void setTesting(boolean testing) {
        mInterstitialView.setTesting(testing);
    }

    public boolean getTesting() {
        return mInterstitialView.getTesting();
    }

    public void setLocalExtras(Map<String, Object> extras) {
        mInterstitialView.setLocalExtras(extras);
    }

    public Map<String, Object> getLocalExtras() {
        return mInterstitialView.getLocalExtras();
    }

    /*
     * Implements CustomEventInterstitialAdapter.CustomEventInterstitialListener
     */

    @Override
    public void onCustomEventInterstitialLoaded() {
        if (mIsDestroyed) return;

        mCurrentInterstitialState = InterstitialState.CUSTOM_EVENT_AD_READY;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialLoaded(this);
        } else if (mListener != null) {
            mListener.OnInterstitialLoaded();
        }
    }

    @Override
    public void onCustomEventInterstitialFailed(AdcashErrorCode errorCode) {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;
        mInterstitialView.loadFailUrl(errorCode);
    }

    @Override
    public void onCustomEventInterstitialShown() {
        if (isDestroyed()) return;

        mInterstitialView.trackImpression();

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialShown(this);
        }
    }

    @Override
    public void onCustomEventInterstitialClicked() {
        if (isDestroyed()) return;

        mInterstitialView.registerClick();

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialClicked(this);
        }
    }

    @Override
    public void onCustomEventInterstitialDismissed() {
        if (isDestroyed()) return;

        mCurrentInterstitialState = InterstitialState.NOT_READY;

        if (mInterstitialAdListener != null) {
            mInterstitialAdListener.onInterstitialDismissed(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class AdcashInterstitialView extends AdcashView {

        public AdcashInterstitialView(Context context) {
            super(context);
            setAutorefreshEnabled(false);
        }

        @Override
        protected void loadCustomEvent(Map<String, String> paramsMap) {
            if (paramsMap == null) {
                Log.d("Adcash", "Couldn't invoke custom event because the server did not specify one.");
                loadFailUrl(ADAPTER_NOT_FOUND);
                return;
            }

            if (mCustomEventInterstitialAdapter != null) {
                mCustomEventInterstitialAdapter.invalidate();
            }

            Log.d("Adcash", "Loading custom event interstitial adapter.");

            mCustomEventInterstitialAdapter = CustomEventInterstitialAdapterFactory.create(
                    AdcashInterstitial.this,
                    paramsMap.get(CUSTOM_EVENT_NAME.getKey()),
                    paramsMap.get(CUSTOM_EVENT_DATA.getKey()));
            mCustomEventInterstitialAdapter.setAdapterListener(AdcashInterstitial.this);
            mCustomEventInterstitialAdapter.loadInterstitial();
        }

        protected void trackImpression() {
            Log.d("Adcash", "Tracking impression for interstitial.");
            if (mAdViewController != null) mAdViewController.trackImpression();
        }

        @Override
        protected void adFailed(AdcashErrorCode errorCode) {
            if (mInterstitialAdListener != null) {
                mInterstitialAdListener.onInterstitialFailed(AdcashInterstitial.this, errorCode);
            }
        }
    }

    @Deprecated // for testing
    void setInterstitialView(AdcashInterstitialView interstitialView) {
        mInterstitialView = interstitialView;
    }

    @Deprecated
    public void setListener(AdcashInterstitialListener listener) {
        mListener = listener;
    }

    @Deprecated
    public AdcashInterstitialListener getListener() {
        return mListener;
    }

    @Deprecated
    public void customEventDidLoadAd() {
        if (mInterstitialView != null) mInterstitialView.trackImpression();
    }

    @Deprecated
    public void customEventDidFailToLoadAd() {
        if (mInterstitialView != null) mInterstitialView.loadFailUrl(AdcashErrorCode.UNSPECIFIED);
    }

    @Deprecated
    public void customEventActionWillBegin() {
        if (mInterstitialView != null) mInterstitialView.registerClick();
    }
}
