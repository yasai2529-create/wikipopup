package com.example.komado
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); val repo = SiteRepository(this); setContent { MainScreen(repo) } }
    override fun onResume() { super.onResume(); setContent { MainScreen(SiteRepository(this)) } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repo: SiteRepository) {
    val ctx = LocalContext.current
    var sites by remember { mutableStateOf(repo.load()) }
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Site?>(null) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var del by remember { mutableStateOf<Site?>(null) }
    val canOverlay = Settings.canDrawOverlays(ctx)
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("小窓検索") }) },
            floatingActionButton = { FloatingActionButton(onClick = { editing=null; name=""; url=""; showDialog=true }) { Icon(Icons.Default.Add, contentDescription="追加") } }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                if (!canOverlay) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("小窓を使うには「他のアプリの上に表示」の許可が必要です", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + ctx.packageName))) }) { Text("許可する") }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Text("文字を選択 → メニュー → 小窓検索 で開けます", style = MaterialTheme.typography.bodyMedium)
                val def = sites.find { it.isDefault }
                Text(if (def != null) "今は毎回「${def.name}」で開く設定です" else "今は毎回選ぶ設定です", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("デフォルトを決めると毎回同じサイト、解除すると毎回選べます", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(sites, key={it.id}) { site ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) { Text(site.name, style=MaterialTheme.typography.titleMedium); Text(site.urlTemplate, style=MaterialTheme.typography.bodySmall) }
                                    IconButton(onClick={editing=site; name=site.name; url=site.urlTemplate; showDialog=true}){Icon(Icons.Default.Edit, contentDescription="編集")}
                                    IconButton(onClick={del=site}){Icon(Icons.Default.Delete, contentDescription="削除")}
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                    if (site.isDefault) OutlinedButton(onClick={sites=repo.clearDefault(sites.toMutableList()).toMutableList()}){Text("デフォルト解除")}
                                    else Button(onClick={sites=repo.setDefault(sites.toMutableList(), site.id).toMutableList()}){Text("デフォルトにする")}
                                    if (site.isDefault) AssistChip(onClick={}, label={Text("デフォルト")})
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("追加のコツ：サイトで1回検索して、そのURLの検索ワード部分を {query} に置き換えてください。例：https://www.amazon.co.jp/s?k={query}", style=MaterialTheme.typography.bodySmall)
            }
        }
        if (showDialog) {
            AlertDialog(onDismissRequest={showDialog=false}, title={Text(if(editing==null)"サイトを追加" else "サイトを編集")},
                text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){ OutlinedTextField(value=name, onValueChange={name=it}, label={Text("名前 例: Amazon")}, modifier=Modifier.fillMaxWidth()); OutlinedTextField(value=url, onValueChange={url=it}, label={Text("URL")}, placeholder={Text("https://www.amazon.co.jp/s?k={query}")}, modifier=Modifier.fillMaxWidth()); Text("{query} に選択した文字が入ります", style=MaterialTheme.typography.bodySmall) }},
                confirmButton={Button(onClick={ if(name.isBlank()||url.isBlank()) return@Button; if(editing==null){ val nl=(sites+Site(name=name.trim(), urlTemplate=url.trim())).toMutableList(); repo.save(nl); sites=nl } else { val nl=sites.map{if(it.id==editing!!.id) it.copy(name=name.trim(), urlTemplate=url.trim()) else it}.toMutableList(); repo.save(nl); sites=nl }; showDialog=false }){Text("保存")}},
                dismissButton={TextButton(onClick={showDialog=false}){Text("キャンセル")}})
        }
        del?.let { target ->
            AlertDialog(onDismissRequest={del=null}, title={Text("削除しますか？")}, text={Text("${target.name} を削除します")},
                confirmButton={Button(onClick={val nl=sites.filter{it.id!=target.id}.toMutableList(); repo.save(nl); sites=nl; del=null}){Text("削除")}},
                dismissButton={TextButton(onClick={del=null}){Text("やめる")}})
        }
    }
}
