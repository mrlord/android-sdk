package com.adcash.mobileads;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import com.adcash.mobileads.Log;

import com.adcash.mobileads.CustomEventBanner.CustomEventBannerListener;
import com.adcash.mobileads.factories.CustomEventBannerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.adcash.mobileads.AdcashErrorCode.ADAPTER_NOT_FOUND;
import static com.adcash.mobileads.AdcashErrorCode.NETWORK_TIMEOUT;
import static com.adcash.mobileads.AdcashErrorCode.UNSPECIFIED;

public class CustomEventBannerAdapter implements CustomEventBannerListener {
    public static final int TIMEOUT_DELAY = 10000;
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
            mServerExtras = Utils.jsonStringToMap(classData);
        } catch (Exception exception) {
            Log.d("Adcash", "Failed to create Map from JSON: " + classData + exception.toString());
        }

        mLocalExtras = mAdcashView.getLocalExtras();
        if (mAdcashView.getLocation() != null) {
            mLocalExtras.put("location", mAdcashView.getLocation());
        }
    }

    void loadAd() {
        if (isInvalidated() || mCustomEventBanner == null) return;

        mHandler.postDelayed(mTimeout, TIMEOUT_DELAY);
        mCustomEventBanner.loadBanner(mContext, this, mLocalExtras, mServerExtras);
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
