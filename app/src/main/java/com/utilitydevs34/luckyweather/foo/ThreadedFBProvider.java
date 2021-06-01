package com.utilitydevs34.luckyweather.foo;

import android.content.Context;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.utilitydevs34.luckyweather.AppSingletonClass;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class ThreadedFBProvider extends AbstractThreadedUtil {
    private MyJsonObject mConfigProvider;
    private Context mContext;
    private static boolean isInitialized = false;

    public ThreadedFBProvider(MyJsonObject cnf, Context ctx) {
        mConfigProvider = cnf;
        mContext = ctx;
    }

    @Override
    protected void runPayload() {
        if (mConfigProvider.getStr("fb_id").length() > 0) {
            if (isInitialized) return;
            FacebookSdk.setApplicationId(mConfigProvider.getStr("fb_id"));
            FacebookSdk.setAdvertiserIDCollectionEnabled(true);
            FacebookSdk.sdkInitialize(AppSingletonClass.getInstance());
            FacebookSdk.fullyInitialize();
            AppEventsLogger.activateApp(AppSingletonClass.getInstance());
            isInitialized = true;

            //deep linking
            /*
            if(!MainClass.getLocalConfig().has(GenericLocalConfigProvider.KEY_FB_DL)){
                final CountDownLatch cdlA = new CountDownLatch(1);
                AppLinkData.fetchDeferredAppLinkData(mContext, appLinkData -> {
                            if (appLinkData != null && appLinkData.getTargetUri() != null) {
                                String dl = appLinkData.getTargetUri().toString();
                                MainClass.getLocalConfig().putString(GenericLocalConfigProvider.KEY_FB_DL, dl);
                            }
                            cdlA.countDown();
                        }
                );

                try {
                    cdlA.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //pass
                }

            }*/
        }
    }
}