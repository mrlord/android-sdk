package com.adcash.mobileads;

import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import com.adcash.mobileads.util.DateAndTime;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.net.ConnectivityManager.*;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.ETHERNET;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.MOBILE;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.UNKNOWN;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.WIFI;

public class AdUrlGenerator extends BaseUrlGenerator {

    public AdUrlGenerator(Context context) {
        mContext = context;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);      
        mSharedPreferences = mContext.getSharedPreferences(AdcashConversionTracker.PREFERENCE_NAME, MODE_PRIVATE);
    }

    public AdUrlGenerator withAdUnitId(String adUnitId) {
        mAdUnitId = adUnitId;
        return this;
    }

    public AdUrlGenerator withKeywords(String keywords) {
        mKeywords = keywords;
        return this;
    }

    public AdUrlGenerator withLocation(Location location) {
        mLocation = location;
        return this;
    }

    @Override
    public String generateUrlString(String serverHostname) {
        initUrlString(serverHostname, AdcashView.AD_HANDLER);

        setApiVersion("6");

        setAdUnitId(mAdUnitId);

        setSdkVersion(Adcash.SDK_VERSION);

        setAndroidId(getAndroidIdFromContext(mContext));
        setDeviceId(getDeviceIdFromContext(mContext));

        setMacAddress(getWifiMacAddress());
        
        setLocation(mLocation);

        setTimezone(getTimeZoneOffsetString());

        setOrientation(mContext.getResources().getConfiguration().orientation);

        setDensity(mContext.getResources().getDisplayMetrics().density);

        setWidth(mContext.getResources().getDisplayMetrics().widthPixels);    
        setHeight(mContext.getResources().getDisplayMetrics().heightPixels);    
        
        setMraidFlag(detectIsMraidSupported());

        String networkOperator = getNetworkOperator();
        setMccCode(networkOperator);
        setMncCode(networkOperator);

        setIsoCountryCode(mTelephonyManager.getNetworkCountryIso());
        setCarrierName(mTelephonyManager.getNetworkOperatorName());

        setNetworkType(getActiveNetworkType());

        setAppVersion(getAppVersionFromContext(mContext));
        setLocale(Locale.getDefault().toString());
        
        setManufacturer(android.os.Build.MANUFACTURER);
        setModel(android.os.Build.MODEL);
        setAndroidVersion(android.os.Build.VERSION.RELEASE);

        setTrackingId(getTrackingId());
        
        String keywords = AdUrlGenerator.addKeyword(mKeywords, AdUrlGenerator.getFacebookKeyword(mContext));
        setKeywords(keywords);
        
        return getFinalUrlString();
    }

    private boolean detectIsMraidSupported() {
        boolean mraid = true;
        try {
            Class.forName("com.adcash.mobileads.MraidView");
        } catch (ClassNotFoundException e) {
            mraid = false;
        }
        return mraid;
    }


    private static String getFacebookKeyword(Context context) {
        try {
            Class<?> facebookKeywordProviderClass = Class.forName("com.adcash.mobileads.FacebookKeywordProvider");
            Method getKeywordMethod = facebookKeywordProviderClass.getMethod("getKeyword", Context.class);

            return (String) getKeywordMethod.invoke(facebookKeywordProviderClass, context);
        } catch (Exception exception) {
            return null;
        }
    }



    private static String addKeyword(String keywords, String addition) {
        if (addition == null || addition.length() == 0) {
            return keywords;
        } else if (keywords == null || keywords.length() == 0) {
            return addition;
        } else {
            return keywords + "," + addition;
        }
    }
}
