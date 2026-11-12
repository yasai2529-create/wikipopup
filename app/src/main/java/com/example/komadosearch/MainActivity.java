package com.example.komadosearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.List;

public class MainActivity extends Activity {

    private List<SiteStore.Site> sites;
    private LinearLayout listLayout;
    private boolean dark;
    private int bg, card, text, accent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        bg     = dark ? 0xFF1A1A1A : 0xFFF2F2F7;
        card   = dark ? 0xFF2C2C2E : 0xFFFFFFFF;
        text   = dark ? 0xFFFFFFFF : 0xFF000000;
        accent = 0xFF0A84FF;

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        root.setPadding(p, dp(48), p, dp(32));

        // タイトル
        TextView title = new TextView(this);
        title.setText("小窓検索");
        title.setTextColor(text);
        title.setTextSize(28);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setPadding(dp(4), 0, 0, dp(4));
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("テキストを選択してメニューから「小窓検索」をタップすると、登録したサイトで小窓が開きます。");
        sub.setTextColor(dark ? 0xFF8E8E93 : 0xFF6E6E73);
        sub.setTextSize(13);
        sub.setPadding(dp(4), 0, 0, dp(20));
        root.addView(sub);

        // サイト一覧
        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        root.addView(listLayout);

        // 追加ボタン
        Button addBtn = new Button(this);
        addBtn.setText("＋ サイトを追加");
        addBtn.setTextColor(accent);
        addBtn.setBackgroundColor(Color.TRANSPARENT);
        addBtn.setTextSize(16);
        addBtn.setOnClickListener(v -> showAddDialog());
        LinearLayout.LayoutParams addLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        addLp.topMargin = dp(8);
        root.addView(addBtn, addLp);

        sv.addView(root);
        setContentView(sv);

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        sites = SiteStore.load(this);
        listLayout.removeAllViews();
        for (int i = 0; i < sites.size(); i++) {
            listLayout.addView(siteCard(i));
        }
    }

    private View siteCard(int idx) {
        SiteStore.Site site = sites.get(idx);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(this.card);
        bg.setCornerRadius(dp(14));
        card.setBackground(bg);
        card.setClipToOutline(true);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);

        // 上段: 名前 + デフォルトスイッチ
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = new TextView(this);
        name.setText(site.name);
        name.setTextColor(text);
        name.setTextSize(16);
        name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        row.addView(name, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // デフォルトラベル
        TextView defLabel = new TextView(this);
        defLabel.setText("デフォルト");
        defLabel.setTextColor(dark ? 0xFF8E8E93 : 0xFF6E6E73);
        defLabel.setTextSize(13);
        defLabel.setPadding(0, 0, dp(6), 0);
        row.addView(defLabel);

        Switch sw = new Switch(this);
        sw.setChecked(site.isDefault);
        sw.setOnCheckedChangeListener((v, checked) -> {
            if (checked) {
                for (SiteStore.Site s : sites) s.isDefault = false;
                sites.get(idx).isDefault = true;
            } else {
                sites.get(idx).isDefault = false;
            }
            SiteStore.save(this, sites);
            refresh();
        });
        row.addView(sw);
        card.addView(row);

        // 下段: URL + 削除ボタン
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER_VERTICAL);
        row2.setPadding(0, dp(6), 0, 0);

        TextView url = new TextView(this);
        url.setText(site.searchUrl);
        url.setTextColor(dark ? 0xFF8E8E93 : 0xFF6E6E73);
        url.setTextSize(12);
        url.setSingleLine(true);
        url.setEllipsize(android.text.TextUtils.TruncateAt.END);
        row2.addView(url, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView del = new TextView(this);
        del.setText("削除");
        del.setTextColor(0xFFFF3B30);
        del.setTextSize(14);
        del.setPadding(dp(12), dp(4), 0, dp(4));
        del.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("「" + site.name + "」を削除しますか？")
                    .setPositiveButton("削除", (d, w) -> {
                        sites.remove(idx);
                        SiteStore.save(this, sites);
                        refresh();
                    })
                    .setNegativeButton("キャンセル", null)
                    .show();
        });
        row2.addView(del);
        card.addView(row2);

        return card;
    }

    private void showAddDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = dp(20);
        layout.setPadding(p, dp(8), p, 0);

        EditText nameEt = new EditText(this);
        nameEt.setHint("サイト名（例: Googleマップ）");
        layout.addView(nameEt);

        EditText urlEt = new EditText(this);
        urlEt.setHint("検索URL（例: https://maps.google.com/maps?q=）");
        urlEt.setSingleLine(false);
        urlEt.setMaxLines(3);
        layout.addView(urlEt);

        TextView note = new TextView(this);
        note.setText("URLの末尾に、選択した文字が自動でくっつきます。");
        note.setTextSize(12);
        note.setTextColor(dark ? 0xFF8E8E93 : 0xFF6E6E73);
        note.setPadding(0, dp(6), 0, 0);
        layout.addView(note);

        new AlertDialog.Builder(this)
                .setTitle("サイトを追加")
                .setView(layout)
                .setPositiveButton("追加", (d, w) -> {
                    String n = nameEt.getText().toString().trim();
                    String u = urlEt.getText().toString().trim();
                    if (!n.isEmpty() && !u.isEmpty()) {
                        sites.add(new SiteStore.Site(n, u, false));
                        SiteStore.save(this, sites);
                        refresh();
                    } else {
                        Toast.makeText(this, "名前とURLを入力してください", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
