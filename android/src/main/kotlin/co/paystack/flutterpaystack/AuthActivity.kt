package co.paystack.flutterpaystack

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Created by Wilberforce on 29/07/18 at 18:47.
 */

const val API_URL = "https://standard.paystack.co/"

class AuthActivity : Activity() {

    private val si = AuthSingleton.instance
    private var responseJson: String? = null
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.co_paystack_android____activity_auth)
        webView = findViewById(R.id.webView)
        title = "Authorize your card"
        setupWebView()
    }

    private fun handleResponse() {
        if (responseJson == null) {
            responseJson = "{\"status\":\"requery\",\"message\":\"Reaffirm Transaction Status on Server\"}"
        }
        synchronized(si) {
            si.responseJson = responseJson!!
            (si as java.lang.Object).notify()
        }
        finish()
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView() {
        webView?.apply {
            settings.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                domStorageEnabled = true // Ensure storage is enabled
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            val authResponseJI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                AuthResponse17JI()
            } else {
                AuthResponseLegacyJI()
            }

            addJavascriptInterface(authResponseJI, "INTERFACE")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (url.contains("$API_URL/charge/three_d_response/")) {
                        view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementById('return').innerText);")
                    }
                }
            }

            loadUrl(si.url)
        }
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause() // Pause WebView resources
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume() // Resume WebView resources
    }

    override fun onDestroy() {
        webView?.apply {
            stopLoading()
            removeJavascriptInterface("INTERFACE")
            clearCache(true)
            clearHistory()
            destroy() // Destroy WebView instance
        }
        handleResponse()
        super.onDestroy()
    }

    abstract class AuthResponseJI {
        abstract fun processContent(aContent: String)
    }

    class AuthResponseLegacyJI : AuthResponseJI() {
        override fun processContent(aContent: String) {
            responseJson = aContent
            handleResponse()
        }
    }

    class AuthResponse17JI : AuthResponseJI() {
        @JavascriptInterface
        override fun processContent(aContent: String) {
            responseJson = aContent
            handleResponse()
        }
    }
}
