package com.adcash.mobileads;

import static android.content.Context.MODE_PRIVATE;

import java.util.Iterator;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;

public class AdcashReferrerReceiver extends BroadcastReceiver {
	// See http://code.google.com/p/android/issues/detail?id=16006

	private SharedPreferences mSharedPreferences;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String referrer = "";
		Bundle extras = intent.getExtras();
		if (extras != null) {
			referrer = extras.getString("referrer");
		}

		Log.d("Adcash", "Received install referrer: " + referrer);

		UrlQuerySanitizer referralParameters = new UrlQuerySanitizer();
		referralParameters.setAllowUnregisteredParamaters(true);

		referralParameters.parseQuery(referrer);
		String adcashTrackingId = referralParameters.getValue("adcash_tid");


        mSharedPreferences = context.getSharedPreferences(AdcashConversionTracker.PREFERENCE_NAME, MODE_PRIVATE);

		Log.d("Adcash", "Read previous click ID parameter from settings: " + mSharedPreferences.getString("adcashTrackingId", ""));

		Log.d("Adcash", "Read Click ID parameter: " + adcashTrackingId);
		if (adcashTrackingId.length() > 1)
		{
          mSharedPreferences
        .edit()
        .putString("adcashTrackingId", adcashTrackingId)
        .commit();
		}
		else
		{
			Log.d("Adcash", "Not updating click ID parameter because new one was not supplied");
		}
		
		Log.d("Adcash", "Read Click ID parameter from settings: " + mSharedPreferences.getString("adcashTrackingId", ""));

		// We read the metadata to forward the INSTALL_REFERRER intent
		try {
			ActivityInfo ai = context.getPackageManager().getReceiverInfo(new ComponentName(context, "com.adcash.mobileads.AdcashReferrerReceiver"), PackageManager.GET_META_DATA);
		    Bundle bundle = ai.metaData;
		    
			Set<String> keys = bundle.keySet();
			//iterate through all metadata tags
			Iterator<String> it = keys.iterator();
			while (it.hasNext())
			{
			  String k = it.next();
			  String v = bundle.getString(k);
			  try {
				   Log.d("Adcash", "Forwarding INSTALL_REFERRER intent to " + v.toString());
				((BroadcastReceiver)Class.forName(v).newInstance()).onReceive(context, intent);
			    } catch (InstantiationException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			   } catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			   } catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			   } //send intent by dynamically creating instance of receiver
			   Log.d("Adcash", "Forwarded INSTALL_REFERRER intent to " + v.toString());
			}
		    
		} catch (NameNotFoundException e) {
		    Log.e("Adcash", "Failed to load meta-data, NameNotFound: " + e.getMessage());
		} catch (NullPointerException e) {
		    Log.e("Adcash", "Failed to load meta-data, NullPointer: " + e.getMessage());         
		}
	}
}
