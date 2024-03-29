/*
 * Copyright (c) 2010-2013, Adcash OU.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import static com.adcash.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_CLICK;
import static com.adcash.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_DISMISS;
import static com.adcash.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_FAIL;
import static com.adcash.mobileads.BaseInterstitialActivity.ACTION_INTERSTITIAL_SHOW;
import static com.adcash.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.adcash.mobileads.AdcashErrorCode.NETWORK_INVALID_STATE;

class EventForwardingBroadcastReceiver extends BroadcastReceiver {
    private final CustomEventInterstitialListener mCustomEventInterstitialListener;
    private Context mContext;

    public EventForwardingBroadcastReceiver(CustomEventInterstitialListener customEventInterstitialListener) {
        mCustomEventInterstitialListener = customEventInterstitialListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mCustomEventInterstitialListener == null) {
            return;
        }

        String action = intent.getAction();
        if (action.equals(ACTION_INTERSTITIAL_FAIL)) {
            mCustomEventInterstitialListener.onInterstitialFailed(NETWORK_INVALID_STATE);
        } else if (action.equals(ACTION_INTERSTITIAL_SHOW)) {
            mCustomEventInterstitialListener.onInterstitialShown();
        } else if (action.equals(ACTION_INTERSTITIAL_DISMISS)) {
            mCustomEventInterstitialListener.onInterstitialDismissed();
        } else if (action.equals(ACTION_INTERSTITIAL_CLICK)) {
            mCustomEventInterstitialListener.onInterstitialClicked();
        }

    }

    public void register(Context context) {
        mContext = context;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, BaseInterstitialActivity.HTML_INTERSTITIAL_INTENT_FILTER);
    }

    public void unregister() {
        if (mContext != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            mContext = null;
        }
    }
}
