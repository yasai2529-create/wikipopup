package com.example.komado
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class WebViewActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url") ?: ""
        val title = intent.getStringExtra("title") ?: "検索結果"
        setContent { MaterialTheme { Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(top=8.dp))
                IconButton(onClick={finish()}){Icon(Icons.Default.Close, contentDescription="閉じる")}
            }
            AndroidView(factory={ ctx -> WebView(ctx).apply{ settings.javaScriptEnabled=true; settings.domStorageEnabled=true; webViewClient=WebViewClient(); loadUrl(url) } }, modifier=Modifier.fillMaxSize())
        } } }
    }
}
