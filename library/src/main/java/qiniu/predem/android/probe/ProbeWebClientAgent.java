package qiniu.predem.android.probe;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Misty on 17/7/4.
 */

public class ProbeWebClientAgent extends ProbeWebClient {
    private static final String TAG = "ProbeWebClientAgent";
    private static final List<ProbeWebClientAgent> mPool = new LinkedList<>();
    protected WebViewClient source;

    protected ProbeWebClientAgent(WebViewClient source) {
        init(source);
    }

    public static ProbeWebClientAgent obtain(WebViewClient source) {
        if (mPool.size() > 0) {
            synchronized (mPool) {
                if (mPool.size() > 0) {
                    ProbeWebClientAgent obj = mPool.get(0);
                    obj.init(source);
                    mPool.remove(0);
                    return obj;
                }
            }
        }
        return new ProbeWebClientAgent(source);
    }

    public void release() {
        synchronized (mPool) {
            this.source = null;
            if (mPool.size() < 256) mPool.add(this);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.release();
    }

    protected void init(WebViewClient source) {
        this.source = source;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return source.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        source.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        source.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        source.onLoadResource(view, url);
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        source.onTooManyRedirects(view, cancelMsg, continueMsg);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        source.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        source.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        source.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        source.onReceivedSslError(view, handler, error);
    }

    @SuppressLint("NewApi")
    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        source.onReceivedClientCertRequest(view, request);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        source.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return source.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        source.onUnhandledKeyEvent(view, event);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        source.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        source.onReceivedLoginRequest(view, realm, account, args);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return source.shouldOverrideUrlLoading(view, request);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onPageCommitVisible(WebView view, String url) {
        source.onPageCommitVisible(view, url);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        source.onReceivedError(view, request, error);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        source.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return source.equals(obj);
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
