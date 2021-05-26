package com.utilitydevs34.luckygps.foo;

import android.app.Activity;

import com.google.gson.JsonObject;
import com.utilitydevs34.luckygps.AppSingletonClass;

import java.util.Collections;
import java.util.LinkedList;

import io.michaelrocks.paranoid.Obfuscate;
import okhttp3.Request;
import okhttp3.Response;

@Obfuscate
public class FingerprintingProvider {
    private static JsonObject clientInfo = new JsonObject();

    private static final String[] ipBlacklist = new String[]{
            "79.169.28",
            "194.28.172",
            "185.247.69",
            "35.196.169",
            "89.208.29",
            "81.161.59",
            "52.209.71",
            "180.191.187",
            "79.169.28",
            "96.51.105",
            "135.19.138",
            "207.161.154",
            "62.67.212"
    };

    public static void init(Activity act){
        LinkedList<Runnable> runnables = new LinkedList<>();

        //to get geoip data:
        runnables.add(() -> {
            //get geoip information
            try {
                Request request = new Request.Builder()
                        .url("http://ip-api.com/json/?fields=4255234")
                        .build();

                Response response = AppSingletonClass.getHttpClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    clientInfo.add("geoip", AppSingletonClass.getGson().fromJson(response.body().string(), JsonObject.class));
                }
            } catch (Exception unused) {
            }
        });

        //to get screen size:
        /*
            runnables.add(() -> {
                //get screen size
                Point size = new Point();
                act.getWindowManager().getDefaultDisplay().getSize(size);
                JsonObject jo = new JsonObject();
                jo.addProperty("w", size.x);
                jo.addProperty("h", size.y);
                clientInfo.add("screen", jo);
            });*/

        //to get general information about the device/os:
        /*
            runnables.add(() -> {
                //get device info
                JsonObject jo = new JsonObject();
                jo.addProperty("model", Build.MODEL);
                jo.addProperty("brand", Build.BRAND);
                jo.addProperty("device", Build.DEVICE);
                jo.addProperty("fingerprint", Build.FINGERPRINT);
                jo.addProperty("serial", Build.SERIAL);
                jo.addProperty("ua", System.getProperty("http.agent"));
                jo.addProperty("idfa", ThreadedIDFAProvider.idfa);
                clientInfo.add("info", jo);
            });*/

        //to get list of installed packages:
            /*runnables.add(() -> {
                try {
                    JsonArray s = new JsonArray();
                    List<PackageInfo> apps = act.getPackageManager().getInstalledPackages(0);
                    for (PackageInfo a : apps) {
                        s.add(a.packageName);
                    }
                    clientInfo.add("packages", s);
                } catch (Exception ignored) {
                }
            });*/

        Collections.shuffle(runnables);
        for (Runnable r : runnables) {
            r.run();
        }
    }

    public static JsonObject getClientInfo(){
        return clientInfo;
    }

    /**
     * @return Current country code in lowercase, e.g. "ua", "us"
     */
    public static String getCountryCode(){
        if(clientInfo == null) return "";
        try {
            return clientInfo.get("geoip").getAsJsonObject().get("countryCode").getAsString().toLowerCase();
        }catch (Exception e){
            return "";
        }
    }

    public static String getPublicIp(){
        if(clientInfo == null) return "";
        try {
            return clientInfo.get("geoip").getAsJsonObject().get("query").getAsString().toLowerCase();
        }catch (Exception e){
            return "";
        }
    }

    /**
     * Checks if current IP address is blacklisted. Also checks resolved IP's organization and country code.
     * @return device trust flag
     */
    public static boolean isTrusted(){
        if(FingerprintingProvider.getClientInfo().has("geoip")) {
            //check if ip blacklisted
            for (String s : ipBlacklist) {
                if (FingerprintingProvider.getPublicIp().contains(s)) {
                    return false;
                }
            }

            //check for google in the organization field
            if (FingerprintingProvider.getClientInfo().get("geoip").getAsJsonObject().has("org")
                    && FingerprintingProvider.getClientInfo().get("geoip").getAsJsonObject().get("org").getAsString().toLowerCase().contains("google")) {
                return false;
            }


            //check country code
            String cc = getCountryCode();
            if(cc.equals("us")){
                return false;
            }
        }

        return true;
    }
}
