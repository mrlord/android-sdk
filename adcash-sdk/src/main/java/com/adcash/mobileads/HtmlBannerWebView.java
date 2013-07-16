package com.adcash.mobileads;

import android.content.Context;

import static com.adcash.mobileads.CustomEventBanner.CustomEventBannerListener;

public class HtmlBannerWebView extends BaseHtmlWebView {
    public static final String EXTRA_AD_CLICK_DATA = "com.adcash.intent.extra.AD_CLICK_DATA";

    public HtmlBannerWebView(Context context) {
        super(context);
    }

    public void init(CustomEventBannerListener customEventBannerListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        super.init(isScrollable);

        setWebViewClient(new HtmlWebViewClient(new HtmlBannerWebViewListener(customEventBannerListener), this, clickthroughUrl, redirectUrl));
    }

    static class HtmlBannerWebViewListener implements HtmlWebViewListener {
        private final CustomEventBannerListener mCustomEventBannerListener;

        public HtmlBannerWebViewListener(CustomEventBannerListener customEventBannerListener) {
            mCustomEventBannerListener = customEventBannerListener;
        }

        @Override
        public void onLoaded(BaseHtmlWebView htmlWebView) {
            mCustomEventBannerListener.onBannerLoaded(htmlWebView);
        }

        @Override
        public void onFailed(AdcashErrorCode errorCode) {
            mCustomEventBannerListener.onBannerFailed(errorCode);
        }

        @Override
        public void onClicked() {
            mCustomEventBannerListener.onBannerClicked();
        }

        @Override
        public void onCollapsed() {
            mCustomEventBannerListener.onBannerCollapsed();
        }

    }
}