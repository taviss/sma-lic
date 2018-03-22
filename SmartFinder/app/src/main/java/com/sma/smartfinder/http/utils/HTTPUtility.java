package com.sma.smartfinder.http.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

//TODO Turn this into a AsyncTask/Wrapper around an image upload
public class HTTPUtility {
    private static String attachmentName = "object";
    private static String attachmentFileName = "object.jpg";
    private static String crlf = "\r\n";
    private static String twoHyphens = "--";
    private static String boundary =  "*****";

    private static final String COOKIES_HEADER = "Set-Cookie";

    private static final CookieManager cookieManager = new CookieManager();

    private static final ExecutorService executors = Executors.newFixedThreadPool(3);

    public static Future<Boolean> login(final String urlString, final String userField, final String user, final String passwordField, final String password) throws JSONException, IOException {

        return executors.submit(
            new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    HttpURLConnection httpUrlConnection = null;
                    URL url = new URL("http://" + urlString);
                    httpUrlConnection = (HttpURLConnection) url.openConnection();
                    httpUrlConnection.setUseCaches(false);
                    httpUrlConnection.setDoOutput(true);

                    if (cookieManager.getCookieStore().getCookies().size() > 0) {
                        httpUrlConnection.setRequestProperty("Cookie",
                                TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
                    }

                    httpUrlConnection.setRequestProperty("Content-Type", "application/json");
                    httpUrlConnection.setRequestProperty("Accept", "application/json");
                    httpUrlConnection.setRequestMethod("POST");
                    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");

                    /*
                    String string = "";
                    string = string.concat(userField).concat("=").concat(user).concat("&");
                    string = string.concat(passwordField).concat("=").concat(password);*/

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(userField, user);
                    jsonObject.put(passwordField, password);

                    DataOutputStream request = new DataOutputStream(
                            httpUrlConnection.getOutputStream());
                    //request.write(string.getBytes("UTF-8"));

                    request.write(jsonObject.toString().getBytes("UTF-8"));

                    request.flush();
                    request.close();
                    int responseCode = httpUrlConnection.getResponseCode();

                    if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST
                            || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        return false;
                    }

                    Map<String, List<String>> headerFields = httpUrlConnection.getHeaderFields();
                    List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                    if (cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                        }
                    }

                    InputStream responseStream = new
                            BufferedInputStream(httpUrlConnection.getInputStream());

                    BufferedReader responseStreamReader =
                            new BufferedReader(new InputStreamReader(responseStream));

                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = responseStreamReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    responseStreamReader.close();

                    String response = stringBuilder.toString();

                    responseStream.close();

                    httpUrlConnection.disconnect();

                    return true;
                }
            });

    }


    public static Future<String> postImage(String urlString, Bitmap bitmap) throws IOException {
        return postImage(urlString, bitmap, new HashMap<String, String>());
    }


    public static Future<String> postImage(final String urlString, final Bitmap bitmap, final HashMap<String, String> extras) throws IOException {
        return executors.submit(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        HttpURLConnection httpUrlConnection = null;
                        URL url = new URL("http://" + urlString);
                        httpUrlConnection = (HttpURLConnection) url.openConnection();
                        httpUrlConnection.setUseCaches(false);
                        httpUrlConnection.setDoOutput(true);

                        if (cookieManager.getCookieStore().getCookies().size() > 0) {
                            httpUrlConnection.setRequestProperty("Cookie",
                                    TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
                        }

                        httpUrlConnection.setRequestMethod("POST");
                        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                        httpUrlConnection.setRequestProperty(
                                "Content-Type", "multipart/form-data;boundary=" + boundary);

                        DataOutputStream request = new DataOutputStream(
                                httpUrlConnection.getOutputStream());

                        request.writeBytes(twoHyphens + boundary + crlf);
                        request.writeBytes("Content-Disposition: form-data; name=\"" +
                                attachmentName + "\";filename=\"" +
                                attachmentFileName + "\"" + crlf);
                        request.writeBytes(crlf);

        /*
        byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
        for (int i = 0; i < bitmap.getWidth(); ++i) {
            for (int j = 0; j < bitmap.getHeight(); ++j) {
                //we're interested only in the MSB of the first byte,
                //since the other 3 bytes are identical for B&W images
                pixels[i + j] = (byte) ((bitmap.getPixel(i, j) & 0x80) >> 7);
            }
        }

        request.write(pixels);
        */

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, request);

                        request.writeBytes(crlf);


                        for(String extra : extras.keySet()) {
                            request.writeBytes(twoHyphens + boundary + crlf);
                            request.writeBytes("Content-Disposition: form-data; name=\"" + extra + "\"" + crlf);
                            request.writeBytes("Content-Type: text/plain" + crlf);
                            request.writeBytes(crlf);
                            request.writeBytes(extras.get(extra));
                            request.writeBytes(crlf);
                        }

                        request.writeBytes(twoHyphens + boundary +
                                twoHyphens + crlf);



                        request.flush();
                        request.close();

                        InputStream responseStream = new
                                BufferedInputStream(httpUrlConnection.getInputStream());

                        BufferedReader responseStreamReader =
                                new BufferedReader(new InputStreamReader(responseStream));

                        String line = "";
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((line = responseStreamReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        responseStreamReader.close();

                        String response = stringBuilder.toString();

                        responseStream.close();

                        httpUrlConnection.disconnect();

                        return response;
                    }
                }
        );
    }
}
