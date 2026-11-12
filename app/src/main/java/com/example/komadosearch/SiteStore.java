package com.example.komadosearch;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SiteStore {

    public static class Site {
        public String name;
        public String searchUrl; // 末尾に検索ワードをくっつけるURL
        public boolean isDefault;

        public Site(String name, String searchUrl, boolean isDefault) {
            this.name = name;
            this.searchUrl = searchUrl;
            this.isDefault = isDefault;
        }
    }

    private static final String PREF = "sites";
    private static final String KEY  = "list";

    private static List<Site> defaults() {
        List<Site> list = new ArrayList<>();
        list.add(new Site("Wikipedia",       "https://ja.wikipedia.org/w/index.php?go=Go&search=",           true));
        list.add(new Site("YouTube",         "https://www.youtube.com/results?search_query=",                false));
        list.add(new Site("ニコニコ動画",    "https://nicovideo.jp/search/",                                 false));
        list.add(new Site("5ちゃんねる",     "https://google.com/search?q=site:5ch.net+",                   false));
        list.add(new Site("爆サイ",          "https://google.com/search?q=site:bakusai.com+",               false));
        list.add(new Site("Google",          "https://www.google.com/search?q=",                            false));
        return list;
    }

    public static List<Site> load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = sp.getString(KEY, null);
        if (json == null) return defaults();
        try {
            JSONArray arr = new JSONArray(json);
            List<Site> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Site(o.getString("name"), o.getString("url"), o.getBoolean("def")));
            }
            return list;
        } catch (Exception e) {
            return defaults();
        }
    }

    public static void save(Context ctx, List<Site> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Site s : list) {
                JSONObject o = new JSONObject();
                o.put("name", s.name);
                o.put("url",  s.searchUrl);
                o.put("def",  s.isDefault);
                arr.put(o);
            }
            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
               .edit().putString(KEY, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static Site getDefault(Context ctx) {
        for (Site s : load(ctx)) if (s.isDefault) return s;
        return null;
    }
}
