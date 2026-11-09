package dev.nkosanamahungela.sonify.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dev.nkosanamahungela.sonify.diagnostics.Report
import kotlinx.coroutines.launch

/**
 * A small, platform-view recovery screen. It deliberately avoids Compose and
 * the player startup path, because either may be what caused the previous crash.
 */
class CrashRecoveryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        window.decorView.setBackgroundColor(Color.BLACK)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(36), dp(24), dp(24))
            setBackgroundColor(Color.BLACK)
        }
        val title = TextView(this).apply {
            text = "Sonify needs a repair"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        val message = TextView(this).apply {
            text = "Sonify closed unexpectedly while opening. Your music and account data have not been cleared. Share a diagnostic report so we can identify the cause."
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(24))
        }
        val status = TextView(this).apply {
            text = "The report stays on your phone until you choose an app to share it."
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(16))
        }
        val share = Button(this).apply {
            text = "Share crash report"
            isAllCaps = false
            setOnClickListener {
                isEnabled = false
                status.text = "Preparing diagnostic report…"
                lifecycleScope.launch {
                    runCatching { Report.build(this@CrashRecoveryActivity, emptyList()) }
                        .onSuccess { file ->
                            status.text = "Choose where to share the report."
                            Report.share(this@CrashRecoveryActivity, file)
                        }
                        .onFailure { error ->
                            status.text = "Could not prepare report: ${error.localizedMessage ?: "unknown error"}"
                            isEnabled = true
                        }
                }
            }
        }
        val retry = Button(this).apply {
            text = "Try opening Sonify again"
            isAllCaps = false
            setOnClickListener {
                startActivity(android.content.Intent(this@CrashRecoveryActivity, MainActivity::class.java)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }
        }
        root.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(message, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(share, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(status, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(retry, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
