package com.adcash.mobileads;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.adcash.mobileads.MraidView.ExpansionStyle;
import com.adcash.mobileads.MraidView.NativeCloseButtonStyle;
import com.adcash.mobileads.MraidView.PlacementType;
import com.adcash.mobileads.MraidView.ViewState;
import com.adcash.mobileads.factories.MraidViewFactory;
import com.adcash.mobileads.util.WebViews;

import static com.adcash.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;

public class MraidActivity extends BaseInterstitialActivity {
    private MraidView mMraidView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastInterstitialAction(ACTION_INTERSTITIAL_SHOW);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }

    @Override
    public View getAdView() {
        mMraidView = MraidViewFactory.create(this, ExpansionStyle.DISABLED, NativeCloseButtonStyle.AD_CONTROLLED,
                PlacementType.INTERSTITIAL);
        
        mMraidView.setOnReadyListener(new MraidView.OnReadyListener() {
            public void onReady(MraidView view) {
                showInterstitialCloseButton();
            }
        });
        
        mMraidView.setOnCloseButtonStateChange(new MraidView.OnCloseButtonStateChangeListener() {
            public void onCloseButtonStateChange(MraidView view, boolean enabled) {
                if (enabled) {
                    showInterstitialCloseButton();
                } else {
                    hideInterstitialCloseButton();
                }
            }
        });
        
        mMraidView.setOnCloseListener(new MraidView.OnCloseListener() {
            public void onClose(MraidView view, ViewState newViewState) {
                finish();
            }
        });
        
        String source = getIntent().getStringExtra(HTML_RESPONSE_BODY_KEY);
        mMraidView.loadHtmlData(source);
        
        return mMraidView;
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebViews.onPause(mMraidView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebViews.onResume(mMraidView);
    }

    @Override
    protected void onDestroy() {
        mMraidView.destroy();
        super.onDestroy();
    }
}
