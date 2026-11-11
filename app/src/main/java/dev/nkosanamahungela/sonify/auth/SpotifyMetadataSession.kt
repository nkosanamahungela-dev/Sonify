package dev.nkosanamahungela.sonify.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

/**
 * Spotify web-player session used only for catalogue metadata.
 *
 * The cookie and short-lived web token are encrypted at rest. This is separate
 * from SpotifyOAuth/EngineCredentials, which belong to the audio engine.
 *
 * Authentication flow adapted from Spotui (GPL-3.0):
 * https://github.com/Spotui/Spotui
 */
object SpotifyMetadataSession {
    private const val PREFS = "spotify_metadata_session"
    private const val COOKIE = "sp_dc"
    private const val ACCESS = "access_token"
    private const val EXPIRES = "expires_at"
    private const val TOKEN_URL = "https://open.spotify.com/api/token"
    private const val SERVER_TIME_URL = "https://open.spotify.com/api/server-time"
    private const val GIST_URL = "https://api.github.com/gists/22ed9c6ba463899e933427f7de1f0eef"
    private val http = OkHttpClient()

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun isConnected(context: Context): Boolean =
        prefs(context).getString(COOKIE, null).orEmpty().isNotBlank()

    fun clear(context: Context) {
        prefs(context).edit().clear().commit()
    }

    /** Saves the cookie only after Spotify has issued a non-anonymous token. */
    suspend fun connect(context: Context, spDc: String) = withContext(Dispatchers.IO) {
        require(spDc.isNotBlank()) { "Spotify login cookie was empty" }
        val nuance = fetchNuance()
        val serverTime = JSONObject(get(SERVER_TIME_URL)).getLong("serverTime")
        val totp = totp(nuance.first, serverTime)
        val url = "$TOKEN_URL?reason=transport&productType=web-player&totp=$totp&totpServer=$totp&totpVer=${nuance.second}"
        val cookie = "sp_dc=" + spDc
        val response = JSONObject(get(url, cookie))
        val token = response.optString("accessToken")
        val anonymous = response.optBoolean("isAnonymous", true)
        require(token.isNotBlank() && !anonymous) {
            "Spotify did not accept this account session. Please sign in again."
        }
        val expiry = response.optLong("accessTokenExpirationTimestampMs", System.currentTimeMillis() + 3600_000L)
        prefs(context).edit()
            .putString(COOKIE, spDc)
            .putString(ACCESS, token)
            .putLong(EXPIRES, expiry)
            .commit()
        true
    }

    private fun fetchNuance(): Pair<String, Int> {
        val root = JSONObject(get(GIST_URL))
        val files = root.getJSONObject("files")
        val names = files.keys()
        while (names.hasNext()) {
            val content = files.getJSONObject(names.next()).optString("content")
            if (content.isBlank()) continue
            val list = JSONArray(content)
            var best: JSONObject? = null
            for (i in 0 until list.length()) {
                val candidate = list.getJSONObject(i)
                if (best == null || candidate.optInt("v") > best!!.optInt("v")) best = candidate
            }
            best?.let { return it.getString("s") to it.getInt("v") }
        }
        error("Spotify token configuration was unavailable")
    }

    private fun get(url: String, cookie: String? = null): String {
        val request = Request.Builder().url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0.0.0 Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .apply { if (cookie != null) header("Cookie", cookie) }
            .build()
        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            check(response.isSuccessful) { "Spotify session check failed (HTTP ${response.code})" }
            return body
        }
    }

    private fun totp(secret: String, serverTimeSec: Long): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val clean = secret.uppercase().replace("=", "")
        var buffer = 0
        var bits = 0
        val key = ArrayList<Byte>()
        for (char in clean) {
            val value = alphabet.indexOf(char)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bits += 5
            if (bits >= 8) {
                bits -= 8
                key += ((buffer shr bits) and 0xff).toByte()
            }
        }
        val step = floor(serverTimeSec.toDouble() / 30.0).toLong()
        val bytes = ByteArray(8)
        var counter = step
        for (i in 7 downTo 0) { bytes[i] = (counter and 0xff).toByte(); counter = counter shr 8 }
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA1"))
        val hash = mac.doFinal(bytes)
        val offset = hash.last().toInt() and 0x0f
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            (hash[offset + 3].toInt() and 0xff)
        return (binary % 1_000_000).toString().padStart(6, '0')
    }
}
