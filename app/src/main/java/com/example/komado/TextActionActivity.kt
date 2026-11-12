package com.example.komado
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class TextActionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val query = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
        val repo = SiteRepository(this)
        val sites = repo.load()
        val def = sites.find { it.isDefault }
        if (def != null && query.isNotBlank()) { openSite(def, query, false); finish(); return }
        if (query.isBlank()) { finish(); return }
        setContent {
            MaterialTheme {
                var selected by remember { mutableStateOf<Site?>(null) }
                var rememberDefault by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text("どのサイトで開く？") },
                    text = {
                        Column {
                            Text("「$query」", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(sites) { site ->
                                    val isSel = selected?.id == site.id
                                    TextButton(onClick = { selected = site }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(containerColor = if(isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                                        Text((if(isSel) "✓ " else "") + site.name, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row { Checkbox(checked = rememberDefault, onCheckedChange = { rememberDefault = it }); Text("次回からこのサイトで開く（デフォルトにする）", modifier = Modifier.padding(top=12.dp)) }
                        }
                    },
                    confirmButton = { Button(onClick = { val s = selected ?: sites.firstOrNull(); if (s != null) { if (rememberDefault) { repo.setDefault(sites.toMutableList(), s.id) }; openSite(s, query, false) }; finish() }, enabled = sites.isNotEmpty()) { Text("小窓で開く") } },
                    dismissButton = { TextButton(onClick = { finish() }) { Text("キャンセル") } }
                )
            }
        }
    }
    private fun openSite(site: Site, query: String, fromOverlay: Boolean) {
        val url = site.buildUrl(query)
        if (Settings.canDrawOverlays(this)) {
            val i = Intent(this, OverlayService::class.java)
            i.putExtra("url", url); i.putExtra("title", site.name); i.putExtra("query", query); i.putExtra("siteId", site.id)
            startService(i)
        } else {
            val i = Intent(this, WebViewActivity::class.java)
            i.putExtra("url", url); i.putExtra("title", site.name); startActivity(i)
        }
    }
}
