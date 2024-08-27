package co.paystack.flutterpaystack

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

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
        setup()
    }

    private fun handleResponse() {
        if (responseJson == null) {
            responseJson = "{\"status\":\"requery\",\"message\":\"Reaffirm Transaction Status on Server\"}"
        }
        synchronized(si) {
            si.responseJson = responseJson!!
            (si as Object).notify()
        }
        finish()
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setup() {
        webView?.keepScreenOn = true

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

        class JIFactory {
            val ji: AuthResponseJI
                get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    AuthResponse17JI()
                } else {
                    AuthResponseLegacyJI()
                }
        }

        webView?.settings?.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        webView?.addJavascriptInterface(JIFactory().ji, "INTERFACE")

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (url.contains(API_URL + "charge/three_d_response/")) {
                    view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementById('return').innerText);")
                }
            }
        }

        webView?.loadUrl(si.url)
    }

    override fun onDestroy() {
        webView?.apply {
            stopLoading()
            clearHistory()
            clearCache(true)
            removeJavascriptInterface("INTERFACE")
            destroy()  // Safely destroy the WebView to prevent memory leaks
        }
        handleResponse()
        super.onDestroy()
    }
}
