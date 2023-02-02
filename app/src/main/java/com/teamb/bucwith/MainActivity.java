package com.teamb.bucwith;

import static android.content.ContentValues.TAG;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        // JS 관련 세팅
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true); // 여러 창 또는 탭 열리는 것 허용 (카카오톡 공유하기)
        webSettings.setLoadWithOverviewMode(true); // 페이지 내에서만 이동하게끔
        webSettings.setUseWideViewPort(true); // 페이지를 웹뷰 width에 맞춤
        webSettings.setSupportZoom(false); // 확대 비활성화
        webSettings.setBuiltInZoomControls(false); // 확대 비활성화
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 캐시 사용안함 (매번 새로 로딩)
        webSettings.setDomStorageEnabled(true);

        String agentNew = webSettings.getUserAgentString() + " ANDROID_APP";
        webSettings.setUserAgentString(agentNew);

        webView.addJavascriptInterface(new WebAppInterface(), "NativeAndroid");   // Bridge ( 링크 복사 )
        webView.setWebViewClient(new WebViewClientClass());
        webView.loadUrl("https://bucwiths.shop");  // 앱에서 표시할 url 입력
    }
    private class WebViewClientClass extends WebViewClient {
        // SSL 인증서 무시
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        // KAKAO 공유하기 에러 처리
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d(TAG, request.getUrl().toString());

            if (Objects.equals(request.getUrl().getScheme(), "intent")) {
                try {
                    // Intent 생성
                    Intent intent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);

                    // 실행 가능한 앱이 있으면 앱 실행
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                        Log.d(TAG, "ACTIVITY: ${intent.`package`}");
                        Log.d(TAG, "카카오톡 실행");
                        return true;
                    }

                    Log.d(TAG, "카카오톡 공유하기 실행 못함");

                    // 실행 못하면 웹뷰는 카카오톡 공유하기 화면으로 이동
                    view.loadUrl("http://kakao-share.s3-website.ap-northeast-2.amazonaws.com/");

//                    // 구글 플레이 카카오톡 마켓으로 이동
//                     intentStore = Intent(Intent.ACTION_VIEW);
//                    intentStore.addCategory(Intent.CATEGORY_DEFAULT)
//                    intentStore.data = Uri.parse("market://details?id=com.kakao.talk");
//                    Log.d(TAG, "구글 플레이 카카오톡 마켓으로 이동");
//                    startActivity(intentStore)

                } catch (URISyntaxException e) {
                    Log.e(TAG, "!!! 에러 Invalid intent request", e);
                }
            }
            return false;
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void copyToClipboard(String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("demo", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    //폰의 뒤로가기 버튼의 동작 입력
    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
