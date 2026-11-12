package com.example.komadosearch;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.*;
import android.widget.*;
import java.util.List;

public class PopupActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CharSequence raw = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        String term = raw == null ? "" : raw.toString().replace('\n', ' ').trim();
        if (term.isEmpty()) { finish(); return; }
        if (term.length() > 100) term = term.substring(0, 100);

        boolean dark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int cardColor = dark ? 0xFF2A2A2A : 0xFFFFFFFF;
        int textColor = dark ? 0xFFFFFFFF : 0xFF202020;

        SiteStore.Site def = SiteStore.getDefault(this);
        if (def != null) {
            openPopup(def, term, dark, cardColor, textColor);
        } else {
            showPicker(term, dark, cardColor, textColor);
        }
    }

    private void showPicker(String term, boolean dark, int cardColor, int textColor) {
        List<SiteStore.Site> sites = SiteStore.load(this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> finish());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardColor);
        bg.setCornerRadius(dp(24));
        card.setBackground(bg);
        card.setClipToOutline(true);
        card.setElevation(dp(8));
        card.setClickable(true);
        card.setPadding(dp(20), dp(20), dp(20), dp(20));

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        cardLp.setMargins(dp(24), 0, dp(24), 0);
        root.addView(card, cardLp);

        // ヘッダー
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(16));

        TextView title = new TextView(this);
        title.setText("「" + term + "」をどこで開く？");
        title.setTextColor(textColor);
        title.setTextSize(15);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        header.addView(title, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextColor(textColor);
        close.setTextSize(20);
        close.setPadding(dp(10), dp(4), 0, dp(4));
        close.setOnClickListener(v -> finish());
        header.addView(close);
        card.addView(header);

        // サイトボタン一覧
        for (SiteStore.Site site : sites) {
            Button btn = new Button(this);
            btn.setText(site.name);
            btn.setTextColor(0xFFFFFFFF);
            btn.setTextSize(15);
            GradientDrawable btnBg = new GradientDrawable();
            btnBg.setColor(0xFF0A84FF);
            btnBg.setCornerRadius(dp(12));
            btn.setBackground(btnBg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(10);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> {
                root.removeAllViews();
                openPopup(site, term, dark, cardColor, textColor);
            });
            card.addView(btn, lp);
        }

        setContentView(root);
    }

    private void openPopup(SiteStore.Site site, String term,
                           boolean dark, int cardColor, int textColor) {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> finish());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardColor);
        bg.setCornerRadius(dp(24));
        card.setBackground(bg);
        card.setClipToOutline(true);
        card.setElevation(dp(8));
        card.setClickable(true);

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
        title.setText(site.name + "　" + term);
        title.setTextColor(textColor);
        title.setTextSize(15);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        header.addView(title, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // サイト切替ボタン
        TextView switchBtn = new TextView(this);
        switchBtn.setText("切替");
        switchBtn.setTextColor(0xFF0A84FF);
        switchBtn.setTextSize(14);
        switchBtn.setPadding(dp(10), dp(10), dp(6), dp(10));
        switchBtn.setOnClickListener(v -> {
            if (webView != null) webView.destroy();
            setContentView(new FrameLayout(this)); // 一旦クリア
            showPicker(term, dark, cardColor, textColor);
        });
        header.addView(switchBtn);

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextColor(textColor);
        close.setTextSize(20);
        close.setPadding(dp(10), dp(10), dp(14), dp(10));
        close.setOnClickListener(v -> finish());
        header.addView(close);
        card.addView(header);

        // WebView
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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return false; // 小窓内で開く
            }
        });
        card.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        webView.loadUrl(site.searchUrl + Uri.encode(term));
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
        if (webView != null) webView.destroy();
        super.onDestroy();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
