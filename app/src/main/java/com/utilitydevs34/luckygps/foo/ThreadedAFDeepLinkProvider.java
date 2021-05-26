package com.utilitydevs34.luckygps.foo;

import android.content.Context;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.utilitydevs34.luckygps.AppSingletonClass;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class ThreadedAFDeepLinkProvider extends AbstractThreadedUtil {
    private MyJsonObject rc;
    private Context mContext;

    public ThreadedAFDeepLinkProvider(MyJsonObject cnf, Context ctx) {
        rc = cnf;
        mContext = ctx;
    }

    @Override
    protected void runPayload() {
        if(rc.getStr("af_id","").length()>0) {
            final CountDownLatch cdlB = new CountDownLatch(1);
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    Map<String, String> cd = new HashMap<>();
                    for (String attrName : conversionData.keySet()) {
                        try {
                            AppSingletonClass.getLocalConfig().putString("af__"+attrName, conversionData.get(attrName).toString());
                            cd.put(attrName, conversionData.get(attrName).toString());
                            Log.d("main", attrName + " " + conversionData.get(attrName).toString());
                        }catch (Exception e){
                            continue;
                        }
                    }
                    //FLR:FlurryAgent.logEvent("onConversionDataSuccess", cd);
                    cdlB.countDown();
                }

                @Override
                public void onConversionDataFail(String errorMessage) {
                    //FLR:FlurryAgent.logEvent("onConversionDataFail", new HashMap<String, String>(){{put("m",errorMessage);}});
                    cdlB.countDown();
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> attributionData) {
                    //FLR:FlurryAgent.logEvent("onAppOpenAttribution", attributionData);

                }

                @Override
                public void onAttributionFailure(String errorMessage) {
                    //FLR:FlurryAgent.logEvent("onAttributionFailure", new HashMap<String, String>(){{put("m",errorMessage);}});
                }
            };


            AppsFlyerLib.getInstance().init(rc.getStr("af_id"), conversionListener, mContext);
            AppsFlyerLib.getInstance().start(mContext);
            AppSingletonClass.getLocalConfig().putString(
                    Utils.KEY_AF_DEV_ID,
                    AppsFlyerLib.getInstance().getAppsFlyerUID(mContext)
            );

            try {
                cdlB.await(10, TimeUnit.SECONDS);
            }catch (Exception e){
                //pass
                e.printStackTrace();
            }
        }
    }
}