package com.adcash.mobileads;

public interface HtmlWebViewListener {
    void onLoaded(BaseHtmlWebView mHtmlWebView);
    void onFailed(AdcashErrorCode unspecified);
    void onClicked();
    void onCollapsed();
}
