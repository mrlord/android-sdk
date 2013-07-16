package com.adcash.mobileads;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import com.adcash.mobileads.Log;

public class BaseHtmlWebView extends BaseWebView {
    public BaseHtmlWebView(Context context) {
        super(context);

        disableScrollingAndZoom();
        getSettings().setJavaScriptEnabled(true);
        getSettings().setPluginsEnabled(true);
        setBackgroundColor(Color.TRANSPARENT);
 //       setBackgroundColor(Color.BLACK);
 //       setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
    }

    public void init(boolean isScrollable) {
        setWebViewScrollingEnabled(isScrollable);
    }

    @Override
    public void loadUrl(String url) {
        if (url == null) return;

        Log.d("Adcash", "Loading url: " + url);
        if (url.startsWith("javascript:")) {
            super.loadUrl(url);
        }
    }

    private void disableScrollingAndZoom() {
        setHorizontalScrollBarEnabled(false);
        setHorizontalScrollbarOverlay(false);
        setVerticalScrollBarEnabled(false);
        setVerticalScrollbarOverlay(false);
        getSettings().setSupportZoom(false);
    }

    void loadHtmlResponse(String htmlResponse) {
    	Log.d("Adcash", "Base Html WebView is loading HTML Reponse");
        loadDataWithBaseURL("http://mad.adcash.com/", htmlResponse, "text/html", "utf-8", null);
    }

    void setWebViewScrollingEnabled(boolean isScrollable) {
        if (isScrollable) {
            setOnTouchListener(null);
        } else {
            setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }
}
