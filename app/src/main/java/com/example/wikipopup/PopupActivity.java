package com.example.wikipopup;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PopupActivity extends Activity {

    private WebView webView;

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CharSequence raw = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        String term = raw == null ? "" : raw.toString().replace('\n', ' ').trim();
        if (term.isEmpty()) {
            finish();
            return;
        }
        if (term.length() > 100) {
            term = term.substring(0, 100);
        }

        boolean dark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int cardColor = dark ? 0xFF2A2A2A : 0xFFFFFFFF;
        int textColor = dark ? 0xFFFFFFFF : 0xFF202020;

        // 背景（外側をタップすると閉じる）
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> finish());

        // カード
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardColor);
        bg.setCornerRadius(dp(24));
        card.setBackground(bg);
        card.setClipToOutline(true);
        card.setElevation(dp(8));
        card.setClickable(true); // カード内のタップで閉じないように

        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.65f);
        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height, Gravity.TOP);
        cardLp.setMargins(dp(12), dp(56), dp(12), dp(12));
        root.addView(card, cardLp);

        // ヘッダー
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(8), dp(8), dp(8));

        TextView title = new TextView(this);
        title.setText("Wikipedia　" + term);
        title.setTextColor(textColor);
        title.setTextSize(15);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        header.addView(title, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextColor(textColor);
        close.setTextSize(20);
        close.setPadding(dp(14), dp(10), dp(14), dp(10));
        close.setOnClickListener(v -> finish());
        header.addView(close);

        card.addView(header);

        // 記事表示
        webView = new WebView(this);
        webView.setBackgroundColor(cardColor);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        if (dark) {
            if (Build.VERSION.SDK_INT >= 33) {
                s.setAlgorithmicDarkeningAllowed(true);
            } else if (Build.VERSION.SDK_INT >= 29) {
                s.setForceDark(WebSettings.FORCE_DARK_ON);
            }
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost() == null ? "" : uri.getHost();
                if (host.endsWith("wikipedia.org") || host.endsWith("wikimedia.org")) {
                    return false; // Wikipedia内のリンクは小窓の中で開く
                }
                startActivity(new Intent(Intent.ACTION_VIEW, uri)); // 外部リンクはブラウザへ
                return true;
            }
        });
        card.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);

        // 完全一致の記事があればそのまま記事、なければ検索結果を表示
        webView.loadUrl("https://ja.wikipedia.org/w/index.php?go=Go&search=" + Uri.encode(term));
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
