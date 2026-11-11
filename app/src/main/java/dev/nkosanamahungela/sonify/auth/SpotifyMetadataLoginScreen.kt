package dev.nkosanamahungela.sonify.auth

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.regular.X
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Spotify's own login page, not a Sonify password form. Sonify reads only the
 * resulting session cookie and validates it before saving it encrypted.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SpotifyMetadataLoginScreen(onDone: (Boolean) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Log in to your Spotify account") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var finished by remember { mutableStateOf(false) }

    fun finish() {
        if (finished || busy) return
        val cookieLine = CookieManager.getInstance().getCookie("https://open.spotify.com").orEmpty()
        val spDc = cookieLine.split(";").firstNotNullOfOrNull { part ->
            val pair = part.trim().split("=", limit = 2)
            pair.takeIf { it.size == 2 && it[0] == "sp_dc" }?.get(1)
        }
        if (spDc.isNullOrBlank()) return
        busy = true
        status = "Verifying Spotify account…"
        error = null
        webView?.stopLoading()
        scope.launch {
            runCatching { SpotifyMetadataSession.connect(context, spDc) }
                .onSuccess {
                    finished = true
                    status = "Spotify account connected"
                    delay(350)
                    onDone(true)
                }
                .onFailure {
                    busy = false
                    error = it.message ?: "Spotify login could not be verified"
                    status = "Login verification failed"
                }
        }
    }

    BackHandler { webView?.stopLoading(); onDone(false) }

    LaunchedEffect(Unit) {
        while (!finished) {
            delay(900)
            finish()
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF101012)).systemBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Spotify account", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text(
                "Close",
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.10f), CircleShape)
                    .clickable { webView?.stopLoading(); onDone(false) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        if (busy) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF1ED760), strokeWidth = 2.dp)
                Text(status, color = Color.White, modifier = Modifier.padding(start = 10.dp))
            }
        } else {
            Text(status, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        error?.let { Text(it, color = Color(0xFFFF6B6B), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            factory = { ctx ->
                CookieManager.getInstance().apply { setAcceptCookie(true) }
                WebView(ctx).apply {
                    webView = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { }
                    }
                    loadUrl("https://accounts.spotify.com/login?continue=https%3A%2F%2Fopen.spotify.com%2F")
                }
            },
        )
    }
}
