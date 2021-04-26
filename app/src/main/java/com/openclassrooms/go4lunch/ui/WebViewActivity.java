package com.openclassrooms.go4lunch.ui;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.openclassrooms.go4lunch.R;

public class WebViewActivity extends AppCompatActivity {

    private WebView mWebView;
    public static final String WEB = "resto_web";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        mWebView = findViewById(R.id.webview);
        String mURL = getIntent().getStringExtra(WEB);

        mWebView.setWebViewClient(new WebViewClient());

        mWebView.loadUrl(mURL);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}