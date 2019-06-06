package com.example.jrd48.chat.VersionUpdate;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestHandle;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by Administrator on 2017/1/13.
 *
 * @author quhuabo 封装 AsynHttpClient 异步发送JSON数据到服务器，固定为UTF-8编码
 */

public class MyAsynHttpClient extends AsyncHttpClient {
    private final String MY_TAG = "MyAsynHttpClient";

    private Context context = null;

    public static final int OK = 1;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public MyAsynHttpClient(Context context) {
        super();
        try {
            setMaxRetriesAndTimeout(0, 15000);
//			this.setTimeout(30000); // 设置为超时30秒

            if (context != null) {
                this.context = context;
                PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
                this.setCookieStore(myCookieStore);
            } else {
                log.d("ASYN_HTTP_CLIENT", "warning: context is null");
                throw new Exception("ASYN_HTTP_CLIENT context should not be NULL.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkStatus(JSONObject response) {
        try {

            return (response.has("status") && response.getInt("status") == OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存一个Cookie，有效期为1年
     *
     * @param szCookieName
     * @param szCookieValue
     */
    public void saveCookie(String szCookieName, String szCookieValue) {
        if (context == null)
            return;

        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);

        BasicClientCookie newCookie = new BasicClientCookie(szCookieName, szCookieValue);
        newCookie.setVersion(1);
        newCookie.setDomain("irobbing.com");
        newCookie.setPath("/");

        Calendar curr = Calendar.getInstance();
        curr.set(Calendar.YEAR, curr.get(Calendar.YEAR) + 1);
        Date date = curr.getTime();

        newCookie.setExpiryDate(date);
        myCookieStore.addCookie(newCookie);

    }

    /**
     * 获取标准 Cookie
     *
     * @param context
     */
    public static String getCookieText(Context context) {
        if (context == null)
            return "";
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = myCookieStore.getCookies();
        // Util.setCookies(cookies);
        // for (Cookie cookie : cookies) {
        // Log.d("mData1", cookie.getName() + " = " + cookie.getValue());
        // }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cookies.size(); i++) {
            Cookie cookie = cookies.get(i);
            String cookieName = cookie.getName();
            String cookieValue = cookie.getValue();
            if (!cookieName.equals("")) {
                sb.append(cookieName + "=");
                sb.append(cookieValue + ";");
            }
        }
        return sb.toString();
    }

    /**
     * 获取一个cookie 的值
     *
     * @param szCookieName
     * @return
     */
    private String getCookie(String szCookieName) {
        if (context == null)
            return "";
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        List<Cookie> cookies = myCookieStore.getCookies();
        Date now = new Date();
        for (Cookie c : cookies) {
            if (c.getName().compareToIgnoreCase(szCookieName) == 0 && c.isExpired(now)) {
                return c.getValue();
            }
        }
        return "";
    }

    /**
     * 向服务器发送 json 数据，并返回 json 数据
     *
     * @param url
     * @param data         (UTF-8 编码)
     * @param respHandler: new JsonHttpResponseHandler { onSuccess, onFailed }
     * @return
     * @throws UnsupportedEncodingException
     */
    public RequestHandle postJson(String url, JSONObject data, JsonHttpResponseHandler respHandler)
            throws UnsupportedEncodingException {
        // setMaxRetriesAndTimeout(DEFAULT_MAX_RETRIES, DEFAULT_SOCKET_TIMEOUT);
        String szTest = data.toString();// json.toString();
        ByteArrayEntity entity = new ByteArrayEntity(szTest.getBytes("UTF-8"));
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        entity.setContentEncoding("utf-8");

        return this.post(context, url, entity, "application/json", respHandler);

    }

    /**
     * 向服务器发送 json 数据，使用回调处理了返回的json 数据，使得调用更简单
     * @param url
     * @param data
     * @param func
     * @return
     */
//    public RequestHandle postJson2(final String url, final JSONObject data, final MyAsynPostListener func) {
//        try {
//            return postJson(url, data, new JsonHttpResponseHandler("utf-8") {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    try {
//                        if (MyAsynHttpClient.checkStatus(response)) {
//                            // log.d(MY_TAG, "[post json success]: url: " + url
//                            // + "\n param: " + data);
//                            func.onGotSuccess(response);
//                        } else {
//                            log.d(MY_TAG, "[post json failed]: url: " + url + "\n param: " + data);
//                            func.onGotFailed(response);
//                        }
//                    } catch (Exception e) {
//                        log.d(MY_TAG, "[post json Exception]: url: " + url + "\nparam: " + data + "\nException: "
//                                + e.getMessage());
//                        e.printStackTrace();
//                        func.onGotException(e);
//
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    func.onGotException(throwable);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                    func.onGotException(throwable);
//                }
//
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//
//                }
//
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, String responseString) {
//
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                    log.d(MY_TAG, "[post json Exception]: url: " + url + "\nparam: " + data + "\nException: "
//                            + throwable.getMessage());
//                    func.onGotException(throwable);
//                }
//
//                @Override
//                public void onFinish() {
//                    func.onFinish();
//                }
//            });
//        } catch (Exception e) {
//            log.d(MY_TAG, "[BEFORE post json Exception]: url: " + url + "\nparam: " + data + "\nException: "
//                    + e.getMessage());
//            func.onGotException(e);
//            func.onFinish();
//        }
//
//        return null;
//    }
}
