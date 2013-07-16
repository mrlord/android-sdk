package com.adcash.mobileads;

import android.net.Uri;

import java.util.Map;

import static com.adcash.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.adcash.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.adcash.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.adcash.mobileads.AdFetcher.SCROLLABLE_KEY;

public class HtmlInterstitial extends ResponseBodyInterstitial {
    private String mHtmlData;
    private String mClickthroughUrl;
    private String mRedirectUrl;
    private boolean mIsScrollable;

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = Uri.decode(serverExtras.get(HTML_RESPONSE_BODY_KEY));
        mRedirectUrl = serverExtras.get(REDIRECT_URL_KEY);
        mClickthroughUrl = serverExtras.get(CLICKTHROUGH_URL_KEY);
        mIsScrollable = Boolean.valueOf(serverExtras.get(SCROLLABLE_KEY));
    }

    @Override
    protected void showInterstitial() {
        AdcashActivity.start(mContext, mHtmlData, mIsScrollable, mRedirectUrl, mClickthroughUrl);
    }
}
