package com.adcash.mobileads;

import static com.adcash.mobileads.AdcashInterstitial.InterstitialAdListener;

public class DefaultInterstitialAdListener implements InterstitialAdListener {
    @Override public void onInterstitialLoaded(AdcashInterstitial interstitial) { }
    @Override public void onInterstitialFailed(AdcashInterstitial interstitial, AdcashErrorCode errorCode) { }
    @Override public void onInterstitialShown(AdcashInterstitial interstitial) { }
    @Override public void onInterstitialClicked(AdcashInterstitial interstitial) { }
    @Override public void onInterstitialDismissed(AdcashInterstitial interstitial) { }
}
