/*
 * Copyright (c) 2010 - 2013, Adcash OU and MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.adcash.mobileads;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.adcash.mobileads.Log;

import static android.content.Context.MODE_PRIVATE;

public class AdcashConversionTracker {
    private static final String TRACK_HOST = "m.adcash.com";
    private static final String TRACK_HANDLER = "/open.php";
    public static final String PREFERENCE_NAME = "adcashSettings";
    
    private Context mContext;
    private String mIsTrackedKey;
    private SharedPreferences mSharedPreferences;
    private String mPackageName;
    
    public void reportAppOpen(Context context) {
        if (context == null) {
            return;
        }

        mContext = context;
        mPackageName = mContext.getPackageName();
        mIsTrackedKey = mPackageName + " tracked";
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);

        mContext = context;

        if (!isAlreadyTracked()) {
            Log.d("Adcash", "Conversion: First open of the application");
        } else {
            Log.d("Adcash", "Conversion already tracked");
        }
        new Thread(mTrackOpen).start();
    }

    Runnable mTrackOpen = new Runnable() {
        public void run() {
            Log.d("Adcash", "Conversion track is initiated, sleeping 1.5 seconds");
            try {
				Thread.sleep(1500);
			} catch (InterruptedException e1) {
	            Log.d("Adcash", "Conversion track has called InterruptedException");
			}
            String url = new ConversionUrlGenerator(mContext).generateUrlString(TRACK_HOST);
            Log.d("Adcash", "Conversion track: " + url);

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response;
            try {
                HttpGet httpget = new HttpGet(url);
                response = httpClient.execute(httpget);
            } catch (Exception e) {
                Log.d("Adcash", "Conversion track failed [" + e.getClass().getSimpleName() + "]: " + url);
                return;
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.d("Adcash", "Conversion track failed: Status code != 200.");
                return;
            }

            HttpEntity entity = response.getEntity();
            if (entity == null || entity.getContentLength() == 0) {
                Log.d("Adcash", "Conversion track failed: Response was empty.");
                return;
            }

            // If we made it here, the request has been tracked
            Log.d("Adcash", "Conversion track successful.");
            mSharedPreferences
                    .edit()
                    .putBoolean(mIsTrackedKey, true)
                    .commit();
        }
    };

    private boolean isAlreadyTracked() {
        return mSharedPreferences.getBoolean(mIsTrackedKey, false);
    }

    
    private class ConversionUrlGenerator extends BaseUrlGenerator {
    //    @Override

        public ConversionUrlGenerator(Context context)
        {
        	mContext = context;
            mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);      
            mSharedPreferences = mContext.getSharedPreferences(AdcashConversionTracker.PREFERENCE_NAME, MODE_PRIVATE);
        }

        public String generateUrlString(String serverHostname) {
        	Log.d("Adcash", "Building tracking URL");
        	
            initUrlString(serverHostname, TRACK_HANDLER);

            setApiVersion("6");
            setPackageId(mPackageName);
            setFirstOpen(!isAlreadyTracked());
            setAndroidId(getAndroidIdFromContext(mContext));
            setDeviceId(getDeviceIdFromContext(mContext));

            setMacAddress(getWifiMacAddress());
            
            setAppVersion(getAppVersionFromContext(mContext));

            setLocation(mLocation);

            setTimezone(getTimeZoneOffsetString());

            setOrientation(mContext.getResources().getConfiguration().orientation);

            setDensity(mContext.getResources().getDisplayMetrics().density);

            setWidth(mContext.getResources().getDisplayMetrics().widthPixels);    
            setHeight(mContext.getResources().getDisplayMetrics().heightPixels);    
       
            
            String networkOperator = getNetworkOperator();
            setMccCode(networkOperator);
            setMncCode(networkOperator);

            setIsoCountryCode(mTelephonyManager.getNetworkCountryIso());
            setCarrierName(mTelephonyManager.getNetworkOperatorName());

            setNetworkType(getActiveNetworkType());
            setLocale(Locale.getDefault().toString());
            
            setManufacturer(android.os.Build.MANUFACTURER);
            setModel(android.os.Build.MODEL);
            setAndroidVersion(android.os.Build.VERSION.RELEASE);

            setTrackingId(getTrackingId());
            
            return getFinalUrlString();
        }

        private void setPackageId(String packageName) {
            addParam("id", packageName);
        }
        
    }
}
