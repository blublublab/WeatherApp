    package com.utilitydevs34.luckyweather;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.utilitydevs34.luckyweather.foo.MyJsonObject;
import com.utilitydevs34.luckyweather.foo.Utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.shrinathbhosale.preffy.Preffy;
import io.michaelrocks.paranoid.Obfuscate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Obfuscate
public class AppSingletonClass extends Application {
    private static OkHttpClient httpClient;
    private static AppSingletonClass self;
    private static Preffy lCfg;
    private static Gson g;
    private static MyJsonObject remoteConfig;
    private static final String REMOTE_CONFIG_URL = "https://egyptjump34.blogspot.com/2021/05/lucky-gps.html"; //TODO: URL as it is
    public static OkHttpClient getHttpClient() {
        return httpClient;
    }

    public static AppSingletonClass getInstance() {
        return self;
    }

    public static Preffy getLocalConfig(){
        if(lCfg == null){
            lCfg = Preffy.getInstance(self);
        }
        return lCfg;
    }

    public static Gson getGson(){
        return g;
    }

    public static MyJsonObject getRemoteConfig(){
        if(remoteConfig ==null){
            return new MyJsonObject(new JsonObject());
        }
        return remoteConfig;
    }

    /**
     * Get app's remote configuration as MyJsonObject.
     * Please note that the very first call should be with allowSyncFetching = TRUE, or NULL will be returned.
     * When allowSyncFetching is TRUE, the call will SYNCHRONOUSLY load data.
     *
     * Please use this encryption mode:
     * { key: sha256(key), iv: md5(key), message } → AES → [~ urlencode( base64( ciphertext ) ) ~]
     *
     * @param allowSyncFetching is synchronous data fetching allowed
     * @param forceFetching is force data fetching needed
     * @return app's remote configuration
     */
    public static MyJsonObject getRemoteConfig(boolean allowSyncFetching, boolean forceFetching){
        if((remoteConfig==null && allowSyncFetching) || forceFetching){
            try {
                Request request = new Request.Builder()
                        .url(REMOTE_CONFIG_URL)
                        .build();

                Response response = httpClient.newCall(request).execute();
                String data = response.body().string();

                Pattern pattern = Pattern.compile("\\[\\~(.*)\\~\\]", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(data);
                if (matcher.find()) {
                    data = java.net.URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.name());
                }

                if (!data.startsWith("{")) {
                    data = Utils.decryptStrAndFromBase64(BuildConfig.APPLICATION_ID, data);
                }
                remoteConfig = new MyJsonObject(getGson().fromJson(data, JsonObject.class));
            }catch (Exception e){
                e.printStackTrace();
                remoteConfig = new MyJsonObject(new JsonObject());
            }
        }
        return remoteConfig;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        g = new Gson();

        self = this;
        httpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(12, TimeUnit.SECONDS)
                .writeTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build();
    }
}
