package com.adcash.mobileads;

import android.content.Context;
import android.net.Uri;
import com.adcash.mobileads.factories.HtmlBannerWebViewFactory;

import java.util.Map;

import static com.adcash.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.adcash.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.adcash.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.adcash.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.adcash.mobileads.AdcashErrorCode.NETWORK_INVALID_STATE;

public class HtmlBanner extends CustomEventBanner {

    private HtmlBannerWebView mHtmlBannerWebView;

    @Override
    protected void loadBanner(
            Context context,
            CustomEventBannerListener customEventBannerListener,
            Map<String, Object> localExtras,
            Map<String, String> serverExtras) {

        String htmlData;
        String redirectUrl;
        String clickthroughUrl;
        Boolean isScrollable;
        if (extrasAreValid(serverExtras)) {
            htmlData = Uri.decode(serverExtras.get(HTML_RESPONSE_BODY_KEY));
            redirectUrl = serverExtras.get(REDIRECT_URL_KEY);
            clickthroughUrl = serverExtras.get(CLICKTHROUGH_URL_KEY);
            isScrollable = Boolean.valueOf(serverExtras.get(SCROLLABLE_KEY));
        } else {
            customEventBannerListener.onBannerFailed(NETWORK_INVALID_STATE);
            return;
        }

        mHtmlBannerWebView = HtmlBannerWebViewFactory.create(customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl);
        AdViewController.setShouldHonorServerDimensions(mHtmlBannerWebView);
        mHtmlBannerWebView.loadHtmlResponse("Debug banner load html response: " + htmlData);
    }

    @Override
    protected void onInvalidate() {
    	Log.d("Adcash", "Called destroy of Adcash HTML banner invalidate");
        if (mHtmlBannerWebView != null) {
        	mHtmlBannerWebView.removeAllViews();
            mHtmlBannerWebView.destroy();
        }
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        return serverExtras.containsKey(HTML_RESPONSE_BODY_KEY);
    }
}
