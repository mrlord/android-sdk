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

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import com.adcash.mobileads.util.Json;
import com.adcash.mobileads.util.Strings;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;

import static com.adcash.mobileads.AdFetcher.CLICKTHROUGH_URL_KEY;
import static com.adcash.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.adcash.mobileads.AdFetcher.REDIRECT_URL_KEY;
import static com.adcash.mobileads.AdFetcher.SCROLLABLE_KEY;
import static com.adcash.mobileads.util.HttpResponses.extractBooleanHeader;
import static com.adcash.mobileads.util.HttpResponses.extractHeader;
import static com.adcash.mobileads.util.ResponseHeader.AD_TYPE;
import static com.adcash.mobileads.util.ResponseHeader.CLICKTHROUGH_URL;
import static com.adcash.mobileads.util.ResponseHeader.CUSTOM_EVENT_DATA;
import static com.adcash.mobileads.util.ResponseHeader.CUSTOM_EVENT_NAME;
import static com.adcash.mobileads.util.ResponseHeader.CUSTOM_SELECTOR;
import static com.adcash.mobileads.util.ResponseHeader.FULL_AD_TYPE;
import static com.adcash.mobileads.util.ResponseHeader.NATIVE_PARAMS;
import static com.adcash.mobileads.util.ResponseHeader.REDIRECT_URL;
import static com.adcash.mobileads.util.ResponseHeader.SCROLLABLE;

abstract class AdLoadTask {
    WeakReference<AdViewController> mWeakAdViewController;
    AdLoadTask(AdViewController adViewController) {
        mWeakAdViewController = new WeakReference<AdViewController>(adViewController);
    }

    abstract void execute();

    /*
     * The AsyncTask thread pool often appears to keep references to these
     * objects, preventing GC. This method should be used to release
     * resources to mitigate the GC issue.
     */
    abstract void cleanup();

    static AdLoadTask fromHttpResponse(HttpResponse response, AdViewController adViewController) throws IOException {
        return new TaskExtractor(response, adViewController).extract();
    }

    private static class TaskExtractor {
        private final HttpResponse response;
        private final AdViewController adViewController;
        private String adType;
        private String adTypeCustomEventName;
        private String fullAdType;

        TaskExtractor(HttpResponse response, AdViewController adViewController){
            this.response = response;
            this.adViewController = adViewController;
        }

        AdLoadTask extract() throws IOException {
            adType = extractHeader(response, AD_TYPE);
            fullAdType = extractHeader(response, FULL_AD_TYPE);

            Log.d("Adcash", "Loading ad type: " + AdTypeTranslator.getAdNetworkType(adType, fullAdType));

            adTypeCustomEventName = AdTypeTranslator.getCustomEventNameForAdType(
                    adViewController.getAdcashView(), adType, fullAdType);

            if ("custom".equals(adType)) {
                return extractCustomEventAdLoadTask();
            } else if (eventDataIsInResponseBody(adType)) {
                return extractCustomEventAdLoadTaskFromResponseBody();
            } else {
                return extractCustomEventAdLoadTaskFromNativeParams();
            }
        }

        private AdLoadTask extractCustomEventAdLoadTask() {
            Log.i("Adcash", "Performing custom event.");

            // If applicable, try to invoke the new custom event system (which uses custom classes)
            adTypeCustomEventName = extractHeader(response, CUSTOM_EVENT_NAME);
            if (adTypeCustomEventName != null) {
                String customEventData = extractHeader(response, CUSTOM_EVENT_DATA);
                return createCustomEventAdLoadTask(customEventData);
            }

            // Otherwise, use the (deprecated) legacy custom event system for older clients
            Header oldCustomEventHeader = response.getFirstHeader(CUSTOM_SELECTOR.getKey());
            return new AdLoadTask.LegacyCustomEventAdLoadTask(adViewController, oldCustomEventHeader);
        }

        private AdLoadTask extractCustomEventAdLoadTaskFromResponseBody() throws IOException {
            HttpEntity entity = response.getEntity();
            String htmlData = entity != null ? Strings.fromStream(entity.getContent()) : "";

            adViewController.getAdConfiguration().setResponseString(htmlData);

            String redirectUrl = extractHeader(response, REDIRECT_URL);
            String clickthroughUrl = extractHeader(response, CLICKTHROUGH_URL);
            boolean scrollingEnabled = extractBooleanHeader(response, SCROLLABLE, false);

            Map<String, String> eventDataMap = new HashMap<String, String>();
            eventDataMap.put(HTML_RESPONSE_BODY_KEY, Uri.encode(htmlData));
            eventDataMap.put(SCROLLABLE_KEY, Boolean.toString(scrollingEnabled));
            if (redirectUrl != null) {
                eventDataMap.put(REDIRECT_URL_KEY, redirectUrl);
            }
            if (clickthroughUrl != null) {
                eventDataMap.put(CLICKTHROUGH_URL_KEY, clickthroughUrl);
            }

            String eventData = Json.mapToJsonString(eventDataMap);
            return createCustomEventAdLoadTask(eventData);
        }

        private AdLoadTask extractCustomEventAdLoadTaskFromNativeParams() throws IOException {
            String eventData = extractHeader(response, NATIVE_PARAMS);

            return createCustomEventAdLoadTask(eventData);
        }

        private AdLoadTask createCustomEventAdLoadTask(String customEventData) {
            Map<String, String> paramsMap = new HashMap<String, String>();
            paramsMap.put(CUSTOM_EVENT_NAME.getKey(), adTypeCustomEventName);

            if (customEventData != null) {
                paramsMap.put(CUSTOM_EVENT_DATA.getKey(), customEventData);
            }

            return new AdLoadTask.CustomEventAdLoadTask(adViewController, paramsMap);
        }

        private boolean eventDataIsInResponseBody(String adType) {
            // XXX Hack
            return "mraid".equals(adType) || "html".equals(adType) || ("interstitial".equals(adType) && "vast".equals(fullAdType));
        }
    }

    /*
     * This is the new way of performing Custom Events. This will be invoked on new clients when
     * X-Adtype is "custom" and the X-Custom-Event-Class-Name header is specified.
     */
    static class CustomEventAdLoadTask extends AdLoadTask {
        private Map<String,String> mParamsMap;

        public CustomEventAdLoadTask(AdViewController adViewController, Map<String, String> paramsMap) {
            super(adViewController);
            mParamsMap = paramsMap;
        }

        @Override
        void execute() {
            AdViewController adViewController = mWeakAdViewController.get();

            if (adViewController == null || adViewController.isDestroyed()) {
                return;
            }

            adViewController.setNotLoading();
            adViewController.getAdcashView().loadCustomEvent(mParamsMap);
        }

        @Override
        void cleanup() {
            mParamsMap = null;
        }

        @Deprecated // for testing
        Map<String, String> getParamsMap() {
            return mParamsMap;
        }
    }

    /*
     * This is the old way of performing Custom Events, and is now deprecated. This will still be
     * invoked on old clients when X-Adtype is "custom" and the new X-Custom-Event-Class-Name header
     * is not specified (legacy custom events parse the X-Customselector header instead).
     */
    @Deprecated
    static class LegacyCustomEventAdLoadTask extends AdLoadTask {
        private Header mHeader;

        public LegacyCustomEventAdLoadTask(AdViewController adViewController, Header header) {
            super(adViewController);
            mHeader = header;
        }

        @Override
        void execute() {
            AdViewController adViewController = mWeakAdViewController.get();
            if (adViewController == null || adViewController.isDestroyed()) {
                return;
            }

            adViewController.setNotLoading();
            AdcashView mpv = adViewController.getAdcashView();

            if (mHeader == null) {
                Log.i("Adcash", "Couldn't call custom method because the server did not specify one.");
                mpv.loadFailUrl(AdcashErrorCode.ADAPTER_NOT_FOUND);
                return;
            }

            String methodName = mHeader.getValue();
            Log.i("Adcash", "Trying to call method named " + methodName);

            Class<? extends Activity> c;
            Method method;
            Activity userActivity = mpv.getActivity();
            try {
                c = userActivity.getClass();
                method = c.getMethod(methodName, AdcashView.class);
                method.invoke(userActivity, mpv);
            } catch (NoSuchMethodException e) {
                Log.d("Adcash", "Couldn't perform custom method named " + methodName +
                        "(AdcashView view) because your activity class has no such method");
                mpv.loadFailUrl(AdcashErrorCode.ADAPTER_NOT_FOUND);
            } catch (Exception e) {
                Log.d("Adcash", "Couldn't perform custom method named " + methodName);
                mpv.loadFailUrl(AdcashErrorCode.ADAPTER_NOT_FOUND);
            }
        }

        @Override
        void cleanup() {
            mHeader = null;
        }

        @Deprecated // for testing
        Header getHeader() {
            return mHeader;
        }
    }
}
