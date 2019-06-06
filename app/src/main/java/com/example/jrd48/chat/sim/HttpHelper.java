package com.example.jrd48.chat.sim;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpHelper {
    public static HttpURLConnection getConnection(String urlStr) throws KeyManagementException, MalformedURLException, NoSuchAlgorithmException, IOException {
        HttpURLConnection conn = null;
        if (urlStr.toLowerCase().startsWith("https"))
            conn = getHttpsConnection(urlStr);
        else
            conn = getHttpConnection(urlStr);
        return conn;
    }

    private static HttpURLConnection getHttpConnection(String urlStr) throws MalformedURLException, IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return conn;
    }

    private static HttpsURLConnection getHttpsConnection(String urlStr) throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException {
        URL url = new URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setHostnameVerifier(new IgnoreHostnameVerifier());
        TrustManager[] tm = {new IgnoreCertificationTrustManger()};
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tm, null);
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        conn.setSSLSocketFactory(ssf);
        return conn;
    }
}

class IgnoreCertificationTrustManger implements X509TrustManager {

    private X509Certificate[] certificates;

    public void checkClientTrusted(X509Certificate certificates[],
                                   String authType) throws CertificateException {
        if (this.certificates == null) {
            this.certificates = certificates;
        }

    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        // TODO Auto-generated method stub

    }

}

class IgnoreHostnameVerifier implements HostnameVerifier {

    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }

}

