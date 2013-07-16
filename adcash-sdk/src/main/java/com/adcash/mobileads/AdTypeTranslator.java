package com.adcash.mobileads;

import java.util.HashMap;
import java.util.Map;

public class AdTypeTranslator {
    public static final String ADMOB_BANNER = "com.adcash.mobileads.GoogleAdMobBanner";
    public static final String ADMOB_INTERSTITIAL = "com.adcash.mobileads.GoogleAdMobInterstitial";
    public static final String MILLENNIAL_BANNER = "com.adcash.mobileads.MillennialBanner";
    public static final String MILLENNIAL_INTERSTITIAL = "com.adcash.mobileads.MillennialInterstitial";
    public static final String MRAID_BANNER = "com.adcash.mobileads.MraidBanner";
    public static final String MRAID_INTERSTITIAL = "com.adcash.mobileads.MraidInterstitial";
    public static final String HTML_BANNER = "com.adcash.mobileads.HtmlBanner";
    public static final String HTML_INTERSTITIAL = "com.adcash.mobileads.HtmlInterstitial";
    private static Map<String, String> customEventNameForAdType = new HashMap<String, String>();

    static {
        customEventNameForAdType.put("admob_native_banner", ADMOB_BANNER);
        customEventNameForAdType.put("admob_full_interstitial", ADMOB_INTERSTITIAL);
        customEventNameForAdType.put("millennial_native_banner", MILLENNIAL_BANNER);
        customEventNameForAdType.put("millennial_full_interstitial", MILLENNIAL_INTERSTITIAL);
        customEventNameForAdType.put("mraid_banner", MRAID_BANNER);
        customEventNameForAdType.put("mraid_interstitial", MRAID_INTERSTITIAL);
        customEventNameForAdType.put("html_banner", HTML_BANNER);
        customEventNameForAdType.put("html_interstitial", HTML_INTERSTITIAL);
    }

    static String getCustomEventNameForAdType(AdcashView adCashView, String adType, String fullAdType) {
        if ("html".equals(adType) || "mraid".equals(adType)) {
            return isInterstitial(adCashView)
                   ? customEventNameForAdType.get(adType + "_interstitial")
                   : customEventNameForAdType.get(adType + "_banner");
        } else {
            return "interstitial".equals(adType)
                    ? customEventNameForAdType.get(fullAdType + "_interstitial")
                    : customEventNameForAdType.get(adType + "_banner");
        }
    }

    private static boolean isInterstitial(AdcashView adCashView) {
        return adCashView instanceof AdcashInterstitial.AdcashInterstitialView;
    }
}
