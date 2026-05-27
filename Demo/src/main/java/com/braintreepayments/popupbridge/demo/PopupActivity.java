package com.braintreepayments.popupbridge.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.api.PopupBridgeClient;
import com.braintreepayments.api.PopupBridgeWebViewClient;

public class PopupActivity extends AppCompatActivity {

    private static final String RETURN_URL_SCHEME = "com.braintreepayments.popupbridgeexample";

    private WebView webView;
    private PopupBridgeClient popupBridgeClient;
    private PopupBridgeWebViewClient popupBridgeWebViewClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        webView = findViewById(R.id.web_view);

        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        webView.setWebChromeClient(popupBridgeRoutingChromeClient());

        WebViewClient webViewClient = demoWebViewClient();

        popupBridgeWebViewClient = new PopupBridgeWebViewClient(webViewClient);

        popupBridgeClient = new PopupBridgeClient(this, webView, RETURN_URL_SCHEME, popupBridgeWebViewClient, true);
        popupBridgeClient.setErrorListener(error -> showDialog(error.getMessage()));

        webView.loadUrl(getIntent().getStringExtra("url"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        popupBridgeClient.handleReturnToApp(getIntent());
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
        popupBridgeClient.handleReturnToApp(newIntent);
    }

    public void showDialog(String message) {
        new AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
            .show();
    }

    // Intercept window.open() popups (e.g. PayPal JS SDK opening the approval URL)
    // and route them through PopupBridge so the host app's external browser handles them.
    // See: docs/Oslo ADR-01_PopupBridge_WindowOpen_Routing.
    private WebChromeClient popupBridgeRoutingChromeClient() {
        return new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView popupCatcher = new WebView(view.getContext());
                popupCatcher.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                        String url = req.getUrl().toString();
                        String escaped = url.replace("\\", "\\\\").replace("'", "\\'");
                        view.evaluateJavascript(
                            "window.popupBridge && window.popupBridge.open('" + escaped + "');",
                            null
                        );
                        v.destroy();
                        return true;
                    }
                });
                ((WebView.WebViewTransport) resultMsg.obj).setWebView(popupCatcher);
                resultMsg.sendToTarget();
                return true;
            }
        };
    }

    private WebViewClient demoWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Toast.makeText(PopupActivity.this, "Page Finished", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Toast.makeText(PopupActivity.this, "Page Started", Toast.LENGTH_SHORT).show();
            }

            // The PayPal JS SDK routes the buyer to PayPal via a TOP-LEVEL navigation
            // (window.location.href = paypalUrl), not via window.open(). Intercept those
            // navigations here and route them through PopupBridge so the host app's
            // external browser handles the approval flow.
            // See: docs/Oslo ADR-01_PopupBridge_WindowOpen_Routing.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (shouldRouteToPopupBridge(url, view.getUrl())) {
                    String escaped = url.replace("\\", "\\\\").replace("'", "\\'");
                    view.evaluateJavascript(
                        "window.popupBridge && window.popupBridge.open('" + escaped + "');",
                        null
                    );
                    return true;
                }
                return false;
            }
        };
    }

    private boolean shouldRouteToPopupBridge(String url, String currentUrl) {
        if (url == null || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            return false;
        }
        android.net.Uri target = android.net.Uri.parse(url);
        String targetHost = target.getHost();
        if (targetHost == null) return false;
        if (currentUrl != null) {
            android.net.Uri current = android.net.Uri.parse(currentUrl);
            if (targetHost.equalsIgnoreCase(current.getHost())) {
                return false;
            }
        }
        return true;
    }
}
