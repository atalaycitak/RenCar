package com.example.rencar_pair.presentation.ui.components.iyzico

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun IyzicoPaymentWebViewScreen(
    state: IyzicoPaymentWebViewContract.State,
    onIntent: (IyzicoPaymentWebViewContract.Intent) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(onClick = { onIntent(IyzicoPaymentWebViewContract.Intent.Dismissed) }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.errorMessage != null -> ErrorContent(
                        message = state.errorMessage,
                        onClose = { onIntent(IyzicoPaymentWebViewContract.Intent.Dismissed) }
                    )
                    state.paymentPageUrl != null -> CheckoutFormWebView(
                        contentOrUrl = state.paymentPageUrl,
                        onCallbackUrlReached = { url ->
                            onIntent(IyzicoPaymentWebViewContract.Intent.CallbackUrlReached(url))
                        }
                    )
                    state.isLoading -> CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onClose) {
            Text("Kapat")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CheckoutFormWebView(
    contentOrUrl: String,
    onCallbackUrlReached: (String) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            var callbackHandled = false

            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {

                    private fun maybeHandleCallback(url: String?) {
                        if (callbackHandled || url == null) return
                        if (url.contains(IyzicoPaymentWebViewContract.CHECKOUT_FORM_CALLBACK_PATH)) {
                            callbackHandled = true
                            onCallbackUrlReached(url)
                        }
                    }

                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        maybeHandleCallback(url)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        maybeHandleCallback(request.url.toString())
                        return false
                    }
                }
                
                if (contentOrUrl.startsWith("http://") || contentOrUrl.startsWith("https://")) {
                    loadUrl(contentOrUrl)
                } else {
                    // HTML tag or JS snippet
                    loadDataWithBaseURL("https://rencar.halitkalayci.com", contentOrUrl, "text/html", "UTF-8", null)
                }
            }
        }
    )
}
