package com.utilitydevs34.luckygps.foo;

import com.utilitydevs34.luckygps.AppSingletonClass;
import com.utilitydevs34.luckygps.BuildConfig;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class UselessCode {
    private static List<Runnable> snippets = null;
    public static double result = 0;

    public static void run() {
        if (snippets == null) {
            snippets = new ArrayList<>();

            snippets.add(() -> {
                int n0 = 1;
                int n1 = 1;
                for (int i = 3; i <= 11; i++) {
                    result = n0 + n1;
                    n0 = n1;
                    n1 = (int) result;
                }
            });

            snippets.add(() -> {
                for (double i = 0; i < BuildConfig.APPLICATION_ID.length() * 5; i++) {
                    if (i % 2 == 0)
                        result += -1 / (2 * i - 1);
                    else
                        result += 1 / (2 * i - 1);
                }
            });

            snippets.add(() -> {
                int count = 10;
                int m = 1;
                int n = 0;
                final double SQRT_12 = Math.sqrt(12);
                while (count > n) {
                    result += SQRT_12 * (Math.pow(-1, n) / (m * Math.pow(3, n)));
                    m += 2;
                    n++;
                }
            });

            snippets.add(() -> {
                int beerNum = 99;
                while (beerNum > 0) {
                    beerNum = beerNum - 1;
                    if (beerNum == 1) {
                        result += 1;
                    }

                    if (beerNum > 0) {
                        result -= 1;
                    }
                }

                if (beerNum == 0) {
                    result = 0;
                }
            });

            snippets.add(() -> {
                try {
                    byte[] bytesOfMessage = String.valueOf(result).getBytes("UTF-8");

                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] thedigest = md.digest(bytesOfMessage);
                    result = thedigest[0];
                } catch (Exception ignored) {
                }
            });

            snippets.add(() -> {
                String str1 = "foo";
                String str2 = BuildConfig.APPLICATION_ID;
                if (str1 == null) {
                    result = 0;
                    return;
                }
                if (str2 == null) {
                    result = 0;
                    return;
                }
                int at = 0;
                String cs1 = str1;
                String cs2 = str2;
                int i;
                for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
                    if (cs1.charAt(i) != cs2.charAt(i)) {
                        break;
                    }
                }
                if (i < cs2.length() || i < cs1.length()) {
                    at = i;
                }
                if (at == -1) {
                    result = -1;
                    return;
                }
                result = at;
            });

            snippets.add(() -> {
                List<String> l = new ArrayList<>();
                l.add(BuildConfig.BUILD_TYPE);
                l.add(String.valueOf(BuildConfig.VERSION_CODE));
                l.add(BuildConfig.APPLICATION_ID);
                l.add(BuildConfig.DEBUG ? "debug" : "release");
                result = AppSingletonClass.getGson().toJson(l).length();
            });

            snippets.add(() -> {
                result = result + 1;
                return;
            });

            snippets.add(() -> {
                result = Utils.randomString(10).length() > 10 ? 1 : 0;
                return;
            });
        }

        Runnable snippet = Utils.getRandomItem(snippets);
        try {
            snippet.run();
        } catch (Exception ignored) {
        }
    }
}