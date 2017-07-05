package qiniu.predem.android.probe;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 17/7/4.
 */
@Aspect
public class WebViewProbe {
    private static final String TAG = "WebViewProbe";
    protected static final WebViewProbe instance = new WebViewProbe();
    protected final static List<WeakReference<WebView>> webviews = new LinkedList<>();

    @Around("call(* android.webkit.WebView+.setWebViewClient(..))")
    public Object onWebViewSetClient(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable || !Configuration.webviewEnable){
            return joinPoint.proceed();
        }
        LogUtils.d(TAG, "-------onWebViewSetClient");
        try {
            if (joinPoint.getArgs().length != 1 || !(joinPoint.getArgs()[0] instanceof WebViewClient)
                    || joinPoint.getArgs()[0] instanceof ProbeWebClientAgent
                    || joinPoint.getArgs()[0] instanceof ProbeWebClient) {

                if (joinPoint.getArgs()[0] instanceof ProbeWebClientAgent
                        || joinPoint.getArgs()[0] instanceof ProbeWebClient) {
                    synchronized (webviews) {
                        webviews.add(new WeakReference<>((WebView) joinPoint.getTarget()));
                    }
                }

                return joinPoint.proceed();
            }

            ((WebView) joinPoint.getTarget()).setWebViewClient(
                    ProbeWebClientAgent.obtain((WebViewClient) joinPoint.getArgs()[0]));

            synchronized (webviews) {
                webviews.add(new WeakReference<>((WebView) joinPoint.getTarget()));
            }
            return null;
        } catch (Throwable e) {
            e.printStackTrace();
            return joinPoint.proceed();
        }
    }

    @Before("call(* android.webkit.WebView+.loadUrl(..))")
    public void onWebViewLoadUrl(JoinPoint joinPoint) {
        if (!Configuration.httpMonitorEnable || !Configuration.webviewEnable){
            return;
        }
        LogUtils.d(TAG, "------onWebViewLoadUrl");
        try {
            if (joinPoint.getTarget() instanceof WebView) {
                WebView web = (WebView) joinPoint.getTarget();

                synchronized (webviews) {
                    for (int i = webviews.size() - 1; i >= 0; i--) {
                        WebView item = webviews.get(i).get();
                        if (item == null) {
                            webviews.remove(i);
                        } else if (item.equals(web)) {
                            return;
                        }
                    }
                    webviews.add(new WeakReference<>(web));
                }

//                web.setWebViewClient(ProbeWebClient.instance);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static WebViewProbe aspectOf() {
        return instance;
    }
}