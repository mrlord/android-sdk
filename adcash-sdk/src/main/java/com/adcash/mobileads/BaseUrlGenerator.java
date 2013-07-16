package com.adcash.mobileads;

import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_MOBILE_DUN;
import static android.net.ConnectivityManager.TYPE_MOBILE_HIPRI;
import static android.net.ConnectivityManager.TYPE_MOBILE_MMS;
import static android.net.ConnectivityManager.TYPE_MOBILE_SUPL;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.ETHERNET;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.MOBILE;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.UNKNOWN;
import static com.adcash.mobileads.BaseUrlGenerator.AdcashNetworkType.WIFI;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import com.adcash.mobileads.AdcashView.LocationAwareness;
import com.adcash.mobileads.util.DateAndTime;

import android.R.bool;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.adcash.mobileads.Log;

public abstract class BaseUrlGenerator {
    private StringBuilder mStringBuilder;
    private boolean mFirstParam;

    public abstract String generateUrlString(String serverHostname);

    public enum LocationAwareness {
        LOCATION_AWARENESS_NORMAL, LOCATION_AWARENESS_TRUNCATED, LOCATION_AWARENESS_DISABLED
    }
    
    private LocationAwareness mLocationAwareness;
    private int mLocationPrecision;
    
    public static final String DEVICE_ORIENTATION_PORTRAIT = "p";
    public static final String DEVICE_ORIENTATION_LANDSCAPE = "l";
    public static final String DEVICE_ORIENTATION_SQUARE = "s";
    public static final String DEVICE_ORIENTATION_UNKNOWN = "u";
    protected Context mContext;
    protected TelephonyManager mTelephonyManager;
    protected ConnectivityManager mConnectivityManager;
    protected SharedPreferences mSharedPreferences;
    protected WifiManager mWifiManager;
    protected String mAdUnitId;
    protected String mKeywords;
    protected Location mLocation;

    public static enum AdcashNetworkType {
        UNKNOWN,
        ETHERNET,
        WIFI,
        MOBILE;

        @Override
        public String toString() {
            return Integer.toString(ordinal());
        }
    }
    
    protected void initUrlString(String serverHostname, String handlerType) {
        mStringBuilder = new StringBuilder("http://" + serverHostname + handlerType);
        mFirstParam = true;
    }

    protected String getFinalUrlString() {
        return mStringBuilder.toString();
    }

    protected void addParam(String key, String value) {
        mStringBuilder.append(getParamDelimiter());
        mStringBuilder.append(key);
        mStringBuilder.append("=");
        String nonNullValue = value != null ? value : "";
        mStringBuilder.append(Uri.encode(nonNullValue));
    }

    private void addParam(String key, AdcashNetworkType value) {
        addParam(key, value.toString());
    }
    
    private String getParamDelimiter() {
        if (mFirstParam) {
            mFirstParam = false;
            return "?";
        }
        return "&";
    }
    
    protected String getTrackingId() {
		return mSharedPreferences.getString("adcashTrackingId", "");
    }
    
    private int mncPortionLength(String networkOperator) {
        return Math.min(3, networkOperator.length());
    }
    
    protected void setApiVersion(String apiVersion) {
        addParam("v", apiVersion);
    }

    protected void setAppVersion(String appVersion) {
        addParam("appv", appVersion);
    }

    protected void setAndroidId(String androidId) {
        addParam("aid", androidId);
    }
    
    protected void setDeviceId(String deviceId) {
        addParam("did", deviceId);
    }

    protected void setWidth(int width) {
        addParam("w", "" + width);
    }
    
    protected void setHeight(int height) {
        addParam("h", "" + height);
    }
    
    protected void setFirstOpen(boolean isFirstOpen) {
    	if (isFirstOpen)
          addParam("fo", "1");
    	else
          addParam("fo", "0");
    }
    
    protected void setMacAddress(String macaddress) {
        addParam("mac", macaddress);
    }
    
    protected void setAdUnitId(String adUnitId) {
        addParam("id", adUnitId);
    }

    protected void setLocale(String localeDesc) {
        addParam("l", localeDesc);
    }
    
    
    protected void setManufacturer(String manufacturer) {
        addParam("mfg", manufacturer);
    }
    
    protected void setModel(String model) {
        addParam("mdl", model);
    }
    
    protected void setAndroidVersion(String androidVersion) {
        addParam("av", androidVersion);
    }
    
    protected void setSdkVersion(String sdkVersion) {
        addParam("sdk", sdkVersion);
    }

    protected void setKeywords(String keywords) {
        if (keywords != null && keywords.length() > 0) {
            addParam("q", keywords);
        }
    }


    /*
     * Returns the last known location of the device using its GPS and network location providers.
     * May be null if:
     * - Location permissions are not requested in the Android manifest file
     * - The location providers don't exist
     * - Location awareness is disabled in the parent AdcashView
     */
    private Location getLastKnownLocation() {
        LocationAwareness locationAwareness = mLocationAwareness;
        int locationPrecision = mLocationPrecision;
        Location result;

        mLocationAwareness = LocationAwareness.LOCATION_AWARENESS_NORMAL;
        mLocationPrecision = 6;
        
        if (locationAwareness == LocationAwareness.LOCATION_AWARENESS_DISABLED) {
            return null;
        }

        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location gpsLocation = null;
        try {
            gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.d("Adcash", "Failed to retrieve GPS location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Log.d("Adcash", "Failed to retrieve GPS location: device has no GPS provider.");
        }

        Location networkLocation = null;
        try {
            networkLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Log.d("Adcash", "Failed to retrieve network location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Log.d("Adcash", "Failed to retrieve network location: device has no network provider.");
        }

        if (gpsLocation == null && networkLocation == null) {
            return null;
        }
        else if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime() > networkLocation.getTime()) result = gpsLocation;
            else result = networkLocation;
        }
        else if (gpsLocation != null) result = gpsLocation;
        else result = networkLocation;

        // Truncate latitude/longitude to the number of digits specified by locationPrecision.
        if (locationAwareness == LocationAwareness.LOCATION_AWARENESS_TRUNCATED) {
            double lat = result.getLatitude();
            double truncatedLat = BigDecimal.valueOf(lat)
                    .setScale(locationPrecision, BigDecimal.ROUND_HALF_DOWN)
                    .doubleValue();
            result.setLatitude(truncatedLat);

            double lon = result.getLongitude();
            double truncatedLon = BigDecimal.valueOf(lon)
                    .setScale(locationPrecision, BigDecimal.ROUND_HALF_DOWN)
                    .doubleValue();
            result.setLongitude(truncatedLon);
        }

        return result;
    }
    
    protected void setLocation(Location location) {
        if (location == null && mContext.checkCallingOrSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED)
        	location = getLastKnownLocation();

        if (location != null) {
        	if (mContext.checkCallingOrSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
                addParam("ll", location.getLatitude() + "," + location.getLongitude());
                addParam("lla", "" + (int) location.getAccuracy());
            }
        }
    }

    protected void setTimezone(String timeZoneOffsetString) {
        addParam("z", timeZoneOffsetString);
    }

    protected void setOrientation(int orientation) {
        String orString = DEVICE_ORIENTATION_UNKNOWN;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            orString = DEVICE_ORIENTATION_PORTRAIT;
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orString = DEVICE_ORIENTATION_LANDSCAPE;
        } else if (orientation == Configuration.ORIENTATION_SQUARE) {
            orString = DEVICE_ORIENTATION_SQUARE;
        }
        addParam("o", orString);
    }

    protected void setDensity(float density) {
        addParam("ds", "" + density);
    }

    protected void setMraidFlag(boolean mraid) {
        if (mraid) addParam("mr", "1");
    }

    protected void setMccCode(String networkOperator) {
        String mcc = networkOperator == null ? "" : networkOperator.substring(0, mncPortionLength(networkOperator));
        addParam("mcc", mcc);
    }

    protected void setMncCode(String networkOperator) {
        String mnc = networkOperator == null ? "" : networkOperator.substring(mncPortionLength(networkOperator));
        addParam("mnc", mnc);
    }

    protected void setIsoCountryCode(String networkCountryIso) {
        addParam("iso", networkCountryIso);
    }

    protected void setCarrierName(String networkOperatorName) {
        addParam("cn", networkOperatorName);
    }

    protected void setNetworkType(int type) {
        switch(type) {
            case TYPE_ETHERNET:
                addParam("ct", ETHERNET);
                break;
            case TYPE_WIFI:
                addParam("ct", WIFI);
                break;
            case TYPE_MOBILE:
            case TYPE_MOBILE_DUN:
            case TYPE_MOBILE_HIPRI:
            case TYPE_MOBILE_MMS:
            case TYPE_MOBILE_SUPL:
                addParam("ct", MOBILE);
                break;
            default:
                addParam("ct", UNKNOWN);
        }
    }


    protected String getNetworkOperator() {
    	String networkOperator = "";
    	try
    	{
       networkOperator = mTelephonyManager.getNetworkOperator();
        if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA &&
                mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
            networkOperator = mTelephonyManager.getSimOperator();
        }
    	}
      	 catch (NullPointerException ignored) {
      		 return "na";
         }
        return networkOperator;
    }

    protected String getWifiMacAddress()
    {
    	try
    	{
    	if (mContext.checkCallingOrSelfPermission(ACCESS_WIFI_STATE) == PERMISSION_GRANTED) {
		  WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		  return wifiInfo.getMacAddress();
    	}
    	}  catch (NullPointerException ignored) {
        }
        return "np";
    }

	protected void setTrackingId(String trackingId) {
        addParam("tid", trackingId);
    }
    
    protected static String getTimeZoneOffsetString() {
        SimpleDateFormat format = new SimpleDateFormat("Z");
        format.setTimeZone(DateAndTime.localTimeZone());
        return format.format(DateAndTime.now());
    }
    
    protected int getActiveNetworkType() {
    	 try {
        if (mContext.checkCallingOrSelfPermission(ACCESS_NETWORK_STATE) == PERMISSION_GRANTED) {
            return mConnectivityManager.getActiveNetworkInfo().getType();
        }
    	 }
    	 catch (NullPointerException ignored) {
    }
        return ConnectivityManager.TYPE_DUMMY; // Will generate the "unknown" code
    }
    
    
    protected String getAppVersionFromContext(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (Exception exception) {
            return null;
        }
    }

    protected String getAndroidIdFromContext(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    
    protected String getDeviceIdFromContext(Context context) {
    	try
    	{
    	if (mContext.checkCallingOrSelfPermission(READ_PHONE_STATE) == PERMISSION_GRANTED) {
          return mTelephonyManager.getDeviceId();
    	}
    }
      	 catch (NullPointerException ignored) {
         }
        return "np";
    }
    
}
