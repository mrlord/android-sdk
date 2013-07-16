package com.adcash.mobileads;

import static com.adcash.mobileads.AdcashView.BannerAdListener;

public class DefaultBannerAdListener implements BannerAdListener {
    @Override public void onBannerLoaded(AdcashView banner) { }
    @Override public void onBannerFailed(AdcashView banner, AdcashErrorCode errorCode) { }
    @Override public void onBannerClicked(AdcashView banner) { }
    @Override public void onBannerExpanded(AdcashView banner) { }
    @Override public void onBannerCollapsed(AdcashView banner) { }
}
