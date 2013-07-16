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

import android.content.Context;
import android.content.Intent;
import android.view.View;
import com.adcash.mobileads.factories.HtmlInterstitialWebViewFactory;

import static com.adcash.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.adcash.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.adcash.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.adcash.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.adcash.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;

public class AdcashActivity extends BaseInterstitialActivity {
    private HtmlInterstitialWebView htmlInterstitialWebView;

    public static void start(Context context, String htmlData, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        Intent intent = createIntent(context, htmlData, isScrollable, redirectUrl, clickthroughUrl);
        context.startActivity(intent);
    }

    public static Intent createIntent(Context context, String htmlData, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        Intent intent = new Intent(context, AdcashActivity.class);
        intent.putExtra(HTML_RESPONSE_BODY_KEY, htmlData);
        intent.putExtra(SCROLLABLE_KEY, isScrollable);
        intent.putExtra(CLICKTHROUGH_URL_KEY, clickthroughUrl);
        intent.putExtra(REDIRECT_URL_KEY, redirectUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public View getAdView() {
        Intent intent = getIntent();
        boolean isScrollable = intent.getBooleanExtra(SCROLLABLE_KEY, false);
        String redirectUrl = intent.getStringExtra(REDIRECT_URL_KEY);
        String clickthroughUrl = intent.getStringExtra(CLICKTHROUGH_URL_KEY);
        String htmlResponse = intent.getStringExtra(HTML_RESPONSE_BODY_KEY);

        htmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(new BroadcastingInterstitialListener(), isScrollable, redirectUrl, clickthroughUrl);
        htmlInterstitialWebView.loadHtmlResponse(htmlResponse);

        return htmlInterstitialWebView;
    }
    
    @Override
    protected void onDestroy() {
    	Log.d("Adcash", "Called destroy of Adcash Activity");
    //	super.mLayout.removeView(htmlInterstitialWebView);
    //	htmlInterstitialWebView.removeAllViews();
        htmlInterstitialWebView.destroy();
        super.onDestroy();
    }

    private class BroadcastingInterstitialListener implements CustomEventInterstitialListener {
        @Override
        public void onInterstitialLoaded() {
        }

        @Override
        public void onInterstitialFailed(AdcashErrorCode errorCode) {
            broadcastInterstitialAction(ACTION_INTERSTITIAL_FAIL);
            finish();
        }

        @Override
        public void onInterstitialShown() {
            broadcastInterstitialAction(ACTION_INTERSTITIAL_SHOW);
        }

        @Override
        public void onInterstitialClicked() {
            broadcastInterstitialAction(ACTION_INTERSTITIAL_CLICK);
        }

        @Override
        public void onLeaveApplication() {
        }

        @Override
        public void onInterstitialDismissed() {
        }
    }
}
