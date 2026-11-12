package com.example.komado
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
class SiteRepository(context: Context) {
    private val prefs = context.getSharedPreferences("sites", Context.MODE_PRIVATE)
    fun load(): MutableList<Site> {
        val json = prefs.getString("sites_json", null)
        if (json == null) { val d = defaultSites(); save(d); return d.toMutableList() }
        val list = mutableListOf<Site>(); val arr = JSONArray(json)
        for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); list.add(Site(o.getString("id"), o.getString("name"), o.getString("urlTemplate"), o.optBoolean("isDefault", false))) }
        return list
    }
    fun save(sites: List<Site>) {
        val arr = JSONArray(); sites.forEach { arr.put(JSONObject().put("id", it.id).put("name", it.name).put("urlTemplate", it.urlTemplate).put("isDefault", it.isDefault)) }
        prefs.edit().putString("sites_json", arr.toString()).apply()
    }
    fun defaultSites() = listOf(
        Site(name="Wikipedia", urlTemplate="https://ja.m.wikipedia.org/wiki/{query}"),
        Site(name="YouTube", urlTemplate="https://www.youtube.com/results?search_query={query}"),
        Site(name="ニコニコ動画", urlTemplate="https://www.nicovideo.jp/search/{query}"),
        Site(name="5ちゃんねる", urlTemplate="https://find.5ch.net/search?q={query}"),
        Site(name="爆サイ", urlTemplate="https://www.google.com/search?q=site:bakusai.com+{query}"),
        Site(name="Google", urlTemplate="https://www.google.com/search?q={query}")
    )
    fun setDefault(sites: MutableList<Site>, id: String): List<Site> { val u = sites.map { it.copy(isDefault = it.id==id) }; save(u); return u }
    fun clearDefault(sites: MutableList<Site>): List<Site> { val u = sites.map { it.copy(isDefault=false) }; save(u); return u }
}
