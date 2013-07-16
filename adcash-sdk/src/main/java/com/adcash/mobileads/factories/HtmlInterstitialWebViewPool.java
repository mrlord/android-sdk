package com.adcash.mobileads.factories;

import android.content.Context;
import com.adcash.mobileads.CustomEventInterstitial;
import com.adcash.mobileads.HtmlInterstitialWebView;

public class HtmlInterstitialWebViewPool extends BaseHtmlWebViewPool<HtmlInterstitialWebView, CustomEventInterstitial.CustomEventInterstitialListener> {

    HtmlInterstitialWebViewPool(Context context) {
        super(context);
    }

    @Override
    protected HtmlInterstitialWebView createNewHtmlWebView() {
        return new HtmlInterstitialWebView(mContext);
    }

    @Override
    protected void initializeHtmlWebView(
            HtmlInterstitialWebView htmlWebView, CustomEventInterstitial.CustomEventInterstitialListener customEventListener,
            boolean isScrollable,
            String redirectUrl,
            String clickthroughUrl) {
        htmlWebView.init(customEventListener, isScrollable, redirectUrl, clickthroughUrl);
    }
}
