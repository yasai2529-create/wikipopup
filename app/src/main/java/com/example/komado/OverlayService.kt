package com.example.komado
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var floatingView: View? = null
    private var webView: WebView? = null
    private var params: WindowManager.LayoutParams? = null
    private var currentQuery: String = ""
    private var currentSiteName: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val title = intent.getStringExtra("title") ?: "小窓検索"
        currentQuery = intent.getStringExtra("query") ?: ""
        currentSiteName = title
        if (!Settings.canDrawOverlays(this)) {
            val i = Intent(this, WebViewActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); putExtra("url", url); putExtra("title", title) }
            startActivity(i); return START_NOT_STICKY
        }
        if (floatingView == null) createFloatingView(title, url) else { updateTitle(title); webView?.loadUrl(url) }
        return START_NOT_STICKY
    }

    private fun updateTitle(t: String) {
        val tv = floatingView?.findViewWithTag<TextView>("title")
        tv?.text = t
    }

    @SuppressWarnings("ClickableViewAccessibility")
    private fun createFloatingView(title: String, url: String) {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val dm = resources.displayMetrics
        val w = (dm.widthPixels * 0.94).toInt()
        val h = (dm.heightPixels * 0.72).toInt()
        params = WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = 24 }

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(0xFFFFFFFF.toInt()); elevation = 16f }

        // ヘッダー（ドラッグで移動）
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setBackgroundColor(0xFFEEEEEE.toInt()); setPadding(16, 16, 16, 16) }
        val titleView = TextView(this).apply { text = title; textSize = 16f; tag = "title"; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        val changeBtn = Button(this).apply { text = "サイトを変える"; textSize = 12f }
        val closeBtn = Button(this).apply { text = "✕"; textSize = 14f }
        header.addView(titleView); header.addView(changeBtn); header.addView(closeBtn)

        // サイト変更パネル（最初は非表示）
        val changePanel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE; setPadding(16, 8, 16, 8) }
        val changeLabel = TextView(this).apply { text = "どのサイトで開く？"; textSize = 14f }
        changePanel.addView(changeLabel)
        val repo = SiteRepository(this)
        val sites = repo.load()
        sites.forEach { site ->
            val b = Button(this).apply { text = site.name; textSize = 14f }
            b.setOnClickListener {
                val newUrl = site.buildUrl(currentQuery)
                currentSiteName = site.name
                updateTitle(site.name)
                webView?.loadUrl(newUrl)
                changePanel.visibility = View.GONE
            }
            changePanel.addView(b)
        }
        val rememberBtn = Button(this).apply { text = "このサイトをデフォルトにする" }
        rememberBtn.setOnClickListener {
            val s = sites.find { it.name == currentSiteName }
            if (s != null) repo.setDefault(sites.toMutableList(), s.id)
            changePanel.visibility = View.GONE
        }
        changePanel.addView(rememberBtn)

        changeBtn.setOnClickListener { changePanel.visibility = if (changePanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE }
        closeBtn.setOnClickListener { close() }

        // WebView
        val wv = WebView(this).apply { settings.javaScriptEnabled = true; settings.domStorageEnabled = true; webViewClient = WebViewClient(); loadUrl(url) }
        webView = wv

        root.addView(header); root.addView(changePanel)
        root.addView(wv, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        // ドラッグ移動
        var lastX = 0f; var lastY = 0f; var downX = 0f; var downY = 0f
        header.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> { lastX = ev.rawX; lastY = ev.rawY; downX = ev.rawX; downY = ev.rawY; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (ev.rawX - lastX).toInt(); val dy = (ev.rawY - lastY).toInt()
                    params?.let { p -> p.x += dx; p.y += dy; try { wm.updateViewLayout(floatingView, p) } catch(e: Exception){} }
                    lastX = ev.rawX; lastY = ev.rawY; true
                }
                else -> false
            }
        }

        floatingView = root
        wm.addView(root, params)
    }

    private fun close() {
        try { floatingView?.let { wm.removeView(it) } } catch(e: Exception){}
        floatingView = null
        webView?.destroy(); webView = null
        stopSelf()
    }
    override fun onDestroy() { super.onDestroy(); try { floatingView?.let { wm.removeView(it) } } catch(e: Exception){} }
}
