package qiniu.predem.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by Misty on 17/7/4.
 */

public class WebViewActivity extends Activity {
    private static final String TAG = "WebViewActivity";

    private WebView mWebView;
    private Intent mIntent;
    private String mUrl;
    private ProgressBar mProgress;
    private WebViewClient webViewClient = new WebViewClient() {
        /** 重写点击动作,用webview载入 */
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            mProgress.setVisibility(View.VISIBLE);
            mWebView.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(TAG, "onReceivedSslError");
            handler.proceed(); // 接受所有网站的证书
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            checkurl(url);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //清除cookie
        CookieSyncManager.createInstance(WebViewActivity.this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        setContentView(R.layout.activity_webview);

        mWebView = (WebView) findViewById(R.id.login_webview);

        mIntent = getIntent();
        mUrl = mIntent.getStringExtra("extr_url");

        mProgress = (ProgressBar) findViewById(R.id.login_webview_progress);

        init();
    }

    private void init() {
        mWebView.setWebViewClient(webViewClient);
//        mWebView.setWebChromeClient(chromeClient);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setAppCacheEnabled(false);
        settings.setSavePassword(false);

        mWebView.loadUrl(mUrl);
    }

//    private WebChromeClient chromeClient = new WebChromeClient(){
//        @Override
//        public void onProgressChanged(WebView view, int newProgress) {
//            if(newProgress == 100)
//            {
//                mProgress.setVisibility(View.GONE);
//            }
//            super.onProgressChanged(view,newProgress);
//        }
//    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWebView.destroy();
    }
}
