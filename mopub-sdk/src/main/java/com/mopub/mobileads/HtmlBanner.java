package com.mopub.mobileads;

import static com.mopub.common.DataKeys.AD_REPORT_KEY;
import static com.mopub.mobileads.MoPubErrorCode.INTERNAL_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;

import com.mopub.common.AdReport;
import com.mopub.common.DataKeys;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.factories.HtmlBannerWebViewFactory;

public class HtmlBanner extends CustomEventBanner {

    private HtmlBannerWebView mHtmlBannerWebView;
    private Activity mActivity;

    @Override
    protected void loadBanner(
            Context context,
            CustomEventBannerListener customEventBannerListener,
            Map<String, Object> localExtras,
            Map<String, String> serverExtras) {

        String htmlData;
        String redirectUrl;
        String clickthroughUrl;
        Boolean isScrollable;
        AdReport adReport;
        if (extrasAreValid(serverExtras)) {
            htmlData = serverExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY);
            redirectUrl = serverExtras.get(DataKeys.REDIRECT_URL_KEY);
            clickthroughUrl = serverExtras.get(DataKeys.CLICKTHROUGH_URL_KEY);
            isScrollable = Boolean.valueOf(serverExtras.get(DataKeys.SCROLLABLE_KEY));
            try {
                adReport = (AdReport) localExtras.get(AD_REPORT_KEY);
            } catch (ClassCastException e) {
                MoPubLog.e("LocalExtras contained an incorrect type.");
                customEventBannerListener.onBannerFailed(INTERNAL_ERROR);
                return;
            }
        } else {
            customEventBannerListener.onBannerFailed(NETWORK_INVALID_STATE);
            return;
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
        	mHtmlBannerWebView = HtmlBannerWebViewFactory.create(context, adReport, customEventBannerListener, isScrollable, redirectUrl, clickthroughUrl);
            AdViewController.setShouldHonorServerDimensions(mHtmlBannerWebView);
            mHtmlBannerWebView.loadHtmlResponse(htmlData);
        } else {
        	final Context pContext = context;
            final CustomEventBannerListener pCustomEventBannerListener = customEventBannerListener;
            final Boolean pIsScrollable = isScrollable;
            final String pRedirectUrl = redirectUrl;
            final String pClickthroughUrl = clickthroughUrl;
            final String pHtmlData = htmlData;
            final AdReport pAdReport = adReport;
            mActivity = (Activity)context; 
            mActivity.runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				mHtmlBannerWebView = HtmlBannerWebViewFactory.create(pContext, pAdReport, pCustomEventBannerListener, pIsScrollable, pRedirectUrl, pClickthroughUrl);
    		        AdViewController.setShouldHonorServerDimensions(mHtmlBannerWebView);
    		        mHtmlBannerWebView.loadHtmlResponse(pHtmlData);
    			}
    		});
        }
        
    }

    @Override
    protected void onInvalidate() {
        if (mHtmlBannerWebView != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
        		mHtmlBannerWebView.destroy();
        	} else {
        		mActivity.runOnUiThread(new Runnable() {
    				@Override
    				public void run() {
    					mHtmlBannerWebView.destroy();
    				}
    			});
        	}
        }
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        return serverExtras.containsKey(DataKeys.HTML_RESPONSE_BODY_KEY);
    }
}
