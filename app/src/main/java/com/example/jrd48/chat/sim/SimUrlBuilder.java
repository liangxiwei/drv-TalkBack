package com.example.jrd48.chat.sim;

/**
 * Created by zhouyuhuan on 2018/3/9.
 */

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.android.internal.R.id.month;

public class SimUrlBuilder {
    private static final String TAG = "SimUrlBuilder";

    public static String searchSimInfo(String url,String body) throws ClientProtocolException, IOException {
        Log.d(TAG,"body="+body);
        String stringToSign = MD5(body).concat(SimValue.SECRET_KEY);
        String signature = SHA1(stringToSign);
        String result = makeRequest(url,body, signature);
        return result;
    }

    public static String makeRequest(String url,String body, String signature) throws ClientProtocolException, IOException {
        HttpClient client = new DefaultHttpClient();//HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("signature", signature);
        post.setHeader("api-versions", SimValue.VERSION);
        post.setEntity(new StringEntity(body));

        HttpResponse response = client.execute(post);
        String resp = EntityUtils.toString(response.getEntity());
        Log.d(TAG,"resp="+resp);
        return resp;
    }

    /***
     *  Container_API - 07.容器订购信息查询
     *
     * "content": {
     *      "tranId":"2017080349454",
     *      "data":{
     *          "cid":"TNS201708034945"
     *       }
     *   }
     * */
    public static String makeJsonOrderItemsByCid(String iccid,String uuid) {

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        ObjectNode metadata = node.putObject("metadata");
        metadata.put("accessKey", SimValue.ACCESS_KEY);
        metadata.put("signType", "SHA1");
        metadata.put("timestamp", System.currentTimeMillis());

        ObjectNode content = node.putObject("content");
        content.put("tranId", uuid);
        ObjectNode data = content.putObject("data");
        data.put("cid", iccid);
        return node.toString();
    }

    /**
     * ICCID_API - 08.码号流量查询
     * --data '{
     *       "metadata": {
     *           "accessKey": "hw8HIRGcJgKi0CuseFmXz8J3nf1Z3gAd",
     *           "signType": "SHA1",
     *          "timestamp": 1494211640358
     *       },
     *       "content": {
     *           "tranId":"2017080349454",
     *           "data":{
     *               "iccid":"2334554545434",
     *               "flowDate":"201707"
     *           }
     *     }
     * @param iccid
     * @return
     */
    public static String makeJsonFlowByIccid(String iccid,String uuid) {

        ObjectNode node = JsonNodeFactory.instance.objectNode();

        ObjectNode metadata = node.putObject("metadata");
        metadata.put("accessKey", SimValue.ACCESS_KEY);
        metadata.put("signType", "SHA1");
        metadata.put("timestamp", System.currentTimeMillis());

        ObjectNode content = node.putObject("content");
        content.put("tranId", uuid);
        ObjectNode data = content.putObject("data");
        data.put("iccid", iccid);
        String date = new SimpleDateFormat("yyyyMM", Locale.CHINESE).format(Calendar.getInstance().getTime());
        Log.d(TAG,"date="+date);
        data.put("flowDate", date);
        return node.toString();
    }
    public static String MD5(String input) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }

        byte byteData[] = md.digest();
        return bytesToHex(byteData).toLowerCase();
    }

    public static String SHA1(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
            md.update(input.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }

        byte byteData[] = md.digest();
        return bytesToHex(byteData).toLowerCase();
    }


    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static String buildGetUrl(String urlDomain, Map<String, String> form) {
        StringBuilder requestUrl = new StringBuilder();
        requestUrl.append(urlDomain);
        requestUrl.append("?");
        String joinChar = "";
        for (Map.Entry<String, String> entry : form.entrySet()) {
            requestUrl.append(joinChar);
            requestUrl.append(entry.getKey());
            requestUrl.append("=");
            String value;
            try {
                value = URLEncoder.encode(entry.getValue(), "UTF-8");
            } catch (Exception e) {
                value = "";
            }
            requestUrl.append(value);
            joinChar = "&";
        }
        return requestUrl.toString();
    }
    public static String getHttpResponse(String url, String method, byte[] body, int connect_timeout, int read_timeout) throws Exception {
        Log.d(TAG, "getHttpResponse url:" + url);
        Log.d(TAG, "connect_timeout:" + connect_timeout + "; read_timeout:" + read_timeout);
        String urlString = url;
        if (urlString == null || urlString.equals("")) {
            throw new Exception("Malformed url");
        }
        try {
            method = method == null ? "" : method.trim();
            if ("GET".equals(method.toUpperCase())) {
                HttpURLConnection connection = (HttpURLConnection)HttpHelper.getConnection(urlString);
                connection.setRequestProperty("User-Agent", "PacificHttpClient");
                connection.setConnectTimeout(connect_timeout);
                connection.setReadTimeout(read_timeout);//20000
                connection.connect(); // 建立到远程对象的实际连接
                if (connection.getResponseCode() == 404) {
                    Log.e(TAG,"getResponseCode 404");
                    throw new Exception("404");
                }
                InputStream in = null;
                try {
                    // Read the response.
                    in = connection.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int count; (count = in.read(buffer)) != -1;) {
                        out.write(buffer, 0, count);
                    }
                    byte[] response = out.toByteArray();
                    return new String(response, "UTF-8");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return "";
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (connection != null) {
                        connection.disconnect(); // 中断连接
                    }
                }
            } else if ("POST".equals(method.toUpperCase())) {
                HttpURLConnection connection = (HttpURLConnection)HttpHelper.getConnection(urlString);
                connection.setRequestProperty("User-Agent", "PacificHttpClient");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);//20000
                OutputStream out = null;
                InputStream in = null;
                try {
                    // Write the request.
                    connection.setRequestMethod("POST");
                    if (body != null) {
                        out = connection.getOutputStream();
                        out.write(body);
                        out.close();
                    }

                    // Read the response.
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new Exception("Unexpected HTTP response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
                    }
                    in = connection.getInputStream();

                    ByteArrayOutputStream array = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int count; (count = in.read(buffer)) != -1;) {
                        array.write(buffer, 0, count);
                    }
                    byte[] response = array.toByteArray();
                    return new String(response, "UTF-8");
                } finally {
                    try {
                        if (out != null)
                            out.close();
                    } catch (Exception e) {
                    }
                    try {
                        if (in != null)
                            in.close();
                    } catch (Exception e) {
                    }

                }
            }

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            throw new Exception("Malformed url");
        } catch (SocketTimeoutException e2) {
            e2.printStackTrace();
            throw new Exception(e2.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return "";
    }

}
