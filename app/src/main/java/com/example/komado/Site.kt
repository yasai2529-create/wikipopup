package com.example.komado
import java.util.UUID
data class Site(val id: String = UUID.randomUUID().toString(), val name: String, val urlTemplate: String, val isDefault: Boolean = false) {
    fun buildUrl(query: String): String {
        val q = query.trim()
        // Wikipediaはスペースを _ に
        val encoded = java.net.URLEncoder.encode(q, "UTF-8")
        val wiki = java.net.URLEncoder.encode(q.replace(" ", "_"), "UTF-8").replace("%5F", "_")
        return if (name == "Wikipedia") urlTemplate.replace("{query}", wiki) else urlTemplate.replace("{query}", encoded)
    }
}
