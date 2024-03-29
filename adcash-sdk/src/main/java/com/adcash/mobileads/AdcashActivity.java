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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.adcash.mobileads.factories.HtmlInterstitialWebViewFactory;

import static com.adcash.mobileads.AdFetcher.AD_CONFIGURATION_KEY;
import static com.adcash.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.adcash.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.adcash.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.adcash.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.adcash.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;
import static com.adcash.mobileads.BaseInterstitialActivity.JavaScriptWebViewCallbacks.WEB_VIEW_DID_CLOSE;
import static com.adcash.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.adcash.mobileads.HtmlWebViewClient.ADCASH_FAIL_LOAD;
import static com.adcash.mobileads.HtmlWebViewClient.ADCASH_FINISH_LOAD;

public class AdcashActivity extends BaseInterstitialActivity {
    private HtmlInterstitialWebView mHtmlInterstitialWebView;

    public static void start(Context context, String htmlData, boolean isScrollable, String redirectUrl, String clickthroughUrl, AdConfiguration adConfiguration) {
        Intent intent = createIntent(context, htmlData, isScrollable, redirectUrl, clickthroughUrl, adConfiguration);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Log.d("AdcashActivity", "AdcashActivity not found - did you declare it in AndroidManifest.xml?");
        }
    }

    static Intent createIntent(Context context, String htmlData, boolean isScrollable, String redirectUrl, String clickthroughUrl, AdConfiguration adConfiguration) {
        Intent intent = new Intent(context, AdcashActivity.class);
        intent.putExtra(HTML_RESPONSE_BODY_KEY, htmlData);
        intent.putExtra(SCROLLABLE_KEY, isScrollable);
        intent.putExtra(CLICKTHROUGH_URL_KEY, clickthroughUrl);
        intent.putExtra(REDIRECT_URL_KEY, redirectUrl);
        intent.putExtra(AD_CONFIGURATION_KEY, adConfiguration);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    static void preRenderHtml(final Context context, final CustomEventInterstitialListener customEventInterstitialListener, String htmlData) {
        HtmlInterstitialWebView dummyWebView = HtmlInterstitialWebViewFactory.create(context, customEventInterstitialListener, false, null, null, null);
        dummyWebView.enablePlugins(false);

        dummyWebView.addAdcashUriJavascriptInterface(new HtmlInterstitialWebView.AdcashUriJavascriptFireFinishLoadListener() {
            @Override
            public void onInterstitialLoaded() {
                customEventInterstitialListener.onInterstitialLoaded();
            }
        });
        dummyWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.equals(ADCASH_FINISH_LOAD)) {
                    customEventInterstitialListener.onInterstitialLoaded();
                } else if (url.equals(ADCASH_FAIL_LOAD)) {
                    customEventInterstitialListener.onInterstitialFailed(null);
                }

                return true;
            }
        });
        dummyWebView.loadHtmlResponse(htmlData);
    }

    @Override
    public View getAdView() {
        Intent intent = getIntent();
        boolean isScrollable = intent.getBooleanExtra(SCROLLABLE_KEY, false);
        String redirectUrl = intent.getStringExtra(REDIRECT_URL_KEY);
        String clickthroughUrl = intent.getStringExtra(CLICKTHROUGH_URL_KEY);
        String htmlResponse = intent.getStringExtra(HTML_RESPONSE_BODY_KEY);

        mHtmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(getApplicationContext(), new BroadcastingInterstitialListener(), isScrollable, redirectUrl, clickthroughUrl, getAdConfiguration());
        mHtmlInterstitialWebView.loadHtmlResponse(htmlResponse);

        return mHtmlInterstitialWebView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastInterstitialAction(ACTION_INTERSTITIAL_SHOW);
    }

    @Override
    protected void onDestroy() {
        mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_CLOSE.getUrl());
        mHtmlInterstitialWebView.destroy();
        broadcastInterstitialAction(ACTION_INTERSTITIAL_DISMISS);
        super.onDestroy();
    }

    class BroadcastingInterstitialListener implements CustomEventInterstitialListener {
        @Override
        public void onInterstitialLoaded() {
            mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());
        }

        @Override
        public void onInterstitialFailed(AdcashErrorCode errorCode) {
            broadcastInterstitialAction(ACTION_INTERSTITIAL_FAIL);
            finish();
        }

        @Override
        public void onInterstitialShown() {
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
