package com.utilitydevs34.luckyweather.foo;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.RawRes;

import com.utilitydevs34.luckyweather.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class Utils {

    public static final String KEY_AF_DEV_ID = "af__device_id";
    public static final String KEY_ADID = "idfa";

    public static String encCaesar(byte key, String src){
        byte[] data = src.getBytes();
        int i = 0;
        for(i=0;i<data.length;i++){
            data[i] = (byte) (data[i]^key);
        }

        return new String(Base64.encode(data, Base64.DEFAULT), StandardCharsets.UTF_8);
    }

    public static <T> T getRandomItem(List<T> list)
    {
        Random random = new Random();
        int listSize = list.size();
        int randomIndex = random.nextInt(listSize);
        return list.get(randomIndex);
    }

    public static List<Integer> getDominantColors(Bitmap bitmap, int w, int h) {
        List<Integer> result = new LinkedList<>();
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        for(int x = 0;x<newBitmap.getWidth();x++){
            for(int y = 0;y<newBitmap.getHeight();y++){
                result.add(newBitmap.getPixel(x,y));
            }
        }
        newBitmap.recycle();
        return result;
    }

    public static String readTextFile(Context context, @RawRes int id){
        InputStream inputStream = context.getResources().openRawResource(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buffer[] = new byte[1024];
        int size;
        try {
            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toString();
    }

    public static void openUrl(String url, Context c){
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            c.startActivity(browserIntent);
        }catch (Exception ignored  ){}
    }

    public static boolean assetExists(Context context, String path) {
        try {
            InputStream stream = context.getAssets().open(path.replace("file:///android_asset/",""));
            stream.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Drawable getResDrawable(String name, Context ctx) {
        Drawable result;
        try {
            int resourceId = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
            result = ctx.getDrawable(resourceId);
        }catch (Resources.NotFoundException e){
            return ctx.getDrawable(R.drawable.com_facebook_button_icon);
        }
        return result;
    }

    public static Bitmap getAssetBitmap(Context context, String strName) {
        AssetManager assetManager = context.getAssets();
        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(strName);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            return null;
        }
        return bitmap;
    }

    public static String getResString(String aString, Context ctx) {
        String packageName = ctx.getPackageName();
        int resId = ctx.getResources().getIdentifier(aString, "string", packageName);
        return ctx.getString(resId);
    }

    /**
     * Append raw URL string with data from another url
     *
     * @param urlSource Raw source URL string (where query parameters will be added)
     * @param urlParams Raw URL string (from where query parameters will be added)
     * @return Updated URL string
     */
    public static String appendQueryParameters(String urlSource, String urlParams) {
        if (urlParams.length() == 0 || !urlParams.contains("://")) return urlSource;

        Uri.Builder uriBuilderSource = Uri.parse(urlSource).buildUpon();
        Uri uriParams = Uri.parse(urlParams);

        for (String paramName : uriParams.getQueryParameterNames()) {
            String val = uriParams.getQueryParameter(paramName);
            if (val == null || val.length() == 0) continue;
            uriBuilderSource = uriBuilderSource.appendQueryParameter(paramName, val);
        }
        return uriBuilderSource.build().toString();
    }

    public static byte[] encrypt(String ivStr, String keyStr, byte[] bytes) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ivStr.getBytes());
        byte[] ivBytes = md.digest();

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(keyStr.getBytes());
        byte[] keyBytes = sha.digest();

        return encrypt(ivBytes, keyBytes, bytes);
    }

    static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] bytes) throws Exception{
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(bytes);
    }

    public static byte[] decrypt(String ivStr, String keyStr, byte[] bytes) throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ivStr.getBytes());
        byte[] ivBytes = md.digest();

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(keyStr.getBytes());
        byte[] keyBytes = sha.digest();

        return decrypt(ivBytes, keyBytes, bytes);
    }

    static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] bytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(bytes);
    }

    public static String encryptStrAndToBase64(String keyStr, String enStr) throws Exception{
        byte[] bytes = encrypt(keyStr, keyStr, enStr.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.encode(bytes ,Base64.DEFAULT), StandardCharsets.UTF_8);
    }

    public static String decryptStrAndFromBase64(String keyStr, String deStr) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException {
        byte[] bytes = decrypt(keyStr, keyStr, Base64.decode(deStr.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static void copy(InputStream in, File dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    public static void runOnUiThread(Runnable r){
        new Handler(Looper.getMainLooper()).post(r);
    }
}

