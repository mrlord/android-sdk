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

import android.os.AsyncTask;
import android.util.Log;
import com.adcash.mobileads.factories.HttpClientFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;

import static com.adcash.mobileads.util.HttpResponses.extractHeader;
import static com.adcash.mobileads.util.ResponseHeader.AD_TYPE;
import static com.adcash.mobileads.util.ResponseHeader.USER_AGENT;
import static com.adcash.mobileads.util.ResponseHeader.WARMUP;

public class AdFetchTask extends AsyncTask<String, Void, AdLoadTask> {
    private TaskTracker mTaskTracker;
    private AdViewController mAdViewController;
    private Exception mException;
    private HttpClient mHttpClient;
    private long mTaskId;
    private String mUserAgent;

    private AdFetcher.FetchStatus mFetchStatus = AdFetcher.FetchStatus.NOT_SET;
    private static final int MAXIMUM_REFRESH_TIME_MILLISECONDS = 600000;
    private static final double EXPONENTIAL_BACKOFF_FACTOR = 1.5;

    public AdFetchTask(TaskTracker taskTracker, AdViewController adViewController, String userAgent, int timeoutMilliseconds) {
        mTaskTracker = taskTracker;

        mAdViewController = adViewController;
        mHttpClient = HttpClientFactory.create(timeoutMilliseconds);
        mTaskId = mTaskTracker.getCurrentTaskId();
        mUserAgent = userAgent;
    }

    @Override
    protected AdLoadTask doInBackground(String... urls) {
        AdLoadTask result = null;
        try {
            result = fetch(urls[0]);
        } catch (Exception exception) {
            mException = exception;
        } finally {
            shutdownHttpClient();
        }
        return result;
    }

    private AdLoadTask fetch(String url) throws Exception {
        HttpGet httpget = new HttpGet(url);
        httpget.addHeader(USER_AGENT.getKey(), mUserAgent);

        if (!isStateValid()) return null;

        HttpResponse response = mHttpClient.execute(httpget);

        if (!isResponseValid(response)) return null;

        mAdViewController.configureUsingHttpResponse(response);

        if (!responseContainsContent(response)) return null;

        return AdLoadTask.fromHttpResponse(response, mAdViewController);
    }

    private boolean responseContainsContent(HttpResponse response) {
        // Ensure that the ad is not warming up.
        if ("1".equals(extractHeader(response, WARMUP))) {
            Log.d("Adcash", "Ad Unit (" + mAdViewController.getAdUnitId() + ") is still warming up. " +
                    "Please try again in a few minutes.");
            mFetchStatus = AdFetcher.FetchStatus.AD_WARMING_UP;
            return false;
        }

        // Ensure that the ad type header is valid and not "clear".
        String adType = extractHeader(response, AD_TYPE);
        if ("clear".equals(adType)) {
            Log.d("Adcash", "No inventory found for adunit (" + mAdViewController.getAdUnitId() + ").");
            mFetchStatus = AdFetcher.FetchStatus.CLEAR_AD_TYPE;
            return false;
        }

        return true;
    }

    private boolean isResponseValid(HttpResponse response) {
        if (response == null || response.getEntity() == null) {
            Log.d("Adcash", "Adcash server returned null response.");
            mFetchStatus = AdFetcher.FetchStatus.INVALID_SERVER_RESPONSE_NOBACKOFF;
            return false;
        }

        final int statusCode = response.getStatusLine().getStatusCode();

        // Client and Server HTTP errors should result in an exponential backoff
        if (statusCode >= 400) {
            Log.d("Adcash", "Server error: returned HTTP status code " + Integer.toString(statusCode) +
                    ". Please try again.");
            mFetchStatus = AdFetcher.FetchStatus.INVALID_SERVER_RESPONSE_BACKOFF;
            return false;
        }
        // Other non-200 HTTP status codes should still fail
        else if (statusCode != HttpStatus.SC_OK) {
            Log.d("Adcash", "Adcash server returned invalid response: HTTP status code " +
                    Integer.toString(statusCode) + ".");
            mFetchStatus = AdFetcher.FetchStatus.INVALID_SERVER_RESPONSE_NOBACKOFF;
            return false;
        }
        return true;
    }

    private boolean isStateValid() {
        // We check to see if this AsyncTask was cancelled, as per
        // http://developer.android.com/reference/android/os/AsyncTask.html
        if (isCancelled()) {
            mFetchStatus = AdFetcher.FetchStatus.FETCH_CANCELLED;
            return false;
        }

        if (mAdViewController == null || mAdViewController.isDestroyed()) {
            Log.d("Adcash", "Error loading ad: AdViewController has already been GCed or destroyed.");
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(AdLoadTask adLoadTask) {
        if (!isMostCurrentTask()) {
            Log.d("Adcash", "Ad response is stale.");
            cleanup();
            return;
        }

        // If cleanup() has already been called on the AdViewController, don't proceed.
        if (mAdViewController == null || mAdViewController.isDestroyed()) {
            if (adLoadTask != null) {
                adLoadTask.cleanup();
            }
            mTaskTracker.markTaskCompleted(mTaskId);
            cleanup();
            return;
        }

        if (adLoadTask == null) {
            if (mException != null) {
                Log.d("Adcash", "Exception caught while loading ad: " + mException);
            }

            AdcashErrorCode errorCode;
            switch (mFetchStatus) {
                case NOT_SET:
                    errorCode = AdcashErrorCode.UNSPECIFIED;
                    break;
                case FETCH_CANCELLED:
                    errorCode = AdcashErrorCode.CANCELLED;
                    break;
                case INVALID_SERVER_RESPONSE_BACKOFF:
                case INVALID_SERVER_RESPONSE_NOBACKOFF:
                    errorCode = AdcashErrorCode.SERVER_ERROR;
                    break;
                case CLEAR_AD_TYPE:
                case AD_WARMING_UP:
                    errorCode = AdcashErrorCode.NO_FILL;
                    break;
                default:
                    errorCode = AdcashErrorCode.UNSPECIFIED;
                    break;
            }

            mAdViewController.adDidFail(errorCode);

            /*
             * There are numerous reasons for the ad fetch to fail, but only in the specific
             * case of actual server failure should we exponentially back off.
             *
             * Note: We place the exponential backoff after AdViewController's adDidFail because we only
             * want to increase refresh times after the first failure refresh timer is
             * scheduled, and not before.
             */
            if (mFetchStatus == AdFetcher.FetchStatus.INVALID_SERVER_RESPONSE_BACKOFF) {
                exponentialBackoff();
                mFetchStatus = AdFetcher.FetchStatus.NOT_SET;
            }
        } else {
            adLoadTask.execute();
            adLoadTask.cleanup();
        }

        mTaskTracker.markTaskCompleted(mTaskId);
        cleanup();
    }

    @Override
    protected void onCancelled() {
        if (!isMostCurrentTask()) {
            Log.d("Adcash", "Ad response is stale.");
            cleanup();
            return;
        }

        Log.d("Adcash", "Ad loading was cancelled.");
        if (mException != null) {
            Log.d("Adcash", "Exception caught while loading ad: " + mException);
        }
        mTaskTracker.markTaskCompleted(mTaskId);
        cleanup();
    }

    /* This helper function is called when a 4XX or 5XX error is received during an ad fetch.
     * It exponentially increases the parent AdViewController's refreshTime up to a specified cap.
     */
    private void exponentialBackoff() {
        if (mAdViewController == null) {
            return;
        }

        int refreshTimeMilliseconds = mAdViewController.getRefreshTimeMilliseconds();

        refreshTimeMilliseconds = (int) (refreshTimeMilliseconds * EXPONENTIAL_BACKOFF_FACTOR);
        if (refreshTimeMilliseconds > MAXIMUM_REFRESH_TIME_MILLISECONDS) {
            refreshTimeMilliseconds = MAXIMUM_REFRESH_TIME_MILLISECONDS;
        }

        mAdViewController.setRefreshTimeMilliseconds(refreshTimeMilliseconds);
    }

    private void cleanup() {
        mTaskTracker = null;
        mException = null;
        mFetchStatus = AdFetcher.FetchStatus.NOT_SET;
    }

    private void shutdownHttpClient() {
        if (mHttpClient != null) {
            ClientConnectionManager manager = mHttpClient.getConnectionManager();
            if (manager != null) {
                manager.shutdown();
            }
            mHttpClient = null;
        }
    }

    private boolean isMostCurrentTask() {
        // if we've been cleaned up already, then we're definitely not the current task
        return (mTaskTracker == null) ? false : mTaskTracker.isMostCurrentTask(mTaskId);
    }
}
